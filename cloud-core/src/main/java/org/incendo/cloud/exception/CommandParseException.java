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
package org.incendo.cloud.exception;

import java.util.Collections;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;

/**
 * Exception thrown when parsing user input into a command
 */
@SuppressWarnings({"unused", "serial"})
@API(status = API.Status.STABLE)
public class CommandParseException extends IllegalArgumentException {

    private final Object commandSender;
    private final List<CommandComponent<?>> currentChain;

    /**
     * Construct a new command parse exception
     *
     * @param commandSender Sender who executed the command
     * @param currentChain  Chain leading up to the exception
     */
    @API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.*")
    protected CommandParseException(
            final @NonNull Object commandSender,
            final @NonNull List<CommandComponent<?>> currentChain
    ) {
        this.commandSender = commandSender;
        this.currentChain = currentChain;
    }

    /**
     * Returns the command sender.
     *
     * @return command sender
     */
    public @NonNull Object commandSender() {
        return this.commandSender;
    }

    /**
     * Returns the command chain leading up to the exception.
     *
     * @return unmodifiable list of command arguments
     */
    public @NonNull List<@NonNull CommandComponent<?>> currentChain() {
        return Collections.unmodifiableList(this.currentChain);
    }
}
