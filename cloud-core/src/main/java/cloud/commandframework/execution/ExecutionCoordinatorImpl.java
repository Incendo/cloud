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
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.CommandParseException;
import cloud.commandframework.services.State;
import cloud.commandframework.types.tuples.Pair;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
final class ExecutionCoordinatorImpl<C> implements ExecutionCoordinator<C> {

    static final Executor NON_SCHEDULING_EXECUTOR = new NonSchedulingExecutor();

    private static final class NonSchedulingExecutor implements Executor {

        @Override
        public void execute(final Runnable command) {
            command.run();
        }
    }

    /**
     * runs parsing logic. when interacting with futures that complete in unknown thread contexts (i.e. parsers), parsing will
     * chain further logic using the 'Async' variant of CF methods and this executor.
     */
    private final @NonNull Executor parsingExecutor;

    /**
     * similar to parsingExecutor but for suggestion logic
     * (also acts as parsing executor for any parsing done during suggestions logic)
     */
    private final @NonNull Executor suggestionsExecutor;

    /**
     * schedules command execution futures
     */
    private final @NonNull Executor defaultExecutionExecutor;

    private final @Nullable Semaphore executionLock;

    ExecutionCoordinatorImpl(
            final @Nullable Executor parsingExecutor,
            final @Nullable Executor suggestionsExecutor,
            final @Nullable Executor defaultExecutionExecutor,
            final boolean syncExecution
    ) {
        this.parsingExecutor = orRunNow(parsingExecutor);
        this.suggestionsExecutor = orRunNow(suggestionsExecutor);
        this.defaultExecutionExecutor = orRunNow(defaultExecutionExecutor);
        this.executionLock = syncExecution ? new Semaphore(1) : null;
    }

    private static @NonNull Executor orRunNow(final @Nullable Executor e) {
        return e == null ? ExecutionCoordinator.nonSchedulingExecutor() : e;
    }

    @Override
    public @NonNull CompletableFuture<CommandResult<C>> coordinateExecution(
            final @NonNull CommandTree<C> commandTree,
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        return commandTree.parse(commandContext, commandInput, this.parsingExecutor)
                .thenApplyAsync(command -> {
                    final boolean passedPostprocessing =
                            commandTree.commandManager().postprocessContext(commandContext, command) == State.ACCEPTED;
                    return Pair.of(command, passedPostprocessing);
                }, this.parsingExecutor)
                .thenComposeAsync(preprocessResult -> {
                    if (!preprocessResult.getSecond()) {
                        return CompletableFuture.completedFuture(CommandResult.of(commandContext));
                    }

                    if (this.executionLock != null) {
                        try {
                            this.executionLock.acquire();
                        } catch (final InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    CompletableFuture<CommandResult<C>> commandResultFuture = null;
                    try {
                        commandResultFuture = preprocessResult.getFirst()
                                .commandExecutionHandler()
                                .executeFuture(commandContext)
                                .exceptionally(exception -> {
                                    final Throwable workingException;
                                    if (exception instanceof CompletionException) {
                                        workingException = exception.getCause();
                                    } else {
                                        workingException = exception;
                                    }

                                    if (workingException instanceof CommandParseException) {
                                        throw (CommandParseException) workingException;
                                    } else if (workingException instanceof CommandExecutionException) {
                                        throw (CommandExecutionException) workingException;
                                    } else {
                                        throw new CommandExecutionException(workingException, commandContext);
                                    }
                                })
                                .thenApply(v -> CommandResult.of(commandContext));
                    } finally {
                        if (this.executionLock != null) {
                            if (commandResultFuture != null) {
                                commandResultFuture.whenComplete(($, $$) -> this.executionLock.release());
                            } else {
                                this.executionLock.release();
                            }
                        }
                    }

                    return commandResultFuture;
                }, this.defaultExecutionExecutor);
    }

    @Override
    public @NonNull CompletableFuture<@NonNull List<@NonNull Suggestion>> coordinateSuggestions(
            final @NonNull CommandTree<C> commandTree,
            final @NonNull CommandContext<C> context,
            final @NonNull CommandInput commandInput
    ) {
        return commandTree.getSuggestions(context, commandInput, this.suggestionsExecutor);
    }
}
