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
import java.util.Queue;

@SuppressWarnings("unused")
public final class CharComponent<C extends CommandSender> extends CommandComponent<C, Character> {

    private CharComponent(final boolean required, @Nonnull final String name,
                          @Nonnull final String defaultValue) {
        super(required, name, new CharacterParser<>(), defaultValue);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the component
     * @param <C>  Command sender type
     * @return Created builder
     */
    @Nonnull
    public static <C extends CommandSender> CharComponent.Builder<C> newBuilder(@Nonnull final String name) {
        return new CharComponent.Builder<>(name);
    }

    /**
     * Create a new required command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    @Nonnull
    public static <C extends CommandSender> CommandComponent<C, Character> required(@Nonnull final String name) {
        return CharComponent.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    @Nonnull
    public static <C extends CommandSender> CommandComponent<C, Character> optional(@Nonnull final String name) {
        return CharComponent.<C>newBuilder(name).asOptional().build();
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
    public static <C extends CommandSender> CommandComponent<C, Character> optional(@Nonnull final String name,
                                                                                 final String defaultNum) {
        return CharComponent.<C>newBuilder(name).asOptionalWithDefault(defaultNum).build();
    }

    public static final class Builder<C extends CommandSender> extends CommandComponent.Builder<C, Character> {

        protected Builder(@Nonnull final String name) {
            super(name);
        }

        /**
         * Builder a new char component
         *
         * @return Constructed component
         */
        @Nonnull
        @Override
        public CharComponent<C> build() {
            return new CharComponent<>(this.isRequired(), this.getName(), this.getDefaultValue());
        }

    }


    private static final class CharacterParser<C extends CommandSender> implements ComponentParser<C, Character> {

        @Nonnull
        @Override
        public ComponentParseResult<Character> parse(@Nonnull final CommandContext<C> commandContext,
                                                  @Nonnull final Queue<String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ComponentParseResult.failure(new NullPointerException("No input was provided"));
            }

            if (input.length() != 1) {
                return ComponentParseResult.failure(new CharParseException(input));
            }

            return ComponentParseResult.success(input.charAt(0));
        }
    }


    public static final class CharParseException extends IllegalArgumentException {

        private final String input;

        /**
         * Construct a new boolean parse exception
         *
         * @param input String input
         */
        public CharParseException(final String input) {
            this.input = input;
        }

        /**
         * Get the supplied input
         *
         * @return Input value
         */
        public String getInput() {
            return input;
        }
    }
}
