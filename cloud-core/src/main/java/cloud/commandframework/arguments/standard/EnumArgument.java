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

import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Argument type that recognizes enums
 *
 * @param <C> Argument sender
 * @param <E> Enum type
 */
@SuppressWarnings("unused")
public class EnumArgument<C, E extends Enum<E>> extends CommandArgument<C, E> {

    protected EnumArgument(
            final @NonNull Class<E> enumClass,
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new EnumParser<>(enumClass), defaultValue, enumClass, suggestionsProvider, defaultDescription);
    }

    /**
     * Create a new builder
     *
     * @param name      Name of the argument
     * @param enumClass Enum class
     * @param <C>       Command sender type
     * @param <E>       Enum type
     * @return Created builder
     */
    public static <C, E extends Enum<E>> EnumArgument.@NonNull Builder<C, E> newBuilder(
            final @NonNull Class<E> enumClass,
            final @NonNull String name
    ) {
        return new EnumArgument.Builder<>(name, enumClass);
    }

    /**
     * Create a new required command argument
     *
     * @param enumClass Enum class
     * @param name      Name of the argument
     * @param <C>       Command sender type
     * @param <E>       Enum type
     * @return Created argument
     */
    public static <C, E extends Enum<E>> @NonNull CommandArgument<C, E> of(
            final @NonNull Class<E> enumClass,
            final @NonNull String name
    ) {
        return EnumArgument.<C, E>newBuilder(enumClass, name).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param enumClass Enum class
     * @param name      Name of the argument
     * @param <C>       Command sender type
     * @param <E>       Enum type
     * @return Created argument
     */
    public static <C, E extends Enum<E>> @NonNull CommandArgument<C, E> optional(
            final @NonNull Class<E> enumClass,
            final @NonNull String name
    ) {
        return EnumArgument.<C, E>newBuilder(enumClass, name).asOptional().build();
    }

    /**
     * Create a new optional command argument with a default value
     *
     * @param enumClass    Enum class
     * @param name         Name of the argument
     * @param defaultValue Default value
     * @param <C>          Command sender type
     * @param <E>          Enum type
     * @return Created argument
     */
    public static <C, E extends Enum<E>> @NonNull CommandArgument<C, E> optional(
            final @NonNull Class<E> enumClass,
            final @NonNull String name,
            final @NonNull E defaultValue
    ) {
        return EnumArgument.<C, E>newBuilder(enumClass, name).asOptionalWithDefault(defaultValue.name().toLowerCase()).build();
    }


    public static final class Builder<C, E extends Enum<E>> extends CommandArgument.Builder<C, E> {

        private final Class<E> enumClass;

        private Builder(final @NonNull String name, final @NonNull Class<E> enumClass) {
            super(enumClass, name);
            this.enumClass = enumClass;
        }

        @Override
        public @NonNull CommandArgument<C, E> build() {
            return new EnumArgument<>(this.enumClass, this.isRequired(), this.getName(),
                    this.getDefaultValue(), this.getSuggestionsProvider(), this.getDefaultDescription()
            );
        }

    }


    public static final class EnumParser<C, E extends Enum<E>> implements ArgumentParser<C, E> {

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
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        EnumParser.class,
                        commandContext
                ));
            }

            for (final E value : this.allowedValues) {
                if (value.name().equalsIgnoreCase(input)) {
                    inputQueue.remove();
                    return ArgumentParseResult.success(value);
                }
            }

            return ArgumentParseResult.failure(new EnumParseException(input, this.enumClass, commandContext));
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return EnumSet.allOf(this.enumClass).stream().map(e -> e.name().toLowerCase()).collect(Collectors.toList());
        }

        @Override
        public boolean isContextFree() {
            return true;
        }

    }


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
                    .map(e -> e.toString().toLowerCase())
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

    }

}
