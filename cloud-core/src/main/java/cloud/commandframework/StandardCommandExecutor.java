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
package cloud.commandframework;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandContextFactory;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.handling.ExceptionController;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.CommandResult;
import cloud.commandframework.services.State;
import cloud.commandframework.util.CompletableFutures;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;

final class StandardCommandExecutor<C> implements CommandExecutor<C> {

    private final CommandManager<C> commandManager;
    private final CommandExecutionCoordinator<C> commandExecutionCoordinator;
    private final CommandContextFactory<C> commandContextFactory;

    StandardCommandExecutor(
            final @NonNull CommandManager<C> commandManager,
            final @NonNull CommandExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull CommandContextFactory<C> commandContextFactory
    ) {
        this.commandManager = commandManager;
        this.commandExecutionCoordinator = commandExecutionCoordinator;
        this.commandContextFactory = commandContextFactory;
    }

    @Override
    public @NonNull CompletableFuture<CommandResult<C>> executeCommand(
            final @NonNull C commandSender,
            final @NonNull String input,
            final @NonNull Consumer<CommandContext<C>> contextConsumer
    ) {
        final CommandContext<C> context = this.commandContextFactory.create(false, commandSender);
        contextConsumer.accept(context);
        final CommandInput commandInput = CommandInput.of(input);
        return this.executeCommand(context, commandInput).whenComplete((result, throwable) -> {
            if (throwable == null) {
                return;
            }
            try {
                this.commandManager.exceptionController().handleException(
                        context,
                        ExceptionController.unwrapCompletionException(throwable)
                );
            } catch (final RuntimeException runtimeException) {
                throw runtimeException;
            } catch (final Throwable e) {
                throw new CompletionException(e);
            }
        });
    }

    private @NonNull CompletableFuture<CommandResult<C>> executeCommand(
            final @NonNull CommandContext<C> context,
            final @NonNull CommandInput commandInput
    ) {
        /* Store a copy of the input queue in the context */
        context.store("__raw_input__", commandInput.copy());
        try {
            if (this.commandManager.preprocessContext(context, commandInput) == State.ACCEPTED) {
                return this.commandExecutionCoordinator().coordinateExecution(context, commandInput);
            }
        } catch (final Exception e) {
            return CompletableFutures.failedFuture(e);
        }
        /* Wasn't allowed to execute the command */
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NonNull CommandExecutionCoordinator<C> commandExecutionCoordinator() {
        return this.commandExecutionCoordinator;
    }
}
