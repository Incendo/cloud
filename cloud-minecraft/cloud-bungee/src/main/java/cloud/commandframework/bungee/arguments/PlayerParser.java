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
package cloud.commandframework.bungee.arguments;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.bungee.BungeeCaptionKeys;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import java.util.List;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Argument parser for {@link ProxiedPlayer players}
 *
 * @param <C> Command sender type
 * @since 2.0.0
 */
public final class PlayerParser<C> implements ArgumentParser<C, ProxiedPlayer>, BlockingSuggestionProvider.Strings<C> {

    /**
     * Creates a new player parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, ProxiedPlayer> playerParser() {
        return ParserDescriptor.of(new PlayerParser<>(), ProxiedPlayer.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #playerParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, ProxiedPlayer> playerComponent() {
        return CommandComponent.<C, ProxiedPlayer>builder().parser(playerParser());
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull ProxiedPlayer> parse(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.peekString();
        if (input.isEmpty()) {
            return ArgumentParseResult.failure(new NoInputProvidedException(
                    PlayerParser.class,
                    commandContext
            ));
        }
        final ProxiedPlayer player = commandContext.<ProxyServer>get("ProxyServer").getPlayer(commandInput.readString());
        if (player == null) {
            return ArgumentParseResult.failure(
                    new PlayerParseException(
                            input,
                            commandContext
                    )
            );
        }
        return ArgumentParseResult.success(player);
    }

    @Override
    public @NonNull List<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull String input
    ) {
        return commandContext.<ProxyServer>get("ProxyServer")
                .getPlayers()
                .stream()
                .map(ProxiedPlayer::getDisplayName)
                .collect(Collectors.toList());
    }

    public static final class PlayerParseException extends ParserException {

        private static final long serialVersionUID = -2685136673577959929L;

        private PlayerParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    PlayerParser.class,
                    context,
                    BungeeCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER,
                    CaptionVariable.of("input", input)
            );
        }
    }
}
