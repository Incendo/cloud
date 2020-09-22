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
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public final class StringArgument<C> extends CommandArgument<C, String> {

    private final StringMode stringMode;

    private StringArgument(final boolean required,
                           @Nonnull final String name,
                           @Nonnull final StringMode stringMode,
                           @Nonnull final String defaultValue,
                           @Nonnull final BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider) {
        super(required, name, new StringParser<>(stringMode, suggestionsProvider),
              defaultValue, String.class, suggestionsProvider);
        this.stringMode = stringMode;
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    @Nonnull
    public static <C> StringArgument.Builder<C> newBuilder(@Nonnull final String name) {
        return new StringArgument.Builder<>(name);
    }

    /**
     * Create a new required single string command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, String> required(@Nonnull final String name) {
        return StringArgument.<C>newBuilder(name).single().asRequired().build();
    }

    /**
     * Create a new required command argument
     *
     * @param name       Argument name
     * @param stringMode String mode
     * @param <C>        Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, String> required(@Nonnull final String name, @Nonnull final StringMode stringMode) {
        return StringArgument.<C>newBuilder(name).withMode(stringMode).asRequired().build();
    }

    /**
     * Create a new optional single string command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, String> optional(@Nonnull final String name) {
        return StringArgument.<C>newBuilder(name).single().asOptional().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name       Argument name
     * @param stringMode String mode
     * @param <C>        Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, String> optional(@Nonnull final String name, @Nonnull final StringMode stringMode) {
        return StringArgument.<C>newBuilder(name).withMode(stringMode).asOptional().build();
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
    public static <C> CommandArgument<C, String> optional(@Nonnull final String name,
                                                          final String defaultNum) {
        return StringArgument.<C>newBuilder(name).asOptionalWithDefault(defaultNum).build();
    }

    /**
     * Get the string mode
     *
     * @return String mode
     */
    @Nonnull
    public StringMode getStringMode() {
        return this.stringMode;
    }


    public enum StringMode {
        SINGLE,
        GREEDY,
        QUOTED
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, String> {

        private StringMode stringMode = StringMode.SINGLE;
        private BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider = (v1, v2) -> Collections.emptyList();

        protected Builder(@Nonnull final String name) {
            super(String.class, name);
        }

        @Nonnull
        private Builder<C> withMode(@Nonnull final StringMode stringMode) {
            this.stringMode = stringMode;
            return this;
        }

        /**
         * Set the string mode to greedy
         *
         * @return Builder instance
         */
        @Nonnull
        public Builder<C> greedy() {
            this.stringMode = StringMode.GREEDY;
            return this;
        }

        /**
         * Set the string mode to single
         *
         * @return Builder instance
         */
        @Nonnull
        public Builder<C> single() {
            this.stringMode = StringMode.SINGLE;
            return this;
        }

        /**
         * Set the string mode to greedy
         *
         * @return Builder instance
         */
        @Nonnull
        public Builder<C> quoted() {
            this.stringMode = StringMode.QUOTED;
            return this;
        }

        /**
         * Set the suggestions provider
         *
         * @param suggestionsProvider Suggestions provider
         * @return Builder instance
         */
        @Nonnull
        public Builder<C> withSuggestionsProvider(@Nonnull final BiFunction<CommandContext<C>,
                String, List<String>> suggestionsProvider) {
            this.suggestionsProvider = suggestionsProvider;
            return this;
        }

        /**
         * Builder a new string argument
         *
         * @return Constructed argument
         */
        @Nonnull
        @Override
        public StringArgument<C> build() {
            return new StringArgument<>(this.isRequired(), this.getName(), this.stringMode,
                                        this.getDefaultValue(), this.suggestionsProvider);
        }

    }


    public static final class StringParser<C> implements ArgumentParser<C, String> {

        private final StringMode stringMode;
        private final BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider;

        /**
         * Construct a new string parser
         *
         * @param stringMode          String parsing mode
         * @param suggestionsProvider Suggestions provider
         */
        public StringParser(@Nonnull final StringMode stringMode,
                            @Nonnull final BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider) {
            this.stringMode = stringMode;
            this.suggestionsProvider = suggestionsProvider;
        }

        @Nonnull
        @Override
        public ArgumentParseResult<String> parse(@Nonnull final CommandContext<C> commandContext,
                                                 @Nonnull final Queue<String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NullPointerException("No input was provided"));
            }

            if (this.stringMode == StringMode.SINGLE) {
                if (commandContext.isSuggestions()) {
                    final List<String> suggestions = this.suggestionsProvider.apply(commandContext, inputQueue.peek());
                    if (!suggestions.isEmpty() && !suggestions.contains(input)) {
                        return ArgumentParseResult.failure(new IllegalArgumentException(
                                String.format("'%s' is not one of: %s", input, String.join(", ", suggestions))
                        ));
                    }
                }
                inputQueue.remove();
                return ArgumentParseResult.success(input);
            }

            final StringJoiner sj = new StringJoiner(" ");
            final int size = inputQueue.size();

            boolean started = false;
            boolean finished = false;

            for (int i = 0; i < size; i++) {
                String string = inputQueue.peek();

                if (string == null) {
                    break;
                }

                if (this.stringMode == StringMode.QUOTED) {
                    if (!started) {
                        if (string.startsWith("\"")) {
                            string = string.substring(1);
                            started = true;
                        } else {
                            return ArgumentParseResult.failure(new StringParseException(string, StringMode.QUOTED));
                        }
                    } else if (string.endsWith("\"")) {
                        sj.add(string.substring(0, string.length() - 1));
                        inputQueue.remove();
                        finished = true;
                        break;
                    }
                }

                sj.add(string);
                inputQueue.remove();
            }

            if (this.stringMode == StringMode.QUOTED && (!started || !finished)) {
                return ArgumentParseResult.failure(new StringParseException(sj.toString(), StringMode.GREEDY));
            }

            return ArgumentParseResult.success(sj.toString());
        }

        @Nonnull
        @Override
        public List<String> suggestions(@Nonnull final CommandContext<C> commandContext, @Nonnull final String input) {
            return this.suggestionsProvider.apply(commandContext, input);
        }

        @Override
        public boolean isContextFree() {
            return true;
        }

        /**
         * Get the string mode
         *
         * @return String mode
         */
        @Nonnull
        public StringMode getStringMode() {
            return this.stringMode;
        }
    }


    public static final class StringParseException extends IllegalArgumentException {

        private final String input;
        private final StringMode stringMode;

        /**
         * Construct a new string parse exception
         *
         * @param input      Input
         * @param stringMode String mode
         */
        public StringParseException(@Nonnull final String input, @Nonnull final StringMode stringMode) {
            this.input = input;
            this.stringMode = stringMode;
        }


        /**
         * Get the input provided by the sender
         *
         * @return Input
         */
        @Nonnull
        public String getInput() {
            return this.input;
        }

        /**
         * Get the string mode
         *
         * @return String mode
         */
        @Nonnull
        public StringMode getStringMode() {
            return this.stringMode;
        }

        @Override
        public String getMessage() {
            if (this.stringMode == StringMode.QUOTED) {
                return "The string needs to be surrounded by quotation marks";
            }
            return String.format("'%s' is not a valid string", this.input);
        }

    }

}
