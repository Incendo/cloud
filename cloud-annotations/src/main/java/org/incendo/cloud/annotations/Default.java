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
package org.incendo.cloud.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * <b>When used on a method parameter:</b><br/>
 * Used to give an optional {@link Argument} a {@link org.incendo.cloud.component.DefaultValue}.
 *
 * <p>If {@link #value()} is used then a parsed default value will be created. If {@link #name()} is used then a dynamic
 * default value registered to the {@link DefaultValueRegistry} will be used.</p>
 *
 * <hr/>
 * <b>When used on a method:</b><br>
 * Used to indicate that a method is a {@link DefaultValueFactory}. The method must return a
 * {@link org.incendo.cloud.component.DefaultValue} instance. The <i>only</i> accepted method parameter is a
 * {@link java.lang.reflect.Parameter} which represents the parameter that the default value is being created for.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
@API(status = API.Status.STABLE)
public @interface Default {

    /**
     * Returns the default value. {@link #name()} will take priority when non-empty and used on a parameter.
     *
     * <p>This value will be parsed when the command is being parsed in the case that the optional parameter has been omitted.</p>
     *
     * @return the default value
     */
    @NonNull String value() default "";

    /**
     * If used on a parameter this returns the name of the {@link DefaultValueFactory} used to create the default value.
     *
     * <p>If used on a method, this indicates the name of the {@link DefaultValueFactory} represented by the method. If this
     * is empty, the method name will be used.</p>
     *
     * <p>The factories must be registered to the {@link DefaultValueRegistry} retrieved
     * through {@link AnnotationParser#defaultValueRegistry()}. Using {@link AnnotationParser#parse(Object[])} will scan &amp;
     * register all {@code @Default}-annotated methods.</p>
     *
     * @return name of the default value registry to use
     */
    @NonNull String name() default "";
}
