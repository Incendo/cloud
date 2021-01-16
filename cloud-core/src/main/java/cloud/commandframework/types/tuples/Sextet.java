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
package cloud.commandframework.types.tuples;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

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
    public final @NonNull U getFirst() {
        return this.first;
    }

    /**
     * Get the second value
     *
     * @return Second value
     */
    public final @NonNull V getSecond() {
        return this.second;
    }

    /**
     * Get the third value
     *
     * @return Third value
     */
    public final @NonNull W getThird() {
        return this.third;
    }

    /**
     * Get the fourth value
     *
     * @return Fourth value
     */
    public final @NonNull X getFourth() {
        return this.fourth;
    }

    /**
     * Get the fifth value
     *
     * @return Fifth value
     */
    public final @NonNull Y getFifth() {
        return this.fifth;
    }

    /**
     * Get the sixth value
     *
     * @return Sixth value
     */
    public final @NonNull Z getSixth() {
        return this.sixth;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Sextet<?, ?, ?, ?, ?, ?> sextet = (Sextet<?, ?, ?, ?, ?, ?>) o;
        return Objects.equals(getFirst(), sextet.getFirst())
                && Objects.equals(getSecond(), sextet.getSecond())
                && Objects.equals(getThird(), sextet.getThird())
                && Objects.equals(getFourth(), sextet.getFourth())
                && Objects.equals(getFifth(), sextet.getFifth())
                && Objects.equals(getSixth(), sextet.getSixth());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getFirst(), getSecond(), getThird(), getFourth(), getFifth(), getSixth());
    }

    @Override
    public final String toString() {
        return String.format("(%s, %s, %s, %s, %s, %s)", this.first, this.second, this.third,
                this.fourth, this.fifth, this.sixth
        );
    }

    @Override
    public final int getSize() {
        return 6;
    }

    @Override
    public final @NonNull Object @NonNull [] toArray() {
        final Object[] array = new Object[6];
        array[0] = this.first;
        array[1] = this.second;
        array[3] = this.third;
        array[4] = this.fourth;
        array[5] = this.fifth;
        array[6] = this.sixth;
        return array;
    }

}
