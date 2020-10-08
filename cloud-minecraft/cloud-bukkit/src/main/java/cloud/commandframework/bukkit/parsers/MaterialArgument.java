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
package cloud.commandframework.bukkit.parsers;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import org.bukkit.Material;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

/**
 * cloud argument type that parses Bukkit {@link Material materials}
 *
 * @param <C> Command sender type
 */
public class MaterialArgument<C> extends CommandArgument<C, Material> {

    protected MaterialArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider
    ) {
        super(required, name, new MaterialParser<>(), defaultValue, Material.class, suggestionsProvider);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> MaterialArgument.@NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new MaterialArgument.Builder<>(name);
    }

    /**
     * Create a new required argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Material> of(final @NonNull String name) {
        return MaterialArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Material> optional(final @NonNull String name) {
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
    public static <C> @NonNull CommandArgument<C, Material> optional(
            final @NonNull String name,
            final @NonNull Material material
    ) {
        return MaterialArgument.<C>newBuilder(name).asOptionalWithDefault(material.name().toLowerCase()).build();
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, Material> {

        protected Builder(final @NonNull String name) {
            super(Material.class, name);
        }

    }


    public static final class MaterialParser<C> implements ArgumentParser<C, Material> {

        @Override
        public @NonNull ArgumentParseResult<Material> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NullPointerException("No input was provided"));
            }

            /* Pre-process input */
            if (input.contains("minecraft:")) {
                input = input.substring("minecraft:".length());
            }
            input = input.toUpperCase();

            try {
                final Material material = Material.valueOf(input);
                inputQueue.remove();
                return ArgumentParseResult.success(material);
            } catch (final IllegalArgumentException exception) {
                return ArgumentParseResult.failure(new MaterialParseException(inputQueue.peek()));
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
        public MaterialParseException(final @NonNull String input) {
            this.input = input;
        }

        /**
         * Get the input
         *
         * @return Input
         */
        public @NonNull String getInput() {
            return this.input;
        }

        @Override
        public String getMessage() {
            return String.format("'%s' is not a valid material name", this.input);
        }

    }

}
