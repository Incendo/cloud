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
package cloud.commandframework.arguments.standard;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.NumberParseException;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public final class ShortParser<C> implements ArgumentParser<C, Short>, BlockingSuggestionProvider.Strings<C> {

    /**
     * Constant for the default/unset minimum value.
     *
     * @since 1.5.0
     */
    @API(status = API.Status.STABLE, since = "1.5.0")
    public static final short DEFAULT_MINIMUM = Short.MIN_VALUE;

    /**
     * Constant for the default/unset maximum value.
     *
     * @since 1.5.0
     */
    @API(status = API.Status.STABLE, since = "1.5.0")
    public static final short DEFAULT_MAXIMUM = Short.MAX_VALUE;

    /**
     * Creates a new short parser using {@link ShortParser#DEFAULT_MINIMUM} and {@link ShortParser#DEFAULT_MAXIMUM} as
     * the limits.
     *
     * @param <C> the command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Short> shortParser() {
        return shortParser(ShortParser.DEFAULT_MINIMUM, ShortParser.DEFAULT_MAXIMUM);
    }

    /**
     * Creates a new short parser using {@link ShortParser#DEFAULT_MAXIMUM} as the maximum value.
     *
     * @param <C>      the command sender type
     * @param minValue the minimum value accepted by the parser
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Short> shortParser(
            final short minValue
    ) {
        return ParserDescriptor.of(new ShortParser<>(minValue, ShortParser.DEFAULT_MAXIMUM), Short.class);
    }

    /**
     * Creates a new short parser.
     *
     * @param <C>      the command sender type
     * @param minValue the minimum value accepted by the parser
     * @param maxValue the maximum value accepted by the parser
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Short> shortParser(
            final short minValue,
            final short maxValue
    ) {
        return ParserDescriptor.of(new ShortParser<>(minValue, maxValue), Short.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #shortParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Short> shortComponent() {
        return CommandComponent.<C, Short>builder().parser(shortParser());
    }

    private final short min;
    private final short max;

    /**
     * Construct a new short parser
     *
     * @param min Minimum acceptable value
     * @param max Maximum acceptable value
     */
    public ShortParser(final short min, final short max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public @NonNull ArgumentParseResult<Short> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        if (!commandInput.isValidShort(this.min, this.max)) {
            return ArgumentParseResult.failure(new ShortParseException(
                    commandInput.peekString(),
                    this,
                    commandContext
            ));
        }
        return ArgumentParseResult.success(commandInput.readShort());
    }

    /**
     * Returns the minimum value accepted by this parser.
     *
     * @return min value
     */
    public short min() {
        return this.min;
    }

    /**
     * Returns the maximum value accepted by this parser.
     *
     * @return min value
     */
    public short max() {
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
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        return IntegerParser.getSuggestions(this.min, this.max, input);
    }


    @API(status = API.Status.STABLE)
    public static final class ShortParseException extends NumberParseException {


        private final ShortParser<?> parser;

        /**
         * Create a new {@link ShortParseException}.
         *
         * @param input          input string
         * @param parser         short parser
         * @param commandContext command context
         * @since 1.5.0
         */
        @API(status = API.Status.STABLE, since = "1.5.0")
        public ShortParseException(
                final @NonNull String input,
                final @NonNull ShortParser<?> parser,
                final @NonNull CommandContext<?> commandContext
        ) {
            super(input, parser.min, parser.max, ShortParser.class, commandContext);
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
        public @NonNull String numberType() {
            return "short";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final ShortParseException that = (ShortParseException) o;
            return this.parser.equals(that.parser);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.parser);
        }
    }
}
