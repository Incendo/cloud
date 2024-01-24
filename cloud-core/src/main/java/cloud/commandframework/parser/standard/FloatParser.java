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

import cloud.commandframework.component.CommandComponent;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exception.parsing.NumberParseException;
import cloud.commandframework.parser.ArgumentParseResult;
import cloud.commandframework.parser.ParserDescriptor;
import cloud.commandframework.types.range.FloatRange;
import cloud.commandframework.types.range.Range;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public final class FloatParser<C> extends NumberParser<C, Float, FloatRange> {

    /**
     * Constant for the default/unset minimum value.
     *
     * @since 1.5.0
     */
    @API(status = API.Status.STABLE, since = "1.5.0")
    public static final float DEFAULT_MINIMUM = Float.NEGATIVE_INFINITY;

    /**
     * Constant for the default/unset maximum value.
     *
     * @since 1.5.0
     */
    @API(status = API.Status.STABLE, since = "1.5.0")
    public static final float DEFAULT_MAXIMUM = Float.POSITIVE_INFINITY;

    /**
     * Creates a new float parser using {@link FloatParser#DEFAULT_MINIMUM} and {@link FloatParser#DEFAULT_MAXIMUM} as
     * the limits.
     *
     * @param <C> the command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Float> floatParser() {
        return floatParser(FloatParser.DEFAULT_MINIMUM, FloatParser.DEFAULT_MAXIMUM);
    }

    /**
     * Creates a new float parser using {@link FloatParser#DEFAULT_MAXIMUM} as the maximum value.
     *
     * @param <C>      the command sender type
     * @param minValue the minimum value accepted by the parser
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Float> floatParser(
            final float minValue
    ) {
        return ParserDescriptor.of(new FloatParser<>(minValue, FloatParser.DEFAULT_MAXIMUM), Float.class);
    }

    /**
     * Creates a new float parser.
     *
     * @param <C>      the command sender type
     * @param minValue the minimum value accepted by the parser
     * @param maxValue the maximum value accepted by the parser
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Float> floatParser(
            final float minValue,
            final float maxValue
    ) {
        return ParserDescriptor.of(new FloatParser<>(minValue, maxValue), Float.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #floatParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Float> floatComponent() {
        return CommandComponent.<C, Float>builder().parser(floatParser());
    }

    /**
     * Construct a new float parser
     *
     * @param min Minimum acceptable value
     * @param max Maximum acceptable value
     */
    public FloatParser(final float min, final float max) {
        super(Range.floatRange(min, max));
    }

    @Override
    public @NonNull ArgumentParseResult<Float> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        if (!commandInput.isValidFloat(this.range())) {
            return ArgumentParseResult.failure(new FloatParseException(
                    commandInput.peekString(),
                    this,
                    commandContext
            ));
        }
        return ArgumentParseResult.success(commandInput.readFloat());
    }

    @Override
    public boolean hasMax() {
        return this.range().maxFloat() != DEFAULT_MAXIMUM;
    }

    @Override
    public boolean hasMin() {
        return this.range().minFloat() != DEFAULT_MINIMUM;
    }


    @API(status = API.Status.STABLE)
    public static final class FloatParseException extends NumberParseException {

        /**
         * Create a new {@link FloatParseException}.
         *
         * @param input          input string
         * @param parser         float parser
         * @param commandContext command context
         * @since 1.5.0
         */
        @API(status = API.Status.STABLE, since = "1.5.0")
        public FloatParseException(
                final @NonNull String input,
                final @NonNull FloatParser<?> parser,
                final @NonNull CommandContext<?> commandContext
        ) {
            super(input, parser, commandContext);
        }

        @Override
        public @NonNull String numberType() {
            return "float";
        }
    }
}
