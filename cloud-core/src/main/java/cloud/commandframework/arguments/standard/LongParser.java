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
import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.NumberParseException;
import java.util.List;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public final class LongParser<C> implements ArgumentParser<C, Long>, BlockingSuggestionProvider.Strings<C> {

    /**
     * Constant for the default/unset minimum value.
     *
     * @since 1.5.0
     */
    @API(status = API.Status.STABLE, since = "1.5.0")
    public static final long DEFAULT_MINIMUM = Long.MIN_VALUE;

    /**
     * Constant for the default/unset maximum value.
     *
     * @since 1.5.0
     */
    @API(status = API.Status.STABLE, since = "1.5.0")
    public static final long DEFAULT_MAXIMUM = Long.MAX_VALUE;

    /**
     * Creates a new long parser using {@link LongParser#DEFAULT_MINIMUM} and {@link LongParser#DEFAULT_MAXIMUM} as
     * the limits.
     *
     * @param <C> the command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Long> longParser() {
        return longParser(LongParser.DEFAULT_MINIMUM, LongParser.DEFAULT_MAXIMUM);
    }

    /**
     * Creates a new long parser using {@link LongParser#DEFAULT_MAXIMUM} as the maximum value.
     *
     * @param <C>      the command sender type
     * @param minValue the minimum value accepted by the parser
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Long> longParser(
            final long minValue
    ) {
        return ParserDescriptor.of(new LongParser<>(minValue, LongParser.DEFAULT_MAXIMUM), Long.class);
    }

    /**
     * Creates a new long parser.
     *
     * @param <C>      the command sender type
     * @param minValue the minimum value accepted by the parser
     * @param maxValue the maximum value accepted by the parser
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Long> longParser(
            final long minValue,
            final long maxValue
    ) {
        return ParserDescriptor.of(new LongParser<>(minValue, maxValue), Long.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #longParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Long> longComponent() {
        return CommandComponent.<C, Long>builder().parser(longParser());
    }

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
            final @NonNull CommandInput commandInput
    ) {
        if (!commandInput.isValidLong(this.min, this.max)) {
            return ArgumentParseResult.failure(new LongParseException(
                    commandInput.peekString(),
                    this,
                    commandContext
            ));
        }
        return ArgumentParseResult.success(commandInput.readLong());
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
    public @NonNull List<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull String input
    ) {
        return IntegerParser.getSuggestions(this.min, this.max, input);
    }


    @API(status = API.Status.STABLE)
    public static final class LongParseException extends NumberParseException {

        private static final long serialVersionUID = 4366856282301198232L;

        private final LongParser<?> parser;

        /**
         * Create a new {@link LongParseException}.
         *
         * @param input          input string
         * @param parser         long parser
         * @param commandContext command context
         * @since 1.5.0
         */
        @API(status = API.Status.STABLE, since = "1.5.0")
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
