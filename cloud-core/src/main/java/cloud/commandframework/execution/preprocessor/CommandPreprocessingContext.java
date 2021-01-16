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
package cloud.commandframework.execution.preprocessor;

import cloud.commandframework.context.CommandContext;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedList;
import java.util.Objects;

/**
 * Context for {@link CommandPreprocessor command preprocessors}
 *
 * @param <C> Command sender type
 */
public final class CommandPreprocessingContext<C> {

    private final CommandContext<C> commandContext;
    private final LinkedList<String> inputQueue;

    /**
     * Construct a new command preprocessing context
     *
     * @param commandContext Command context
     * @param inputQueue     Command input as supplied by sender
     */
    public CommandPreprocessingContext(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull LinkedList<@NonNull String> inputQueue
    ) {
        this.commandContext = commandContext;
        this.inputQueue = inputQueue;
    }

    /**
     * Get the command context
     *
     * @return Command context
     */
    public @NonNull CommandContext<C> getCommandContext() {
        return this.commandContext;
    }

    /**
     * Get the original input queue. All changes will persist and will be
     * used during parsing
     *
     * @return Input queue
     */
    public @NonNull LinkedList<@NonNull String> getInputQueue() {
        return this.inputQueue;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CommandPreprocessingContext<?> that = (CommandPreprocessingContext<?>) o;
        return Objects.equals(getCommandContext(), that.getCommandContext())
                && Objects.equals(getInputQueue(), that.getInputQueue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandContext(), getInputQueue());
    }

}
