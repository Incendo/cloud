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
package cloud.commandframework.paper.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.bukkit.parsers.WorldArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

/**
 * Argument type that parses Bukkit {@link World worlds} from a {@link NamespacedKey}.
 *
 * @param <C> Command sender type
 */
public class KeyedWorldArgument<C> extends CommandArgument<C, World> {

    protected KeyedWorldArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new Parser<>(), defaultValue, World.class, suggestionsProvider, defaultDescription);
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> KeyedWorldArgument.@NonNull Builder<C> builder(final @NonNull String name) {
        return new KeyedWorldArgument.Builder<>(name);
    }

    /**
     * Create a new required {@link KeyedWorldArgument}.
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull KeyedWorldArgument<C> of(final @NonNull String name) {
        return KeyedWorldArgument.<C>builder(name).asRequired().build();
    }

    /**
     * Create a new optional {@link KeyedWorldArgument}.
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull KeyedWorldArgument<C> optional(final @NonNull String name) {
        return KeyedWorldArgument.<C>builder(name).asOptional().build();
    }

    /**
     * Create a new {@link KeyedWorldArgument} with the specified default value.
     *
     * @param name         Argument name
     * @param defaultValue Default value
     * @param <C>          Command sender type
     * @return Created argument
     */
    public static <C> @NonNull KeyedWorldArgument<C> optional(
            final @NonNull String name,
            final @NonNull String defaultValue
    ) {
        return KeyedWorldArgument.<C>builder(name).asOptionalWithDefault(defaultValue).build();
    }

    public static final class Builder<C> extends CommandArgument.TypedBuilder<C, World, Builder<C>> {

        private Builder(final @NonNull String name) {
            super(World.class, name);
        }

        @Override
        public @NonNull KeyedWorldArgument<C> build() {
            return new KeyedWorldArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

    }

    public static final class Parser<C> implements ArgumentParser<C, World> {

        @Override
        public @NonNull ArgumentParseResult<@NonNull World> parse(
                @NonNull final CommandContext<@NonNull C> commandContext,
                @NonNull final Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        Parser.class,
                        commandContext
                ));
            }

            final NamespacedKey key = NamespacedKey.fromString(input);
            if (key == null) {
                return ArgumentParseResult.failure(new WorldArgument.WorldParseException(input, commandContext));
            }

            final World world = Bukkit.getWorld(key);
            if (world == null) {
                return ArgumentParseResult.failure(new WorldArgument.WorldParseException(input, commandContext));
            }

            inputQueue.remove();
            return ArgumentParseResult.success(world);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            final List<String> completions = new ArrayList<>();
            for (final World world : Bukkit.getWorlds()) {
                if (world.getKey().getNamespace().equals(NamespacedKey.MINECRAFT)) {
                    completions.add(world.getKey().getKey());
                } else {
                    completions.add(world.getKey().toString());
                }
            }
            return completions;
        }

    }
}
