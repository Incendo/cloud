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

import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public final class ByteArgument<C> extends CommandArgument<C, Byte> {

    private final byte min;
    private final byte max;

    private ByteArgument(
            final boolean required,
            final @NonNull String name,
            final byte min,
            final byte max,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new ByteParser<>(min, max), defaultValue, Byte.class, suggestionsProvider, defaultDescription);
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
    public static <C> @NonNull CommandArgument<C, Byte> of(final @NonNull String name) {
        return ByteArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Byte> optional(final @NonNull String name) {
        return ByteArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new required command argument with a default value
     *
     * @param name       Argument name
     * @param defaultNum Default num
     * @param <C>        Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Byte> optional(
            final @NonNull String name,
            final byte defaultNum
    ) {
        return ByteArgument.<C>newBuilder(name).asOptionalWithDefault(Byte.toString(defaultNum)).build();
    }

    /**
     * Get the minimum accepted byteeger that could have been parsed
     *
     * @return Minimum byteeger
     */
    public byte getMin() {
        return this.min;
    }

    /**
     * Get the maximum accepted byteeger that could have been parsed
     *
     * @return Maximum byteeger
     */
    public byte getMax() {
        return this.max;
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, Byte> {

        private byte min = Byte.MIN_VALUE;
        private byte max = Byte.MAX_VALUE;

        private Builder(final @NonNull String name) {
            super(Byte.class, name);
        }

        /**
         * Set a minimum value
         *
         * @param min Minimum value
         * @return Builder instance
         */
        public @NonNull Builder<C> withMin(final byte min) {
            this.min = min;
            return this;
        }

        /**
         * Set a maximum value
         *
         * @param max Maximum value
         * @return Builder instance
         */
        public @NonNull Builder<C> withMax(final byte max) {
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
            return new ByteArgument<>(this.isRequired(), this.getName(), this.min, this.max,
                    this.getDefaultValue(), this.getSuggestionsProvider(), this.getDefaultDescription()
            );
        }

    }

    public static final class ByteParser<C> implements ArgumentParser<C, Byte> {

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
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        ByteParser.class,
                        commandContext
                ));
            }
            try {
                final byte value = Byte.parseByte(input);
                if (value < this.min || value > this.max) {
                    return ArgumentParseResult.failure(
                            new ByteParseException(
                                    input,
                                    this.min,
                                    this.max,
                                    commandContext
                            ));
                }
                inputQueue.remove();
                return ArgumentParseResult.success(value);
            } catch (final Exception e) {
                return ArgumentParseResult.failure(
                        new ByteParseException(
                                input,
                                this.min,
                                this.max,
                                commandContext
                        ));
            }
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

    }


    /**
     * Byte parse exception
     */
    public static final class ByteParseException extends NumberParseException {

        private static final long serialVersionUID = -4724241304872989208L;

        /**
         * Construct a new byte parse exception
         *
         * @param input   String input
         * @param min     Minimum value
         * @param max     Maximum value
         * @param context Command context
         */
        public ByteParseException(
                final @NonNull String input,
                final byte min,
                final byte max,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    input,
                    min,
                    max,
                    ByteParser.class,
                    context
            );
        }

        @Override
        public boolean hasMin() {
            return this.getMin().byteValue() != Byte.MIN_VALUE;
        }

        @Override
        public boolean hasMax() {
            return this.getMax().byteValue() != Byte.MAX_VALUE;
        }

        @Override
        public @NonNull String getNumberType() {
            return "byte";
        }

    }

}
