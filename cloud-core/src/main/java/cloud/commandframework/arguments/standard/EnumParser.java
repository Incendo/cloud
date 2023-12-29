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

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.ParserException;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public final class EnumParser<C, E extends Enum<E>> implements ArgumentParser<C, E>,
        BlockingSuggestionProvider.Strings<C> {

    /**
     * Creates a new enum parser.
     *
     * @param <C>       command sender type
     * @param <E>       the enum type
     * @param enumClass the enum class
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C, E extends Enum<E>> @NonNull ParserDescriptor<C, E> enumParser(final @NonNull Class<E> enumClass) {
        return ParserDescriptor.of(new EnumParser<>(enumClass), enumClass);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #enumParser(Class)} as the parser.
     *
     * @param <C>       command sender type
     * @param <E>       the enum type
     * @param enumClass the enum class
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C, E extends Enum<E>> CommandComponent.@NonNull Builder<C, E> enumComponent(
            final @NonNull Class<E> enumClass
    ) {
        return CommandComponent.<C, E>builder().parser(enumParser(enumClass));
    }

    private final Class<E> enumClass;
    private final EnumSet<E> allowedValues;

    /**
     * Construct a new enum parser
     *
     * @param enumClass Enum class
     */
    public EnumParser(final @NonNull Class<E> enumClass) {
        this.enumClass = enumClass;
        this.allowedValues = EnumSet.allOf(enumClass);
    }

    @Override
    public @NonNull ArgumentParseResult<E> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.readString();

        for (final E value : this.allowedValues) {
            if (value.name().equalsIgnoreCase(input)) {
                return ArgumentParseResult.success(value);
            }
        }

        return ArgumentParseResult.failure(new EnumParseException(input, this.enumClass, commandContext));
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext,
                                                                final @NonNull CommandInput input) {
        return EnumSet.allOf(this.enumClass).stream().map(e -> e.name().toLowerCase(Locale.ROOT)).collect(Collectors.toList());
    }


    @API(status = API.Status.STABLE)
    public static final class EnumParseException extends ParserException {

        private static final long serialVersionUID = 3465389578951428862L;
        private final String input;
        private final Class<? extends Enum<?>> enumClass;

        /**
         * Construct a new enum parse exception
         *
         * @param input     Input
         * @param enumClass Enum class
         * @param context   Command context
         */
        public EnumParseException(
                final @NonNull String input,
                final @NonNull Class<? extends Enum<?>> enumClass,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    EnumParser.class,
                    context,
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_ENUM,
                    CaptionVariable.of("input", input),
                    CaptionVariable.of("acceptableValues", join(enumClass))
            );
            this.input = input;
            this.enumClass = enumClass;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static @NonNull String join(final @NonNull Class<? extends Enum> clazz) {
            final EnumSet<?> enumSet = EnumSet.allOf(clazz);
            return enumSet.stream()
                    .map(e -> e.toString().toLowerCase(Locale.ROOT))
                    .collect(Collectors.joining(", "));
        }

        /**
         * Get the input provided by the sender
         *
         * @return Input
         */
        public @NonNull String getInput() {
            return this.input;
        }

        /**
         * Get the enum class that was attempted to be parsed
         *
         * @return Enum class
         */
        public @NonNull Class<? extends Enum<?>> getEnumClass() {
            return this.enumClass;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final EnumParseException that = (EnumParseException) o;
            return this.input.equals(that.input) && this.enumClass.equals(that.enumClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.input, this.enumClass);
        }
    }
}
