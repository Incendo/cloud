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
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

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
    static <C> Builder<C> builder() {
        return new ExecutionCoordinatorBuilderImpl<>();
    }

    static <C> @NonNull ExecutionCoordinator<C> simpleCoordinator() {
        return ExecutionCoordinator.<C>builder().build();
    }

    static <C> @NonNull ExecutionCoordinator<C> coordinatorFor(final @NonNull Executor executor) {
        return ExecutionCoordinator.<C>builder().executor(executor).build();
    }

    static <C> @NonNull ExecutionCoordinator<C> asyncCoordinator() {
        return ExecutionCoordinator.<C>builder().commonPoolExecutor().build();
    }

    /**
     * Coordinate the execution of a command and return the result
     *
     * @param commandContext Command context
     * @param commandInput   Command input
     * @return Future that completes with the result
     */
    @NonNull CompletableFuture<CommandResult<C>> coordinateExecution(
            @NonNull CommandTree<C> commandTree,
            @NonNull CommandContext<C> commandContext,
            @NonNull CommandInput commandInput
    );

    @NonNull CompletableFuture<@NonNull List<@NonNull Suggestion>> coordinateSuggestions(
            @NonNull CommandTree<C> commandTree,
            @NonNull CommandContext<C> context,
            @NonNull CommandInput commandInput
    );

    /**
     * Builder for {@link ExecutionCoordinator}.
     *
     * @param <C> command sender type
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    interface Builder<C> {

        default @This @NonNull Builder<C> executor(final @NonNull Executor executor) {
            return this.parsingExecutor(executor)
                    .suggestionsExecutor(executor)
                    .postProcessingExecutor(executor)
                    .executionSchedulingExecutor(executor);
        }

        default @This @NonNull Builder<C> commonPoolExecutor() {
            return this.executor(ForkJoinPool.commonPool());
        }

        @This @NonNull Builder<C> parsingExecutor(@NonNull Executor executor);

        @This @NonNull Builder<C> suggestionsExecutor(@NonNull Executor executor);

        @This @NonNull Builder<C> postProcessingExecutor(@NonNull Executor executor);

        @This @NonNull Builder<C> executionSchedulingExecutor(@NonNull Executor executor);

        @This @NonNull Builder<C> lockExecution();

        /**
         * Creates a new {@link ExecutionCoordinator} from the state of this builder.
         *
         * @return new {@link ExecutionCoordinator}
         */
        @NonNull ExecutionCoordinator<C> build();
    }
}
