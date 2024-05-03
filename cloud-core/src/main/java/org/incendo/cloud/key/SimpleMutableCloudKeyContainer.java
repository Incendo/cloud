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
package org.incendo.cloud.key;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = API.Status.INTERNAL)
public final class SimpleMutableCloudKeyContainer implements MutableCloudKeyContainer {

    private final Map<CloudKey<?>, Object> map;

    /**
     * Creates a new {@link SimpleMutableCloudKeyContainer}.
     *
     * @param map backing map
     */
    public SimpleMutableCloudKeyContainer(final Map<CloudKey<?>, Object> map) {
        this.map = map;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull <V> Optional<V> optional(final @NonNull CloudKey<V> key) {
        return Optional.ofNullable((V) this.map.get(key));
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull <V> Optional<V> optional(final @NonNull String key) {
        return (Optional<V>) this.optional(CloudKey.of(key));
    }

    @Override
    public boolean contains(final @NonNull CloudKey<?> key) {
        return this.map.containsKey(key);
    }

    @Override
    public @NonNull Map<CloudKey<?>, ? extends @NonNull Object> all() {
        return Collections.unmodifiableMap(this.map);
    }

    @Override
    public <V> void store(final @NonNull CloudKey<V> key, final @NonNull V value) {
        this.map.put(key, value);
    }

    @Override
    public <V> void store(final @NonNull String key, final @NonNull V value) {
        this.map.put(CloudKey.of(key), value);
    }

    @Override
    public void remove(final @NonNull CloudKey<?> key) {
        this.map.remove(key);
    }

    @SuppressWarnings({"CodeBlock2Expr", "unchecked"})
    @Override
    public <V> V computeIfAbsent(
            final @NonNull CloudKey<V> key,
            final @NonNull Function<@NonNull CloudKey<V>, V> defaultFunction
    ) {
        return (V) this.map.computeIfAbsent(key, $ -> {
            return defaultFunction.apply(key);
        });
    }

    /**
     * Get the value, or null if not present, without boxing in an optional.
     *
     * @param key key
     * @param <V> value type
     * @return value or null
     */
    @SuppressWarnings("unchecked")
    public <V> @Nullable V getOrNull(final CloudKey<V> key) {
        return (V) this.map.get(key);
    }
}
