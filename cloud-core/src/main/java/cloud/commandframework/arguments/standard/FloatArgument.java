//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NumberParseException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public final class FloatArgument<C> extends CommandArgument<C, Float> {

    private final float min;
    private final float max;

    private FloatArgument(final boolean required,
                          @NonNull final String name,
                          final float min,
                          final float max,
                          @NonNull final String defaultValue,
                          @Nullable final BiFunction<@NonNull CommandContext<C>,
                                  @NonNull String, @NonNull List<@NonNull String>> suggestionsProvider) {
        super(required, name, new FloatParser<>(min, max), defaultValue, Float.class, suggestionsProvider);
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
    public static <C> @NonNull Builder<C> newBuilder(@NonNull final String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Float> of(@NonNull final String name) {
        return FloatArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Float> optional(@NonNull final String name) {
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
    public static <C> @NonNull CommandArgument<C, Float> optional(@NonNull final String name,
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

        protected Builder(@NonNull final String name) {
            super(Float.class, name);
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
         * Builder a new float argument
         *
         * @return Constructed argument
         */
        @Override
        public @NonNull FloatArgument<C> build() {
            return new FloatArgument<>(this.isRequired(), this.getName(), this.min, this.max,
                                       this.getDefaultValue(), this.getSuggestionsProvider());
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

        @Override
        public @NonNull ArgumentParseResult<Float> parse(
                @NonNull final CommandContext<C> commandContext,
                @NonNull final Queue<String> inputQueue) {
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

        /**
         * Get the max value
         *
         * @return Max value
         */
        public float getMax() {
            return this.max;
        }

        /**
         * Get the min value
         *
         * @return Min value
         */
        public float getMin() {
            return this.min;
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
        public FloatParseException(@NonNull final String input, final float min, final float max) {
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
        public @NonNull String getNumberType() {
            return "float";
        }

    }

}
