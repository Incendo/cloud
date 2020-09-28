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
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.context.CommandContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public final class CharArgument<C> extends CommandArgument<C, Character> {

    private CharArgument(final boolean required, @Nonnull final String name,
                         @Nonnull final String defaultValue, @Nullable
                         final BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider) {
        super(required, name, new CharacterParser<>(), defaultValue, Character.class, suggestionsProvider);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    @Nonnull
    public static <C> CharArgument.Builder<C> newBuilder(@Nonnull final String name) {
        return new CharArgument.Builder<>(name);
    }

    /**
     * Create a new required command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, Character> required(@Nonnull final String name) {
        return CharArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, Character> optional(@Nonnull final String name) {
        return CharArgument.<C>newBuilder(name).asOptional().build();
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
    public static <C> CommandArgument<C, Character> optional(@Nonnull final String name,
                                                             final String defaultNum) {
        return CharArgument.<C>newBuilder(name).asOptionalWithDefault(defaultNum).build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, Character> {

        protected Builder(@Nonnull final String name) {
            super(Character.class, name);
        }

        /**
         * Builder a new char argument
         *
         * @return Constructed argument
         */
        @Nonnull
        @Override
        public CharArgument<C> build() {
            return new CharArgument<>(this.isRequired(), this.getName(),
                                      this.getDefaultValue(), this.getSuggestionsProvider());
        }

    }


    public static final class CharacterParser<C> implements ArgumentParser<C, Character> {

        @Nonnull
        @Override
        public ArgumentParseResult<Character> parse(@Nonnull final CommandContext<C> commandContext,
                                                    @Nonnull final Queue<String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NullPointerException("No input was provided"));
            }

            if (input.length() != 1) {
                return ArgumentParseResult.failure(new CharParseException(input));
            }

            return ArgumentParseResult.success(input.charAt(0));
        }

        @Override
        public boolean isContextFree() {
            return true;
        }

    }


    /**
     * Char parse exception
     */
    public static final class CharParseException extends IllegalArgumentException {

        private final String input;

        /**
         * Construct a new Char parse exception
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

        @Override
        public String getMessage() {
            return String.format("'%s' is not a valid character.", input);
        }
    }
}
