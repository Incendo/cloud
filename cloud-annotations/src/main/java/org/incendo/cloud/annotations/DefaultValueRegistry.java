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

import java.util.Optional;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

/**
 * Registry that stores mappings between names and {@link DefaultValueFactory default value factories}.
 *
 * @param <C> command sender type
 */
@API(status = API.Status.STABLE)
public interface DefaultValueRegistry<C> {

    /**
     * Registers the named default value.
     *
     * @param <T>          default value type
     * @param name         name of the default value
     * @param defaultValue default value
     * @return {@code this}
     */
    <T> @This @NonNull DefaultValueRegistry<C> register(@NonNull String name, @NonNull DefaultValueFactory<C, T> defaultValue);

    /**
     * Returns the default value with the given {@code name}, if it exists.
     *
     * @param name name of the default value
     * @return optional that contains the default value, if it has been {@link #register(String, DefaultValueFactory) registered}
     */
    @NonNull Optional<@NonNull DefaultValueFactory<C, ?>> named(@NonNull String name);
}
