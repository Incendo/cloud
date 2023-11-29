//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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
package cloud.commandframework.arguments.aggregate;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.keys.SimpleCloudKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("unchecked")
final class AggregateCommandContextImpl<C> implements AggregateCommandContext<C> {

    private final Map<CloudKey<?>, Object> storage = new HashMap<>();
    private final Collection<@NonNull String> validKeys;

    AggregateCommandContextImpl(
            final @NonNull AggregateCommandParser<C, ?> parser
    ) {
        this.validKeys = parser.components()
                .stream()
                .map(CommandComponent::name)
                .collect(Collectors.toList());
    }

    @Override
    public void store(final @NonNull CloudKey<?> key, final @NonNull Object value) {
        this.storage.put(key, value);
    }

    @Override
    public <T> @NonNull T get(@NonNull final CloudKey<T> key) {
        if (!this.validKeys.contains(key.getName())) {
            throw new NullPointerException("No value with the given key has been stored in the context");
        }
        final Object value = Objects.requireNonNull(this.storage.get(key));
        return (T) value;
    }

    @Override
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public <T> @NonNull T get(@NonNull final String key) {
        if (!this.validKeys.contains(key)) {
            throw new NullPointerException("No value with the given key has been stored in the context");
        }
        final Object value = Objects.requireNonNull(this.storage.get(SimpleCloudKey.of(key)));
        return (T) value;
    }

    @Override
    public boolean contains(@NonNull final CloudKey<?> key) {
        return this.storage.containsKey(key);
    }

    @Override
    public boolean contains(@NonNull final String key) {
        return this.storage.containsKey(SimpleCloudKey.of(key));
    }
}
