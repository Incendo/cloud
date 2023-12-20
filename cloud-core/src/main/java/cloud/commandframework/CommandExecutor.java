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
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.CommandResult;
import cloud.commandframework.execution.postprocessor.CommandPostprocessor;
import cloud.commandframework.execution.preprocessor.CommandPreprocessor;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE, since = "2.0.0")
public interface CommandExecutor<C> {

    /**
     * Executes a command and get a future that completes with the result. The command may be executed immediately
     * or at some point in the future, depending on the {@link CommandExecutionCoordinator} used in the command manager.
     *
     * <p>The command may also be filtered out by preprocessors (see {@link CommandPreprocessor}) before they are parsed,
     * or by the {@link CommandComponent} command components during parsing. The execution may also be filtered out
     * after parsing by a {@link CommandPostprocessor}. In the case that a command was filtered out at any of the
     * execution stages, the future will complete with {@code null}.</p>
     *
     * <p>The future may also complete exceptionally.
     * These exceptions will be handled using exception handlers registered in the
     * {@link cloud.commandframework.exceptions.handling.ExceptionController}.
     * The exceptions will be forwarded to the future, if the exception was transformed during the exception handling, then the
     * new exception will be present in the completed future.</p>
     *
     * @param commandSender Sender of the command
     * @param input         Input provided by the sender. Prefixes should be removed before the method is being called, and
     *                      the input here will be passed directly to the command parsing pipeline, after having been tokenized.
     * @return future that completes with the command result, or {@code null} if the execution was cancelled at any of the
     *         processing stages.
     */
    default @NonNull CompletableFuture<CommandResult<C>> executeCommand(
            final @NonNull C commandSender,
            final @NonNull String input
    ) {
        return this.executeCommand(commandSender, input, context -> {});
    }

    /**
     * Executes a command and get a future that completes with the result. The command may be executed immediately
     * or at some point in the future, depending on the {@link CommandExecutionCoordinator} used in the command manager.
     *
     * <p>The command may also be filtered out by preprocessors (see {@link CommandPreprocessor}) before they are parsed,
     * or by the {@link CommandComponent} command components during parsing. The execution may also be filtered out
     * after parsing by a {@link CommandPostprocessor}. In the case that a command was filtered out at any of the
     * execution stages, the future will complete with {@code null}.</p>
     *
     * <p>The future may also complete exceptionally.
     * These exceptions will be handled using exception handlers registered in the
     * {@link cloud.commandframework.exceptions.handling.ExceptionController}.
     * The exceptions will be forwarded to the future, if the exception was transformed during the exception handling, then the
     * new exception will be present in the completed future.</p>
     *
     * @param commandSender   the sender of the command
     * @param input           the input provided by the sender. Prefixes should be removed before the method is being called, and
     *                        the input here will be passed directly to the command parsing pipeline, after having been tokenized.
     * @param contextConsumer consumer that accepts the context before the command is executed
     * @return future that completes with the command result, or {@code null} if the execution was cancelled at any of the
     *         processing stages.
     */
    @NonNull CompletableFuture<CommandResult<C>> executeCommand(
            @NonNull C commandSender,
            @NonNull String input,
            @NonNull Consumer<CommandContext<C>> contextConsumer
    );

    /**
     * Returns the command execution coordinator.
     *
     * @return command execution coordinator
     */
    @NonNull CommandExecutionCoordinator<C> commandExecutionCoordinator();
}
