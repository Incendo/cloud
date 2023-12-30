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
package cloud.commandframework.execution;

import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.suggestion.Suggestions;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.checkerframework.dataflow.qual.Pure;

/**
 * The {@link ExecutionCoordinator execution coordinator} is responsible for coordinating execution of command parsing,
 * handlers, suggestions, etc. It can determine what thread context certain stages of the pipeline will run in, allowing it to
 * for example, disallow concurrent command execution.
 *
 * @param <C> command sender type
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public interface ExecutionCoordinator<C> {

    /**
     * Creates a new {@link Builder}.
     *
     * @param <C> command sender type
     * @return new {@link Builder}
     */
    static <C> @Pure @NonNull Builder<C> builder() {
        return new ExecutionCoordinatorBuilderImpl<>();
    }

    /**
     * Create a new execution coordinator that does not make any attempt to schedule tasks to a particular executor. Parsing
     * and suggestions will run on the calling thread until redirected by a parser, suggestion provider, or similar, at which
     * point execution will continue on that thread.
     *
     * @param <C> command sender type
     * @return new coordinator
     */
    static <C> @Pure @NonNull ExecutionCoordinator<C> simpleCoordinator() {
        return ExecutionCoordinator.<C>builder().build();
    }

    /**
     * Create a new execution coordinator that schedules to {@code executor} at every possible point in the pipeline.
     *
     * @param executor executor to use
     * @param <C>      command sender type
     * @return new coordinator
     */
    static <C> @Pure @NonNull ExecutionCoordinator<C> coordinatorFor(final @NonNull Executor executor) {
        return ExecutionCoordinator.<C>builder().executor(executor).build();
    }

    /**
     * Create a new execution coordinator that schedules to {@link ForkJoinPool#commonPool() the common pool} at every possible
     * point in the pipeline.
     *
     * @param <C> command sender type
     * @return new coordinator
     */
    static <C> @Pure @NonNull ExecutionCoordinator<C> asyncCoordinator() {
        return ExecutionCoordinator.<C>builder().commonPoolExecutor().build();
    }

    /**
     * Coordinate the execution of a command and return the result
     *
     * @param commandTree    command tree to suggest from
     * @param commandContext command context
     * @param commandInput   command input
     * @return future that completes with the result
     */
    @NonNull CompletableFuture<CommandResult<C>> coordinateExecution(
            @NonNull CommandTree<C> commandTree,
            @NonNull CommandContext<C> commandContext,
            @NonNull CommandInput commandInput
    );

    /**
     * Coordinates the execution of a suggestions query.
     *
     * @param commandTree  command tree to suggest from
     * @param context      command context
     * @param commandInput command input
     * @return future that completes with the result
     */
    @NonNull CompletableFuture<@NonNull Suggestions<C, ?>> coordinateSuggestions(
            @NonNull CommandTree<C> commandTree,
            @NonNull CommandContext<C> context,
            @NonNull CommandInput commandInput
    );

    /**
     * Returns the non-scheduling executor. This is an executor that simply invokes {@link Runnable#run()} immediately on the
     * calling thread of {@link Executor#execute(Runnable)}.
     *
     * @return the non-scheduling executor
     */
    static @Pure @NonNull Executor nonSchedulingExecutor() {
        return ExecutionCoordinatorImpl.NON_SCHEDULING_EXECUTOR;
    }

    /**
     * Builder for {@link ExecutionCoordinator}.
     *
     * <p>Any executors left unset will default to a {@link #nonSchedulingExecutor() non-scheduling executor} that
     * executes tasks immediately on the calling thread (meaning it will not redirect execution to another thread context, it
     * will continue in the current one).</p>
     *
     * @param <C> command sender type
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    interface Builder<C> {

        /**
         * Sets all of:
         * <ul>
         *     <li>{@link #parsingExecutor(Executor)}</li>
         *     <li>{@link #suggestionsExecutor(Executor)}</li>
         *     <li>{@link #executionSchedulingExecutor(Executor)}</li>
         * </ul>
         * using the provided executor.
         *
         * @param executor executor to use
         * @return this builder
         */
        default @This @NonNull Builder<C> executor(final @NonNull Executor executor) {
            return this.parsingExecutor(executor)
                    .suggestionsExecutor(executor)
                    .executionSchedulingExecutor(executor);
        }

        /**
         * Sets {@link #executor(Executor)} to the {@link ForkJoinPool#commonPool() common pool}.
         *
         * @return this builder
         */
        default @This @NonNull Builder<C> commonPoolExecutor() {
            return this.executor(ForkJoinPool.commonPool());
        }

        /**
         * Sets the executor to run parsing logic on.
         *
         * @param executor executor to use
         * @return this builder
         */
        @This @NonNull Builder<C> parsingExecutor(@NonNull Executor executor);

        /**
         * Sets the executor to run suggestions logic on.
         *
         * @param executor executor to use
         * @return this builder
         */
        @This @NonNull Builder<C> suggestionsExecutor(@NonNull Executor executor);

        /**
         * Sets the executor to {@link CommandExecutionHandler#executeFuture(CommandContext) schedule command execution} from.
         *
         * @param executor executor to use
         * @return this builder
         */
        @This @NonNull Builder<C> executionSchedulingExecutor(@NonNull Executor executor);

        /**
         * Sets the execution coordinator to disallow concurrent {@link CommandExecutionHandler command handler} execution.
         *
         * @return this builder
         */
        default @This @NonNull Builder<C> synchronizeExecution() {
            return this.synchronizeExecution(true);
        }

        /**
         * Sets whether the execution coordinator should allow concurrent {@link CommandExecutionHandler command handler}
         * execution.
         *
         * @param synchronizeExecution whether execution should be synchronized
         * @return this builder
         */
        @This @NonNull Builder<C> synchronizeExecution(boolean synchronizeExecution);

        /**
         * Creates a new {@link ExecutionCoordinator} from the current state of this builder.
         *
         * @return new {@link ExecutionCoordinator}
         */
        @NonNull ExecutionCoordinator<C> build();
    }
}
