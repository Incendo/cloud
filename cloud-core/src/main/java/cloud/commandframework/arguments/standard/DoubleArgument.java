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
public final class DoubleArgument<C> extends CommandArgument<C, Double> {

    private final double min;
    private final double max;

    private DoubleArgument(
            final boolean required,
            final @NonNull String name,
            final double min,
            final double max,
            final String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new DoubleParser<>(min, max), defaultValue, Double.class, suggestionsProvider, defaultDescription);
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
     * Create a new required {@link DoubleArgument}.
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Double> of(final @NonNull String name) {
        return DoubleArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional {@link DoubleArgument}.
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Double> optional(final @NonNull String name) {
        return DoubleArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new optional {@link DoubleArgument} with the specified default value.
     *
     * @param name       Argument name
     * @param defaultNum Default num
     * @param <C>        Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Double> optional(
            final @NonNull String name,
            final double defaultNum
    ) {
        return DoubleArgument.<C>newBuilder(name).asOptionalWithDefault(defaultNum).build();
    }

    /**
     * Get the minimum accepted double that could have been parsed
     *
     * @return Minimum double
     */
    public double getMin() {
        return this.min;
    }

    /**
     * Get the maximum accepted double that could have been parsed
     *
     * @return Maximum double
     */
    public double getMax() {
        return this.max;
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, Double> {

        private double min = DoubleParser.DEFAULT_MINIMUM;
        private double max = DoubleParser.DEFAULT_MAXIMUM;

        private Builder(final @NonNull String name) {
            super(Double.class, name);
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
        public @NonNull Builder<C> asOptionalWithDefault(final double defaultValue) {
            return (Builder<C>) this.asOptionalWithDefault(Double.toString(defaultValue));
        }

        /**
         * Builder a new double argument
         *
         * @return Constructed argument
         */
        @Override
        public @NonNull DoubleArgument<C> build() {
            return new DoubleArgument<>(this.isRequired(), this.getName(), this.min, this.max,
                    this.getDefaultValue(), this.getSuggestionsProvider(), this.getDefaultDescription()
            );
        }

    }

    public static final class DoubleParser<C> implements ArgumentParser<C, Double> {

        /**
         * Constant for the default/unset minimum value.
         *
         * @since 1.5.0
         */
        public static final double DEFAULT_MINIMUM = Double.NEGATIVE_INFINITY;

        /**
         * Constant for the default/unset maximum value.
         *
         * @since 1.5.0
         */
        public static final double DEFAULT_MAXIMUM = Double.POSITIVE_INFINITY;

        private final double min;
        private final double max;

        /**
         * Construct a new double parser
         *
         * @param min Minimum value
         * @param max Maximum value
         */
        public DoubleParser(final double min, final double max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public @NonNull ArgumentParseResult<Double> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(DoubleParser.class, commandContext));
            }
            try {
                final double value = Double.parseDouble(input);
                if (value < this.min || value > this.max) {
                    return ArgumentParseResult.failure(new DoubleParseException(input, this, commandContext));
                }
                inputQueue.remove();
                return ArgumentParseResult.success(value);
            } catch (final Exception e) {
                return ArgumentParseResult.failure(new DoubleParseException(input, this, commandContext));
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
        public double getMax() {
            return this.max;
        }

        /**
         * Get the min value
         *
         * @return Min value
         */
        public double getMin() {
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
    public static final class DoubleParseException extends NumberParseException {

        private static final long serialVersionUID = 1764554911581976586L;

        private final DoubleParser<?> parser;

        /**
         * Construct a new double parse exception
         *
         * @param input          String input
         * @param min            Minimum value
         * @param max            Maximum value
         * @param commandContext Command context
         * @deprecated use {@link #DoubleParseException(String, DoubleParser, CommandContext)} instead
         */
        @Deprecated
        public DoubleParseException(
                final @NonNull String input,
                final double min,
                final double max,
                final @NonNull CommandContext<?> commandContext
        ) {
            this(input, new DoubleParser<>(min, max), commandContext);
        }

        /**
         * Create a new {@link DoubleParseException}.
         *
         * @param input          input string
         * @param parser         double parser
         * @param commandContext command context
         * @since 1.5.0
         */
        public DoubleParseException(
                final @NonNull String input,
                final @NonNull DoubleParser<?> parser,
                final @NonNull CommandContext<?> commandContext
        ) {
            super(input, parser.min, parser.max, DoubleParser.class, commandContext);
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
            return "double";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final DoubleParseException that = (DoubleParseException) o;
            return this.parser.equals(that.parser);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.parser);
        }

    }

}
