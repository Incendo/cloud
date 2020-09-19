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
package com.intellectualsites.commands.bukkit.parsers;

import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.arguments.parser.ArgumentParseResult;
import com.intellectualsites.commands.arguments.parser.ArgumentParser;
import com.intellectualsites.commands.context.CommandContext;
import org.bukkit.Material;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * cloud argument type that parses Bukkit {@link Material materials}
 *
 * @param <C> Command sender type
 */
public class MaterialArgument<C> extends CommandArgument<C, Material> {

    protected MaterialArgument(final boolean required,
                               @Nonnull final String name,
                               @Nonnull final String defaultValue,
                               @Nullable final BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider) {
        super(required, name, new MaterialParser<>(), defaultValue, Material.class, suggestionsProvider);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    @Nonnull
    public static <C> MaterialArgument.Builder<C> newBuilder(@Nonnull final String name) {
        return new MaterialArgument.Builder<>(name);
    }

    /**
     * Create a new required argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, Material> required(@Nonnull final String name) {
        return MaterialArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, Material> optional(@Nonnull final String name) {
        return MaterialArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new optional argument with a default value
     *
     * @param name     Argument name
     * @param material Default value
     * @param <C>      Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, Material> optional(@Nonnull final String name,
                                                            @Nonnull final Material material) {
        return MaterialArgument.<C>newBuilder(name).asOptionalWithDefault(material.name().toLowerCase()).build();
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, Material> {

        protected Builder(@Nonnull final String name) {
            super(Material.class, name);
        }

    }


    public static final class MaterialParser<C> implements ArgumentParser<C, Material> {

        @Nonnull
        @Override
        public ArgumentParseResult<Material> parse(@Nonnull final CommandContext<C> commandContext,
                                                   @Nonnull final Queue<String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NullPointerException("No input was provided"));
            }

            try {
                final Material material = Material.valueOf(input.replace("minecraft:", "").toUpperCase());
                inputQueue.remove();
                return ArgumentParseResult.success(material);
            } catch (final IllegalArgumentException exception) {
                return ArgumentParseResult.failure(new MaterialParseException(input));
            }
        }

    }


    public static final class MaterialParseException extends IllegalArgumentException {

        private final String input;

        /**
         * Construct a new MaterialParseException
         *
         * @param input Input
         */
        public MaterialParseException(@Nonnull final String input) {
            this.input = input;
        }

        /**
         * Get the input
         *
         * @return Input
         */
        @Nonnull
        public String getInput() {
            return this.input;
        }

        @Override
        public String getMessage() {
            return EnumSet.allOf(Material.class).stream().map(Material::name).map(String::toLowerCase)
                          .collect(Collectors.joining(", "));
        }
    }

}
