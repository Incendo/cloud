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

import cloud.commandframework.arguments.CommandArgument;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

/**
 * Exception thrown when a command sender tries to execute
 * a command that doesn't exist
 */
@SuppressWarnings("unused")
public final class NoSuchCommandException extends CommandParseException {

    private static final long serialVersionUID = -7775865652882764771L;
    private final String suppliedCommand;

    /**
     * Construct a no such command exception
     *
     * @param commandSender Sender who executed the command
     * @param currentChain  Chain leading up to the exception
     * @param command       Entered command (following the command chain)
     */
    public NoSuchCommandException(
            final @NonNull Object commandSender,
            final @NonNull List<CommandArgument<?, ?>> currentChain,
            final @NonNull String command
    ) {
        super(commandSender, currentChain);
        this.suppliedCommand = command;
    }


    @Override
    public String getMessage() {
        final StringBuilder builder = new StringBuilder();
        for (final CommandArgument<?, ?> commandArgument : this.getCurrentChain()) {
            if (commandArgument == null) {
                continue;
            }
            builder.append(" ").append(commandArgument.getName());
        }
        return String.format("Unrecognized command input '%s' following chain%s", this.suppliedCommand, builder.toString());
    }

    /**
     * Get the supplied command
     *
     * @return Supplied command
     */
    public @NonNull String getSuppliedCommand() {
        return this.suppliedCommand;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    @Override
    public synchronized Throwable initCause(final Throwable cause) {
        return this;
    }

}
