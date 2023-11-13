//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.ParserException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;

@SuppressWarnings("unused")
@API(status = API.Status.STABLE)
public final class BooleanArgument<C> extends CommandArgument<C, Boolean> {

    private final boolean liberal;

    private BooleanArgument(
            final @NonNull String name,
            final boolean liberal,
            final @Nullable SuggestionProvider<C> suggestionProvider,
            final @NonNull ArgumentDescription description
    ) {
        super(name, new BooleanParser<>(liberal), Boolean.class, suggestionProvider, description);
        this.liberal = liberal;
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return new {@link Builder}
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     * @deprecated prefer {@link #builder(String)}
     */
    @API(status = API.Status.DEPRECATED, since = "1.8.0")
    @Deprecated
    public static <C> @NonNull Builder<C> newBuilder(final @NonNull String name) {
        return builder(name);
    }

    /**
     * Create a new required command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Boolean> of(final @NonNull String name) {
        return BooleanArgument.<C>builder(name).build();
    }

    /**
     * Get the liberal boolean
     *
     * @return Liberal boolean
     */
    public boolean isLiberal() {
        return this.liberal;
    }


    @API(status = API.Status.STABLE)
    public static final class Builder<C> extends CommandArgument.TypedBuilder<C, Boolean, Builder<C>> {

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
        public @NonNull @This Builder<C> withLiberal(final boolean liberal) {
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
                    this.getName(),
                    this.liberal,
                    this.suggestionProvider(),
                    this.getDefaultDescription()
            );
        }
    }


    @API(status = API.Status.STABLE)
    public static final class BooleanParser<C> implements ArgumentParser<C, Boolean> {

        public static final List<String> STRICT = Arrays.asList("TRUE", "FALSE");
        public static final List<String> LIBERAL = Arrays.asList("TRUE", "YES", "ON", "FALSE", "NO", "OFF");
        public static final List<String> LIBERAL_TRUE = Arrays.asList("TRUE", "YES", "ON");
        public static final List<String> LIBERAL_FALSE = Arrays.asList("FALSE", "NO", "OFF");

        private static final List<String> STRICT_LOWER = STRICT
                .stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
        private static final List<String> LIBERAL_LOWER = LIBERAL
                .stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toList());

        private final boolean liberal;

        /**
         * Construct a new boolean parser
         *
         * @param liberal Whether it'll accept boolean-esque strings, or just booleans
         */
        public BooleanParser(final boolean liberal) {
            this.liberal = liberal;
        }

        @Override
        public @NonNull ArgumentParseResult<Boolean> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull CommandInput commandInput
        ) {
            if (!commandInput.isValidBoolean(this.liberal)) {
                return ArgumentParseResult.failure(new BooleanParseException(commandInput.peekString(), this.liberal, commandContext));
            }
            return ArgumentParseResult.success(commandInput.readBoolean());
        }

        @Override
        public @NonNull List<@NonNull String> stringSuggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            if (!this.liberal) {
                return STRICT_LOWER;
            }

            return LIBERAL_LOWER;
        }

        @Override
        public boolean isContextFree() {
            return true;
        }
    }


    /**
     * Boolean parse exception
     */
    @API(status = API.Status.STABLE)
    public static final class BooleanParseException extends ParserException {

        private static final long serialVersionUID = -2688852086944850025L;
        private final String input;
        private final boolean liberal;

        /**
         * Construct a new boolean parse exception
         *
         * @param input   Input
         * @param liberal Whether the parser allows truthy and falsy values, or strictly true/false
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
        public @NonNull String getInput() {
            return this.input;
        }

        /**
         * Get the liberal boolean value
         *
         * @return Liberal value
         */
        public boolean isLiberal() {
            return this.liberal;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final BooleanParseException that = (BooleanParseException) o;
            return this.liberal == that.liberal && this.input.equals(that.input);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.input, this.liberal);
        }
    }
}
