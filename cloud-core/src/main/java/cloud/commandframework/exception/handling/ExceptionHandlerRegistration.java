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
package cloud.commandframework.exception.handling;

import io.leangen.geantyref.TypeToken;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Used to register a {@link ExceptionHandler} in the {@link ExceptionController}.
 *
 * @param <C> the command sender type
 * @param <T> the exception type
 */
@SuppressWarnings("unused")
@API(status = API.Status.STABLE, since = "2.0.0")
public final class ExceptionHandlerRegistration<C, T extends Throwable> {

    /**
     * Returns a new registration.
     *
     * @param <C>              the command sender type
     * @param <T>              the exception type
     * @param exceptionType    the type handled by the exception handler
     * @param exceptionHandler the exception handler
     * @return the created registration
     */
    public static <C, T extends Throwable> @NonNull ExceptionHandlerRegistration<C, ? extends T> of(
            final @NonNull TypeToken<T> exceptionType,
            final @NonNull ExceptionHandler<C, ? extends T> exceptionHandler
    ) {
       return ExceptionHandlerRegistration.<C, T>builder(exceptionType).exceptionHandler(exceptionHandler).build();
    }

    /**
     * Returns a builder.
     * <p>
     * The builder is immutable, and each method results in a new builder instance.
     *
     * @param <C>           the command sender type
     * @param <T>           the exception type
     * @param exceptionType the type handled by the exception handler
     * @return the builder
     */
    public static <C, T extends Throwable> @NonNull ExceptionControllerBuilder<C, T> builder(
            final @NonNull TypeToken<T> exceptionType
    ) {
        return new ExceptionControllerBuilder<>(exceptionType);
    }

    private final TypeToken<T> exceptionType;
    private final ExceptionHandler<C, ? extends T> exceptionHandler;
    private final Predicate<T> exceptionFilter;

    private ExceptionHandlerRegistration(
            final @NonNull TypeToken<T> exceptionType,
            final @NonNull ExceptionHandler<C, ? extends T> exceptionHandler,
            final @NonNull Predicate<T> exceptionFilter
    ) {
        this.exceptionType = exceptionType;
        this.exceptionHandler = exceptionHandler;
        this.exceptionFilter = exceptionFilter;
    }

    /**
     * Returns the exception type handler by this handler.
     * <p>
     * More precise exception types will always get higher priority when selecting the exception handlers
     * for any given exception.
     *
     * @return the exception type
     */
    public @NonNull TypeToken<T> exceptionType() {
        return this.exceptionType;
    }

    /**
     * Returns the exception handler.
     *
     * @return the exception handler
     */
    public @NonNull ExceptionHandler<C, ? extends T> exceptionHandler() {
        return this.exceptionHandler;
    }

    /**
     * Returns the exception filter.
     * <p>
     * Exceptions should only be handled by the {@link #exceptionHandler()} if the predicate evaluates to {@code true}.
     *
     * @return the exception filter
     */
    public @NonNull Predicate<T> exceptionFilter() {
        return this.exceptionFilter;
    }


    @API(status = API.Status.STABLE)
    public static final class ExceptionControllerBuilder<C, T extends Throwable> {

        private final TypeToken<T> exceptionType;
        private final ExceptionHandler<C, ? extends T> exceptionHandler;
        private final Predicate<T> exceptionFilter;

        private ExceptionControllerBuilder(
                final @NonNull TypeToken<T> exceptionType,
                final @NonNull ExceptionHandler<C, ? extends T> exceptionHandler,
                final @NonNull Predicate<T> exceptionFilter
        ) {
            this.exceptionType = exceptionType;
            this.exceptionHandler = exceptionHandler;
            this.exceptionFilter = exceptionFilter;
        }

        private ExceptionControllerBuilder(
                final @NonNull TypeToken<T> exceptionType
        ) {
            this(exceptionType, ExceptionHandler.noopHandler(), exception -> true);
        }

        /**
         * Returns a new builder with the given {@code exceptionHandler}.
         *
         * @param exceptionHandler the new exception handler
         * @return new builder instance
         */
        public @NonNull ExceptionControllerBuilder<C, T> exceptionHandler(
                final @NonNull ExceptionHandler<C, ? extends T> exceptionHandler
        ) {
            return new ExceptionControllerBuilder<>(this.exceptionType, exceptionHandler, this.exceptionFilter);
        }

        /**
         * Returns a new builder with the given {@code exceptionFilter}.
         *
         * @param exceptionFilter the new filter, only exceptions that evaluate to {@code true} will be handled by the handler
         * @return new builder instance
         */
        public @NonNull ExceptionControllerBuilder<C, T> exceptionFilter(
                final @NonNull Predicate<T> exceptionFilter
        ) {
            return new ExceptionControllerBuilder<>(this.exceptionType, this.exceptionHandler, exceptionFilter);
        }

        /**
         * Builds a registration from this builder.
         *
         * @return the registration
         */
        public @NonNull ExceptionHandlerRegistration<C, ? extends T> build() {
            return new ExceptionHandlerRegistration<>(this.exceptionType, this.exceptionHandler, this.exceptionFilter);
        }
    }

    @FunctionalInterface
    @API(status = API.Status.STABLE)
    public interface BuilderDecorator<C, T extends Throwable> {

        /**
         * Decorates the given {@code builder} and returns the updated builder.
         *
         * @param builder the builder
         * @return the updated builder
         */
        @NonNull ExceptionControllerBuilder<C, T> decorate(@NonNull ExceptionControllerBuilder<C, T> builder);
    }
}
