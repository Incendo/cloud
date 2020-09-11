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
package com.intellectualsites.commands.components.standard;

import com.intellectualsites.commands.components.CommandComponent;
import com.intellectualsites.commands.components.parser.ComponentParseResult;
import com.intellectualsites.commands.components.parser.ComponentParser;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.exceptions.parsing.NumberParseException;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import java.util.Queue;

@SuppressWarnings("unused")
public final class DoubleComponent<C extends CommandSender> extends CommandComponent<C, Double> {

    private final double min;
    private final double max;

    private DoubleComponent(final boolean required,
                            @Nonnull final String name,
                            final double min,
                            final double max,
                            final String defaultValue) {
        super(required, name, new DoubleParser<>(min, max), defaultValue);
        this.min = min;
        this.max = max;
    }

    /**
     * Create a new builder
     *
     * @param name Name of the component
     * @param <C>  Command sender type
     * @return Created builder
     */
    @Nonnull
    public static <C extends CommandSender> Builder<C> newBuilder(@Nonnull final String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    @Nonnull
    public static <C extends CommandSender> CommandComponent<C, Double> required(@Nonnull final String name) {
        return DoubleComponent.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    @Nonnull
    public static <C extends CommandSender> CommandComponent<C, Double> optional(@Nonnull final String name) {
        return DoubleComponent.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new required command component with a default value
     *
     * @param name       Component name
     * @param defaultNum Default num
     * @param <C>        Command sender type
     * @return Created component
     */
    @Nonnull
    public static <C extends CommandSender> CommandComponent<C, Double> optional(@Nonnull final String name,
                                                                                 final double defaultNum) {
        return DoubleComponent.<C>newBuilder(name).asOptionalWithDefault(Double.toString(defaultNum)).build();
    }


    public static final class Builder<C extends CommandSender> extends CommandComponent.Builder<C, Double> {

        private double min = Double.MIN_VALUE;
        private double max = Double.MAX_VALUE;

        protected Builder(@Nonnull final String name) {
            super(name);
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
         * Builder a new double component
         *
         * @return Constructed component
         */
        @Nonnull
        @Override
        public DoubleComponent<C> build() {
            return new DoubleComponent<>(this.isRequired(), this.getName(), this.min, this.max, this.getDefaultValue());
        }

    }


    /**
     * Get the minimum accepted double that could have been parsed
     *
     * @return Minimum double
     */
    public double getMin() {
        return this.min;
    }

    /**
     * Get the maximum accepted double that could have been parsed
     *
     * @return Maximum double
     */
    public double getMax() {
        return this.max;
    }


    private static final class DoubleParser<C extends CommandSender> implements ComponentParser<C, Double> {

        private final double min;
        private final double max;

        private DoubleParser(final double min, final double max) {
            this.min = min;
            this.max = max;
        }

        @Nonnull
        @Override
        public ComponentParseResult<Double> parse(
                @Nonnull final CommandContext<C> commandContext,
                @Nonnull final Queue<String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ComponentParseResult.failure(new NullPointerException("No input was provided"));
            }
            try {
                final double value = Double.parseDouble(input);
                if (value < this.min || value > this.max) {
                    return ComponentParseResult.failure(new DoubleParseException(input, this.min, this.max));
                }
                inputQueue.remove();
                return ComponentParseResult.success(value);
            } catch (final Exception e) {
                return ComponentParseResult.failure(new DoubleParseException(input, this.min, this.max));
            }
        }

    }


    public static final class DoubleParseException extends NumberParseException {

        /**
         * Construct a new double parse exception
         *
         * @param input String input
         * @param min   Minimum value
         * @param max   Maximum value
         */
        public DoubleParseException(@Nonnull final String input, final double min, final double max) {
            super(input, min, max);
        }

        @Override
        public boolean hasMin() {
            return this.getMin().doubleValue() != Double.MIN_VALUE;
        }

        @Override
        public boolean hasMax() {
            return this.getMax().doubleValue() != Double.MAX_VALUE;
        }

        @Override
        @Nonnull
        public String getNumberType() {
            return "double";
        }

    }

}
