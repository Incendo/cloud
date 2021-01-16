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
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public final class CharArgument<C> extends CommandArgument<C, Character> {

    private CharArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>,
                    @NonNull String, @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
        ) {
        super(required, name, new CharacterParser<>(), defaultValue, Character.class, suggestionsProvider, defaultDescription);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> CharArgument.@NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new CharArgument.Builder<>(name);
    }

    /**
     * Create a new required command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Character> of(final @NonNull String name) {
        return CharArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Character> optional(final @NonNull String name) {
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
    public static <C> @NonNull CommandArgument<C, Character> optional(
            final @NonNull String name,
            final @NonNull String defaultNum
    ) {
        return CharArgument.<C>newBuilder(name).asOptionalWithDefault(defaultNum).build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, Character> {

        private Builder(final @NonNull String name) {
            super(Character.class, name);
        }

        /**
         * Builder a new char argument
         *
         * @return Constructed argument
         */
        @Override
        public @NonNull CharArgument<C> build() {
            return new CharArgument<>(this.isRequired(), this.getName(),
                    this.getDefaultValue(), this.getSuggestionsProvider(), this.getDefaultDescription()
            );
        }

    }


    public static final class CharacterParser<C> implements ArgumentParser<C, Character> {

        @Override
        public @NonNull ArgumentParseResult<Character> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        CharacterParser.class,
                        commandContext
                ));
            }

            if (input.length() != 1) {
                return ArgumentParseResult.failure(new CharParseException(input, commandContext));
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
    public static final class CharParseException extends ParserException {

        private static final long serialVersionUID = 6458851071584278854L;
        private final String input;

        /**
         * Construct a new Char parse exception
         *
         * @param input   String input
         * @param context Command context
         */
        public CharParseException(final @NonNull String input, final @NonNull CommandContext<?> context) {
            super(
                    CharacterParser.class,
                    context,
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_CHAR,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Get the supplied input
         *
         * @return Input value
         */
        public @NonNull String getInput() {
            return input;
        }

    }

}
