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
package cloud.commandframework.paper.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.bukkit.internal.CraftBukkitReflection;
import cloud.commandframework.bukkit.parsers.WorldArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Argument type that parses Bukkit {@link World worlds} from a {@link NamespacedKey}.
 *
 * <p>Falls back to parsing by name, using the {@link WorldArgument.WorldParser} on server implementations where {@link World}
 * does not implement {@link org.bukkit.Keyed}.</p>
 *
 * @param <C> Command sender type
 * @since 1.6.0
 */
public final class KeyedWorldArgument<C> extends CommandArgument<C, World> {

    KeyedWorldArgument(
            final @NonNull String name,
            final @Nullable SuggestionProvider<C> suggestionProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(name, new Parser<>(), World.class, suggestionProvider, defaultDescription);
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     * @since 1.6.0
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
     * @since 1.6.0
     */
    public static <C> @NonNull KeyedWorldArgument<C> of(final @NonNull String name) {
        return KeyedWorldArgument.<C>builder(name).build();
    }


    /**
     * Builder for {@link KeyedWorldArgument}.
     *
     * @param <C> sender type
     * @since 1.6.0
     */
    public static final class Builder<C> extends CommandArgument.TypedBuilder<C, World, Builder<C>> {

        private Builder(final @NonNull String name) {
            super(World.class, name);
        }

        /**
         * Build a new {@link KeyedWorldArgument}.
         *
         * @return constructed argument
         * @since 1.6.0
         */
        @Override
        public @NonNull KeyedWorldArgument<C> build() {
            return new KeyedWorldArgument<>(
                    this.getName(),
                    this.suggestionProvider(),
                    this.getDefaultDescription()
            );
        }
    }

    /**
     * Parser for {@link World worlds} from their {@link NamespacedKey}.
     *
     * @param <C> sender type
     * @since 1.6.0
     */
    public static final class Parser<C> implements ArgumentParser<C, World> {

        private final ArgumentParser<C, World> parser;

        /**
         * Create a new {@link Parser}.
         */
        public Parser() {
            final Class<?> keyed = CraftBukkitReflection.findClass("org.bukkit.Keyed");
            if (keyed != null && keyed.isAssignableFrom(World.class)) {
                this.parser = null;
            } else {
                this.parser = new WorldArgument.WorldParser<>();
            }
        }

        @Override
        public @NonNull ArgumentParseResult<@NonNull World> parse(
                @NonNull final CommandContext<@NonNull C> commandContext,
                @NonNull final CommandInput commandInput
        ) {
            final String input = commandInput.peekString();
            if (input.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        Parser.class,
                        commandContext
                ));
            }

            if (this.parser != null) {
                return this.parser.parse(commandContext, commandInput);
            }

            final NamespacedKey key = NamespacedKey.fromString(commandInput.readString());
            if (key == null) {
                return ArgumentParseResult.failure(new WorldArgument.WorldParseException(input, commandContext));
            }

            final World world = Bukkit.getWorld(key);
            if (world == null) {
                return ArgumentParseResult.failure(new WorldArgument.WorldParseException(input, commandContext));
            }

            return ArgumentParseResult.success(world);
        }

        @Override
        public @NonNull List<@NonNull Suggestion> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            if (this.parser != null) {
                return this.parser.suggestions(commandContext, input);
            }

            final List<World> worlds = Bukkit.getWorlds();
            final List<Suggestion> completions = new ArrayList<>(worlds.size() * 2);
            for (final World world : worlds) {
                final NamespacedKey key = world.getKey();
                if (!input.isEmpty() && key.getNamespace().equals(NamespacedKey.MINECRAFT_NAMESPACE)) {
                    completions.add(Suggestion.simple(key.getKey()));
                }
                completions.add(Suggestion.simple(key.getNamespace() + ':' + key.getKey()));
            }
            return completions;
        }
    }
}
