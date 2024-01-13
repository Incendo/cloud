//
// MIT License
//
// Copyright (c) 2024 Incendo
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
package cloud.commandframework.velocity;

import cloud.commandframework.CommandManager;
import cloud.commandframework.SenderMapper;
import cloud.commandframework.SenderMapperHolder;
import cloud.commandframework.arguments.suggestion.SuggestionFactory;
import cloud.commandframework.brigadier.BrigadierManagerHolder;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.brigadier.suggestion.TooltipSuggestion;
import cloud.commandframework.captions.FactoryDelegatingCaptionRegistry;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.exceptions.handling.ExceptionContext;
import cloud.commandframework.exceptions.handling.ExceptionHandler;
import cloud.commandframework.execution.ExecutionCoordinator;
import cloud.commandframework.velocity.arguments.PlayerParser;
import cloud.commandframework.velocity.arguments.ServerParser;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * {@link CommandManager} implementation for Velocity.
 * <p>
 * This can be injected if {@link CloudInjectionModule} is registered in the
 * injector. This can be achieved by using {@link com.google.inject.Injector#createChildInjector(Module...)}
 * <p>
 * {@link #suggestionFactory()} has been overridden to map suggestions to {@link TooltipSuggestion}.
 * You may use {@link TooltipSuggestion} to display tooltips for your suggestions.
 * {@link com.velocitypowered.api.command.VelocityBrigadierMessage} can be used to make use of Adventure
 * {@link net.kyori.adventure.text.Component components} in the tooltips.
 *
 * @param <C> Command sender type
 */
@Singleton
public class VelocityCommandManager<C> extends CommandManager<C>
        implements BrigadierManagerHolder<C, CommandSource>, SenderMapperHolder<CommandSource, C> {

    private static final String MESSAGE_INTERNAL_ERROR = "An internal error occurred while attempting to perform this command.";
    private static final String MESSAGE_NO_PERMS =
            "I'm sorry, but you do not have permission to perform this command. "
                    + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command. Type \"/help\" for help.";

    /**
     * Default caption for {@link VelocityCaptionKeys#ARGUMENT_PARSE_FAILURE_PLAYER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_PLAYER = "'<input>' is not a valid player";

    /**
     * Default caption for {@link VelocityCaptionKeys#ARGUMENT_PARSE_FAILURE_SERVER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_SERVER = "'<input>' is not a valid server";

    private final ProxyServer proxyServer;
    private final SenderMapper<CommandSource, C> senderMapper;
    private final SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory;

    /**
     * Create a new command manager instance
     *
     * @param plugin                       Container for the owning plugin
     * @param proxyServer                  ProxyServer instance
     * @param commandExecutionCoordinator  Coordinator provider
     * @param senderMapper                 Function that maps {@link CommandSource} to the command sender type
     */
    @Inject
    @SuppressWarnings("unchecked")
    public VelocityCommandManager(
            final @NonNull PluginContainer plugin,
            final @NonNull ProxyServer proxyServer,
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull SenderMapper<CommandSource, C> senderMapper
    ) {
        super(commandExecutionCoordinator, new VelocityPluginRegistrationHandler<>());
        this.proxyServer = proxyServer;
        this.senderMapper = senderMapper;
        this.suggestionFactory = super.suggestionFactory().mapped(TooltipSuggestion::tooltipSuggestion);

        ((VelocityPluginRegistrationHandler<C>) this.commandRegistrationHandler()).initialize(this);

        /* Register Velocity Preprocessor */
        this.registerCommandPreProcessor(new VelocityCommandPreprocessor<>(this));

        /* Register Velocity Parsers */
        this.parserRegistry()
                .registerParser(PlayerParser.playerParser())
                .registerParser(ServerParser.serverParser());

        /* Register default captions */
        if (this.captionRegistry() instanceof FactoryDelegatingCaptionRegistry) {
            final FactoryDelegatingCaptionRegistry<C> factoryDelegatingCaptionRegistry = (FactoryDelegatingCaptionRegistry<C>)
                    this.captionRegistry();
            factoryDelegatingCaptionRegistry.registerMessageFactory(
                    VelocityCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER,
                    (context, key) -> ARGUMENT_PARSE_FAILURE_PLAYER
            );
            factoryDelegatingCaptionRegistry.registerMessageFactory(
                    VelocityCaptionKeys.ARGUMENT_PARSE_FAILURE_SERVER,
                    (context, key) -> ARGUMENT_PARSE_FAILURE_SERVER
            );
        }

        this.proxyServer.getEventManager().register(plugin, ServerPreConnectEvent.class, ev -> {
            this.lockRegistration();
        });
        this.parameterInjectorRegistry().registerInjector(
                CommandSource.class,
                (context, annotations) -> this.senderMapper.reverse(context.sender())
        );

        this.registerDefaultExceptionHandlers();
    }

    @Override
    public final boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        return this.senderMapper.reverse(sender).hasPermission(permission);
    }

    /**
     * {@inheritDoc}
     *
     * <p>This will always return true for {@link VelocityCommandManager}s.</p>
     *
     * @return {@code true}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @Override
    public final boolean hasBrigadierManager() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>{@link VelocityCommandManager}s always use Brigadier for registration, so the aforementioned check is not needed.</p>
     *
     * @return {@inheritDoc}
     * @since 1.2.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @Override
    public final @NonNull CloudBrigadierManager<C, CommandSource> brigadierManager() {
        return ((VelocityPluginRegistrationHandler<C>) this.commandRegistrationHandler()).brigadierManager();
    }

    @Override
    public final @NonNull SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory() {
        return this.suggestionFactory;
    }

    final @NonNull ProxyServer proxyServer() {
        return this.proxyServer;
    }

    private void registerDefaultExceptionHandlers() {
        this.registerHandler(Throwable.class, (source, exception) -> {
                source.sendMessage(
                        Identity.nil(),
                        Component.text(MESSAGE_INTERNAL_ERROR, NamedTextColor.RED)
                );
                exception.printStackTrace();
        });
        this.registerHandler(CommandExecutionException.class, (source, exception) -> {
                source.sendMessage(
                        Identity.nil(),
                        Component.text(
                                MESSAGE_INTERNAL_ERROR,
                                NamedTextColor.RED
                        )
                );
                exception.getCause().printStackTrace();
        });
        this.registerHandler(ArgumentParseException.class, (source, exception) ->
                source.sendMessage(
                        Identity.nil(),
                        Component.text()
                                .append(Component.text("Invalid Command Argument: ", NamedTextColor.RED))
                                .append(Component.text(exception.getCause().getMessage(), NamedTextColor.GRAY))
                                .build()
                )
        );
        this.registerHandler(NoSuchCommandException.class, (source, exception) ->
                source.sendMessage(Identity.nil(), Component.text(MESSAGE_UNKNOWN_COMMAND))
        );
        this.registerHandler(NoPermissionException.class, (source, exception) ->
                source.sendMessage(Identity.nil(), Component.text(MESSAGE_NO_PERMS))
        );
        this.registerHandler(InvalidCommandSenderException.class, (source, exception) ->
                source.sendMessage(Identity.nil(), Component.text(exception.getMessage(), NamedTextColor.RED))
        );
        this.registerHandler(InvalidSyntaxException.class, (source, exception) ->
                source.sendMessage(
                        Identity.nil(),
                        Component.text()
                                .append(Component.text("Invalid Command Syntax. Correct command syntax is: ", NamedTextColor.RED))
                                .append(Component.text(exception.correctSyntax(), NamedTextColor.GRAY))
                                .build()
                )
        );
    }

    private <T extends Throwable> void registerHandler(
            final @NonNull Class<T> exceptionType,
            final @NonNull VelocityExceptionHandler<C, T> handler
    ) {
        this.exceptionController().registerHandler(exceptionType, handler);
    }

    @Override
    public final @NonNull SenderMapper<CommandSource, C> senderMapper() {
        return this.senderMapper;
    }


    @FunctionalInterface
    @SuppressWarnings("FunctionalInterfaceMethodChanged")
    private interface VelocityExceptionHandler<C, T extends Throwable> extends ExceptionHandler<C, T> {

        @Override
        default void handle(@NonNull ExceptionContext<C, T> context) throws Throwable {
            final CommandSource source = context.context().inject(CommandSource.class).orElseThrow(NullPointerException::new);
            this.handle(source, context.exception());
        }

        void handle(@NonNull CommandSource source, @NonNull T exception) throws Throwable;
    }
}
