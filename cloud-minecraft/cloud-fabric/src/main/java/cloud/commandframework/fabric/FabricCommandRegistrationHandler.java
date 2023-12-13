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
package cloud.commandframework.fabric;

import cloud.commandframework.Command;
import cloud.commandframework.CommandComponent;
import cloud.commandframework.brigadier.permission.BrigadierPermissionChecker;
import cloud.commandframework.fabric.argument.FabricVanillaArgumentParsers;
import cloud.commandframework.internal.CommandRegistrationHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A registration handler for Fabric API.
 *
 * <p>Subtypes exist for client and server commands.</p>
 *
 * @param <C> command sender type
 * @param <S> native sender type
 */
abstract class FabricCommandRegistrationHandler<C, S extends SharedSuggestionProvider> implements CommandRegistrationHandler<C> {

    private @MonotonicNonNull FabricCommandManager<C, S> commandManager;

    void initialize(final FabricCommandManager<C, S> manager) {
        this.commandManager = manager;
    }

    FabricCommandManager<C, S> commandManager() {
        return this.commandManager;
    }

    /**
     * Returns a literal node that redirects its execution to
     * the given destination node.
     *
     * <p>This method is taken from MIT licensed code in the Velocity project, see
     * <a href="https://github.com/VelocityPowered/Velocity/blob/b88c573eb11839a95bea1af947b0c59a5956368b/proxy/src/main/java/com/velocitypowered/proxy/util/BrigadierUtils.java#L33">
     * Velocity's BrigadierUtils class</a></p>
     *
     * @param alias       the command alias
     * @param destination the destination node
     * @param <S>         brig sender type
     * @return the built node
     */
    private static <S> LiteralCommandNode<S> buildRedirect(
            final @NonNull String alias,
            final @NonNull CommandNode<S> destination
    ) {
        // Redirects only work for nodes with children, but break the top argument-less command.
        // Manually adding the root command after setting the redirect doesn't fix it.
        // (See https://github.com/Mojang/brigadier/issues/46) Manually clone the node instead.
        final LiteralArgumentBuilder<S> builder = LiteralArgumentBuilder
                .<S>literal(alias)
                .requires(destination.getRequirement())
                .forward(
                        destination.getRedirect(),
                        destination.getRedirectModifier(),
                        destination.isFork()
                )
                .executes(destination.getCommand());
        for (final CommandNode<S> child : destination.getChildren()) {
            builder.then(child);
        }
        return builder.build();
    }

    static class Client<C> extends FabricCommandRegistrationHandler<C, FabricClientCommandSource> {

        private final Set<Command<C>> registeredCommands = ConcurrentHashMap.newKeySet();
        private volatile boolean registerEventFired = false;

        @Override
        void initialize(final FabricCommandManager<C, FabricClientCommandSource> manager) {
            super.initialize(manager);
            ClientCommandRegistrationCallback.EVENT.register(this::registerCommands);
            ClientPlayConnectionEvents.DISCONNECT.register(($, $$) -> this.registerEventFired = false);
        }

        @Override
        public boolean registerCommand(final @NonNull Command<C> command) {
            this.registeredCommands.add(command);
            if (this.registerEventFired) {
                final ClientPacketListener connection = Minecraft.getInstance().getConnection();
                if (connection == null) {
                    throw new IllegalStateException("Expected connection to be present but it wasn't!");
                }
                final CommandDispatcher<FabricClientCommandSource> dispatcher = ClientCommandManager.getActiveDispatcher();
                if (dispatcher == null) {
                    throw new IllegalStateException("Expected an active dispatcher!");
                }
                FabricVanillaArgumentParsers.ContextualArgumentTypeProvider.withBuildContext(
                        this.commandManager(),
                        CommandBuildContext.simple(connection.registryAccess(), connection.enabledFeatures()),
                        false,
                        () -> this.registerClientCommand(dispatcher, command)
                );
            }
            return true;
        }

