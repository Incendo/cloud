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
package cloud.commandframework.types;

import cloud.commandframework.internal.ImmutableImpl;
import java.util.Optional;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;

/**
 * An object that contains either a value of type {@link U} or type {@link V}.
 *
 * @param <U> primary value type
 * @param <V> fallback value type
 * @since 2.0.0
 */
@ImmutableImpl
@Value.Immutable
@API(status = API.Status.STABLE, since = "2.0.0")
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
        return EitherImpl.of(value, null);
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
        return EitherImpl.of(null, value);
    }

    /**
     * Returns an optional containing the value of type {@link U}, if it exists.
     *
     * @return the first value
     */
    @NonNull Optional<@NonNull U> primary();

    /**
     * Returns an optional containing the value of type {@link V}, if it exists.
     *
     * @return the second value
     */
    @NonNull Optional<@NonNull V> fallback();
}
