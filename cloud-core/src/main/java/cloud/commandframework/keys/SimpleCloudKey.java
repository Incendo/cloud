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
package cloud.commandframework.keys;

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * Simple immutable implementation of {@link CloudKey}. Key equality is
 * determined solely by the key name. Two keys with matching names will
 * be equal, no matter if their type tokens are identical.
 *
 * @param <T> Key type
 * @since 1.4.0
 */
public final class SimpleCloudKey<@NonNull T> implements CloudKey<T> {

    private final String name;
    private final TypeToken<T> type;

    private SimpleCloudKey(
            final @NonNull String name,
            final @NonNull TypeToken<T> type
    ) {
        this.name = name;
        this.type = type;
    }

    /**
     * Create a new simple cloud key
     *
     * @param name The name of the key
     * @param type The type of the value represented by the key
     * @param <T>  The generic type of the value represented by the key
     * @return The created key instance
     */
    public static <@NonNull T> CloudKey<T> of(
            final @NonNull String name,
            final @NonNull TypeToken<T> type
    ) {
        return new SimpleCloudKey<>(name, type);
    }

    /**
     * Create a new type-less simple cloud key
     *
     * @param name The name of the key
     * @return The created key instance
     */
    public static @NonNull CloudKey<Void> of(final @NonNull String name) {
        return new SimpleCloudKey<>(name, TypeToken.get(Void.TYPE));
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

    @Override
    public @NonNull TypeToken<T> getType() {
        return this.type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SimpleCloudKey<?> key = (SimpleCloudKey<?>) o;
        return name.equals(key.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return this.name;
    }

}
