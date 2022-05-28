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
public final class LongArgument<C> extends CommandArgument<C, Long> {

    private final long min;
    private final long max;

    private LongArgument(
            final boolean required,
            final @NonNull String name,
            final long min,
            final long max,
            final String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new LongParser<>(min, max), defaultValue, Long.class, suggestionsProvider, defaultDescription);
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
    public static <C> LongArgument.@NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required {@link LongArgument}.
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Long> of(final @NonNull String name) {
        return LongArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional {@link LongArgument}.
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Long> optional(final @NonNull String name) {
        return LongArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new optional {@link LongArgument} with the specified default value.
     *
     * @param name       Argument name
     * @param defaultNum Default num
     * @param <C>        Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Long> optional(
            final @NonNull String name,
            final long defaultNum
    ) {
        return LongArgument.<C>newBuilder(name).asOptionalWithDefault(defaultNum).build();
    }

    /**
     * Get the minimum accepted long that could have been parsed
     *
     * @return Minimum long
     */
    public long getMin() {
        return this.min;
    }

    /**
     * Get the maximum accepted long that could have been parsed
     *
     * @return Maximum long
     */
    public long getMax() {
        return this.max;
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, Long> {

        private long min = LongParser.DEFAULT_MINIMUM;
        private long max = LongParser.DEFAULT_MAXIMUM;

        private Builder(final @NonNull String name) {
            super(Long.class, name);
        }

        /**
         * Set a minimum value
         *
         * @param min Minimum value
         * @return Builder instance
         */
        public @NonNull Builder<C> withMin(final long min) {
            this.min = min;
            return this;
        }

        /**
         * Set a maximum value
         *
         * @param max Maximum value
         * @return Builder instance
         */
        public @NonNull Builder<C> withMax(final long max) {
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
        public @NonNull Builder<C> asOptionalWithDefault(final long defaultValue) {
            return (Builder<C>) this.asOptionalWithDefault(Long.toString(defaultValue));
        }

        @Override
        public @NonNull LongArgument<C> build() {
            return new LongArgument<>(this.isRequired(), this.getName(), this.min,
                    this.max, this.getDefaultValue(), this.getSuggestionsProvider(), this.getDefaultDescription()
            );
        }

    }

    public static final class LongParser<C> implements ArgumentParser<C, Long> {

        /**
         * Constant for the default/unset minimum value.
         *
         * @since 1.5.0
         */
        public static final long DEFAULT_MINIMUM = Long.MIN_VALUE;

        /**
         * Constant for the default/unset maximum value.
         *
         * @since 1.5.0
         */
        public static final long DEFAULT_MAXIMUM = Long.MAX_VALUE;

        private final long min;
        private final long max;

        /**
         * Construct a new long parser
         *
         * @param min Minimum acceptable value
         * @param max Maximum acceptable value
         */
        public LongParser(final long min, final long max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public @NonNull ArgumentParseResult<Long> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(LongParser.class, commandContext));
            }
            try {
                final long value = Long.parseLong(input);
                if (value < this.min || value > this.max) {
                    return ArgumentParseResult.failure(new LongParseException(input, this, commandContext));
                }
                inputQueue.remove();
                return ArgumentParseResult.success(value);
            } catch (final Exception e) {
                return ArgumentParseResult.failure(new LongParseException(input, this, commandContext));
            }
        }

        /**
         * Get the minimum value accepted by this parser
         *
         * @return Min value
         */
        public long getMin() {
            return this.min;
        }

        /**
         * Get the maximum value accepted by this parser
         *
         * @return Max value
         */
        public long getMax() {
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

        @Override
        public boolean isContextFree() {
            return true;
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return IntegerArgument.IntegerParser.getSuggestions(this.min, this.max, input);
        }

    }


    @SuppressWarnings("serial")
    public static final class LongParseException extends NumberParseException {

        private static final long serialVersionUID = 4366856282301198232L;

        private final LongParser<?> parser;

        /**
         * Construct a new long parse exception
         *
         * @param input          String input
         * @param min            Minimum value
         * @param max            Maximum value
         * @param commandContext Command context
         * @deprecated use {@link #LongParseException(String, LongParser, CommandContext)} instead
         */
        @Deprecated
        public LongParseException(
                final @NonNull String input,
                final long min,
                final long max,
                final @NonNull CommandContext<?> commandContext
        ) {
            this(input, new LongParser<>(min, max), commandContext);
        }

        /**
         * Create a new {@link LongParseException}.
         *
         * @param input          input string
         * @param parser         long parser
         * @param commandContext command context
         * @since 1.5.0
         */
        public LongParseException(
                final @NonNull String input,
                final @NonNull LongParser<?> parser,
                final @NonNull CommandContext<?> commandContext
        ) {
            super(input, parser.min, parser.max, LongParser.class, commandContext);
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
            return "long";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final LongParseException that = (LongParseException) o;
            return this.parser.equals(that.parser);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.parser);
        }

    }

}
