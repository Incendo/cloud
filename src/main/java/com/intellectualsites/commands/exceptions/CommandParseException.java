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
package com.intellectualsites.commands.exceptions;

import com.intellectualsites.commands.components.CommandComponent;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when parsing user input into a command
 */
public class CommandParseException extends IllegalArgumentException {

    private final CommandSender commandSender;
    private final List<CommandComponent<?>> currentChain;

    /**
     * Construct a new command parse exception
     *
     * @param commandSender Sender who executed the command
     * @param currentChain  Chain leading up to the exception
     */
    protected CommandParseException(@Nonnull final CommandSender commandSender, @Nonnull final List<CommandComponent<?>> currentChain) {
        this.commandSender = commandSender;
        this.currentChain = currentChain;
    }

    /**
     * Get the command sender
     *
     * @return Command sender
     */
    @Nonnull
    public CommandSender getCommandSender() {
        return this.commandSender;
    }

    /**
     * Get the command chain leading up to the exception
     *
     * @return Unmodifiable list of command components
     */
    @Nonnull
    public List<CommandComponent<?>> getCurrentChain() {
        return Collections.unmodifiableList(this.currentChain);
    }

}
