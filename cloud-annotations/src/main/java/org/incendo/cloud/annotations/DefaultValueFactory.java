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

import java.lang.reflect.Parameter;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.DefaultValue;

/**
 * Factory that produces default value instances.
 *
 * @param <C> command sender type
 * @param <T> value type
 */
@FunctionalInterface
@API(status = API.Status.STABLE)
public interface DefaultValueFactory<C, T> {

    /**
     * Creates a default value factory that always returns the given {@code defaultValue}.
     *
     * @param <C>          command sender type
     * @param <T>          type of the value
     * @param defaultValue default value instance
     * @return the created factory
     */
    static <C, T> @NonNull DefaultValueFactory<C, T> constant(final @NonNull DefaultValue<C, T> defaultValue) {
        return parameter -> defaultValue;
    }

    /**
     * Creates the default value.
     *
     * @param parameter parameter to create the default value for
     * @return the default value
     */
    @NonNull DefaultValue<C, T> create(@NonNull Parameter parameter);
}
