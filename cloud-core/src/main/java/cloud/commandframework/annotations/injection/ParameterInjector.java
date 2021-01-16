//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
package cloud.commandframework.annotations.injection;

import cloud.commandframework.annotations.AnnotationAccessor;
import cloud.commandframework.context.CommandContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Injector that injects parameters into CommandMethod annotated
 * methods
 *
 * @param <C> Command sender type
 * @param <T> Type of the value that is injected by this injector
 * @since 1.2.0
 */
@FunctionalInterface
public interface ParameterInjector<C, T> {

    /**
     * Attempt to create a a value that should then be injected into the CommandMethod
     * annotated method. If the injector cannot (or shouldn't) create a value, it is free to return {@code null}.
     *
     * @param context            Command context that is requesting the injection
     * @param annotationAccessor Annotation accessor proxying the method and parameter which the value is being injected into
     * @return The value, if it could be created. Else {@code null}, in which case no value will be injected
     *         by this particular injector
     */
    @Nullable T create(@NonNull CommandContext<C> context, @NonNull AnnotationAccessor annotationAccessor);

}
