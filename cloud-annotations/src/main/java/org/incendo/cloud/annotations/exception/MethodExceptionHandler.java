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
package org.incendo.cloud.annotations.exception;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.annotations.method.AnnotatedMethodHandler;
import org.incendo.cloud.annotations.method.ParameterValue;
import org.incendo.cloud.exception.handling.ExceptionContext;
import org.incendo.cloud.exception.handling.ExceptionHandler;
import org.incendo.cloud.injection.ParameterInjectorRegistry;

/**
 * Represents a method annotated with {@link org.incendo.cloud.annotations.exception.ExceptionHandler}.
 *
 * @param <C> the command sender type
 */
@API(status = API.Status.STABLE)
public final class MethodExceptionHandler<C> extends AnnotatedMethodHandler<C> implements ExceptionHandler<C, Throwable> {

    /**
     * Creates a new handler.
     *
     * @param instance         parsed instance
     * @param method           method
     * @param injectorRegistry injector registry
     */
    public MethodExceptionHandler(
            final @NonNull Object instance,
            final @NonNull Method method,
            final @NonNull ParameterInjectorRegistry<C> injectorRegistry
    ) {
        super(method, instance, injectorRegistry);
    }

    @Override
    public void handle(final @NonNull ExceptionContext<C, Throwable> context) throws Throwable {
        final List<Object> arguments = this.createParameterValues(
                context.context(),
                this.parameters(),
                Arrays.asList(context, context.exception())
        ).stream().map(ParameterValue::value).collect(Collectors.toList());
        this.methodHandle().invokeWithArguments(arguments);
    }
}
