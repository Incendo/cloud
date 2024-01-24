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
package cloud.commandframework.type.tuple;

import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Immutable generic 2-tuple
 *
 * @param <U> First type
 * @param <V> Second type
 */
@API(status = API.Status.STABLE)
public class Pair<U, V> implements Tuple {

    private final U first;
    private final V second;

    protected Pair(
            final U first,
            final V second
    ) {
        this.first = first;
        this.second = second;
    }

    /**
     * Create a new 2-tuple
     *
     * @param first  First value
     * @param second Second value
     * @param <U>    First type
     * @param <V>    Second type
     * @return Created pair
     */
    public static <U, V> @NonNull Pair<U, V> of(
            final U first,
            final V second
    ) {
        return new Pair<>(first, second);
    }

    /**
     * Returns the first value.
     *
     * @return first value
     */
    public final U first() {
        return this.first;
    }

    /**
     * Returns the second value.
     *
     * @return second value
     */
    public final V second() {
        return this.second;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(this.first(), pair.first())
                && Objects.equals(this.second(), pair.second());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.first(), this.second());
    }

    @Override
    public final String toString() {
        return String.format("(%s, %s)", this.first, this.second);
    }

    @Override
    public final int size() {
        return 2;
    }

    @Override
    public final @NonNull Object @NonNull [] toArray() {
        final Object[] array = new Object[2];
        array[0] = this.first;
        array[1] = this.second;
        return array;
    }
}
