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
package com.intellectualsites.commands.execution.postprocessor;

import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.context.CommandContext;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Context for {@link CommandPostprocessor command postprocessors}
 *
 * @param <C> Command sender type
 */
public final class CommandPostprocessingContext<C> {

    private final CommandContext<C> commandContext;
    private final Command<C> command;

    /**
     * Construct a new command postprocessing context
     *
     * @param commandContext Command context
     * @param command        Command instance
     */
    public CommandPostprocessingContext(@Nonnull final CommandContext<C> commandContext,
                                        @Nonnull final Command<C> command) {
        this.commandContext = commandContext;
        this.command = command;
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
     * Get the command instance
     *
     * @return Command instance
     */
    @Nonnull
    public Command<C> getCommand() {
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
