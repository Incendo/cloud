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
package org.incendo.cloud.exception.handling;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;

@API(status = API.Status.INTERNAL)
public final class ExceptionContextFactory<C> {

    private final ExceptionController<C> controller;

    /**
     * Creates a new factory.
     *
     * @param controller the controller
     */
    public ExceptionContextFactory(final @NonNull ExceptionController<C> controller) {
        this.controller = controller;
    }

    /**
     * Creates a new exception context.
     *
     * @param <T>       the exception type
     * @param context   the command context
     * @param exception the exception
     * @return the created context
     */
    public <T extends Throwable> @NonNull ExceptionContext<C, T> createContext(
            final @NonNull CommandContext<C> context,
            final @NonNull T exception
    ) {
        return new ExceptionContext.ExceptionContextImpl<>(exception, context, this.controller);
    }
}
