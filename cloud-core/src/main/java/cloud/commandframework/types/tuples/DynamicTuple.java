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

/**
 * Dynamic sized tuple backed by a {@code Object[]}
 */
public final class DynamicTuple implements Tuple {

    private final Object[] internalArray;

    private DynamicTuple(final @NonNull Object @NonNull [] internalArray) {
        this.internalArray = internalArray;
    }

    /**
     * Create a new dynamic tuple, containing the given elements
     *
     * @param elements Elements that should be contained in the tuple
     * @return Created tuple, preserving the order of the given elements
     */
    public static @NonNull DynamicTuple of(final @NonNull Object... elements) {
        return new DynamicTuple(elements);
    }

    @Override
    public int getSize() {
        return this.internalArray.length;
    }

    @Override
    public @NonNull Object @NonNull [] toArray() {
        final @NonNull Object @NonNull [] newArray = new Object[this.internalArray.length];
        System.arraycopy(this.internalArray, 0, newArray, 0, this.internalArray.length);
        return newArray;
    }

}
