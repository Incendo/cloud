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

import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when parsing user input into a command
 */
@SuppressWarnings("unused")
public class CommandParseException extends IllegalArgumentException {

    private static final long serialVersionUID = -2415981126382517435L;
    private final Object commandSender;
    private final List<CommandArgument<?, ?>> currentChain;

    /**
     * Construct a new command parse exception
     *
     * @param commandSender Sender who executed the command
     * @param currentChain  Chain leading up to the exception
     */
    protected CommandParseException(
            final @NonNull Object commandSender,
            final @NonNull List<CommandArgument<?, ?>> currentChain
    ) {
        this.commandSender = commandSender;
        this.currentChain = currentChain;
    }

    /**
     * Get the command sender
     *
     * @return Command sender
     */
    public @NonNull Object getCommandSender() {
        return this.commandSender;
    }

    /**
     * Get the command chain leading up to the exception
     *
     * @return Unmodifiable list of command arguments
     */
    public @NonNull List<@NonNull CommandArgument<?, ?>> getCurrentChain() {
        return Collections.unmodifiableList(this.currentChain);
    }

}
