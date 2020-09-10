//
// MIT License
//
// Copyright (c) 2020 IntellectualSites
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
package com.intellectualsites.commands.execution;

import com.intellectualsites.commands.meta.CommandMeta;
import com.intellectualsites.commands.CommandTree;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * The command execution coordinator is responsible for
 * coordinating command execution. This includes determining
 * what thread the command should be executed on, whether or
 * not command may be executed in parallel, etc.
 *
 * @param <C> Command sender type
 * @param <M> Command meta type
 */
public abstract class CommandExecutionCoordinator<C extends CommandSender, M extends CommandMeta> {

    private final CommandTree<C, M> commandTree;

    /**
     * Construct a new command execution coordinator
     *
     * @param commandTree Command tree
     */
    public CommandExecutionCoordinator(@Nonnull final CommandTree<C, M> commandTree) {
        this.commandTree = commandTree;
    }

    /**
     * Returns a simple command execution coordinator that executes all commands immediately, on the calling thread
     *
     * @param <C> Command sender type
     * @param <M> Command meta type
     * @return New coordinator instance
     */
    public static <C extends CommandSender, M extends CommandMeta> Function<CommandTree<C, M>, CommandExecutionCoordinator<C, M>> simpleCoordinator() {
        return SimpleCoordinator::new;
    }

    public abstract CompletableFuture<CommandResult> coordinateExecution(@Nonnull final CommandContext<C> commandContext,
                                                                         @Nonnull final Queue<String> input);

    /**
     * Get the command tree
     *
     * @return Command tree
     */
    @Nonnull
    protected CommandTree<C, M> getCommandTree() {
        return this.commandTree;
    }


    /**
     * A simple command execution coordinator that executes all commands immediately, on the calling thread
     *
     * @param <C> Command sender type
     * @param <M> Command meta type
     */
    public static final class SimpleCoordinator<C extends CommandSender, M extends CommandMeta> extends CommandExecutionCoordinator<C, M> {

        private SimpleCoordinator(@Nonnull final CommandTree<C, M> commandTree) {
            super(commandTree);
        }

        @Override
        public CompletableFuture<CommandResult> coordinateExecution(@Nonnull CommandContext<C> commandContext,
                                                                    @Nonnull Queue<String> input) {
            final CompletableFuture<CommandResult> completableFuture = new CompletableFuture<>();
            try {
                this.getCommandTree().parse(commandContext, input).ifPresent(
                        command -> command.getCommandExecutionHandler().execute(commandContext));
                completableFuture.complete(new CommandResult());
            } catch (final Exception e) {
                completableFuture.completeExceptionally(e);
            }
            return completableFuture;
        }

    }

}
