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
package cloud.commandframework.annotations.exception;

import cloud.commandframework.exceptions.handling.ExceptionContext;
import cloud.commandframework.exceptions.handling.ExceptionHandler;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Represents a method annotated with {@link cloud.commandframework.annotations.exception.ExceptionHandler}.
 *
 * @param <C> the command sender type
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public final class MethodExceptionHandler<C> implements ExceptionHandler<C, Throwable> {

    private final Class<?>[] parameters;
    private final MethodHandle methodHandle;

    /**
     * Creates a new handler.
     *
     * @param instance the parsed instance
     * @param method   the method
     */
    public MethodExceptionHandler(
            final @NonNull Object instance,
            final @NonNull Method method
    ) {
        this.parameters = method.getParameterTypes();
        try {
            this.methodHandle = MethodHandles.lookup().unreflect(method).bindTo(instance);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(final @NonNull ExceptionContext<C, Throwable> context) throws Throwable {
        final List<Object> arguments = new ArrayList<>(this.parameters.length);
        for (final Class<?> argument : this.parameters) {
            if (argument.isInstance(context)) {
                arguments.add(context);
            } else if (argument.isInstance(context.exception())) {
                arguments.add(context.exception());
            } else if (argument.isInstance(context.context())) {
                arguments.add(context.context());
            } else if (argument.isInstance(context.context().getSender())) {
                arguments.add(context.context().getSender());
            } else {
                arguments.add(context.context().inject(argument).orElseThrow(() ->
                        new IllegalArgumentException("Can't map argument of type " + argument.getName())));
            }
        }
        this.methodHandle.invokeWithArguments(arguments);
    }
}
