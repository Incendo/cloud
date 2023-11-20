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
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.services.State;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * The command execution coordinator is responsible for
 * coordinating command execution. This includes determining
 * what thread the command should be executed on, whether or
 * not command may be executed in parallel, etc.
 *
 * @param <C> Command sender type
 */
@API(status = API.Status.STABLE)
public abstract class CommandExecutionCoordinator<C> {

    private final CommandTree<C> commandTree;

    /**
     * Construct a new command execution coordinator
     *
     * @param commandTree Command tree
     */
    protected CommandExecutionCoordinator(final @NonNull CommandTree<C> commandTree) {
        this.commandTree = commandTree;
    }

    /**
     * Returns a simple command execution coordinator that executes all commands immediately, on the calling thread
     *
     * @param <C> Command sender type
     * @return New coordinator instance
     */
    public static <C> @NonNull Function<@NonNull CommandTree<C>,
            @NonNull CommandExecutionCoordinator<C>> simpleCoordinator() {
        return SimpleCoordinator::new;
    }

    /**
     * Coordinate the execution of a command and return the result
     *
     * @param commandContext Command context
     * @param commandInput   Command input
     * @return Future that completes with the result
     */
    public abstract @NonNull CompletableFuture<CommandResult<C>> coordinateExecution(
            @NonNull CommandContext<C> commandContext,
            @NonNull CommandInput commandInput
    );

    /**
     * Get the command tree
     *
     * @return Command tree
     */
    protected @NonNull CommandTree<C> getCommandTree() {
        return this.commandTree;
    }


    /**
     * A simple command execution coordinator that executes all commands immediately, on the calling thread
     *
     * @param <C> Command sender type
     */
    @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
    public static final class SimpleCoordinator<C> extends
            CommandExecutionCoordinator<C> {

        private SimpleCoordinator(final @NonNull CommandTree<C> commandTree) {
            super(commandTree);
        }

        @Override
        public @NonNull CompletableFuture<CommandResult<C>> coordinateExecution(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull CommandInput commandInput
        ) {
            return this.getCommandTree().parse(commandContext, commandInput).thenCompose(command -> {
                if (this.getCommandTree().commandManager().postprocessContext(commandContext, command) == State.ACCEPTED) {
                    return command.getCommandExecutionHandler()
                            .executeFuture(commandContext)
                            .thenApply(v -> new CommandResult<>(commandContext));
                }
                return CompletableFuture.completedFuture(new CommandResult<>(commandContext));
            });
        }
    }
}
