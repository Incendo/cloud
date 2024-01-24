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
package cloud.commandframework.exception.parsing;

import cloud.commandframework.caption.CaptionVariable;
import cloud.commandframework.caption.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.parser.standard.NumberParser;
import cloud.commandframework.type.range.Range;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public abstract class NumberParseException extends ParserException {

    private final String input;
    private final NumberParser<?, ?, ?> parser;

    /**
     * Constructs a new number parse exception.
     *
     * @param input   input that failed to parser
     * @param parser  parser that failed the parsing
     * @param context command context
     */
    protected NumberParseException(
            final @NonNull String input,
            final @NonNull NumberParser<?, ?, ?> parser,
            final @NonNull CommandContext<?> context
    ) {
        super(
                parser.getClass(),
                context,
                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_NUMBER,
                CaptionVariable.of("input", input),
                CaptionVariable.of("min", String.valueOf(parser.range().min())),
                CaptionVariable.of("max", String.valueOf(parser.range().max()))
        );
        this.input = input;
        this.parser = parser;
    }

    /**
     * Returns the number type.
     *
     * @return number type
     */
    public abstract @NonNull String numberType();

    /**
     * Returns the parser.
     *
     * @return the parser
     */
    public final @NonNull NumberParser<?, ?, ?> parser() {
        return this.parser;
    }

    /**
     * Returns whether the parser has a maximum value.
     *
     * @return {@code true} if there was a maximum value, else {@code false}
     */
    public final boolean hasMax() {
        return this.parser.hasMax();
    }

    /**
     * Returns whether the parser has a minimum value.
     *
     * @return {@code true} if there was a minimum value, else {@code false}
     */
    public final boolean hasMin() {
        return this.parser.hasMax();
    }

    /**
     * Returns the input that failed to parse.
     *
     * @return input
     */
    public @NonNull String input() {
        return this.input;
    }

    /**
     * Returns the range of acceptable input.
     *
     * @return parser range
     */
    public final @NonNull Range<? extends Number> range() {
        return this.parser.range();
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final NumberParseException that = (NumberParseException) o;
        return this.parser().equals(that.parser());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.parser());
    }
}
