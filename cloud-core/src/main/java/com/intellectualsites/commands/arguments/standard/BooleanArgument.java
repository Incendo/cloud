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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public final class BooleanArgument<C> extends CommandArgument<C, Boolean> {
    private final boolean liberal;

    private BooleanArgument(final boolean required, @Nonnull final String name,
                            final boolean liberal, @Nonnull final String defaultValue,
                            @Nullable final BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider) {
        super(required, name, new BooleanParser<>(liberal), defaultValue, Boolean.class, suggestionsProvider);
        this.liberal = liberal;
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
    public static <C> CommandArgument<C, Boolean> required(@Nonnull final String name) {
        return BooleanArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, Boolean> optional(@Nonnull final String name) {
        return BooleanArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new required command argument with a default value
     *
     * @param name           Argument name
     * @param defaultBoolean Default num
     * @param <C>            Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, Boolean> optional(@Nonnull final String name,
                                                           final boolean defaultBoolean) {
        return BooleanArgument.<C>newBuilder(name).asOptionalWithDefault(Boolean.toString(defaultBoolean)).build();
    }

    /**
     * Get the liberal boolean
     *
     * @return Liberal boolean
     */
    public boolean isLiberal() {
        return liberal;
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, Boolean> {

        private boolean liberal = false;

        protected Builder(@Nonnull final String name) {
            super(Boolean.class, name);
        }

        /**
         * Set the liberal toggle
         *
         * @param liberal liberal value
         * @return Builder instance
         */
        @Nonnull
        public Builder<C> withLiberal(final boolean liberal) {
            this.liberal = liberal;
            return this;
        }

        /**
         * Builder a new boolean argument
         *
         * @return Constructed argument
         */
        @Nonnull
        @Override
        public BooleanArgument<C> build() {
            return new BooleanArgument<>(this.isRequired(), this.getName(), this.liberal,
                                         this.getDefaultValue(), this.getSuggestionsProvider());
        }

    }

    public static final class BooleanParser<C> implements ArgumentParser<C, Boolean> {

        private static final List<String> LIBERAL = Arrays.asList("TRUE", "YES", "ON", "FALSE", "NO", "OFF");
        private static final List<String> LIBERAL_TRUE = Arrays.asList("TRUE", "YES", "ON");
        private static final List<String> LIBERAL_FALSE = Arrays.asList("FALSE", "NO", "OFF");

        private final boolean liberal;

        /**
         * Construct a new boolean parser
         *
         * @param liberal Whether or not it'll accept boolean-esque strings, or just booleans
         */
        public BooleanParser(final boolean liberal) {
            this.liberal = liberal;
        }

        @Nonnull
        @Override
        public ArgumentParseResult<Boolean> parse(@Nonnull final CommandContext<C> commandContext,
                                                  @Nonnull final Queue<String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NullPointerException("No input was provided"));
            }
            inputQueue.remove();

            if (!liberal) {
                if (input.equalsIgnoreCase("true")) {
                    return ArgumentParseResult.success(true);
                }

                if (input.equalsIgnoreCase("false")) {
                    return ArgumentParseResult.success(false);
                }

                return ArgumentParseResult.failure(new BooleanParseException(input, false));
            }

            final String uppercaseInput = input.toUpperCase();

            if (LIBERAL_TRUE.contains(uppercaseInput)) {
                return ArgumentParseResult.success(true);
            }

            if (LIBERAL_FALSE.contains(uppercaseInput)) {
                return ArgumentParseResult.success(false);
            }

            return ArgumentParseResult.failure(new BooleanParseException(input, true));
        }

        @Nonnull
        @Override
        public List<String> suggestions(@Nonnull final CommandContext<C> commandContext,
                                        @Nonnull final String input) {
            if (!liberal) {
                return Arrays.asList("TRUE", "FALSE");
            }

            return LIBERAL;
        }

        @Override
        public boolean isContextFree() {
            return true;
        }
    }


    /**
     * Boolean parse exception
     */
    public static final class BooleanParseException extends IllegalArgumentException {

        private final String input;
        private final boolean liberal;

        /**
         * Construct a new boolean parse exception
         *
         * @param input   String input
         * @param liberal Liberal value
         */
        public BooleanParseException(@Nonnull final String input, final boolean liberal) {
            this.input = input;
            this.liberal = liberal;
        }

        /**
         * Get the supplied input
         *
         * @return String value
         */
        public String getInput() {
            return input;
        }

        /**
         * Get the liberal boolean value
         *
         * @return Liberal value
         */
        public boolean isLiberal() {
            return liberal;
        }
    }
}
