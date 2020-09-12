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
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

@SuppressWarnings("unused")
public final class BooleanComponent<C extends CommandSender> extends CommandComponent<C, Boolean> {
    private final boolean liberal;

    private BooleanComponent(final boolean required, @Nonnull final String name,
                             final boolean liberal, @Nonnull final String defaultValue) {
        super(required, name, new BooleanParser<>(liberal), defaultValue);
        this.liberal = liberal;
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
    public static <C extends CommandSender> CommandComponent<C, Boolean> required(@Nonnull final String name) {
        return BooleanComponent.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    @Nonnull
    public static <C extends CommandSender> CommandComponent<C, Boolean> optional(@Nonnull final String name) {
        return BooleanComponent.<C>newBuilder(name).asOptional().build();
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
    public static <C extends CommandSender> CommandComponent<C, Boolean> optional(@Nonnull final String name,
                                                                                  final String defaultNum) {
        return BooleanComponent.<C>newBuilder(name).asOptionalWithDefault(defaultNum).build();
    }


    public static final class Builder<C extends CommandSender> extends CommandComponent.Builder<C, Boolean> {

        private boolean liberal = false;

        protected Builder(@Nonnull final String name) {
            super(name);
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
         * Builder a new boolean component
         *
         * @return Constructed component
         */
        @Nonnull
        @Override
        public BooleanComponent<C> build() {
            return new BooleanComponent<>(this.isRequired(), this.getName(), this.liberal, this.getDefaultValue());
        }

    }

    /**
     * Get the liberal boolean
     *
     * @return Liberal boolean
     */
    public boolean isLiberal() {
        return liberal;
    }


    private static final class BooleanParser<C extends CommandSender> implements ComponentParser<C, Boolean> {

        private static final List<String> LIBERAL = Arrays.asList("TRUE", "YES", "ON", "FALSE", "NO", "OFF");
        private static final List<String> LIBERAL_TRUE = Arrays.asList("TRUE", "YES", "ON");
        private static final List<String> LIBERAL_FALSE = Arrays.asList("FALSE", "NO", "OFF");

        private final boolean liberal;

        private BooleanParser(final boolean liberal) {
            this.liberal = liberal;
        }

        @Nonnull
        @Override
        public ComponentParseResult<Boolean> parse(@Nonnull final CommandContext<C> commandContext,
                                                   @Nonnull final Queue<String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ComponentParseResult.failure(new NullPointerException("No input was provided"));
            }
            inputQueue.remove();

            if (!liberal) {
                if (input.equalsIgnoreCase("true")) {
                    return ComponentParseResult.success(true);
                }

                if (input.equalsIgnoreCase("false")) {
                    return ComponentParseResult.success(false);
                }

                return ComponentParseResult.failure(new BooleanParseException(input, false));
            }

            final String uppercaseInput = input.toUpperCase();

            if (LIBERAL_TRUE.contains(uppercaseInput)) {
                return ComponentParseResult.success(true);
            }

            if (LIBERAL_FALSE.contains(uppercaseInput)) {
                return ComponentParseResult.success(false);
            }

            return ComponentParseResult.failure(new BooleanParseException(input, true));
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
