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

import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;

@API(status = API.Status.STABLE)
public class ArgumentParseException extends CommandParseException {

    private final Throwable cause;

    /**
     * Create a new command parse exception
     *
     * @param throwable     Exception that caused the parsing error
     * @param commandSender Command sender
     * @param currentChain  Chain leading up to the exception
     */
    @API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.*")
    public ArgumentParseException(
            final @NonNull Throwable throwable,
            final @NonNull Object commandSender,
            final @NonNull List<@NonNull CommandComponent<?>> currentChain
    ) {
        super(commandSender, currentChain);
        this.cause = throwable;
    }

    /**
     * Get the cause of the exception
     *
     * @return Cause
     */
    @Override
    public synchronized @NonNull Throwable getCause() {
        return this.cause;
    }
}
