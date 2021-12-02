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
package cloud.commandframework.fabric;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.internal.CommandRegistrationHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands.CommandSelection;
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
abstract class FabricCommandRegistrationHandler<C, S extends SharedSuggestionProvider> implements CommandRegistrationHandler {
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
     * @param <S> brig sender type
     * @return the built node
     */
    private static <S> LiteralCommandNode<S> buildRedirect(
            final @NonNull String alias,
            final @NonNull CommandNode<S> destination
    ) {
        // Redirects only work for nodes with children, but break the top argument-less command.
        // Manually adding the root command after setting the redirect doesn't fix it.
        // (See https://github.com/Mojang/brigadier/issues/46) Manually clone the node instead.
        LiteralArgumentBuilder<S> builder = LiteralArgumentBuilder
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
        @Override
        void initialize(final FabricCommandManager<C, FabricClientCommandSource> manager) {
            super.initialize(manager);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean registerCommand(final @NonNull Command<?> cmd) {
            final Command<C> command = (Command<C>) cmd;
            final RootCommandNode<FabricClientCommandSource> rootNode = ClientCommandManager.DISPATCHER.getRoot();
            final StaticArgument<C> first = ((StaticArgument<C>) command.getArguments().get(0));
            final CommandNode<FabricClientCommandSource> baseNode = this
                    .commandManager()
                    .brigadierManager()
                    .createLiteralCommandNode(
                            first.getName(),
                            command,
                            (src, perm) -> this.commandManager().hasPermission(
                                    this.commandManager().commandSourceMapper().apply(src),
                                    perm
                            ),
                            true,
                            new FabricExecutor<>(
                                    this.commandManager(),
                                    source -> source.getPlayer().getGameProfile().getName(),
                                    FabricClientCommandSource::sendError
                            )
                    );

            rootNode.addChild(baseNode);

            for (final String alias : first.getAlternativeAliases()) {
                rootNode.addChild(buildRedirect(alias, baseNode));
            }
            return true;
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
        @SuppressWarnings("unchecked")
        public boolean registerCommand(@NonNull final Command<?> command) {
            return this.registeredCommands.add((Command<C>) command);
        }

        private void registerAllCommands(final CommandDispatcher<CommandSourceStack> dispatcher, final boolean isDedicated) {
            this.commandManager().registrationCalled();
            for (final Command<C> command : this.registeredCommands) {
                /* Only register commands in the declared environment */
                final CommandSelection env = command.getCommandMeta().getOrDefault(
                        FabricServerCommandManager.META_REGISTRATION_ENVIRONMENT,
                        CommandSelection.ALL
                );

                if ((env == CommandSelection.INTEGRATED && isDedicated)
                        || (env == CommandSelection.DEDICATED && !isDedicated)) {
                    continue;
                }
                this.registerCommand(dispatcher.getRoot(), command);
            }
        }

        private void registerCommand(final RootCommandNode<CommandSourceStack> dispatcher, final Command<C> command) {
            @SuppressWarnings("unchecked")
            final StaticArgument<C> first = ((StaticArgument<C>) command.getArguments().get(0));
            final CommandNode<CommandSourceStack> baseNode = this.commandManager().brigadierManager().createLiteralCommandNode(
                    first.getName(),
                    command,
                    (src, perm) -> this.commandManager().hasPermission(
                            this.commandManager().commandSourceMapper().apply(src),
                            perm
                    ),
                    true,
                    new FabricExecutor<>(this.commandManager(), CommandSourceStack::getTextName, CommandSourceStack::sendFailure));

            dispatcher.addChild(baseNode);

            for (final String alias : first.getAlternativeAliases()) {
                dispatcher.addChild(buildRedirect(alias, baseNode));
            }
        }
    }
}
