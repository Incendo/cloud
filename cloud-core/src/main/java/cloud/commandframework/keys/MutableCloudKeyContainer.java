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

import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0")
public interface MutableCloudKeyContainer extends CloudKeyContainer {

    /**
     * Stores the {@code key}-{@code value} pair.
     * <p>
     * This will overwrite any existing value stored with the same {@code key}.
     *
     * @param <V>   the type of the value
     * @param key   the key
     * @param value the value
     */
     <V extends @NonNull Object> void store(@NonNull CloudKey<V> key, V value);

    /**
     * Stores the {@code key}-{@code value} pair.
     * <p>
     * This will overwrite any existing value stored with the same {@code key}.
     *
     * @param <V>   the type of the value
     * @param key   the key
     * @param value the value
     */
    <V extends @NonNull Object> void store(@NonNull String key, V value);

    /**
     * Stores the key-value pair for the key held by the {@code keyHolder}.
     * <p>
     * This will overwrite any existing value stored with the same key.
     *
     * @param <V>       the type of the value
     * @param keyHolder the holder of the key
     * @param value     the value
     */
    default <V extends @NonNull Object> void store(@NonNull CloudKeyHolder<V> keyHolder, V value) {
        this.store(keyHolder.key(), value);
    }

    /**
     * Removes value associated with the given {@code key}.
     *
     * @param key the key
     */
    void remove(@NonNull CloudKey<?> key);

    /**
     * Removes value associated with the given {@code key}.
     *
     * @param key the key
     */
    default void remove(final @NonNull String key) {
        this.remove(CloudKey.of(key));
    }

    /**
     * Removes value associated with the given {@code keyHolder}.
     *
     * @param keyHolder the holder of the key
     */
    default void remove(final @NonNull CloudKeyHolder<?> keyHolder) {
        this.remove(keyHolder.key());
    }

    /**
     * Sets the {@code value} associated with the given {@code key}.
     * <p>
     * If the {@code value} is {@code null} the {@code key} will be {@link #remove(CloudKey) removed}.
     *
     * @param <V>   the type of the value
     * @param key   the key
     * @param value the value
     */
    default <V> void set(final @NonNull CloudKey<V> key, final @Nullable V value) {
        if (value == null) {
            this.remove(key);
        } else {
            this.store(key, value);
        }
    }

    /**
     * Sets the {@code value} associated with the given {@code key}.
     * <p>
     * If the {@code value} is {@code null} the {@code key} will be {@link #remove(String) removed}.
     *
     * @param <V>   the type of the value
     * @param key   the key
     * @param value the value
     */
    default <V> void set(final @NonNull String key, final @Nullable V value) {
        if (value == null) {
            this.remove(key);
        } else {
            this.store(key, value);
        }
    }

    /**
     * Sets the {@code value} associated with the given {@code keyHolder}.
     * <p>
     * If the {@code value} is {@code null} the key will be {@link #remove(CloudKeyHolder) removed}.
     *
     * @param <V>       the type of the value
     * @param keyHolder the holder of the key
     * @param value     the value
     */
    default <V> void set(final @NonNull CloudKeyHolder<V> keyHolder, final @Nullable V value) {
        if (value == null) {
            this.remove(keyHolder);
        } else {
            this.store(keyHolder, value);
        }
    }

    /**
     * Returns the value associated with the given {@code key} if it exists, else computes and stores the
     * value returned by the given {@code defaultFunction} and then returns it.
     *
     * @param <V>             the type of the value
     * @param key             the key
     * @param defaultFunction the function used to generate the value in case it's missing
     * @return the present or computed value
     */
    <V> V computeIfAbsent(@NonNull CloudKey<V> key, @NonNull Function<@NonNull CloudKey<V>, V> defaultFunction);

    /**
     * Returns the value associated with the given {@code keyHolder} if it exists, else computes and stores the
     * value returned by the given {@code defaultFunction} and then returns it.
     *
     * @param <V>             the type of the value
     * @param keyHolder       the holder of the key
     * @param defaultFunction the function used to generate the value in case it's missing
     * @return the present or computed value
     */
    default <V> V computeIfAbsent(
            final @NonNull CloudKeyHolder<V> keyHolder,
            final @NonNull Function<@NonNull CloudKey<V>, V> defaultFunction
    ) {
        return this.computeIfAbsent(keyHolder.key(), defaultFunction);
    }
}
