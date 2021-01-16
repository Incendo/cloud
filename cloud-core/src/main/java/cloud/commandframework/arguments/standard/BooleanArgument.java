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

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public final class BooleanArgument<C> extends CommandArgument<C, Boolean> {

    private final boolean liberal;

    private BooleanArgument(
            final boolean required,
            final @NonNull String name,
            final boolean liberal,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription description
    ) {
        super(required, name, new BooleanParser<>(liberal), defaultValue, Boolean.class, suggestionsProvider, description);
        this.liberal = liberal;
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> @NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Boolean> of(final @NonNull String name) {
        return BooleanArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Boolean> optional(final @NonNull String name) {
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
    public static <C> @NonNull CommandArgument<C, Boolean> optional(
            final @NonNull String name,
            final boolean defaultBoolean
    ) {
        return BooleanArgument.<C>newBuilder(name).asOptionalWithDefault(Boolean.toString(defaultBoolean)).build();
    }

    /**
     * Get the liberal boolean
     *
     * @return Liberal boolean
     */
    public boolean isLiberal() {
        return this.liberal;
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, Boolean> {

        private boolean liberal = false;

        private Builder(final @NonNull String name) {
            super(Boolean.class, name);
        }

        /**
         * Set the liberal toggle
         *
         * @param liberal liberal value
         * @return Builder instance
         */
        public @NonNull Builder<C> withLiberal(final boolean liberal) {
            this.liberal = liberal;
            return this;
        }

        /**
         * Builder a new boolean argument
         *
         * @return Constructed argument
         */
        @Override
        public @NonNull BooleanArgument<C> build() {
            return new BooleanArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.liberal,
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
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

        @Override
        public @NonNull ArgumentParseResult<Boolean> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        BooleanParser.class,
                        commandContext
                ));
            }
            inputQueue.remove();

            if (!liberal) {
                if (input.equalsIgnoreCase("true")) {
                    return ArgumentParseResult.success(true);
                }

                if (input.equalsIgnoreCase("false")) {
                    return ArgumentParseResult.success(false);
                }

                return ArgumentParseResult.failure(new BooleanParseException(input, false, commandContext));
            }

            final String uppercaseInput = input.toUpperCase();

            if (LIBERAL_TRUE.contains(uppercaseInput)) {
                return ArgumentParseResult.success(true);
            }

            if (LIBERAL_FALSE.contains(uppercaseInput)) {
                return ArgumentParseResult.success(false);
            }

            return ArgumentParseResult.failure(new BooleanParseException(input, true, commandContext));
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
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
    public static final class BooleanParseException extends ParserException {

        private static final long serialVersionUID = -2688852086944850025L;
        private final String input;
        private final boolean liberal;

        /**
         * Construct a new boolean parse exception
         *
         * @param input   Input
         * @param liberal Whether or not the parser allows truthy and falsy values, or strictly true/false
         * @param context Command context
         */
        public BooleanParseException(
                final @NonNull String input,
                final boolean liberal,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    BooleanParser.class,
                    context,
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_BOOLEAN,
                    CaptionVariable.of("input", input)
            );
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
