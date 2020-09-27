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
package cloud.commandframework.exceptions.parsing;

import javax.annotation.Nonnull;

public abstract class NumberParseException extends IllegalArgumentException {

    private final String input;
    private final Number min;
    private final Number max;

    /**
     * Construct a new number parse exception
     *
     * @param input Input
     * @param min   Maximum value
     * @param max   Minimum value
     */
    public NumberParseException(@Nonnull final String input, @Nonnull final Number min, @Nonnull final Number max) {
        this.input = input;
        this.min = min;
        this.max = max;
    }

    @Override
    public final String getMessage() {
        if (this.hasMin() && this.hasMax()) {
            return "'" + this.input + "' is not a valid " + this.getNumberType() + " in the range ["
                    + this.min + ", " + this.max + "]";
        } else if (this.hasMin()) {
            return "'" + this.input + "' is not a valid " + this.getNumberType() + " above " + this.min;
        } else if (this.hasMax()) {
            return "'" + this.input + "' is not a valid " + this.getNumberType() + " below " + this.max;
        } else {
            return String.format("'%s' is not a valid %s", this.input, this.getNumberType());
        }
    }

    /**
     * Get the number type
     *
     * @return Number type
     */
    @Nonnull
    public abstract String getNumberType();

    /**
     * If the parser had a maximum value
     *
     * @return {@code true} if there was a maximum value, else {@code false}
     */
    public abstract boolean hasMax();

    /**
     * If the parser had a minimum value
     *
     * @return {@code true} if there was a minimum value, else {@code false}
     */
    public abstract boolean hasMin();

    /**
     * Get the input that failed to parse
     *
     * @return Input
     */
    @Nonnull
    public String getInput() {
        return this.input;
    }

    /**
     * Get the minimum accepted integer that could have been parsed
     *
     * @return Minimum integer
     */
    public Number getMin() {
        return this.min;
    }

    /**
     * Get the maximum accepted integer that could have been parsed
     *
     * @return Maximum integer
     */
    public Number getMax() {
        return this.max;
    }

}
