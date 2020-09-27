//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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

import javax.annotation.Nonnull;

/**
 * Utility class for dealing with tuples
 */
public final class Tuples {

    private Tuples() {
    }

    /**
     * Create a new pair
     *
     * @param first  First value
     * @param second Second value
     * @param <U>    First type
     * @param <V>    Second type
     * @return Created pair
     */
    @Nonnull
    public static <U, V> Pair<U, V> of(@Nonnull final U first,
                                       @Nonnull final V second) {
        return Pair.of(first, second);
    }

    /**
     * Create a new triplet
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
        return Triplet.of(first, second, third);
    }

    /**
     * Create a new quartet
     *
     * @param first  First value
     * @param second Second value
     * @param third  Third value
     * @param fourth Fourth value
     * @param <U>    First type
     * @param <V>    Second type
     * @param <W>    Third type
     * @param <X>    Fourth type
     * @return Created quartet
     */
    @Nonnull
    public static <U, V, W, X> Quartet<U, V, W, X> of(@Nonnull final U first,
                                                      @Nonnull final V second,
                                                      @Nonnull final W third,
                                                      @Nonnull final X fourth) {
        return Quartet.of(first, second, third, fourth);
    }

    /**
     * Create a new quintet
     *
     * @param first  First value
     * @param second Second value
     * @param third  Third value
     * @param fourth Fourth value
     * @param fifth  Fifth value
     * @param <U>    First type
     * @param <V>    Second type
     * @param <W>    Third type
     * @param <X>    Fourth type
     * @param <Y>    Fifth type
     * @return Created quintet
     */
    @Nonnull
    public static <U, V, W, X, Y> Quintet<U, V, W, X, Y> of(@Nonnull final U first,
                                                            @Nonnull final V second,
                                                            @Nonnull final W third,
                                                            @Nonnull final X fourth,
                                                            @Nonnull final Y fifth) {
        return Quintet.of(first, second, third, fourth, fifth);
    }

    /**
     * Create a new sextet
     *
     * @param first  First value
     * @param second Second value
     * @param third  Third value
     * @param fourth Fourth value
     * @param fifth  Fifth value
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
        return Sextet.of(first, second, third, fourth, fifth, sixth);
    }

}
