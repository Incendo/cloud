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

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.NumberParseException;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;

@SuppressWarnings("unused")
@API(status = API.Status.STABLE)
public final class ByteArgument<C> extends CommandArgument<C, Byte> {

    private final byte min;
    private final byte max;

    private ByteArgument(
            final @NonNull String name,
            final byte min,
            final byte max,
            final @Nullable SuggestionProvider<C> suggestionProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(name, new ByteParser<>(min, max), Byte.class, suggestionProvider, defaultDescription);
        this.min = min;
        this.max = max;
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return new {@link Builder}
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     * @deprecated prefer {@link #builder(String)}
     */
    @API(status = API.Status.DEPRECATED, since = "1.8.0")
    @Deprecated
    public static <C> @NonNull Builder<C> newBuilder(final @NonNull String name) {
        return builder(name);
    }

    /**
     * Create a new required {@link ByteArgument}.
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Byte> of(final @NonNull String name) {
        return ByteArgument.<C>builder(name).build();
    }

    /**
     * Get the minimum accepted byte that could have been parsed
     *
     * @return Minimum byte
     */
    public byte getMin() {
        return this.min;
    }

    /**
     * Get the maximum accepted byte that could have been parsed
     *
     * @return Maximum byte
     */
    public byte getMax() {
        return this.max;
    }


    @API(status = API.Status.STABLE)
    public static final class Builder<C> extends CommandArgument.TypedBuilder<C, Byte, Builder<C>> {

        private byte min = ByteParser.DEFAULT_MINIMUM;
        private byte max = ByteParser.DEFAULT_MAXIMUM;

        private Builder(final @NonNull String name) {
            super(Byte.class, name);
        }

        /**
         * Set a minimum value
         *
         * @param min Minimum value
         * @return Builder instance
         */
        public @NonNull @This Builder<C> withMin(final byte min) {
            this.min = min;
            return this;
        }

        /**
         * Set a maximum value
         *
         * @param max Maximum value
         * @return Builder instance
         */
        public @NonNull @This Builder<C> withMax(final byte max) {
            this.max = max;
            return this;
        }

        /**
         * Builder a new byte argument
         *
         * @return Constructed argument
         */
        @Override
        public @NonNull ByteArgument<C> build() {
            return new ByteArgument<>(this.getName(), this.min, this.max, this.suggestionProvider(), this.getDefaultDescription());
        }
    }


    @API(status = API.Status.STABLE)
    public static final class ByteParser<C> implements ArgumentParser<C, Byte> {

        /**
         * Constant for the default/unset minimum value.
         *
         * @since 1.5.0
         */
        @API(status = API.Status.STABLE, since = "1.5.0")
        public static final byte DEFAULT_MINIMUM = Byte.MIN_VALUE;

        /**
         * Constant for the default/unset maximum value.
         *
         * @since 1.5.0
         */
        @API(status = API.Status.STABLE, since = "1.5.0")
        public static final byte DEFAULT_MAXIMUM = Byte.MAX_VALUE;

        private final byte min;
        private final byte max;

        /**
         * Construct a new byte parser
         *
         * @param min Minimum value
         * @param max Maximum value
         */
        public ByteParser(final byte min, final byte max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public @NonNull ArgumentParseResult<Byte> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(ByteParser.class, commandContext));
            }
            try {
                final byte value = Byte.parseByte(input);
                if (value < this.min || value > this.max) {
                    return ArgumentParseResult.failure(new ByteParseException(input, this, commandContext));
                }
                inputQueue.remove();
                return ArgumentParseResult.success(value);
            } catch (final Exception e) {
                return ArgumentParseResult.failure(new ByteParseException(input, this, commandContext));
            }
        }

        @Override
        public boolean isContextFree() {
            return true;
        }

        @Override
        public @NonNull List<@NonNull Suggestion> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return IntegerArgument.IntegerParser.getSuggestions(this.min, this.max, input, Long::byteValue);
        }

        /**
         * Get the max value
         *
         * @return Max value
         */
        public byte getMax() {
            return this.max;
        }

        /**
         * Get the min value
         *
         * @return Min value
         */
        public byte getMin() {
            return this.min;
        }

        /**
         * Get whether this parser has a maximum set.
         * This will compare the parser's maximum to {@link #DEFAULT_MAXIMUM}.
         *
         * @return whether the parser has a maximum set
         * @since 1.5.0
         */
        @API(status = API.Status.STABLE, since = "1.5.0")
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
        @API(status = API.Status.STABLE, since = "1.5.0")
        public boolean hasMin() {
            return this.min != DEFAULT_MINIMUM;
        }
    }


    /**
     * Byte parse exception
     */
    @SuppressWarnings("serial")
    public static final class ByteParseException extends NumberParseException {

        private static final long serialVersionUID = -4724241304872989208L;

        private final ByteParser<?> parser;

        /**
         * Construct a new byte parse exception
         *
         * @param input   String input
         * @param min     Minimum value
         * @param max     Maximum value
         * @param context Command context
         * @deprecated use {@link #ByteParseException(String, ByteParser, CommandContext)} instead
         */
        @Deprecated
        @API(status = API.Status.DEPRECATED, since = "1.5.0")
        public ByteParseException(
                final @NonNull String input,
                final byte min,
                final byte max,
                final @NonNull CommandContext<?> context
        ) {
            this(input, new ByteParser<>(min, max), context);
        }

        /**
         * Create a new {@link ByteParseException}.
         *
         * @param input   input string
         * @param parser  byte parser
         * @param context command context
         * @since 1.5.0
         */
        @API(status = API.Status.STABLE, since = "1.5.0")
        public ByteParseException(
                final @NonNull String input,
                final @NonNull ByteParser<?> parser,
                final @NonNull CommandContext<?> context
        ) {
            super(input, parser.min, parser.max, ByteParser.class, context);
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
            return "byte";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final ByteParseException that = (ByteParseException) o;
            return this.parser.equals(that.parser);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.parser);
        }
    }
}
