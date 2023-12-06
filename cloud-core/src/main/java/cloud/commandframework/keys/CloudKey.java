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
package cloud.commandframework.keys;

import cloud.commandframework.internal.ImmutableImpl;
import io.leangen.geantyref.TypeToken;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;

/**
 * A typed, named key.
 *
 * @param <T> The type of the key
 * @since 1.4.0
 */
@ImmutableImpl
@Value.Immutable
@API(status = API.Status.STABLE, since = "1.4.0")
public abstract class CloudKey<T> {

    /**
     * Creates a new key.
     *
     * @param <T>  the generic type of the value represented by the key
     * @param name the name of the key
     * @param type the type of the value represented by the key
     * @return the created key
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <@NonNull T> CloudKey<T> of(
            final @NonNull String name,
            final @NonNull TypeToken<T> type
    ) {
        return CloudKeyImpl.of(name, type);
    }

    /**
     * Creates a new key.
     *
     * @param <T>  the generic type of the value represented by the key
     * @param name the name of the key
     * @param type the type of the value represented by the key
     * @return the created key
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <@NonNull T> CloudKey<T> of(
            final @NonNull String name,
            final @NonNull Class<T> type
    ) {
        return CloudKeyImpl.of(name, TypeToken.get(type));
    }

    /**
     * Creates a new type-less key.
     *
     * @param name the name of the key
     * @return the created key
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static @NonNull CloudKey<Void> of(final @NonNull String name) {
        return CloudKeyImpl.of(name, TypeToken.get(Void.TYPE));
    }

    /**
     * Returns the name of the key.
     * <p>
     * The name of the key should be used to
     * determine key equality. That means that two keys sharing the same
     * name are considered equal.
     *
     * @return the name of the key
     */
    public abstract @NonNull String name();

    /**
     * Returns the type of the value that this key holds.
     *
     * @return the type of the key value.
     */
    public abstract @NonNull TypeToken<@NonNull T> type();

    @Override
    public final boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        final CloudKey<?> that = (CloudKey<?>) other;
        return Objects.equals(this.name(), that.name());
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(this.name());
    }
}
