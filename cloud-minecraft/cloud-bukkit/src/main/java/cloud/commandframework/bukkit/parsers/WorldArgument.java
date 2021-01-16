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
package cloud.commandframework.bukkit.parsers;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.bukkit.BukkitCaptionKeys;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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

    protected WorldArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new WorldParser<>(), defaultValue, World.class, suggestionsProvider, defaultDescription);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> CommandArgument.@NonNull Builder<C, World> newBuilder(final @NonNull String name) {
        return new WorldArgument.Builder<>(name);
    }

    /**
     * Create a new required argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, World> of(final @NonNull String name) {
        return WorldArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, World> optional(final @NonNull String name) {
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
    public static <C> @NonNull CommandArgument<C, World> optional(
            final @NonNull String name,
            final @NonNull String defaultValue
    ) {
        return WorldArgument.<C>newBuilder(name).asOptionalWithDefault(defaultValue).build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, World> {

        private Builder(final @NonNull String name) {
            super(World.class, name);
        }

        @Override
        public @NonNull CommandArgument<C, World> build() {
            return new WorldArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

    }


    public static final class WorldParser<C> implements ArgumentParser<C, World> {

        @Override
        public @NonNull ArgumentParseResult<World> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        WorldParser.class,
                        commandContext
                ));
            }

            final World world = Bukkit.getWorld(input);
            if (world == null) {
                return ArgumentParseResult.failure(new WorldParseException(input, commandContext));
            }

            inputQueue.remove();
            return ArgumentParseResult.success(world);
        }

        @Override
        public @NonNull List<String> suggestions(final @NonNull CommandContext<C> commandContext, final @NonNull String input) {
            return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
        }

    }


    public static final class WorldParseException extends ParserException {

        private static final long serialVersionUID = 561648144491587450L;
        private final String input;

        /**
         * Construct a new WorldParseException
         *
         * @param input   Input
         * @param context Command context
         */
        public WorldParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    WorldParser.class,
                    context,
                    BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_WORLD,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Get the input provided by the sender
         *
         * @return Input
         */
        public @NonNull String getInput() {
            return this.input;
        }

    }

}
