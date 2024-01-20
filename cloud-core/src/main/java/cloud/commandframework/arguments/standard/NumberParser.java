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
package cloud.commandframework.arguments.standard;

import cloud.commandframework.arguments.Range;
import cloud.commandframework.arguments.parser.ArgumentParser;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE, since = "2.0.0")
public abstract class NumberParser<C, N extends Number> implements ArgumentParser<C, N> {

    private final Range<N> range;

    protected NumberParser(final @NonNull Range<N> range) {
        this.range = Objects.requireNonNull(range, "range");
    }

    /**
     * Returns the range of acceptable values.
     *
     * @return acceptable value range
     */
    public final @NonNull Range<N> range() {
        return this.range;
    }

    /**
     * Returns whether this parser has a maximum value set.
     *
     * @return whether the parser has a maximum set
     */
    public abstract boolean hasMax();

    /**
     * Returns whether this parser has a minimum value set.
     *
     * @return whether the parser has a minimum set
     */
    public abstract boolean hasMin();

    /**
     * Returns the minimum value that is accepted by this parser.
     *
     * @return min value
     */
    public final @NonNull N min() {
        return this.range.min();
    }

    /**
     * Returns the maximum value that is accepted by this parser.
     *
     * @return max value
     */
    public final @NonNull N max() {
        return this.range.max();
    }
}
