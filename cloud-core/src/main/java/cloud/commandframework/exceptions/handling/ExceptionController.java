//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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
package cloud.commandframework.exceptions.handling;

import cloud.commandframework.context.CommandContext;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

/**
 * The controller handles registrations of exception handlers, as well as the routing of incoming exceptions to the handlers.
 *
 * @param <C> the command sender type
 * @since 2.0.0
 */
@SuppressWarnings("unused")
@API(status = API.Status.STABLE, since = "2.0.0")
public final class ExceptionController<C> {

    private final ExceptionContextFactory<C> exceptionContextFactory = new ExceptionContextFactory<>(this);
    private final Map<@NonNull Type, @NonNull LinkedList<@NonNull ExceptionHandlerRegistration<C, ?>>> registrations;

    /**
     * Unwraps a {@link CompletionException} recursively until a cause is encountered that is not a completion exception.
     *
     * @param throwable the throwable
     * @return the original cause
     */
    public static @NonNull Throwable unwrapCompletionException(final @NonNull Throwable throwable) {
        if (throwable instanceof CompletionException) {
            return unwrapCompletionException(throwable.getCause());
        }
        return throwable;
    }

    /**
     * Creates a new exception controller.
     */
    public ExceptionController() {
        this.registrations = new HashMap<>();
    }

    /**
     * Attempts to handle the given {@code exception} gracefully.
     * <p>
     * The controller will attempt to find exception handlers for the exception type, and any of its supertypes.
     * Only one exception handler will get to fully handle the exception.
     * <p>
     * If no exception handler was able to handle the exception, the exception will be re-thrown.
     * The thrown exception might be different from the initial exception in the case that
     * any of the exception handlers throws a new exception.
     *
     * @param <T>            the exception type
     * @param commandContext the command context
     * @param exception      the exception
     * @throws Throwable any exception left unhandled is re-thrown and should be handled by the caller
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends Throwable> void handleException(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull T exception
    ) throws Throwable {
        final ExceptionContext<C, T> exceptionContext = this.exceptionContextFactory.createContext(commandContext, exception);

        Class<?> exceptionClass = exception.getClass();
        while (exceptionClass != Object.class) {
            final List<ExceptionHandlerRegistration<C, ?>> registrations = this.registrations(exceptionClass);
            for (final ExceptionHandlerRegistration<C, ?> registration : registrations) {
                if (!((Predicate) registration.exceptionFilter()).test(exception)) {
                    continue;
                }

                try {
                    ((ExceptionHandlerRegistration) registration).exceptionHandler().handle(exceptionContext);
                } catch (final Throwable throwable) {
                    if (throwable.equals(exception)) {
                        continue;
                    }
                    // We try to handle the new exception instead.
                    this.handleException(commandContext, throwable);
                }
                return;
            }
            exceptionClass = exceptionClass.getSuperclass();
        }

        // If nothing was able to handle the exception, then we re-throw.
        throw exception;
    }

    /**
     * Registers the given {@code registration}.
     * <p>
     * The ordering matters when multiple handlers are registered for the same exception type.
     * The last registered handler will get priority.
     *
     * @param <T>          the exception type handled by the exception handler
     * @param registration the exception handler registration
     * @return {@code this} exception controller
     */
    public synchronized <T extends Throwable> @NonNull @This ExceptionController<C> register(
            final @NonNull ExceptionHandlerRegistration<C, ? extends T> registration
    ) {
        this.registrations.computeIfAbsent(registration.exceptionType().getType(), t -> new LinkedList<>())
                .addFirst(registration);
        return this;
    }

    /**
     * Decorates a registration builder and registers the result.
     * <p>
     * The ordering matters when multiple handlers are registered for the same exception type.
     * The last registered handler will get priority.
     *
     * @param <T>            the exception type handled by the exception handler
     * @param exceptionType  the exception type handled by the exception handler
     * @param decorator     the builder decorator
     * @return {@code this} exception controller
     */
    public <T extends Throwable> @NonNull @This ExceptionController<C> register(
            final @NonNull TypeToken<T> exceptionType,
            final ExceptionHandlerRegistration.@NonNull BuilderDecorator<C, T> decorator
    ) {
        return this.register(decorator.decorate(ExceptionHandlerRegistration.builder(exceptionType)).build());
    }

    /**
     * Decorates a registration builder and registers the result.
     * <p>
     * The ordering matters when multiple handlers are registered for the same exception type.
     * The last registered handler will get priority.
     *
     * @param <T>            the exception type handled by the exception handler
     * @param exceptionType  the exception type handled by the exception handler
     * @param decorator     the builder decorator
     * @return {@code this} exception controller
     */
    public <T extends Throwable> @NonNull @This ExceptionController<C> register(
            final @NonNull Class<T> exceptionType,
            final ExceptionHandlerRegistration.@NonNull BuilderDecorator<C, T> decorator
    ) {
        return this.register(decorator.decorate(ExceptionHandlerRegistration.builder(TypeToken.get(exceptionType))).build());
    }

    /**
     * Registers the given {@code exceptionHandler}.
     * <p>
     * The ordering matters when multiple handlers are registered for the same exception type.
     * The last registered handler will get priority.
     *
     * @param <T>              the exception type handled by the exception handler
     * @param exceptionType    the exception type handled by the exception handler
     * @param exceptionHandler the exception handler
     * @return {@code this} exception controller
     */
    public <T extends Throwable> @NonNull @This ExceptionController<C> registerHandler(
            final @NonNull TypeToken<T> exceptionType,
            final @NonNull ExceptionHandler<C, ? extends T> exceptionHandler
    ) {
        return this.register(ExceptionHandlerRegistration.of(exceptionType, exceptionHandler));
    }

    /**
     * Registers the given {@code exceptionHandler}.
     * <p>
     * The ordering matters when multiple handlers are registered for the same exception type.
     * The last registered handler will get priority.
     *
     * @param <T>              the exception type handled by the exception handler
     * @param exceptionType    the exception type handled by the exception handler
     * @param exceptionHandler the exception handler
     * @return {@code this} exception controller
     */
    public <T extends Throwable> @NonNull @This ExceptionController<C> registerHandler(
            final @NonNull Class<T> exceptionType,
            final @NonNull ExceptionHandler<C, ? extends T> exceptionHandler
    ) {
        return this.register(ExceptionHandlerRegistration.of(TypeToken.get(exceptionType), exceptionHandler));
    }

    /**
     * Removes all registered handlers.
     * <p>
     * This can be used to make sure that no default handlers of higher precision are invoked before your handler.
     * <p>
     * It is recommended that you register a handler for {@link Throwable} if you use this, to make sure that no uncaught
     * errors leak out of the controller.
     */
    public void clearHandlers() {
        this.registrations.clear();
    }

    private @NonNull List<@NonNull ExceptionHandlerRegistration<C, ?>> registrations(final @NonNull Type type) {
        return Collections.unmodifiableList(this.registrations.getOrDefault(type, new LinkedList<>()));
    }
}
