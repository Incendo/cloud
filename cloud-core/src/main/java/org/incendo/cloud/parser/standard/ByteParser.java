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
package org.incendo.cloud.parser.standard;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.NumberParseException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.type.range.ByteRange;
import org.incendo.cloud.type.range.Range;

@API(status = API.Status.STABLE)
public final class ByteParser<C> extends NumberParser<C, Byte, ByteRange> implements BlockingSuggestionProvider.Strings<C> {

    /**
     * Constant for the default/unset minimum value.
     *
     */
    @API(status = API.Status.STABLE)
    public static final byte DEFAULT_MINIMUM = Byte.MIN_VALUE;

    /**
     * Constant for the default/unset maximum value.
     *
     */
    @API(status = API.Status.STABLE)
    public static final byte DEFAULT_MAXIMUM = Byte.MAX_VALUE;

    /**
     * Creates a new byte parser using {@link ByteParser#DEFAULT_MINIMUM} and {@link ByteParser#DEFAULT_MAXIMUM} as
     * the limits.
     *
     * @param <C> the command sender type
     * @return the created parser
     */
    @API(status = API.Status.STABLE)
    public static <C> @NonNull ParserDescriptor<C, Byte> byteParser() {
        return byteParser(ByteParser.DEFAULT_MINIMUM, ByteParser.DEFAULT_MAXIMUM);
    }

    /**
     * Creates a new byte parser using {@link ByteParser#DEFAULT_MAXIMUM} as the maximum value.
     *
     * @param <C>      the command sender type
     * @param minValue the minimum value accepted by the parser
     * @return the created parser
     */
    @API(status = API.Status.STABLE)
    public static <C> @NonNull ParserDescriptor<C, Byte> byteParser(
            final byte minValue
    ) {
        return ParserDescriptor.of(new ByteParser<>(minValue, ByteParser.DEFAULT_MAXIMUM), Byte.class);
    }

    /**
     * Creates a new byte parser.
     *
     * @param <C>      the command sender type
     * @param minValue the minimum value accepted by the parser
     * @param maxValue the maximum value accepted by the parser
     * @return the created parser
     */
    @API(status = API.Status.STABLE)
    public static <C> @NonNull ParserDescriptor<C, Byte> byteParser(
            final byte minValue,
            final byte maxValue
    ) {
        return ParserDescriptor.of(new ByteParser<>(minValue, maxValue), Byte.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #byteParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     */
    @API(status = API.Status.STABLE)
    public static <C> CommandComponent.@NonNull Builder<C, Byte> byteComponent() {
        return CommandComponent.<C, Byte>builder().parser(byteParser());
    }

    /**
     * Construct a new byte parser
     *
     * @param min Minimum acceptable value
     * @param max Maximum acceptable value
     */
    public ByteParser(final byte min, final byte max) {
        super(Range.byteRange(min, max));
    }

    @Override
    public @NonNull ArgumentParseResult<Byte> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        if (!commandInput.isValidByte(this.range())) {
            return ArgumentParseResult.failure(new ByteParseException(
                    commandInput.peekString(),
                    this,
                    commandContext
            ));
        }
        return ArgumentParseResult.success(commandInput.readByte());
    }

    @Override
    public boolean hasMax() {
        return this.range().maxByte() != DEFAULT_MAXIMUM;
    }

    @Override
    public boolean hasMin() {
        return this.range().minByte() != DEFAULT_MINIMUM;
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        return IntegerParser.getSuggestions(this.range(), input);
    }


    @API(status = API.Status.STABLE)
    public static final class ByteParseException extends NumberParseException {

        /**
         * Create a new {@link ByteParseException}.
         *
         * @param input          input string
         * @param parser         byte parser
         * @param commandContext command context
         */
        @API(status = API.Status.STABLE)
        public ByteParseException(
                final @NonNull String input,
                final @NonNull ByteParser<?> parser,
                final @NonNull CommandContext<?> commandContext
        ) {
            super(input, parser, commandContext);
        }

        @Override
        public @NonNull String numberType() {
            return "byte";
        }
    }
}
