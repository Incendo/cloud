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
package cloud.commandframework.injection;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.util.annotation.AnnotationAccessor;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Injector that injects parameters into Command annotated
 * methods
 *
 * @param <C> command sender type
 * @param <T> Type of the value that is injected by this injector
 */
@FunctionalInterface
@API(status = API.Status.STABLE, since = "1.2.0")
public interface ParameterInjector<C, T> {

    /**
     * Returns a parameter injector that always injects {@code value}.
     *
     * @param <C>   command sender type
     * @param <T>   type of the value
     * @param value value to inject
     * @return the injector
     */
    @API(status = API.Status.STABLE)
    static <C, T> @NonNull ParameterInjector<C, T> constantInjector(final @NonNull T value) {
        return new ConstantInjector<>(value);
    }

    /**
     * Attempts to create a value that should then be injected into the Command
     * annotated method.
     *
     * <p>If the injector cannot (or shouldn't) create a value, it is free to return {@code null}.</p>
     *
     * @param context            command context that is requesting the injection
     * @param annotationAccessor annotation accessor proxying the method and parameter which the value is being injected into
     * @return the value if it could be created, else {@code null} in which case no value will be injected
     *         by this particular injector
     */
    @Nullable T create(@NonNull CommandContext<C> context, @NonNull AnnotationAccessor annotationAccessor);


    final class ConstantInjector<C, T> implements ParameterInjector<C, T> {

        private final T value;

        private ConstantInjector(final @NonNull T value) {
            this.value = value;
        }

        @Override
        public @NonNull T create(
                final @NonNull CommandContext<C> context,
                final @NonNull AnnotationAccessor annotationAccessor
        ) {
            return this.value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final ConstantInjector<?, ?> that = (ConstantInjector<?, ?>) o;
            return Objects.equals(this.value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.value);
        }

        @Override
        public String toString() {
            return "ConstantInjector{value=" + this.value + '}';
        }
    }
}
