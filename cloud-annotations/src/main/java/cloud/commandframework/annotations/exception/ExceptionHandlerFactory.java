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
package cloud.commandframework.annotations.exception;

import cloud.commandframework.annotations.injection.ParameterInjectorRegistry;
import cloud.commandframework.exceptions.handling.ExceptionHandler;
import java.lang.reflect.Method;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(status = API.Status.STABLE, since = "2.0.0")
public interface ExceptionHandlerFactory<C> {

    /**
     * Returns a factory that produces {@link MethodExceptionHandler} instances.
     *
     * @param <C> the command sender type
     * @return the created factory
     */
    static <C> @NonNull ExceptionHandlerFactory<C> defaultFactory() {
        return MethodExceptionHandler::new;
    }

    /**
     * Creates an exception handler using the given {@code method}.
     *
     * @param instance         parsed instance
     * @param method           exception handler method
     * @param injectorRegistry injector registry
     * @return the method handler
     */
    @NonNull ExceptionHandler<C, Throwable> createExceptionHandler(
            @NonNull Object instance,
            @NonNull Method method,
            @NonNull ParameterInjectorRegistry<C> injectorRegistry
    );
}