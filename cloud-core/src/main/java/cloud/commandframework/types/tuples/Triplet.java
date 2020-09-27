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
 * Immutable generic 3-tuple
 *
 * @param <U> First type
 * @param <V> Second type
 * @param <W> Third type
 */
public class Triplet<U, V, W> {

    @Nonnull
    private final U first;
    @Nonnull
    private final V second;
    @Nonnull
    private final W third;

    protected Triplet(@Nonnull final U first,
                      @Nonnull final V second,
                      @Nonnull final W third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    /**
     * Create a new 3-tuple
     *
     * @param first  First value
     * @param second Second value
     * @param third  Third value
     * @param <U>    First type
     * @param <V>    Second type
     * @param <W>    Third type
     * @return Created triplet
     */
    @Nonnull
    public static <U, V, W> Triplet<U, V, W> of(@Nonnull final U first,
                                                @Nonnull final V second,
                                                @Nonnull final W third) {
        return new Triplet<>(first, second, third);
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

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) o;
        return Objects.equal(getFirst(), triplet.getFirst())
                && Objects.equal(getSecond(), triplet.getSecond())
                && Objects.equal(getThird(), triplet.getThird());
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(getFirst(), getSecond(), getThird());
    }

    @Override
    public final String toString() {
        return String.format("(%s, %s, %s)", this.first, this.second, this.third);
    }

}
