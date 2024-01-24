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
package cloud.commandframework.type;

import cloud.commandframework.internal.ImmutableImpl;
import java.util.Optional;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;

import static java.util.Objects.requireNonNull;

/**
 * An object that contains either a value of type {@link U} or type {@link V}.
 *
 * @param <U> primary value type
 * @param <V> fallback value type
 */
@ImmutableImpl
@Value.Immutable
@API(status = API.Status.STABLE)
public interface Either<U, V> {

    /**
     * Creates an instance with a {@code value} of the primary type {@link U}.
     *
     * @param <U>   primary value type
     * @param <V>   secondary value type
     * @param value primary value
     * @return the instance
     */
    static <U, V> @NonNull Either<U, V> ofPrimary(final @NonNull U value) {
        return EitherImpl.of(requireNonNull(value, "value"), null);
    }

    /**
     * Creates an instance with a {@code value} of the fallback type {@link V}.
     *
     * @param <U>   primary value type
     * @param <V>   secondary value type
     * @param value primary value
     * @return the instance
     */
    static <U, V> @NonNull Either<U, V> ofFallback(final @NonNull V value) {
        return EitherImpl.of(null, requireNonNull(value, "value"));
    }

    /**
     * Returns an optional containing the value of type {@link U}, if it exists.
     *
     * @return the first value
     */
    @NonNull Optional<U> primary();

    /**
     * Returns an optional containing the value of type {@link V}, if it exists.
     *
     * @return the second value
     */
    @NonNull Optional<V> fallback();

    /**
     * Extract {@link #primary()}, or if it's not present, map {@link #fallback()} to {@code U}.
     *
     * @param mapFallback function mapping the fallback type to {@code U}
     * @return extracted or mapped value
     */
    default @NonNull U primaryOrMapFallback(final @NonNull Function<V, U> mapFallback) {
        return this.primary().orElseGet(() -> mapFallback.apply(this.fallback().get()));
    }

    /**
     * Extract {@link #fallback()}, or if it's not present, map {@link #primary()} to {@code V}.
     *
     * @param mapPrimary function mapping the primary type to {@code V}
     * @return extracted or mapped value
     */
    default @NonNull V fallbackOrMapPrimary(final @NonNull Function<U, V> mapPrimary) {
        return this.fallback().orElseGet(() -> mapPrimary.apply(this.primary().get()));
    }

    /**
     * Extract {@link #primary()} or {@link #fallback()}, in that order, applying the appropriate function
     * to map the result to {@code R}.
     *
     * @param mapPrimary  function mapping the primary type to {@code R}
     * @param mapFallback function mapping the fallback type to {@code R}
     * @param <R>         mapped type
     * @return mapped value
     */
    default @NonNull <R> R mapEither(
            final @NonNull Function<U, R> mapPrimary,
            final @NonNull Function<V, R> mapFallback
    ) {
        return this.primary()
                .map(mapPrimary)
                .orElseGet(() -> this.fallback().map(mapFallback).get());
    }
}
