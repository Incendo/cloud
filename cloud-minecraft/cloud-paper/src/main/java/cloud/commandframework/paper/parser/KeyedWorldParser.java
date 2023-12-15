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
package cloud.commandframework.paper.parser;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.bukkit.internal.CraftBukkitReflection;
import cloud.commandframework.bukkit.parsers.WorldParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Parses Bukkit {@link World worlds} from a {@link NamespacedKey}.
 *
 * <p>Falls back to parsing by name, using the {@link WorldParser} on server implementations where {@link World}
 * does not implement {@link org.bukkit.Keyed}.</p>
 *
 * @param <C> Command sender type
 * @since 1.6.0
 */
public final class KeyedWorldParser<C> implements ArgumentParser<C, World>, SuggestionProvider<C> {

    /**
     * Creates a new keyed world parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, World> keyedWorldParser() {
        return ParserDescriptor.of(new KeyedWorldParser<>(), World.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #keyedWorldParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, World> keyedWorldComponent() {
        return CommandComponent.<C, World>builder().parser(keyedWorldParser());
    }

    private final ArgumentParser<C, World> parser;

    /**
     * Create a new {@link KeyedWorldParser}.
     */
    public KeyedWorldParser() {
        final Class<?> keyed = CraftBukkitReflection.findClass("org.bukkit.Keyed");
        if (keyed != null && keyed.isAssignableFrom(World.class)) {
            this.parser = null;
        } else {
            this.parser = new WorldParser<>();
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
                    KeyedWorldParser.class,
                    commandContext
            ));
        }

        if (this.parser != null) {
            return this.parser.parse(commandContext, commandInput);
        }

        final NamespacedKey key = NamespacedKey.fromString(commandInput.readString());
        if (key == null) {
            return ArgumentParseResult.failure(new WorldParser.WorldParseException(input, commandContext));
        }

        final World world = Bukkit.getWorld(key);
        if (world == null) {
            return ArgumentParseResult.failure(new WorldParser.WorldParseException(input, commandContext));
        }

        return ArgumentParseResult.success(world);
    }

    @Override
    public @NonNull CompletableFuture<@NonNull List<@NonNull Suggestion>> suggestionsFuture(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull String input
    ) {
        if (this.parser != null) {
            return this.parser.suggestionProvider().suggestionsFuture(commandContext, input);
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
        return CompletableFuture.completedFuture(completions);
    }
}
