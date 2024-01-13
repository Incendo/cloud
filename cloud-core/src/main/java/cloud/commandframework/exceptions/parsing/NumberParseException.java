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
package cloud.commandframework.exceptions.parsing;

import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public abstract class NumberParseException extends ParserException {

    private final String input;
    private final Number min;
    private final Number max;

    /**
     * Construct a new number parse exception
     *
     * @param input       Input
     * @param min         Maximum value
     * @param max         Minimum value
     * @param parserClass Parser class
     * @param context     Command context
     */
    protected NumberParseException(
            final @NonNull String input,
            final @NonNull Number min,
            final @NonNull Number max,
            final @NonNull Class<?> parserClass,
            final @NonNull CommandContext<?> context
    ) {
        super(
                parserClass,
                context,
                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_NUMBER,
                CaptionVariable.of("input", input),
                CaptionVariable.of("min", String.valueOf(min)),
                CaptionVariable.of("max", String.valueOf(max))
        );
        this.input = input;
        this.min = min;
        this.max = max;
    }

    /**
     * Returns the number type.
     *
     * @return number type
     */
    public abstract @NonNull String numberType();

    /**
     * Returns whether the parser has a maximum value.
     *
     * @return {@code true} if there was a maximum value, else {@code false}
     */
    public abstract boolean hasMax();

    /**
     * Returns whether the parser has a minimum value.
     *
     * @return {@code true} if there was a minimum value, else {@code false}
     */
    public abstract boolean hasMin();

    /**
     * Returns the input that failed to parse.
     *
     * @return input
     */
    public @NonNull String input() {
        return this.input;
    }

    /**
     * Returns the minimum accepted integer that could have been parsed.
     *
     * @return minimum integer
     */
    public Number min() {
        return this.min;
    }

    /**
     * Returns the maximum accepted integer that could have been parsed.
     *
     * @return maximum integer
     */
    public Number max() {
        return this.max;
    }
}
