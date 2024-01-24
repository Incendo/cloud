//
// MIT License
//
// Copyright (c) 2024 Incendo
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
package cloud.commandframework.parser.standard;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.parser.ArgumentParseResult;
import cloud.commandframework.parser.ArgumentParser;
import cloud.commandframework.parser.ParserDescriptor;
import cloud.commandframework.suggestion.BlockingSuggestionProvider;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public final class BooleanParser<C> implements ArgumentParser<C, Boolean>, BlockingSuggestionProvider.Strings<C> {

    private static final List<String> STRICT_LOWER = CommandInput.BOOLEAN_STRICT
            .stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
    private static final List<String> LIBERAL_LOWER = CommandInput.BOOLEAN_LIBERAL
            .stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toList());

    private final boolean liberal;

    /**
     * Creates a new boolean parser that only accepts {@code true} and {@code false}.
     *
     * @param <C> the command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Boolean> booleanParser() {
        return booleanParser(false /* liberal */);
    }

    /**
     * Creates a new boolean parser. If {@code liberal} is {@code true} the parser only
     * accepts {@code true} and {@code false}.
     *
     * @param <C>     the command sender type
     * @param liberal whether the parser should be liberal, see {@link CommandInput#isValidBoolean(boolean)}
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Boolean> booleanParser(final boolean liberal) {
        return ParserDescriptor.of(new BooleanParser<>(liberal), Boolean.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #booleanParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Boolean> booleanComponent() {
        return CommandComponent.<C, Boolean>builder().parser(booleanParser());
    }

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
            return ArgumentParseResult.failure(new BooleanParseException(
                    commandInput.peekString(),
                    this.liberal,
                    commandContext
            ));
        }
        return ArgumentParseResult.success(commandInput.readBoolean());
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext,
                                                                final @NonNull CommandInput input) {
        if (!this.liberal) {
            return STRICT_LOWER;
        }

        return LIBERAL_LOWER;
    }


    /**
     * Boolean parse exception
     */
    @API(status = API.Status.STABLE)
    public static final class BooleanParseException extends ParserException {

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
         * Returns the supplied input.
         *
         * @return supplied input
         */
        public @NonNull String input() {
            return this.input;
        }

        /**
         * Returns whether the parser is liberal.
         *
         * @return {@code true} if the parser is liberal, {@code false} if not
         */
        public boolean liberal() {
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
