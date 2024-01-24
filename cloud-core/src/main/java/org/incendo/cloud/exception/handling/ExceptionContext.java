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

import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;

@SuppressWarnings("unused")
@API(status = API.Status.STABLE)
public interface ExceptionContext<C, T extends Throwable> {

    /**
     * Returns the exception.
     *
     * @return the exception
     */
    @NonNull T exception();

    /**
     * Returns the command context.
     *
     * @return the command context
     */
    @NonNull CommandContext<C> context();

    /**
     * Returns the exception controller that created this context instance.
     *
     * @return the controller
     */
    @NonNull ExceptionController<C> controller();


    @API(status = API.Status.INTERNAL)
    final class ExceptionContextImpl<C, T extends Throwable> implements ExceptionContext<C, T> {

        private final T exception;
        private final CommandContext<C> context;
        private final ExceptionController<C> controller;

        ExceptionContextImpl(
                final @NonNull T exception,
                final @NonNull CommandContext<C> context,
                final @NonNull ExceptionController<C> controller
        ) {
            this.exception = exception;
            this.context = context;
            this.controller = controller;
        }

        @Override
        public @NonNull T exception() {
            return this.exception;
        }

        @Override
        public @NonNull CommandContext<C> context() {
            return this.context;
        }

        @Override
        public @NonNull ExceptionController<C> controller() {
            return this.controller;
        }

        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            final ExceptionContextImpl<?, ?> that = (ExceptionContextImpl<?, ?>) object;
            return Objects.equals(this.exception, that.exception)
                    && Objects.equals(this.context, that.context)
                    && Objects.equals(this.controller, that.controller);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.exception, this.context, this.controller);
        }
    }
}
