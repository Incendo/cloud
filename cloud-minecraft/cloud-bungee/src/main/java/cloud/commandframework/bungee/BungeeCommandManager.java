//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
import cloud.commandframework.bungee.arguments.PlayerArgument;
import cloud.commandframework.bungee.arguments.ServerArgument;
import cloud.commandframework.captions.FactoryDelegatingCaptionRegistry;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import io.leangen.geantyref.TypeToken;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

public class BungeeCommandManager<C> extends CommandManager<C> {

    /**
     * Default caption for {@link BungeeCaptionKeys#ARGUMENT_PARSE_FAILURE_PLAYER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_PLAYER = "'{input}' is not a valid player";

    /**
     * Default caption for {@link BungeeCaptionKeys#ARGUMENT_PARSE_FAILURE_SERVER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_SERVER = "'{input}' is not a valid server";

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
        ((BungeePluginRegistrationHandler<C>) this.getCommandRegistrationHandler()).initialize(this);
        this.owningPlugin = owningPlugin;
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;

        /* Register Bungee Preprocessor */
        this.registerCommandPreProcessor(new BungeeCommandPreprocessor<>(this));

        /* Register Bungee Parsers */
        this.getParserRegistry().registerParserSupplier(TypeToken.get(ProxiedPlayer.class), parserParameters ->
                new PlayerArgument.PlayerParser<>());
        this.getParserRegistry().registerParserSupplier(TypeToken.get(ServerInfo.class), parserParameters ->
                new ServerArgument.ServerParser<>());

        /* Register default captions */
        if (this.getCaptionRegistry() instanceof FactoryDelegatingCaptionRegistry) {
            final FactoryDelegatingCaptionRegistry<C> factoryDelegatingCaptionRegistry = (FactoryDelegatingCaptionRegistry<C>)
                    this.getCaptionRegistry();
            factoryDelegatingCaptionRegistry.registerMessageFactory(
                    BungeeCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER,
                    (context, key) -> ARGUMENT_PARSE_FAILURE_PLAYER
            );
            factoryDelegatingCaptionRegistry.registerMessageFactory(
                    BungeeCaptionKeys.ARGUMENT_PARSE_FAILURE_SERVER,
                    (context, key) -> ARGUMENT_PARSE_FAILURE_SERVER
            );
        }
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

    @Override
    public final @NonNull SimpleCommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
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

}
