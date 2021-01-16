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
package cloud.commandframework.velocity;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.brigadier.BrigadierManagerHolder;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.captions.FactoryDelegatingCaptionRegistry;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.velocity.arguments.PlayerArgument;
import cloud.commandframework.velocity.arguments.ServerArgument;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

/**
 * {@link CommandManager} implementation for Velocity.
 * <p>
 * This can be injected if {@link CloudInjectionModule} is registered in the
 * injector. This can be achieved by using {@link com.google.inject.Injector#createChildInjector(Module...)}
 *
 * @param <C> Command sender type
 */
@Singleton
public class VelocityCommandManager<C> extends CommandManager<C> implements BrigadierManagerHolder<C> {

    /**
     * Default caption for {@link VelocityCaptionKeys#ARGUMENT_PARSE_FAILURE_PLAYER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_PLAYER = "'{input}' is not a valid player";

    /**
     * Default caption for {@link VelocityCaptionKeys#ARGUMENT_PARSE_FAILURE_SERVER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_SERVER = "'{input}' is not a valid server";

    private final ProxyServer proxyServer;
    private final Function<CommandSource, C> commandSenderMapper;
    private final Function<C, CommandSource> backwardsCommandSenderMapper;

    /**
     * Create a new command manager instance.
     *
     * @param proxyServer                  ProxyServer instance
     * @param commandExecutionCoordinator  Coordinator provider
     * @param commandSenderMapper          Function that maps {@link CommandSource} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link CommandSource}
     */
    @Deprecated
    public VelocityCommandManager(
            final @NonNull ProxyServer proxyServer,
            final @NonNull Function<@NonNull CommandTree<C>, @NonNull CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @NonNull Function<@NonNull CommandSource, @NonNull C> commandSenderMapper,
            final @NonNull Function<@NonNull C, @NonNull CommandSource> backwardsCommandSenderMapper
    ) {
        this(null, proxyServer, commandExecutionCoordinator, commandSenderMapper, backwardsCommandSenderMapper);
    }

    /**
     * Create a new command manager instance
     *
     * @param plugin                       Container for the owning plugin. Nullable for backwards compatibility
     * @param proxyServer                  ProxyServer instance
     * @param commandExecutionCoordinator  Coordinator provider
     * @param commandSenderMapper          Function that maps {@link CommandSource} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link CommandSource}
     */
    @Inject
    @SuppressWarnings("unchecked")
    public VelocityCommandManager(
            final @Nullable PluginContainer plugin,
            final @NonNull ProxyServer proxyServer,
            final @NonNull Function<@NonNull CommandTree<C>, @NonNull CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @NonNull Function<@NonNull CommandSource, @NonNull C> commandSenderMapper,
            final @NonNull Function<@NonNull C, @NonNull CommandSource> backwardsCommandSenderMapper
    ) {
        super(commandExecutionCoordinator, new VelocityPluginRegistrationHandler<>());
        ((VelocityPluginRegistrationHandler<C>) this.getCommandRegistrationHandler()).initialize(this);
        this.proxyServer = proxyServer;
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;

        /* Register Velocity Preprocessor */
        this.registerCommandPreProcessor(new VelocityCommandPreprocessor<>(this));

        /* Register Velocity Parsers */
        this.getParserRegistry().registerParserSupplier(TypeToken.get(Player.class), parserParameters ->
                new PlayerArgument.PlayerParser<>());
        this.getParserRegistry().registerParserSupplier(TypeToken.get(RegisteredServer.class), parserParameters ->
                new ServerArgument.ServerParser<>());

        /* Register default captions */
        if (this.getCaptionRegistry() instanceof FactoryDelegatingCaptionRegistry) {
            final FactoryDelegatingCaptionRegistry<C> factoryDelegatingCaptionRegistry = (FactoryDelegatingCaptionRegistry<C>)
                    this.getCaptionRegistry();
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
    }

    @Override
    public final boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        return this.backwardsCommandSenderMapper.apply(sender).hasPermission(permission);
    }

    @Override
    public final @NonNull CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

    /**
     * {@inheritDoc}
     * <p>
     * In the case of the {@link VelocityCommandManager}, Brigadier is always used for command registration,
     * and therefore this method will never return {@code null}.
     *
     * @since 1.2.0
     */
    @SuppressWarnings("unchecked")
    @Override
    public @NonNull CloudBrigadierManager<C, CommandSource> brigadierManager() {
        return ((VelocityPluginRegistrationHandler<C>) this.getCommandRegistrationHandler()).brigadierManager();
    }

    final @NonNull ProxyServer getProxyServer() {
        return this.proxyServer;
    }

    final @NonNull Function<@NonNull CommandSource, @NonNull C> getCommandSenderMapper() {
        return this.commandSenderMapper;
    }

}
