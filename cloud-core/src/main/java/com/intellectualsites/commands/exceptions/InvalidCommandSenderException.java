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
package com.intellectualsites.commands.exceptions;

import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Exception thrown when an invalid command sender tries to execute a command
 */
public final class InvalidCommandSenderException extends CommandParseException {

    private final Class<?> requiredSender;

    /**
     * Construct a new command parse exception
     *
     * @param commandSender  Sender who executed the command
     * @param requiredSender The sender type that is required
     * @param currentChain   Chain leading up to the exception
     */
    public InvalidCommandSenderException(@Nonnull final CommandSender commandSender,
                                         @Nonnull final Class<?> requiredSender,
                                         @Nonnull final List<CommandArgument<?, ?>> currentChain) {
        super(commandSender, currentChain);
        this.requiredSender = requiredSender;
    }

    /**
     * Get the required sender type
     *
     * @return Required sender type
     */
    @Nonnull
    public Class<?> getRequiredSender() {
        return this.requiredSender;
    }

    @Override
    public String getMessage() {
        return String.format("%s is not allowed to execute that command. Must be of type %s",
                             getCommandSender().toString(),
                             requiredSender.getSimpleName());
    }
}
