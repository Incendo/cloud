//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
package com.intellectualsites.commands.execution.preprocessor;

import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Queue;

/**
 * Context for {@link CommandPreprocessor command preprocessors}
 *
 * @param <C> Command sender type
 */
public final class CommandPreprocessingContext<C extends CommandSender> {

    private final CommandContext<C> commandContext;
    private final Queue<String> inputQueue;

    /**
     * Construct a new command preprocessing context
     *
     * @param commandContext Command context
     * @param inputQueue     Command input as supplied by sender
     */
    public CommandPreprocessingContext(@Nonnull final CommandContext<C> commandContext,
                                       @Nonnull final Queue<String> inputQueue) {
        this.commandContext = commandContext;
        this.inputQueue = inputQueue;
    }

    /**
     * Get the command context
     *
     * @return Command context
     */
    @Nonnull
    public CommandContext<C> getCommandContext() {
        return this.commandContext;
    }

    /**
     * Get the original input queue. All changes will persist and will be
     * used during parsing
     *
     * @return Input queue
     */
    @Nonnull
    public Queue<String> getInputQueue() {
        return this.inputQueue;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CommandPreprocessingContext<?> that = (CommandPreprocessingContext<?>) o;
        return Objects.equals(getCommandContext(), that.getCommandContext()) &&
                Objects.equals(getInputQueue(), that.getInputQueue());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getCommandContext(), getInputQueue());
    }

}
