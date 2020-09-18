//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
package com.intellectualsites.commands.arguments.standard;

import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.arguments.parser.ArgumentParseResult;
import com.intellectualsites.commands.arguments.parser.ArgumentParser;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.exceptions.parsing.NumberParseException;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Queue;

@SuppressWarnings("unused")
public final class LongArgument<C> extends CommandArgument<C, Long> {

    private final long min;
    private final long max;

    private LongArgument(final boolean required,
                         @Nonnull final String name,
                         final long min,
                         final long max,
                         final String defaultValue) {
        super(required, name, new LongParser<>(min, max), defaultValue, Long.class);
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
    @Nonnull
    public static <C> LongArgument.Builder<C> newBuilder(@Nonnull final String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, Long> required(@Nonnull final String name) {
        return LongArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, Long> optional(@Nonnull final String name) {
        return LongArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new required command argument with a default value
     *
     * @param name       Argument name
     * @param defaultNum Default num
     * @param <C>        Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, Long> optional(@Nonnull final String name,
                                                                              final long defaultNum) {
        return LongArgument.<C>newBuilder(name).asOptionalWithDefault(Long.toString(defaultNum)).build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, Long> {

        private long min = Long.MIN_VALUE;
        private long max = Long.MAX_VALUE;

        protected Builder(@Nonnull final String name) {
            super(Long.class, name);
        }

        /**
         * Set a minimum value
         *
         * @param min Minimum value
         * @return Builder instance
         */
        @Nonnull
        public Builder<C> withMin(final long min) {
            this.min = min;
            return this;
        }

        /**
         * Set a maximum value
         *
         * @param max Maximum value
         * @return Builder instance
         */
        @Nonnull
        public Builder<C> withMax(final long max) {
            this.max = max;
            return this;
        }

        /**
         * Builder a new long argument
         *
         * @return Constructed argument
         */
        @Nonnull
        @Override
        public LongArgument<C> build() {
            return new LongArgument<>(this.isRequired(), this.getName(), this.min, this.max, this.getDefaultValue());
        }

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


    private static final class LongParser<C> implements ArgumentParser<C, Long> {

        private final long min;
        private final long max;

        private LongParser(final long min, final long max) {
            this.min = min;
            this.max = max;
        }

        @Nonnull
        @Override
        public ArgumentParseResult<Long> parse(
                @Nonnull final CommandContext<C> commandContext,
                @Nonnull final Queue<String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NullPointerException("No input was provided"));
            }
            try {
                final long value = Long.parseLong(input);
                if (value < this.min || value > this.max) {
                    return ArgumentParseResult.failure(new LongParseException(input, this.min, this.max));
                }
                inputQueue.remove();
                return ArgumentParseResult.success(value);
            } catch (final Exception e) {
                return ArgumentParseResult.failure(new LongParseException(input, this.min, this.max));
            }
        }

        @Override
        public boolean isContextFree() {
            return true;
        }

        @Nonnull
        @Override
        public List<String> suggestions(@Nonnull final CommandContext<C> commandContext,
                                        @Nonnull final String input) {
            return IntegerArgument.IntegerParser.getSuggestions(this.min, this.max, input);
        }

    }


    public static final class LongParseException extends NumberParseException {

        /**
         * Construct a new long parse exception
         *
         * @param input String input
         * @param min   Minimum value
         * @param max   Maximum value
         */
        public LongParseException(@Nonnull final String input, final long min, final long max) {
            super(input, min, max);
        }

        @Override
        public boolean hasMin() {
            return this.getMin().longValue() != Long.MIN_VALUE;
        }

        @Override
        public boolean hasMax() {
            return this.getMax().longValue() != Long.MAX_VALUE;
        }

        @Override
        @Nonnull
        public String getNumberType() {
            return "long";
        }

    }

}
