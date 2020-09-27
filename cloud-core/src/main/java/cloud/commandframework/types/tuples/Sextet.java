//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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

import com.google.common.base.Objects;

import javax.annotation.Nonnull;

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
public class Sextet<U, V, W, X, Y, Z> {

    @Nonnull
    private final U first;
    @Nonnull
    private final V second;
    @Nonnull
    private final W third;
    @Nonnull
    private final X fourth;
    @Nonnull
    private final Y fifth;
    @Nonnull
    private final Z sixth;

    protected Sextet(@Nonnull final U first,
                     @Nonnull final V second,
                     @Nonnull final W third,
                     @Nonnull final X fourth,
                     @Nonnull final Y fifth,
                     @Nonnull final Z sixth) {
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
    @Nonnull
    public static <U, V, W, X, Y, Z> Sextet<U, V, W, X, Y, Z> of(@Nonnull final U first,
                                                                 @Nonnull final V second,
                                                                 @Nonnull final W third,
                                                                 @Nonnull final X fourth,
                                                                 @Nonnull final Y fifth,
                                                                 @Nonnull final Z sixth) {
        return new Sextet<>(first, second, third, fourth, fifth, sixth);
    }

    /**
     * Get the first value
     *
     * @return First value
     */
    @Nonnull
    public final U getFirst() {
        return this.first;
    }

    /**
     * Get the second value
     *
     * @return Second value
     */
    @Nonnull
    public final V getSecond() {
        return this.second;
    }

    /**
     * Get the third value
     *
     * @return Third value
     */
    @Nonnull
    public final W getThird() {
        return this.third;
    }

    /**
     * Get the fourth value
     *
     * @return Fourth value
     */
    @Nonnull
    public final X getFourth() {
        return this.fourth;
    }

    /**
     * Get the fifth value
     *
     * @return Fifth value
     */
    @Nonnull
    public final Y getFifth() {
        return this.fifth;
    }

    /**
     * Get the sixth value
     *
     * @return Sixth value
     */
    @Nonnull
    public final Z getSixth() {
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
        return Objects.equal(getFirst(), sextet.getFirst())
                && Objects.equal(getSecond(), sextet.getSecond())
                && Objects.equal(getThird(), sextet.getThird())
                && Objects.equal(getFourth(), sextet.getFourth())
                && Objects.equal(getFifth(), sextet.getFifth())
                && Objects.equal(getSixth(), sextet.getSixth());
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(getFirst(), getSecond(), getThird(), getFourth(), getFifth(), getSixth());
    }

    @Override
    public final String toString() {
        return String.format("(%s, %s, %s, %s, %s, %s)", this.first, this.second, this.third,
                             this.fourth, this.fifth, this.sixth);
    }

}
