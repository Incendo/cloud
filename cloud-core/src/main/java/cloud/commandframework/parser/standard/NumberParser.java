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
package cloud.commandframework.parser.standard;

import cloud.commandframework.parser.ArgumentParser;
import cloud.commandframework.types.range.Range;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE, since = "2.0.0")
public abstract class NumberParser<C, N extends Number, R extends Range<N>> implements ArgumentParser<C, N> {

    private final R range;

    protected NumberParser(final @NonNull R range) {
        this.range = Objects.requireNonNull(range, "range");
    }

    /**
     * Returns the range of acceptable values.
     *
     * @return acceptable value range
     */
    public final @NonNull R range() {
        return this.range;
    }

    /**
     * Returns whether this parser has a maximum value set.
     *
     * <p>In other words, whether the {@link Range#min() maximum} of it's {@link #range()} is different than the default
     * maximum value.</p>
     *
     * @return whether the parser has a maximum set
     */
    public abstract boolean hasMax();

    /**
     * Returns whether this parser has a minimum value set.
     *
     * <p>In other words, whether the {@link Range#min() minimum} of it's {@link #range()} is different than the default
     * minimum value.</p>
     *
     * @return whether the parser has a minimum set
     */
    public abstract boolean hasMin();
}
