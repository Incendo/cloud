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
package cloud.commandframework.execution.postprocessor;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * Context for {@link CommandPostprocessor command postprocessors}
 *
 * @param <C> Command sender type
 */
public final class CommandPostprocessingContext<C> {

    private final CommandContext<@NonNull C> commandContext;
    private final Command<@NonNull C> command;

    /**
     * Construct a new command postprocessing context
     *
     * @param commandContext Command context
     * @param command        Command instance
     */
    public CommandPostprocessingContext(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull Command<@NonNull C> command
    ) {
        this.commandContext = commandContext;
        this.command = command;
    }

    /**
     * Get the command context
     *
     * @return Command context
     */
    public @NonNull CommandContext<@NonNull C> getCommandContext() {
        return this.commandContext;
    }

    /**
     * Get the command instance
     *
     * @return Command instance
     */
    public @NonNull Command<@NonNull C> getCommand() {
        return this.command;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CommandPostprocessingContext<?> that = (CommandPostprocessingContext<?>) o;
        return Objects.equals(getCommandContext(), that.getCommandContext())
                && Objects.equals(this.getCommand(), that.getCommand());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getCommandContext(), this.getCommand());
    }

}
