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

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.NumberParseException;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@API(status = API.Status.STABLE)
public final class IntegerParser<C> implements ArgumentParser<C, Integer> {

    /**
     * Constant for the default/unset minimum value.
     *
     * @since 1.5.0
     */
    @API(status = API.Status.STABLE, since = "1.5.0")
    public static final int DEFAULT_MINIMUM = Integer.MIN_VALUE;

    /**
     * Constant for the default/unset maximum value.
     *
     * @since 1.5.0
     */
    @API(status = API.Status.STABLE, since = "1.5.0")
    public static final int DEFAULT_MAXIMUM = Integer.MAX_VALUE;

    private static final int MAX_SUGGESTIONS_INCREMENT = 10;
    private static final int NUMBER_SHIFT_MULTIPLIER = 10;

    /**
     * Creates a new integer parser using {@link IntegerParser#DEFAULT_MINIMUM} and {@link IntegerParser#DEFAULT_MAXIMUM} as
     * the limits.
     *
     * @param <C> the command sender type
     * @return the created parser
     * @since 2.0.0
     */
    public static <C> @NonNull ParserDescriptor<C, Integer> integer() {
        return integer(IntegerParser.DEFAULT_MINIMUM, IntegerParser.DEFAULT_MAXIMUM);
    }

    /**
     * Creates a new integer parser using {@link IntegerParser#DEFAULT_MAXIMUM} as the maximum value.
     *
     * @param <C>      the command sender type
     * @param minValue the minimum value accepted by the parser
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Integer> integer(
            final int minValue
    ) {
        return ParserDescriptor.of(new IntegerParser<>(minValue, IntegerParser.DEFAULT_MAXIMUM), Integer.class);
    }

    /**
     * Creates a new integer parser.
     *
     * @param <C>      the command sender type
     * @param minValue the minimum value accepted by the parser
     * @param maxValue the maximum value accepted by the parser
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Integer> integer(
            final int minValue,
            final int maxValue
    ) {
        return ParserDescriptor.of(new IntegerParser<>(minValue, maxValue), Integer.class);
    }

    private final int min;
    private final int max;

    /**
     * Construct a new integer parser
     *
     * @param min Minimum acceptable value
     * @param max Maximum acceptable value
     */
    public IntegerParser(final int min, final int max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Get integer suggestions. This supports both positive and negative numbers
     *
     * @param min   Minimum value
     * @param max   Maximum value
     * @param input Input
     * @return List of suggestions
     */
    @SuppressWarnings("MixedMutabilityReturnType")
    public static @NonNull List<@NonNull String> getSuggestions(
            final long min,
            final long max,
            final @NonNull String input
    ) {
        final Set<Long> numbers = new TreeSet<>();

        try {
            final long inputNum = Long.parseLong(input.equals("-") ? "-0" : input.isEmpty() ? "0" : input);
            final long inputNumAbsolute = Math.abs(inputNum);

            numbers.add(inputNumAbsolute); /* It's a valid number, so we suggest it */
            for (int i = 0; i < MAX_SUGGESTIONS_INCREMENT
                    && (inputNum * NUMBER_SHIFT_MULTIPLIER) + i <= max; i++) {
                numbers.add((inputNumAbsolute * NUMBER_SHIFT_MULTIPLIER) + i);
            }

            final List<String> suggestions = new LinkedList<>();
            for (long number : numbers) {
                if (input.startsWith("-")) {
                    number = -number; /* Preserve sign */
                }
                if (number < min || number > max) {
                    continue;
                }
                suggestions.add(String.valueOf(number));
            }

            return suggestions;
        } catch (final Exception ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    public @NonNull ArgumentParseResult<Integer> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        if (!commandInput.isValidInteger(this.min, this.max)) {
            return ArgumentParseResult.failure(new IntegerParseException(
                    commandInput.peekString(),
                    this,
                    commandContext
            ));
        }
        return ArgumentParseResult.success(commandInput.readInteger());
    }

    /**
     * Get the minimum value accepted by this parser
     *
     * @return Min value
     */
    public int getMin() {
        return this.min;
    }

    /**
     * Get the maximum value accepted by this parser
     *
     * @return Max value
     */
    public int getMax() {
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
    public @NonNull List<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull String input
    ) {
        return getSuggestions(this.min, this.max, input);
    }


    @API(status = API.Status.STABLE)
    public static final class IntegerParseException extends NumberParseException {

        private static final long serialVersionUID = -6933923056628373853L;

        private final IntegerParser<?> parser;

        /**
         * Create a new {@link IntegerParseException}.
         *
         * @param input          input string
         * @param parser         integer parser
         * @param commandContext command context
         * @since 1.5.0
         */
        @API(status = API.Status.STABLE, since = "1.5.0")
        public IntegerParseException(
                final @NonNull String input,
                final @NonNull IntegerParser<?> parser,
                final @NonNull CommandContext<?> commandContext
        ) {
            super(input, parser.min, parser.max, IntegerParser.class, commandContext);
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
            return "integer";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final IntegerParseException that = (IntegerParseException) o;
            return this.parser.equals(that.parser);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.parser);
        }
    }
}
