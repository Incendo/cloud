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

import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * Exception thrown when an invalid command sender tries to execute a command
 */
public final class InvalidCommandSenderException extends CommandParseException {

    private static final long serialVersionUID = 7372142477529875598L;
    private final Class<?> requiredSender;
    private final Command<?> command;

    /**
     * Construct a new command parse exception
     *
     * @param commandSender  Sender who executed the command
     * @param requiredSender The sender type that is required
     * @param currentChain   Chain leading up to the exception
     */
    public InvalidCommandSenderException(
            final @NonNull Object commandSender,
            final @NonNull Class<?> requiredSender,
            final @NonNull List<@NonNull CommandArgument<?, ?>> currentChain
    ) {
        this(commandSender, requiredSender, currentChain, null);
    }

    /**
     * Construct a new command parse exception
     *
     * @param commandSender  Sender who executed the command
     * @param requiredSender The sender type that is required
     * @param currentChain   Chain leading up to the exception
     * @param command        Command
     * @since 1.4.0
     */
    public InvalidCommandSenderException(
            final @NonNull Object commandSender,
            final @NonNull Class<?> requiredSender,
            final @NonNull List<@NonNull CommandArgument<?, ?>> currentChain,
            final @Nullable Command<?> command
    ) {
        super(commandSender, currentChain);
        this.requiredSender = requiredSender;
        this.command = command;
    }

    /**
     * Get the required sender type
     *
     * @return Required sender type
     */
    public @NonNull Class<?> getRequiredSender() {
        return this.requiredSender;
    }

    @Override
    public String getMessage() {
        return String.format(
                "%s is not allowed to execute that command. Must be of type %s",
                getCommandSender().getClass().getSimpleName(),
                requiredSender.getSimpleName()
        );
    }

    /**
     * Get the Command which the sender is invalid for
     *
     * @return Command
     * @since 1.4.0
     */
    public @Nullable Command<?> getCommand() {
        return this.command;
    }

}
