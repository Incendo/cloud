//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package cloud.commandframework.bungee;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.bungee.arguments.PlayerParser;
import cloud.commandframework.bungee.arguments.ServerParser;
import cloud.commandframework.captions.FactoryDelegatingCaptionRegistry;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import io.leangen.geantyref.TypeToken;
import java.util.function.Function;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BungeeCommandManager<C> extends CommandManager<C> {

    private static final String MESSAGE_INTERNAL_ERROR = "An internal error occurred while attempting to perform this command.";
    private static final String MESSAGE_NO_PERMS =
            "I'm sorry, but you do not have permission to perform this command. "
                    + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command. Type \"/help\" for help.";

    /**
     * Default caption for {@link BungeeCaptionKeys#ARGUMENT_PARSE_FAILURE_PLAYER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_PLAYER = "'<input>' is not a valid player";

    /**
     * Default caption for {@link BungeeCaptionKeys#ARGUMENT_PARSE_FAILURE_SERVER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_SERVER = "'<input>' is not a valid server";

    private final Plugin owningPlugin;
    private final Function<CommandSender, C> commandSenderMapper;
    private final Function<C, CommandSender> backwardsCommandSenderMapper;

    /**
     * Construct a new Bungee command manager
     *
     * @param owningPlugin                 Plugin that is constructing the manager
     * @param commandExecutionCoordinator  Coordinator provider
     * @param commandSenderMapper          Function that maps {@link CommandSender} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link CommandSender}
     */
    @SuppressWarnings("unchecked")
    public BungeeCommandManager(
            final @NonNull Plugin owningPlugin,
            final @NonNull Function<@NonNull CommandTree<C>,
                    @NonNull CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @NonNull Function<@NonNull CommandSender, @NonNull C> commandSenderMapper,
            final @NonNull Function<@NonNull C, @NonNull CommandSender> backwardsCommandSenderMapper
    ) {
        super(commandExecutionCoordinator, new BungeePluginRegistrationHandler<>());
        ((BungeePluginRegistrationHandler<C>) this.commandRegistrationHandler()).initialize(this);
        this.owningPlugin = owningPlugin;
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;

        this.commandSuggestionProcessor(new FilteringCommandSuggestionProcessor<>(
                FilteringCommandSuggestionProcessor.Filter.<C>startsWith(true).andTrimBeforeLastSpace()
        ));

        /* Register Bungee Preprocessor */
        this.registerCommandPreProcessor(new BungeeCommandPreprocessor<>(this));

        /* Register Bungee Parsers */
        this.parserRegistry().registerParserSupplier(TypeToken.get(ProxiedPlayer.class), parserParameters ->
                new PlayerParser<>());
        this.parserRegistry().registerParserSupplier(TypeToken.get(ServerInfo.class), parserParameters ->
                new ServerParser<>());

        /* Register default captions */
        if (this.captionRegistry() instanceof FactoryDelegatingCaptionRegistry) {
            final FactoryDelegatingCaptionRegistry<C> factoryDelegatingCaptionRegistry = (FactoryDelegatingCaptionRegistry<C>)
                    this.captionRegistry();
            factoryDelegatingCaptionRegistry.registerMessageFactory(
                    BungeeCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER,
                    (context, key) -> ARGUMENT_PARSE_FAILURE_PLAYER
            );
            factoryDelegatingCaptionRegistry.registerMessageFactory(
                    BungeeCaptionKeys.ARGUMENT_PARSE_FAILURE_SERVER,
                    (context, key) -> ARGUMENT_PARSE_FAILURE_SERVER
            );
        }

        this.registerDefaultExceptionHandlers();
    }

    @Override
    public final boolean hasPermission(
            final @NonNull C sender,
            final @NonNull String permission
    ) {
        if (permission.isEmpty()) {
            return true;
        }
        return this.backwardsCommandSenderMapper.apply(sender).hasPermission(permission);
    }

    final @NonNull Function<@NonNull CommandSender, @NonNull C> getCommandSenderMapper() {
        return this.commandSenderMapper;
    }

    /**
     * Get the owning plugin
     *
     * @return Owning plugin
     */
    public @NonNull Plugin getOwningPlugin() {
        return this.owningPlugin;
    }

    private void registerDefaultExceptionHandlers() {
        this.exceptionController().registerHandler(Throwable.class, context -> {
            this.backwardsCommandSenderMapper.apply(context.context().sender())
                    .sendMessage(new ComponentBuilder(MESSAGE_INTERNAL_ERROR).color(ChatColor.RED).create());
            this.owningPlugin.getLogger().log(
                    Level.SEVERE,
                    "An unhandled exception was thrown during command execution",
                    context.exception());
        }).registerHandler(CommandExecutionException.class, context -> {
            this.backwardsCommandSenderMapper.apply(context.context().sender())
                    .sendMessage(new ComponentBuilder(MESSAGE_INTERNAL_ERROR)
                    .color(ChatColor.RED)
                    .create());
            this.owningPlugin.getLogger().log(
                    Level.SEVERE,
                    "Exception executing command handler",
                    context.exception().getCause()
            );
        }).registerHandler(ArgumentParseException.class, context -> this.backwardsCommandSenderMapper.apply(
                context.context().sender()).sendMessage(new ComponentBuilder("Invalid Command Argument: ")
                .color(ChatColor.GRAY).append(context.exception().getCause().getMessage()).create())
        ).registerHandler(NoSuchCommandException.class, context -> this.backwardsCommandSenderMapper.apply(
                context.context().sender()).sendMessage(new ComponentBuilder(MESSAGE_UNKNOWN_COMMAND)
                .color(ChatColor.WHITE).create())
        ).registerHandler(NoPermissionException.class, context -> this.backwardsCommandSenderMapper.apply(
                context.context().sender()).sendMessage(new ComponentBuilder(MESSAGE_NO_PERMS)
                .color(ChatColor.WHITE).create())
        ).registerHandler(InvalidCommandSenderException.class, context -> this.backwardsCommandSenderMapper.apply(
                context.context().sender()).sendMessage(new ComponentBuilder(context.exception().getMessage())
                .color(ChatColor.RED).create())
        ).registerHandler(InvalidSyntaxException.class, context -> this.backwardsCommandSenderMapper.apply(
                context.context().sender()).sendMessage(new ComponentBuilder(
                        "Invalid Command Syntax. Correct command syntax is: ").color(ChatColor.RED).append("/")
                        .color(ChatColor.GRAY).append(context.exception().getCorrectSyntax()).color(ChatColor.GRAY).create()
        ));
    }
}
