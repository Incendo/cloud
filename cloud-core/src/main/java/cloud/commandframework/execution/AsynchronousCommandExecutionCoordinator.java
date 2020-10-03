//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
package cloud.commandframework.execution;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.services.State;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Execution coordinator parses and/or executes commands on a separate thread from the calling thread
 *
 * @param <C> Command sender type
 */
public final class AsynchronousCommandExecutionCoordinator<C> extends CommandExecutionCoordinator<C> {

    private final CommandManager<C> commandManager;
    private final Executor executor;
    private final boolean synchronizeParsing;

    private AsynchronousCommandExecutionCoordinator(final @Nullable Executor executor,
                                                    final boolean synchronizeParsing,
                                                    final @NonNull CommandTree<C> commandTree) {
        super(commandTree);
        this.executor = executor;
        this.synchronizeParsing = synchronizeParsing;
        this.commandManager = commandTree.getCommandManager();
    }

    /**
     * Create a new {@link Builder} instance
     *
     * @param <C> Command sender type
     * @return Builder
     */
    public static <C> @NonNull Builder<C> newBuilder() {
        return new Builder<>();
    }

    @Override
    public @NonNull CompletableFuture<CommandResult<C>> coordinateExecution(final @NonNull CommandContext<C> commandContext,
                                                                            final @NonNull Queue<@NonNull String> input) {

        final Consumer<Command<C>> commandConsumer = command -> {
            if (this.commandManager.postprocessContext(commandContext, command) == State.ACCEPTED) {
                command.getCommandExecutionHandler().execute(commandContext);
            }
        };
        final Supplier<CommandResult<C>> supplier;
        if (this.synchronizeParsing) {
            final Optional<Command<C>> commandOptional = this.getCommandTree().parse(commandContext, input);
            supplier = () -> {
                commandOptional.ifPresent(commandConsumer);
                return new CommandResult<>(commandContext);
            };
        } else {
            supplier = () -> {
                this.getCommandTree().parse(commandContext, input).ifPresent(commandConsumer);
                return new CommandResult<>(commandContext);
            };
        }
        if (this.executor != null) {
            return CompletableFuture.supplyAsync(supplier, this.executor);
        } else {
            return CompletableFuture.supplyAsync(supplier);
        }
    }


    /**
     * Builder for {@link AsynchronousCommandExecutionCoordinator} instances
     *
     * @param <C> Command sender type
     */
    public static final class Builder<C> {

        private Executor executor = null;
        private boolean synchronizeParsing = false;

        private Builder() {
        }

        /**
         * This forces the command parsing to run on the calling thread,
         * and only the actual command execution will run using the executor
         *
         * @return Builder instance
         */
        public @NonNull Builder<C> withSynchronousParsing() {
            this.synchronizeParsing = true;
            return this;
        }

        /**
         * Both command parsing and execution will run using the executor
         *
         * @return Builder instance
         */
        public @NonNull Builder<C> withAsynchronousParsing() {
            this.synchronizeParsing = false;
            return this;
        }

        /**
         * Specify an executor that will be used to coordinate tasks.
         * By default the executor uses {@link java.util.concurrent.ForkJoinPool#commonPool()}
         *
         * @param executor Executor to use
         * @return Builder instance
         */
        public @NonNull Builder<C> withExecutor(final @NonNull Executor executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Builder a function that generates a command execution coordinator
         * using the options specified in this builder
         *
         * @return Function that builds the coordinator
         */
        public @NonNull Function<@NonNull CommandTree<C>, @NonNull CommandExecutionCoordinator<C>> build() {
            return tree -> new AsynchronousCommandExecutionCoordinator<>(this.executor, this.synchronizeParsing, tree);
        }

    }

}
