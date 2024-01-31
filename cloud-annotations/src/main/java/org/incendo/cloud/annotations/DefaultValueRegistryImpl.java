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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

final class DefaultValueRegistryImpl<C> implements DefaultValueRegistry<C> {

    private final Map<String, DefaultValueFactory<C, ?>> factories = new HashMap<>();

    @Override
    public @This @NonNull <T> DefaultValueRegistry<C> register(
            final @NonNull String name,
            final @NonNull DefaultValueFactory<C, T> defaultValue
    ) {
        this.factories.put(Objects.requireNonNull(name, "name"), Objects.requireNonNull(defaultValue, "defaultValue"));
        return this;
    }

    @Override
    public @NonNull Optional<@NonNull DefaultValueFactory<C, ?>> named(final @NonNull String name) {
        return Optional.ofNullable(this.factories.get(Objects.requireNonNull(name, "name")));
    }
}
