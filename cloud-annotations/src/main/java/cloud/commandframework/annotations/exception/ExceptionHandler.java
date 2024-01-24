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

import cloud.commandframework.exception.handling.ExceptionContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Annotation used to map methods to {@link cloud.commandframework.exception.handling.ExceptionHandler exception handlers}.
 * <p>
 * The method signature is not fixed, and the parameters can be any combination of: <ul>
 *  <li>{@link cloud.commandframework.exception.handling.ExceptionContext}</li>
 *  <li>{@link #value()}</li>
 *  <li>{@link cloud.commandframework.context.CommandContext}</li>
 *  <li>the command sender type</li>
 *  <li>any type that can be injected using {@link cloud.commandframework.context.CommandContext#inject(Class)}</li>
 * </ul>
 * <p>
 * See {@link cloud.commandframework.exception.handling.ExceptionHandler#handle(ExceptionContext)} for information about
 * the exception handler behavior.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@API(status = API.Status.STABLE)
public @interface ExceptionHandler {

    /**
     * Returns the type of exceptions caught by the exception handler.
     *
     * @return the exception type
     */
    @NonNull Class<? extends Throwable> value();
}
