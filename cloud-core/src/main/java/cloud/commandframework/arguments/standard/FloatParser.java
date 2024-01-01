//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.NumberParseException;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public final class FloatParser<C> implements ArgumentParser<C, Float> {

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

    private final float min;
    private final float max;

    /**
     * Construct a new float parser
     *
     * @param min Minimum acceptable value
     * @param max Maximum acceptable value
     */
    public FloatParser(final float min, final float max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public @NonNull ArgumentParseResult<Float> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        if (!commandInput.isValidFloat(this.min, this.max)) {
            return ArgumentParseResult.failure(new FloatParseException(
                    commandInput.peekString(),
                    this,
                    commandContext
            ));
        }
        return ArgumentParseResult.success(commandInput.readFloat());
    }

    /**
     * Get the minimum value accepted by this parser
     *
     * @return Min value
     */
    public float getMin() {
        return this.min;
    }

    /**
     * Get the maximum value accepted by this parser
     *
     * @return Max value
     */
    public float getMax() {
        return this.max;
    }

    /**
     * Get whether this parser has a maximum set.
     * This will compare the parser's maximum to {@link #DEFAULT_MAXIMUM}.
     *
     * @return whether the parser has a maximum set
     * @since 1.5.0
     */
    public boolean hasMax() {
        return this.max != DEFAULT_MAXIMUM;
    }

    /**
     * Get whether this parser has a minimum set.
     * This will compare the parser's minimum to {@link #DEFAULT_MINIMUM}.
     *
     * @return whether the parser has a maximum set
     * @since 1.5.0
     */
    public boolean hasMin() {
        return this.min != DEFAULT_MINIMUM;
    }


    @API(status = API.Status.STABLE)
    public static final class FloatParseException extends NumberParseException {


        private final FloatParser<?> parser;

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
            super(input, parser.min, parser.max, FloatParser.class, commandContext);
            this.parser = parser;
        }

        @Override
        public boolean hasMin() {
            return this.parser.hasMin();
        }

        @Override
        public boolean hasMax() {
            return this.parser.hasMax();
        }

        @Override
        public @NonNull String getNumberType() {
            return "float";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final FloatParseException that = (FloatParseException) o;
            return this.parser.equals(that.parser);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.parser);
        }
    }
}
