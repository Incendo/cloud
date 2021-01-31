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
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A registration handler for Fabric API.
 *
 * <p>Subtypes exist for client and server commands.</p>
 *
 * @param <C> command sender type
 * @param <S> native sender type
 */
abstract class FabricCommandRegistrationHandler<C, S extends CommandSource> implements CommandRegistrationHandler {
    private @MonotonicNonNull FabricCommandManager<C, S> commandManager;

    void initialize(final FabricCommandManager<C, S> manager) {
        this.commandManager = manager;
    }

    FabricCommandManager<C, S> getCommandManager() {
        return this.commandManager;
    }

    static class Server<C> extends FabricCommandRegistrationHandler<C, ServerCommandSource> {
        private final Set<Command<C>> registeredCommands = ConcurrentHashMap.newKeySet();

        @Override
        void initialize(final FabricCommandManager<C, ServerCommandSource> manager) {
            super.initialize(manager);
            CommandRegistrationCallback.EVENT.register(this::registerAllCommands);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean registerCommand(@NonNull final Command<?> command) {
            return this.registeredCommands.add((Command<C>) command);
        }

        private void registerAllCommands(final CommandDispatcher<ServerCommandSource> dispatcher, final boolean isDedicated) {
            this.getCommandManager().registrationCalled();
            for (final Command<C> command : this.registeredCommands) {
                /* Only register commands in the declared environment */
                final RegistrationEnvironment env = command.getCommandMeta().getOrDefault(
                        FabricServerCommandManager.META_REGISTRATION_ENVIRONMENT,
                        RegistrationEnvironment.ALL
                );

                if ((env == RegistrationEnvironment.INTEGRATED && isDedicated)
                        || (env == RegistrationEnvironment.DEDICATED && !isDedicated)) {
                    continue;
                }
                this.registerCommand(dispatcher.getRoot(), command);
            }
        }

        private void registerCommand(final RootCommandNode<ServerCommandSource> dispatcher, final Command<C> command) {
            @SuppressWarnings("unchecked")
            final StaticArgument<C> first = ((StaticArgument<C>) command.getArguments().get(0));
            final CommandNode<ServerCommandSource> baseNode = this.getCommandManager().brigadierManager().createLiteralCommandNode(
                    first.getName(),
                    command,
                    (src, perm) -> this.getCommandManager().hasPermission(
                            this.getCommandManager().getCommandSourceMapper().apply(src),
                            perm
                    ),
                    true,
                    new FabricExecutor<>(this.getCommandManager(), ServerCommandSource::getName, ServerCommandSource::sendError));

            dispatcher.addChild(baseNode);

            for (final String alias : first.getAlternativeAliases()) {
                dispatcher.addChild(CommandManager.literal(alias).redirect(baseNode).build());
            }
        }

    }
}
