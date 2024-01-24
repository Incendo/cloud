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
package cloud.commandframework.key;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals", "unused"})
@API(status = API.Status.STABLE)
public interface CloudKeyContainer {

    /**
     * Returns the value associated with the given {@code key}.
     *
     * @param <V> the type of the value
     * @param key the key
     * @return the value
     */
    <V extends @NonNull Object> @NonNull Optional<V> optional(@NonNull CloudKey<V> key);

    /**
     * Returns the value associated with the given {@code key}.
     *
     * @param <V> the type of the value
     * @param key the key
     * @return the value
     */
    <V extends @NonNull Object> @NonNull Optional<V> optional(@NonNull String key);

    /**
     * Returns the value associated with the given {@code keyHolder}.
     *
     * @param <V>       the type of the value
     * @param keyHolder the holder of the key
     * @return the value
     */
    default <V extends @NonNull Object> @NonNull Optional<V> optional(@NonNull CloudKeyHolder<V> keyHolder) {
        return this.optional(keyHolder.key());
    }

    /**
     * Returns the value associated with the given {@code key} if it exists,
     * or else the {@code defaultValue}.
     *
     * @param <V>          the type of the value
     * @param key          the key
     * @param defaultValue the default value to use if the value isn't present
     * @return the value
     */
    default <V> V getOrDefault(@NonNull CloudKey<@NonNull V> key, V defaultValue) {
        return this.optional(key).orElse(defaultValue);
    }

    /**
     * Returns the value associated with the given {@code key} if it exists,
     * or else the {@code defaultValue}.
     *
     * @param <V>          the type of the value
     * @param key          the key
     * @param defaultValue the default value to use if the value isn't present
     * @return the value
     */
    default <V> V getOrDefault(@NonNull String key, V defaultValue) {
        return this.<V>optional(key).orElse(defaultValue);
    }

    /**
     * Returns the value associated with the given {@code keyHolder} if it exists,
     * or else the {@code defaultValue}.
     *
     * @param <V>          the type of the value
     * @param keyHolder    the holder of the key
     * @param defaultValue the default value to use if the value isn't present
     * @return the value
     */
    default <V> V getOrDefault(@NonNull CloudKeyHolder<@NonNull V> keyHolder, V defaultValue) {
        return this.getOrDefault(keyHolder.key(), defaultValue);
    }

    /**
     * Returns the value associated with the given {@code key} if it exists,
     * or else the value supplied by the given {@code supplier}
     *
     * @param <V>      the type of the value
     * @param key      the key
     * @param supplier supplier of the default value
     * @return the value
     */
    default <V> V getOrSupplyDefault(@NonNull CloudKey<@NonNull V> key, @NonNull Supplier<V> supplier) {
        return this.optional(key).orElseGet(supplier);
    }

    /**
     * Returns the value associated with the given {@code key} if it exists,
     * or else the value supplied by the given {@code supplier}
     *
     * @param <V>      the type of the value
     * @param key      the key
     * @param supplier supplier of the default value
     * @return the value
     */
    default <V> V getOrSupplyDefault(@NonNull String key, @NonNull Supplier<V> supplier) {
        return this.<V>optional(key).orElseGet(supplier);
    }

    /**
     * Returns the value associated with the given {@code keyHolder} if it exists,
     * or else the value supplied by the given {@code supplier}
     *
     * @param <V>       the type of the value
     * @param keyHolder the holder of the key
     * @param supplier  supplier of the default value
     * @return the value
     */
    default <V> V getOrSupplyDefault(@NonNull CloudKeyHolder<@NonNull V> keyHolder, @NonNull Supplier<V> supplier) {
        return this.optional(keyHolder).orElseGet(supplier);
    }

    /**
     * Returns the value associated with the given {@code key} or fails exceptionally if it's missing.
     *
     * @param <V> the type of the value
     * @param key the key
     * @return the value
     * @throws NullPointerException if the value is missing
     */
    default <V extends @NonNull Object> V get(@NonNull CloudKey<V> key) {
        return this.optional(key).orElseThrow(() -> new NullPointerException(
                String.format("There is no object in the registry identified by the key '%s'", key.name())
        ));
    }

    /**
     * Returns the value associated with the given {@code key} or fails exceptionally if it's missing.
     *
     * @param <V> the type of the value
     * @param key the key
     * @return the value
     * @throws NullPointerException if the value is missing
     */
    default <V extends @NonNull Object> V get(@NonNull String key) {
        return this.optional(key).map(value -> (V) value).orElseThrow(() -> new NullPointerException(
                String.format("There is no object in the registry identified by the key '%s'", key)
        ));
    }

    /**
     * Returns the value associated with the given {@code keyHolder} or fails exceptionally if it's missing.
     *
     * @param <V>       the type of the value
     * @param keyHolder the holder of the key
     * @return the value
     * @throws NullPointerException if the value is missing
     */
    default <V extends @NonNull Object> V get(@NonNull CloudKeyHolder<V> keyHolder) {
        return this.get(keyHolder.key());
    }

    /**
     * Returns whether the registry contains a value associated with the given {@code key}.
     *
     * @param key the key
     * @return {@code true} if the value exists, or {@code false} if it does not
     */
    boolean contains(@NonNull CloudKey<?> key);

    /**
     * Returns whether the registry contains a value associated with the given {@code key}.
     *
     * @param key the key
     * @return {@code true} if the value exists, or {@code false} if it does not
     */
    default boolean contains(@NonNull String key) {
        return this.contains(CloudKey.of(key));
    }

    /**
     * Returns whether the registry contains a value associated with the given {@code keyHolder}.
     *
     * @param keyHolder the holder of the key
     * @return {@code true} if the value exists, or {@code false} if it does not
     */
    default boolean contains(@NonNull CloudKeyHolder<?> keyHolder) {
        return this.contains(keyHolder.key());
    }

    /**
     * Returns all stored values.
     *
     * @return immutable view of all values
     */
    @NonNull Map<CloudKey<?>, ? extends @NonNull Object> all();
}
