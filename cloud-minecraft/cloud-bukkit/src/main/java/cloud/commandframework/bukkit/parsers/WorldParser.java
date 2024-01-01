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
package cloud.commandframework.bukkit.parsers;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.bukkit.BukkitCaptionKeys;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.ParserException;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class WorldParser<C> implements ArgumentParser<C, World>, BlockingSuggestionProvider.Strings<C> {

    /**
     * Creates a new world parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, World> worldParser() {
        return ParserDescriptor.of(new WorldParser<>(), World.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #worldParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, World> characterComponent() {
        return CommandComponent.<C, World>builder().parser(worldParser());
    }

    @Override
    public @NonNull ArgumentParseResult<World> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.readString();
        final World world = Bukkit.getWorld(input);
        if (world == null) {
            return ArgumentParseResult.failure(new WorldParseException(input, commandContext));
        }

        return ArgumentParseResult.success(world);
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext,
                                                                final @NonNull CommandInput input) {
        return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
    }


    public static final class WorldParseException extends ParserException {

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
