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
package org.incendo.cloud.parser.aggregate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.key.CloudKey;

@SuppressWarnings("unchecked")
final class AggregateParsingContextImpl<C> implements AggregateParsingContext<C> {

    private final Map<CloudKey<?>, Object> storage = new HashMap<>();
    private final Collection<@NonNull String> validKeys;

    AggregateParsingContextImpl(
            final @NonNull AggregateParser<C, ?> parser
    ) {
        this.validKeys = parser.components()
                .stream()
                .map(CommandComponent::name)
                .collect(Collectors.toList());
    }

    @Override
    public <V> void store(final @NonNull CloudKey<V> key, final @NonNull V value) {
        this.storage.put(key, value);
    }

    @Override
    public <V> void store(final @NonNull String key, final @NonNull V value) {
        this.storage.put(CloudKey.of(key), value);
    }

    @Override
    public void remove(final @NonNull CloudKey<?> key) {
        this.storage.remove(key);
    }

    @Override
    public <V> V computeIfAbsent(
            final @NonNull CloudKey<V> key,
            final @NonNull Function<@NonNull CloudKey<V>, V> defaultFunction
    ) {
        return (V) this.storage.computeIfAbsent(key, k -> defaultFunction.apply((CloudKey<V>) k));
    }

    @Override
    public @NonNull <V> Optional<V> optional(final @NonNull CloudKey<V> key) {
        final Object value = this.storage.get(key);
        if (value != null) {
            @SuppressWarnings("unchecked") final V castedValue = (V) value;
            return Optional.of(castedValue);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public @NonNull <V> Optional<V> optional(final @NonNull String key) {
        final Object value = this.storage.get(CloudKey.of(key));
        if (value != null) {
            @SuppressWarnings("unchecked") final V castedValue = (V) value;
            return Optional.of(castedValue);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public <V> @NonNull V get(final @NonNull CloudKey<V> key) {
        if (!this.validKeys.contains(key.name())) {
            throw new NullPointerException("No value with the given key has been stored in the context");
        }
        final Object value = Objects.requireNonNull(this.storage.get(key));
        return (V) value;
    }

    @Override
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public <V> @NonNull V get(final @NonNull String key) {
        if (!this.validKeys.contains(key)) {
            throw new NullPointerException("No value with the given key has been stored in the context");
        }
        final Object value = Objects.requireNonNull(this.storage.get(CloudKey.of(key)));
        return (V) value;
    }

    @Override
    public boolean contains(final @NonNull CloudKey<?> key) {
        return this.storage.containsKey(key);
    }

    @Override
    public boolean contains(final @NonNull String key) {
        return this.storage.containsKey(CloudKey.of(key));
    }

    @Override
    public @NonNull Map<CloudKey<?>, ? extends @NonNull Object> all() {
        return this.storage;
    }
}
