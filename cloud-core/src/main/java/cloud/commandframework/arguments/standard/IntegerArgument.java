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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public final class IntegerArgument<C> extends CommandArgument<C, Integer> {

    private static final int MAX_SUGGESTIONS_INCREMENT = 10;
    private static final int NUMBER_SHIFT_MULTIPLIER = 10;

    private final int min;
    private final int max;

    private IntegerArgument(
            final boolean required,
            final @NonNull String name,
            final int min,
            final int max,
            final String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new IntegerParser<>(min, max), defaultValue, Integer.class, suggestionsProvider, defaultDescription);
        this.min = min;
        this.max = max;
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> @NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Integer> of(final @NonNull String name) {
        return IntegerArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Integer> optional(final @NonNull String name) {
        return IntegerArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new required command argument with a default value
     *
     * @param name       Argument name
     * @param defaultNum Default num
     * @param <C>        Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Integer> optional(
            final @NonNull String name,
            final int defaultNum
    ) {
        return IntegerArgument.<C>newBuilder(name).asOptionalWithDefault(Integer.toString(defaultNum)).build();
    }

    /**
     * Get the minimum accepted integer that could have been parsed
     *
     * @return Minimum integer
     */
    public int getMin() {
        return this.min;
    }

    /**
     * Get the maximum accepted integer that could have been parsed
     *
     * @return Maximum integer
     */
    public int getMax() {
        return this.max;
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, Integer> {

        private int min = Integer.MIN_VALUE;
        private int max = Integer.MAX_VALUE;

        private Builder(final @NonNull String name) {
            super(Integer.class, name);
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
         * Builder a new integer argument
         *
         * @return Constructed argument
         */
        @Override
        public @NonNull IntegerArgument<C> build() {
            return new IntegerArgument<>(this.isRequired(), this.getName(), this.min, this.max,
                    this.getDefaultValue(), this.getSuggestionsProvider(), this.getDefaultDescription()
            );
        }

    }

    public static final class IntegerParser<C> implements ArgumentParser<C, Integer> {

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
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        IntegerParser.class,
                        commandContext
                ));
            }
            try {
                final int value = Integer.parseInt(input);
                if (value < this.min || value > this.max) {
                    return ArgumentParseResult.failure(new IntegerParseException(
                            input,
                            this.min,
                            this.max,
                            commandContext
                    ));
                }
                inputQueue.remove();
                return ArgumentParseResult.success(value);
            } catch (final Exception e) {
                return ArgumentParseResult.failure(new IntegerParseException(
                        input,
                        this.min,
                        this.max,
                        commandContext
                ));
            }
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

        @Override
        public boolean isContextFree() {
            return true;
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return getSuggestions(this.min, this.max, input);
        }

    }


    public static final class IntegerParseException extends NumberParseException {

        private static final long serialVersionUID = -6933923056628373853L;

        /**
         * Construct a new integer parse exception
         *
         * @param input          String input
         * @param min            Minimum value
         * @param max            Maximum value
         * @param commandContext Command context
         */
        public IntegerParseException(
                final @NonNull String input,
                final int min,
                final int max,
                final @NonNull CommandContext<?> commandContext
        ) {
            super(
                    input,
                    min,
                    max,
                    IntegerParser.class,
                    commandContext
            );
        }

        @Override
        public boolean hasMin() {
            return this.getMin().intValue() != Integer.MIN_VALUE;
        }

        @Override
        public boolean hasMax() {
            return this.getMax().intValue() != Integer.MAX_VALUE;
        }

        @Override
        public @NonNull String getNumberType() {
            return "integer";
        }

    }

}