        public void registerCommands(
                final CommandDispatcher<FabricClientCommandSource> dispatcher,
                final CommandBuildContext commandBuildContext
        ) {
            this.registerEventFired = true;
            FabricVanillaArgumentParsers.ContextualArgumentTypeProvider.withBuildContext(
                    this.commandManager(),
                    commandBuildContext,
                    true,
                    () -> {
                        for (final Command<C> command : this.registeredCommands) {
                            this.registerClientCommand(dispatcher, command);
                        }
                    }
            );
        }

        private void registerClientCommand(
                final CommandDispatcher<FabricClientCommandSource> dispatcher,
                final Command<C> command
        ) {
            final RootCommandNode<FabricClientCommandSource> rootNode = dispatcher.getRoot();
            final CommandComponent<C> component = command.rootComponent();
            final CommandNode<FabricClientCommandSource> baseNode = this.commandManager()
                    .brigadierManager()
                    .literalBrigadierNodeFactory()
                    .createNode(
                            component.name(),
                            command,
                            (src, perm) -> this.commandManager().hasPermission(
                                    this.commandManager().commandSourceMapper().apply(src),
                                    perm
                            ),
                            true,
                            new FabricExecutor<>(this.commandManager())
                    );

            rootNode.addChild(baseNode);

            for (final String alias : component.alternativeAliases()) {
                rootNode.addChild(buildRedirect(alias, baseNode));
            }
        }
    }

    static class Server<C> extends FabricCommandRegistrationHandler<C, CommandSourceStack> {

        private final Set<Command<C>> registeredCommands = ConcurrentHashMap.newKeySet();

        @Override
        void initialize(final FabricCommandManager<C, CommandSourceStack> manager) {
            super.initialize(manager);
            CommandRegistrationCallback.EVENT.register(this::registerAllCommands);
        }

        @Override
        public boolean registerCommand(@NonNull final Command<C> command) {
            return this.registeredCommands.add(command);
        }

        private void registerAllCommands(
                final CommandDispatcher<CommandSourceStack> dispatcher,
                final CommandBuildContext access,
                final Commands.CommandSelection side
        ) {
            this.commandManager().registrationCalled();
            FabricVanillaArgumentParsers.ContextualArgumentTypeProvider.withBuildContext(
                    this.commandManager(),
                    access,
                    true,
                    () -> {
                        for (final Command<C> command : this.registeredCommands) {
                            /* Only register commands in the declared environment */
                            final Commands.CommandSelection env = command.commandMeta().getOrDefault(
                                    FabricServerCommandManager.META_REGISTRATION_ENVIRONMENT,
                                    Commands.CommandSelection.ALL
                            );

                            if ((env == Commands.CommandSelection.INTEGRATED && !side.includeIntegrated)
                                    || (env == Commands.CommandSelection.DEDICATED && !side.includeDedicated)) {
                                continue;
                            }
                            this.registerCommand(dispatcher.getRoot(), command);
                        }
                    }
            );
        }

        private void registerCommand(final RootCommandNode<CommandSourceStack> dispatcher, final Command<C> command) {
            final CommandComponent<C> component = command.rootComponent();
            final FabricExecutor<C, CommandSourceStack> executor = new FabricExecutor<>(this.commandManager());
            final BrigadierPermissionChecker<CommandSourceStack> permission = (src, perm) -> this.commandManager().hasPermission(
                    this.commandManager().commandSourceMapper().apply(src),
                    perm
            );
            final CommandNode<CommandSourceStack> baseNode = this.commandManager()
                    .brigadierManager()
                    .literalBrigadierNodeFactory()
                    .createNode(
                        component.name(),
                        command,
                        permission,
                        true /* forceRegister */,
                        executor
            );

            dispatcher.addChild(baseNode);

            for (final String alias : component.alternativeAliases()) {
                dispatcher.addChild(buildRedirect(alias, baseNode));
            }
        }
    }
}
