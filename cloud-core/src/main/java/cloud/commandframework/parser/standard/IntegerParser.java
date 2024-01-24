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

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.NumberParseException;
import cloud.commandframework.parser.ArgumentParseResult;
import cloud.commandframework.parser.ParserDescriptor;
import cloud.commandframework.types.range.IntRange;
import cloud.commandframework.types.range.Range;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public final class IntegerParser<C> extends NumberParser<C, Integer, IntRange> implements BlockingSuggestionProvider.Strings<C> {

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
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Integer> integerParser() {
        return integerParser(IntegerParser.DEFAULT_MINIMUM, IntegerParser.DEFAULT_MAXIMUM);
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
    public static <C> @NonNull ParserDescriptor<C, Integer> integerParser(
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
    public static <C> @NonNull ParserDescriptor<C, Integer> integerParser(
            final int minValue,
            final int maxValue
    ) {
        return ParserDescriptor.of(new IntegerParser<>(minValue, maxValue), Integer.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #integerParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Integer> integerComponent() {
        return CommandComponent.<C, Integer>builder().parser(integerParser());
    }

    /**
     * Construct a new integer parser
     *
     * @param min Minimum acceptable value
     * @param max Maximum acceptable value
     */
    public IntegerParser(final int min, final int max) {
        super(Range.intRange(min, max));
    }

    /**
     * Returns integer suggestions. This supports both positive and negative numbers
     *
     * @param range accepted range
     * @param input input
     * @return list of suggestions
     */
    @SuppressWarnings("MixedMutabilityReturnType")
    public static @NonNull List<@NonNull String> getSuggestions(
            final @NonNull Range<? extends Number> range,
            final @NonNull CommandInput input
    ) {
        final Set<Long> numbers = new TreeSet<>();
        final String token = input.peekString();

        try {
            final long inputNum = Long.parseLong(token.equals("-") ? "-0" : token.isEmpty() ? "0" : token);
            final long inputNumAbsolute = Math.abs(inputNum);

            final long min = range.min().longValue();
            final long max = range.max().longValue();

            numbers.add(inputNumAbsolute); /* It's a valid number, so we suggest it */
            for (int i = 0; i < MAX_SUGGESTIONS_INCREMENT
                    && (inputNum * NUMBER_SHIFT_MULTIPLIER) + i <= max; i++) {
                numbers.add((inputNumAbsolute * NUMBER_SHIFT_MULTIPLIER) + i);
            }

            final List<String> suggestions = new LinkedList<>();
            for (long number : numbers) {
                if (token.startsWith("-")) {
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
        if (!commandInput.isValidInteger(this.range())) {
            return ArgumentParseResult.failure(new IntegerParseException(
                    commandInput.peekString(),
                    this,
                    commandContext
            ));
        }
        return ArgumentParseResult.success(commandInput.readInteger());
    }

    @Override
    public boolean hasMax() {
        return this.range().maxInt() != DEFAULT_MAXIMUM;
    }

    @Override
    public boolean hasMin() {
        return this.range().minInt() != DEFAULT_MINIMUM;
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        return getSuggestions(this.range(), input);
    }


    @API(status = API.Status.STABLE)
    public static final class IntegerParseException extends NumberParseException {

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
            super(input, parser, commandContext);
        }

        @Override
        public @NonNull String numberType() {
            return "integer";
        }
    }
}
