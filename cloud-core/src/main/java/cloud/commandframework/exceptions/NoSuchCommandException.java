//
// MIT License
//
// Copyright (c) 2024 Incendo
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

import cloud.commandframework.CommandComponent;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Exception thrown when a command sender tries to execute
 * a command that doesn't exist
 */
@SuppressWarnings("unused")
@API(status = API.Status.STABLE)
public final class NoSuchCommandException extends CommandParseException {

    private final String suppliedCommand;

    /**
     * Construct a no such command exception
     *
     * @param commandSender Sender who executed the command
     * @param currentChain  Chain leading up to the exception
     * @param command       Entered command (following the command chain)
     */
    @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
    public NoSuchCommandException(
            final @NonNull Object commandSender,
            final @NonNull List<CommandComponent<?>> currentChain,
            final @NonNull String command
    ) {
        super(commandSender, currentChain);
        this.suppliedCommand = command;
    }


    @Override
    public String getMessage() {
        final StringBuilder builder = new StringBuilder();
        for (final CommandComponent<?> commandComponent : this.currentChain()) {
            if (commandComponent == null) {
                continue;
            }
            builder.append(" ").append(commandComponent.name());
        }
        return String.format("Unrecognized command input '%s' following chain%s", this.suppliedCommand, builder.toString());
    }

    /**
     * Returns the supplied command.
     *
     * @return supplied command
     */
    public @NonNull String suppliedCommand() {
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
