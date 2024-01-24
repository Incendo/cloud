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

import java.util.function.Consumer;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Handles an exception thrown during command parsing &amp; execution.
 *
 * @param <C> the command sender type
 * @param <T> the exception type
 */
@SuppressWarnings("unused")
@FunctionalInterface
@API(status = API.Status.STABLE, since = "2.0.0")
public interface ExceptionHandler<C, T extends Throwable> {

    /**
     * Returns an exception handler that does nothing.
     *
     * @param <C> the command sender type
     * @param <T> the exception type
     * @return the exception handler
     */
    static <C, T extends Throwable> @NonNull ExceptionHandler<C, T> noopHandler() {
        return ctx -> {};
    }

    /**
     * Returns an exception handler that re-throws the {@link ExceptionContext#exception()}.
     * <p>
     * This will allow other exception handlers to handle the exception.
     *
     * @param <C> the command sender type
     * @param <T> the exception type
     * @return the exception handler
     */
    static <C, T extends Throwable> @NonNull ExceptionHandler<C, T> passThroughHandler() {
        return ctx -> {
            throw ctx.exception();
        };
    }

    /**
     * Returns an exception handler that re-throws the {@link ExceptionContext#exception()} after
     * invoking the given {@code consumer}.
     *
     * @param <C>      the command sender type
     * @param <T>      the exception type
     * @param consumer the consumer
     * @return the exception handler
     */
    static <C, T extends Throwable> @NonNull ExceptionHandler<C, T> passThroughHandler(
            final @NonNull Consumer<ExceptionContext<C, T>> consumer
    ) {
        return ctx -> {
            consumer.accept(ctx);
            throw ctx.exception();
        };
    }

    /**
     * Returns an exception handler that throws the cause of the {@link ExceptionContext#exception()} if it's
     * not {@code null} and the {@code predicate} evaluates to {@code true}.
     * Otherwise, it will re-throw the {@link ExceptionContext#exception()}.
     *
     * @param <C>       the command sender type
     * @param <T>       the exception type
     * @param predicate predicate that tests the cause of the exception
     * @return the exception handler
     */
    static <C, T extends Throwable> @NonNull ExceptionHandler<C, T> unwrappingHandler(
            final @NonNull Predicate<Throwable> predicate
    ) {
        return ctx -> {
            final Throwable cause = ctx.exception().getCause();
            if (cause != null && predicate.test(cause)) {
                throw cause;
            }
            throw ctx.exception();
        };
    }

    /**
     * Returns an exception handler that throws the cause of the {@link ExceptionContext#exception()} if it's and instance
     * of {@code causeClass}.
     * Otherwise, it will re-throw the {@link ExceptionContext#exception()}.
     *
     * @param <C>        the command sender type
     * @param <T>        the exception type
     * @param causeClass the type of the cause
     * @return the exception handler
     */
    static <C, T extends Throwable> @NonNull ExceptionHandler<C, T> unwrappingHandler(
            final @NonNull Class<? extends Throwable> causeClass
    ) {
        return unwrappingHandler(causeClass::isInstance);
    }

    /**
     * Returns an exception handler that throws the cause of the {@link ExceptionContext#exception()} if it's
     * not {@code null}.
     * Otherwise, it will re-throw the {@link ExceptionContext#exception()}.
     *
     * @param <C> the command sender type
     * @param <T> the exception type
     * @return the exception handler
     */
    static <C, T extends Throwable> @NonNull ExceptionHandler<C, T> unwrappingHandler() {
        return unwrappingHandler(throwable -> true);
    }

    /**
     * Handles the exception in the given {@code context}.
     * <p>
     * Any exception thrown by the handler will be handled by the {@link ExceptionController}.
     * <p>
     * If the {@link ExceptionContext#exception()} is re-thrown, then the next exception handler in
     * line will get to handle the exception instead.
     *
     * @param context the exception context
     * @throws Exception any exception thrown by the handler
     */
    void handle(@NonNull ExceptionContext<C, T> context) throws Throwable;
}
