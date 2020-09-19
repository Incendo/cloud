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
import java.util.Queue;

@SuppressWarnings("unused")
public final class FloatArgument<C> extends CommandArgument<C, Float> {

    private final float min;
    private final float max;

    private FloatArgument(final boolean required,
                          @Nonnull final String name,
                          final float min,
                          final float max,
                          final String defaultValue) {
        super(required, name, new FloatParser<>(min, max), defaultValue, Float.class);
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
    public static <C> Builder<C> newBuilder(@Nonnull final String name) {
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
    public static <C> CommandArgument<C, Float> required(@Nonnull final String name) {
        return FloatArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, Float> optional(@Nonnull final String name) {
        return FloatArgument.<C>newBuilder(name).asOptional().build();
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
    public static <C> CommandArgument<C, Float> optional(@Nonnull final String name,
                                                         final float defaultNum) {
        return FloatArgument.<C>newBuilder(name).asOptionalWithDefault(Float.toString(defaultNum)).build();
    }

    /**
     * Get the minimum accepted float that could have been parsed
     *
     * @return Minimum float
     */
    public float getMin() {
        return this.min;
    }

    /**
     * Get the maximum accepted float that could have been parsed
     *
     * @return Maximum float
     */
    public float getMax() {
        return this.max;
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, Float> {

        private float min = Float.MIN_VALUE;
        private float max = Float.MAX_VALUE;

        protected Builder(@Nonnull final String name) {
            super(Float.class, name);
        }

        /**
         * Set a minimum value
         *
         * @param min Minimum value
         * @return Builder instance
         */
        @Nonnull
        public Builder<C> withMin(final int min) {
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
        public Builder<C> withMax(final int max) {
            this.max = max;
            return this;
        }

        /**
         * Builder a new float argument
         *
         * @return Constructed argument
         */
        @Nonnull
        @Override
        public FloatArgument<C> build() {
            return new FloatArgument<>(this.isRequired(), this.getName(), this.min, this.max, this.getDefaultValue());
        }

    }

    public static final class FloatParser<C> implements ArgumentParser<C, Float> {

        private final float min;
        private final float max;

        /**
         * Construct a new float parser
         *
         * @param min Minimum value
         * @param max Maximum value
         */
        public FloatParser(final float min, final float max) {
            this.min = min;
            this.max = max;
        }

        @Nonnull
        @Override
        public ArgumentParseResult<Float> parse(
                @Nonnull final CommandContext<C> commandContext,
                @Nonnull final Queue<String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NullPointerException("No input was provided"));
            }
            try {
                final float value = Float.parseFloat(input);
                if (value < this.min || value > this.max) {
                    return ArgumentParseResult.failure(new FloatParseException(input, this.min, this.max));
                }
                inputQueue.remove();
                return ArgumentParseResult.success(value);
            } catch (final Exception e) {
                return ArgumentParseResult.failure(new FloatParseException(input, this.min, this.max));
            }
        }

        @Override
        public boolean isContextFree() {
            return true;
        }
    }


    public static final class FloatParseException extends NumberParseException {

        /**
         * Construct a new float parse exception
         *
         * @param input String input
         * @param min   Minimum value
         * @param max   Maximum value
         */
        public FloatParseException(@Nonnull final String input, final float min, final float max) {
            super(input, min, max);
        }

        @Override
        public boolean hasMin() {
            return this.getMin().floatValue() != Float.MIN_VALUE;
        }

        @Override
        public boolean hasMax() {
            return this.getMax().floatValue() != Float.MAX_VALUE;
        }

        @Override
        @Nonnull
        public String getNumberType() {
            return "float";
        }

    }

}
