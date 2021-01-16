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
package cloud.commandframework.exceptions;

import cloud.commandframework.context.CommandContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Exception thrown when there is an exception during execution of a command handler
 *
 * @since 1.2.0
 */
public class CommandExecutionException extends IllegalArgumentException {

    private static final long serialVersionUID = -4785446899438294661L;
    private final CommandContext<?> commandContext;

    /**
     * Exception thrown when there is an exception during execution of a command handler
     *
     * @param cause Exception thrown during the execution of a command handler
     */
    public CommandExecutionException(final @NonNull Throwable cause) {
        this(cause, null);
    }

    /**
     * Exception thrown when there is an exception during execution of a command handler
     *
     * @param cause Exception thrown during the execution of a command handler
     * @param commandContext Command context
     * @since 1.4.0
     */
    public CommandExecutionException(final @NonNull Throwable cause, final @Nullable CommandContext<?> commandContext) {
        super(cause);
        this.commandContext = commandContext;
    }

    /**
     * Get the CommandContext which led to this exception
     *
     * @return Command
     * @since 1.4.0
     */
    public @Nullable CommandContext<?> getCommandContext() {
        return this.commandContext;
    }

}
