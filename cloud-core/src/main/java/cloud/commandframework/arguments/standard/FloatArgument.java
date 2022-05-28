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
package cloud.commandframework.arguments.standard;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.NumberParseException;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiFunction;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("unused")
public final class FloatArgument<C> extends CommandArgument<C, Float> {

    private final float min;
    private final float max;

    private FloatArgument(
            final boolean required,
            final @NonNull String name,
            final float min,
            final float max,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>,
                    @NonNull String, @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new FloatParser<>(min, max), defaultValue, Float.class, suggestionsProvider, defaultDescription);
        this.min = min;
        this.max = max;
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> @NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required {@link FloatArgument}.
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Float> of(final @NonNull String name) {
        return FloatArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional {@link FloatArgument}.
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Float> optional(final @NonNull String name) {
        return FloatArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new optional {@link FloatArgument} with the specified default value.
     *
     * @param name       Argument name
     * @param defaultNum Default num
     * @param <C>        Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Float> optional(
            final @NonNull String name,
            final float defaultNum
    ) {
        return FloatArgument.<C>newBuilder(name).asOptionalWithDefault(defaultNum).build();
    }

    /**
     * Get the minimum accepted float that could have been parsed
     *
     * @return Minimum float
     */
    public float getMin() {
        return this.min;
    }

    /**
     * Get the maximum accepted float that could have been parsed
     *
     * @return Maximum float
     */
    public float getMax() {
        return this.max;
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, Float> {

        private float min = FloatParser.DEFAULT_MINIMUM;
        private float max = FloatParser.DEFAULT_MAXIMUM;

        private Builder(final @NonNull String name) {
            super(Float.class, name);
        }

        /**
         * Set a minimum value
         *
         * @param min Minimum value
         * @return Builder instance
         */
        public @NonNull Builder<C> withMin(final int min) {
            this.min = min;
            return this;
        }

        /**
         * Set a maximum value
         *
         * @param max Maximum value
         * @return Builder instance
         */
        public @NonNull Builder<C> withMax(final int max) {
            this.max = max;
            return this;
        }

        /**
         * Sets the command argument to be optional, with the specified default value.
         *
         * @param defaultValue default value
         * @return this builder
         * @see CommandArgument.Builder#asOptionalWithDefault(String)
         * @since 1.5.0
         */
        public @NonNull Builder<C> asOptionalWithDefault(final float defaultValue) {
            return (Builder<C>) this.asOptionalWithDefault(Float.toString(defaultValue));
        }

        @Override
        public @NonNull FloatArgument<C> build() {
            return new FloatArgument<>(this.isRequired(), this.getName(), this.min, this.max,
                    this.getDefaultValue(), this.getSuggestionsProvider(), this.getDefaultDescription()
            );
        }

    }

    public static final class FloatParser<C> implements ArgumentParser<C, Float> {

        /**
         * Constant for the default/unset minimum value.
         *
         * @since 1.5.0
         */
        public static final float DEFAULT_MINIMUM = Float.NEGATIVE_INFINITY;

        /**
         * Constant for the default/unset maximum value.
         *
         * @since 1.5.0
         */
        public static final float DEFAULT_MAXIMUM = Float.POSITIVE_INFINITY;

        private final float min;
        private final float max;

        /**
         * Construct a new float parser
         *
         * @param min Minimum value
         * @param max Maximum value
         */
        public FloatParser(final float min, final float max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public @NonNull ArgumentParseResult<Float> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(FloatParser.class, commandContext));
            }
            try {
                final float value = Float.parseFloat(input);
                if (value < this.min || value > this.max) {
                    return ArgumentParseResult.failure(new FloatParseException(input, this, commandContext));
                }
                inputQueue.remove();
                return ArgumentParseResult.success(value);
            } catch (final Exception e) {
                return ArgumentParseResult.failure(new FloatParseException(input, this, commandContext));
            }
        }

        @Override
        public boolean isContextFree() {
            return true;
        }

        /**
         * Get the max value
         *
         * @return Max value
         */
        public float getMax() {
            return this.max;
        }

        /**
         * Get the min value
         *
         * @return Min value
         */
        public float getMin() {
            return this.min;
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

    }


    @SuppressWarnings("serial")
    public static final class FloatParseException extends NumberParseException {

        private static final long serialVersionUID = -1162983846751812292L;

        private final FloatParser<?> parser;

        /**
         * Construct a new float parse exception
         *
         * @param input          String input
         * @param min            Minimum value
         * @param max            Maximum value
         * @param commandContext Command context
         * @deprecated use {@link #FloatParseException(String, FloatParser, CommandContext)} instead
         */
        @Deprecated
        public FloatParseException(
                final @NonNull String input,
                final float min,
                final float max,
                final @NonNull CommandContext<?> commandContext
        ) {
            this(input, new FloatParser<>(min, max), commandContext);
        }

        /**
         * Create a new {@link FloatParseException}.
         *
         * @param input          input string
         * @param parser         float parser
         * @param commandContext command context
         * @since 1.5.0
         */
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
