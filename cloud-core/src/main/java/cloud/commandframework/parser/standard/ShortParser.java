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
import cloud.commandframework.types.range.Range;
import cloud.commandframework.types.range.ShortRange;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public final class ShortParser<C> extends NumberParser<C, Short, ShortRange> implements BlockingSuggestionProvider.Strings<C> {

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

    /**
     * Construct a new short parser
     *
     * @param min Minimum acceptable value
     * @param max Maximum acceptable value
     */
    public ShortParser(final short min, final short max) {
        super(Range.shortRange(min, max));
    }

    @Override
    public @NonNull ArgumentParseResult<Short> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        if (!commandInput.isValidShort(this.range())) {
            return ArgumentParseResult.failure(new ShortParseException(
                    commandInput.peekString(),
                    this,
                    commandContext
            ));
        }
        return ArgumentParseResult.success(commandInput.readShort());
    }

    @Override
    public boolean hasMax() {
        return this.range().maxShort() != DEFAULT_MAXIMUM;
    }

    @Override
    public boolean hasMin() {
        return this.range().minShort() != DEFAULT_MINIMUM;
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        return IntegerParser.getSuggestions(this.range(), input);
    }


    @API(status = API.Status.STABLE)
    public static final class ShortParseException extends NumberParseException {

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
            super(input, parser, commandContext);
        }

        @Override
        public @NonNull String numberType() {
            return "short";
        }
    }
}
