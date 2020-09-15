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
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Component type that recognizes enums
 *
 * @param <C> Component sender
 * @param <E> Enum type
 */
@SuppressWarnings("unused")
public class EnumComponent<C extends CommandSender, E extends Enum<E>> extends CommandComponent<C, E> {

    protected EnumComponent(@Nonnull final Class<E> enumClass,
                         final boolean required,
                         @Nonnull final String name,
                         @Nonnull final String defaultValue) {
        super(required, name, new EnumParser<>(enumClass), defaultValue, enumClass);
    }

    /**
     * Create a new builder
     *
     * @param name      Name of the component
     * @param enumClass Enum class
     * @param <C>       Command sender type
     * @param <E>       Enum type
     * @return Created builder
     */
    @Nonnull
    public static <C extends CommandSender, E extends Enum<E>> EnumComponent.Builder<C, E> newBuilder(
            @Nonnull final Class<E> enumClass, @Nonnull final String name) {
        return new EnumComponent.Builder<>(name, enumClass);
    }

    /**
     * Create a new required command component
     *
     * @param enumClass Enum class
     * @param name      Name of the component
     * @param <C>       Command sender type
     * @param <E>       Enum type
     * @return Created component
     */
    @Nonnull
    public static <C extends CommandSender, E extends Enum<E>> CommandComponent<C, E> required(
            @Nonnull final Class<E> enumClass, @Nonnull final String name) {
        return EnumComponent.<C, E>newBuilder(enumClass, name).asRequired().build();
    }

    /**
     * Create a new optional command component
     *
     * @param enumClass Enum class
     * @param name      Name of the component
     * @param <C>       Command sender type
     * @param <E>       Enum type
     * @return Created component
     */
    @Nonnull
    public static <C extends CommandSender, E extends Enum<E>> CommandComponent<C, E> optional(
            @Nonnull final Class<E> enumClass, @Nonnull final String name) {
        return EnumComponent.<C, E>newBuilder(enumClass, name).asOptional().build();
    }

    /**
     * Create a new optional command component with a default value
     *
     * @param enumClass    Enum class
     * @param name         Name of the component
     * @param defaultValue Default value
     * @param <C>          Command sender type
     * @param <E>          Enum type
     * @return Created component
     */
    @Nonnull
    public static <C extends CommandSender, E extends Enum<E>> CommandComponent<C, E> optional(
            @Nonnull final Class<E> enumClass, @Nonnull final String name, @Nonnull final E defaultValue) {
        return EnumComponent.<C, E>newBuilder(enumClass, name).asOptionalWithDefault(defaultValue.name().toLowerCase()).build();
    }


    public static final class Builder<C extends CommandSender, E extends Enum<E>> extends CommandComponent.Builder<C, E> {

        private final Class<E> enumClass;

        protected Builder(@Nonnull final String name, @Nonnull final Class<E> enumClass) {
            super(enumClass, name);
            this.enumClass = enumClass;
        }

        @Nonnull
        @Override
        public CommandComponent<C, E> build() {
            return new EnumComponent<>(this.enumClass, this.isRequired(), this.getName(), this.getDefaultValue());
        }
    }


    public static final class EnumParser<C extends CommandSender, E extends Enum<E>> implements ComponentParser<C, E> {

        private final Class<E> enumClass;
        private final EnumSet<E> allowedValues;

        /**
         * Construct a new enum parser
         *
         * @param enumClass Enum class
         */
        public EnumParser(@Nonnull final Class<E> enumClass) {
            this.enumClass = enumClass;
            this.allowedValues = EnumSet.allOf(enumClass);
        }

        @Nonnull
        @Override
        public ComponentParseResult<E> parse(@Nonnull final CommandContext<C> commandContext,
                                             @Nonnull final Queue<String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ComponentParseResult.failure(new NullPointerException("No input was provided"));
            }

            for (final E value : this.allowedValues) {
                if (value.name().equalsIgnoreCase(input)) {
                    inputQueue.remove();
                    return ComponentParseResult.success(value);
                }
            }

            return ComponentParseResult.failure(new EnumParseException(input, this.enumClass));
        }

        @Nonnull
        @Override
        public List<String> suggestions(@Nonnull final CommandContext<C> commandContext, @Nonnull final String input) {
            return EnumSet.allOf(this.enumClass).stream().map(e -> e.name().toLowerCase()).collect(Collectors.toList());
        }

        @Override
        public boolean isContextFree() {
            return true;
        }
    }


    public static final class EnumParseException extends IllegalArgumentException {

        private final String input;
        private final Class<? extends Enum<?>> enumClass;

        /**
         * Construct a new enum parse exception
         *
         * @param input     Input
         * @param enumClass Enum class
         */
        public EnumParseException(@Nonnull final String input, @Nonnull final Class<? extends Enum<?>> enumClass) {
            this.input = input;
            this.enumClass = enumClass;
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
         * Get the enum class that was attempted to be parsed
         *
         * @return Enum class
         */
        public Class<? extends Enum<?>> getEnumClass() {
            return this.enumClass;
        }

        @Override
        public String getMessage() {
            return String.format("'%s' is not one of the following: %s", this.input, join(enumClass));
        }

        @Nonnull
        @SuppressWarnings("all")
        private static String join(@Nonnull final Class<? extends Enum> clazz) {
            final EnumSet<?> enumSet = EnumSet.allOf(clazz);
            return enumSet.stream()
                          .map(e -> e.toString().toLowerCase())
                          .collect(Collectors.joining(", "));
        }

    }

}
