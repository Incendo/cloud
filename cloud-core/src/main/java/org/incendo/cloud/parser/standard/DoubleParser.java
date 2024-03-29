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
package org.incendo.cloud.parser.standard;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.NumberParseException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.type.range.DoubleRange;
import org.incendo.cloud.type.range.Range;

@API(status = API.Status.STABLE)
public final class DoubleParser<C> extends NumberParser<C, Double, DoubleRange> {

    /**
     * Constant for the default/unset minimum value.
     *
     */
    @API(status = API.Status.STABLE)
    public static final double DEFAULT_MINIMUM = Double.NEGATIVE_INFINITY;

    /**
     * Constant for the default/unset maximum value.
     *
     */
    @API(status = API.Status.STABLE)
    public static final double DEFAULT_MAXIMUM = Double.POSITIVE_INFINITY;

    /**
     * Creates a new double parser using {@link DoubleParser#DEFAULT_MINIMUM} and {@link DoubleParser#DEFAULT_MAXIMUM} as
     * the limits.
     *
     * @param <C> the command sender type
     * @return the created parser
     */
    @API(status = API.Status.STABLE)
    public static <C> @NonNull ParserDescriptor<C, Double> doubleParser() {
        return doubleParser(DoubleParser.DEFAULT_MINIMUM, DoubleParser.DEFAULT_MAXIMUM);
    }

    /**
     * Creates a new double parser using {@link DoubleParser#DEFAULT_MAXIMUM} as the maximum value.
     *
     * @param <C>      the command sender type
     * @param minValue the minimum value accepted by the parser
     * @return the created parser
     */
    @API(status = API.Status.STABLE)
    public static <C> @NonNull ParserDescriptor<C, Double> doubleParser(
            final double minValue
    ) {
        return ParserDescriptor.of(new DoubleParser<>(minValue, DoubleParser.DEFAULT_MAXIMUM), Double.class);
    }

    /**
     * Creates a new double parser.
     *
     * @param <C>      the command sender type
     * @param minValue the minimum value accepted by the parser
     * @param maxValue the maximum value accepted by the parser
     * @return the created parser
     */
    @API(status = API.Status.STABLE)
    public static <C> @NonNull ParserDescriptor<C, Double> doubleParser(
            final double minValue,
            final double maxValue
    ) {
        return ParserDescriptor.of(new DoubleParser<>(minValue, maxValue), Double.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #doubleParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     */
    @API(status = API.Status.STABLE)
    public static <C> CommandComponent.@NonNull Builder<C, Double> doubleComponent() {
        return CommandComponent.<C, Double>builder().parser(doubleParser());
    }

    /**
     * Construct a new double parser
     *
     * @param min Minimum acceptable value
     * @param max Maximum acceptable value
     */
    public DoubleParser(final double min, final double max) {
        super(Range.doubleRange(min, max));
    }

    @Override
    public @NonNull ArgumentParseResult<Double> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        if (!commandInput.isValidDouble(this.range())) {
            return ArgumentParseResult.failure(new DoubleParseException(
                    commandInput.peekString(),
                    this,
                    commandContext
            ));
        }
        return ArgumentParseResult.success(commandInput.readDouble());
    }

    @Override
    public boolean hasMax() {
        return this.range().maxDouble() != DEFAULT_MAXIMUM;
    }

    @Override
    public boolean hasMin() {
        return this.range().minDouble() != DEFAULT_MINIMUM;
    }


    @API(status = API.Status.STABLE)
    public static final class DoubleParseException extends NumberParseException {

        /**
         * Create a new {@link DoubleParseException}.
         *
         * @param input          input string
         * @param parser         double parser
         * @param commandContext command context
         */
        @API(status = API.Status.STABLE)
        public DoubleParseException(
                final @NonNull String input,
                final @NonNull DoubleParser<?> parser,
                final @NonNull CommandContext<?> commandContext
        ) {
            super(input, parser, commandContext);
        }

        @Override
        public @NonNull String numberType() {
            return "double";
        }
    }
}
