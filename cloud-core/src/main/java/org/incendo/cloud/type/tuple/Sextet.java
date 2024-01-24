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
package org.incendo.cloud.type.tuple;

import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Immutable generic 6-tuple
 *
 * @param <U> First type
 * @param <V> Second type
 * @param <W> Third type
 * @param <X> Fourth type
 * @param <Y> Fifth type
 * @param <Z> Sixth type
 */
@API(status = API.Status.STABLE)
public class Sextet<U, V, W, X, Y, Z> implements Tuple {

    private final U first;
    private final V second;
    private final W third;
    private final X fourth;
    private final Y fifth;
    private final Z sixth;

    protected Sextet(
            final @NonNull U first,
            final @NonNull V second,
            final @NonNull W third,
            final @NonNull X fourth,
            final @NonNull Y fifth,
            final @NonNull Z sixth
    ) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.fifth = fifth;
        this.sixth = sixth;
    }

    /**
     * Create a new 6-tuple
     *
     * @param first  First value
     * @param second Second value
     * @param third  Third value
     * @param fourth Fourth value
     * @param fifth  fifth value
     * @param sixth  Sixth value
     * @param <U>    First type
     * @param <V>    Second type
     * @param <W>    Third type
     * @param <X>    Fourth type
     * @param <Y>    Fifth type
     * @param <Z>    Sixth type
     * @return Created sextet
     */
    public static <U, V, W, X, Y, Z> @NonNull Sextet<@NonNull U, @NonNull V, @NonNull W, @NonNull X, @NonNull Y, @NonNull Z> of(
            final @NonNull U first,
            final @NonNull V second,
            final @NonNull W third,
            final @NonNull X fourth,
            final @NonNull Y fifth,
            final @NonNull Z sixth
    ) {
        return new Sextet<>(first, second, third, fourth, fifth, sixth);
    }

    /**
     * Get the first value
     *
     * @return First value
     */
    public final @NonNull U first() {
        return this.first;
    }

    /**
     * Get the second value
     *
     * @return Second value
     */
    public final @NonNull V second() {
        return this.second;
    }

    /**
     * Get the third value
     *
     * @return Third value
     */
    public final @NonNull W third() {
        return this.third;
    }

    /**
     * Get the fourth value
     *
     * @return Fourth value
     */
    public final @NonNull X fourth() {
        return this.fourth;
    }

    /**
     * Get the fifth value
     *
     * @return Fifth value
     */
    public final @NonNull Y fifth() {
        return this.fifth;
    }

    /**
     * Get the sixth value
     *
     * @return Sixth value
     */
    public final @NonNull Z sixth() {
        return this.sixth;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Sextet<?, ?, ?, ?, ?, ?> sextet = (Sextet<?, ?, ?, ?, ?, ?>) o;
        return Objects.equals(this.first(), sextet.first())
                && Objects.equals(this.second(), sextet.second())
                && Objects.equals(this.third(), sextet.third())
                && Objects.equals(this.fourth(), sextet.fourth())
                && Objects.equals(this.fifth(), sextet.fifth())
                && Objects.equals(this.sixth(), sextet.sixth());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(
                this.first(),
                this.second(),
                this.third(),
                this.fourth(),
                this.fifth(),
                this.sixth()
        );
    }

    @Override
    public final String toString() {
        return String.format("(%s, %s, %s, %s, %s, %s)", this.first, this.second, this.third,
                this.fourth, this.fifth, this.sixth
        );
    }

    @Override
    public final int size() {
        return 6;
    }

    @Override
    public final @NonNull Object @NonNull [] toArray() {
        final Object[] array = new Object[6];
        array[0] = this.first;
        array[1] = this.second;
        array[2] = this.third;
        array[3] = this.fourth;
        array[4] = this.fifth;
        array[5] = this.sixth;
        return array;
    }
}
