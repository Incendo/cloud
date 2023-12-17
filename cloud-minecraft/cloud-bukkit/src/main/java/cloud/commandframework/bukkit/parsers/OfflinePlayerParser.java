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
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.bukkit.BukkitCaptionKeys;
import cloud.commandframework.bukkit.BukkitCommandContextKeys;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import java.util.List;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Parser type that parses into {@link OfflinePlayer}. This is not thread safe. This
 * may also result in a blocking request to get the UUID for the offline player.
 * <p>
 * Avoid using this type if possible.
 *
 * @param <C> Command sender type
 */
public final class OfflinePlayerParser<C> implements ArgumentParser<C, OfflinePlayer>, BlockingSuggestionProvider<C> {

    /**
     * Creates a new offline player parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, OfflinePlayer> offlinePlayerParser() {
        return ParserDescriptor.of(new OfflinePlayerParser<>(), OfflinePlayer.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #offlinePlayerParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, OfflinePlayer> playerComponent() {
        return CommandComponent.<C, OfflinePlayer>builder().parser(offlinePlayerParser());
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NonNull ArgumentParseResult<OfflinePlayer> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.peekString();
        if (input.isEmpty()) {
            return ArgumentParseResult.failure(new NoInputProvidedException(
                    OfflinePlayerParser.class,
                    commandContext
            ));
        }

        final OfflinePlayer player = Bukkit.getOfflinePlayer(commandInput.readString());

        if (!player.hasPlayedBefore() && !player.isOnline()) {
            return ArgumentParseResult.failure(new OfflinePlayerParseException(input, commandContext));
        }

        return ArgumentParseResult.success(player);
    }

    @Override
    public @NonNull List<@NonNull Suggestion> suggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull String input
    ) {
        final CommandSender bukkit = commandContext.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER);
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> !(bukkit instanceof Player && !((Player) bukkit).canSee(player)))
                .map(Player::getName)
                .map(Suggestion::simple)
                .collect(Collectors.toList());
    }


    /**
     * OfflinePlayer parse exception
     */
    public static final class OfflinePlayerParseException extends ParserException {

        private static final long serialVersionUID = 7632293268451349508L;
        private final String input;

        /**
         * Construct a new OfflinePlayer parse exception
         *
         * @param input   String input
         * @param context Command context
         */
        public OfflinePlayerParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    OfflinePlayerParser.class,
                    context,
                    BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Get the supplied input
         *
         * @return String value
         */
        public @NonNull String getInput() {
            return this.input;
        }
    }
}
