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
import org.bukkit.Bukkit;
import org.bukkit.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * cloud argument type that parses Bukkit {@link org.bukkit.World worlds}
 *
 * @param <C> Command sender type
 */
public class WorldArgument<C> extends CommandArgument<C, World> {

    protected WorldArgument(final boolean required,
                            @Nonnull final String name,
                            @Nonnull final String defaultValue,
                            @Nullable final BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider) {
        super(required, name, new WorldParser<>(), defaultValue, World.class, suggestionsProvider);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    @Nonnull
    public static <C> CommandArgument.Builder<C, World> newBuilder(@Nonnull final String name) {
        return new WorldArgument.Builder<>(name);
    }

    /**
     * Create a new required argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, World> required(@Nonnull final String name) {
        return WorldArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, World> optional(@Nonnull final String name) {
        return WorldArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new optional argument with a default value
     *
     * @param name         Argument name
     * @param defaultValue Default value
     * @param <C>          Command sender type
     * @return Created argument
     */
    @Nonnull
    public static <C> CommandArgument<C, World> optional(@Nonnull final String name,
                                                         @Nonnull final String defaultValue) {
        return WorldArgument.<C>newBuilder(name).asOptionalWithDefault(defaultValue).build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, World> {

        protected Builder(@Nonnull final String name) {
            super(World.class, name);
        }

        @Nonnull
        @Override
        public CommandArgument<C, World> build() {
            return new WorldArgument<>(this.isRequired(), this.getName(), this.getDefaultValue(), this.getSuggestionsProvider());
        }
    }


    public static final class WorldParser<C> implements ArgumentParser<C, World> {

        @Nonnull
        @Override
        public ArgumentParseResult<World> parse(@Nonnull final CommandContext<C> commandContext,
                                                @Nonnull final Queue<String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NullPointerException("No input was provided"));
            }

            final World world = Bukkit.getWorld(input);
            if (world == null) {
                return ArgumentParseResult.failure(new WorldParseException(input));
            }

            inputQueue.remove();
            return ArgumentParseResult.success(world);
        }

        @Nonnull
        @Override
        public List<String> suggestions(@Nonnull final CommandContext<C> commandContext, @Nonnull final String input) {
            return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
        }

    }


    public static final class WorldParseException extends IllegalArgumentException {

        private final String input;

        /**
         * Construct a new WorldParseException
         *
         * @param input Input
         */
        public WorldParseException(@Nonnull final String input) {
            this.input = input;
        }

        /**
         * Get the input provided by the sender
         *
         * @return Input
         */
        public String getInput() {
            return this.input;
        }

        @Override
        public String getMessage() {
            return String.format("'%s' is not a valid Minecraft world", this.input);
        }
    }

}
