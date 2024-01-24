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
package cloud.commandframework.type.range;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An inclusive range of numbers.
 *
 * @param <N> number type
 */
@API(status = API.Status.STABLE)
public interface Range<N extends Number> {

    /**
     * Returns the minimum value (inclusive).
     *
     * @return minimum value
     */
    @NonNull N min();

    /**
     * Returns the maximum value (inclusive).
     *
     * @return maximum value
     */
    @NonNull N max();

    /**
     * Creates a new range.
     *
     * @param min min value
     * @param max max value
     * @return the range
     */
    static @NonNull ByteRange byteRange(final byte min, final byte max) {
        return ByteRangeImpl.of(min, max);
    }

    /**
     * Creates a new range.
     *
     * @param min min value
     * @param max max value
     * @return the range
     */
    static @NonNull DoubleRange doubleRange(final double min, final double max) {
        return DoubleRangeImpl.of(min, max);
    }

    /**
     * Creates a new range.
     *
     * @param min min value
     * @param max max value
     * @return the range
     */
    static @NonNull FloatRange floatRange(final float min, final float max) {
        return FloatRangeImpl.of(min, max);
    }

    /**
     * Creates a new range.
     *
     * @param min min value
     * @param max max value
     * @return the range
     */
    static @NonNull IntRange intRange(final int min, final int max) {
        return IntRangeImpl.of(min, max);
    }

    /**
     * Creates a new range.
     *
     * @param min min value
     * @param max max value
     * @return the range
     */
    static @NonNull LongRange longRange(final long min, final long max) {
        return LongRangeImpl.of(min, max);
    }

    /**
     * Creates a new range.
     *
     * @param min min value
     * @param max max value
     * @return the range
     */
    static @NonNull ShortRange shortRange(final short min, final short max) {
        return ShortRangeImpl.of(min, max);
    }
}
