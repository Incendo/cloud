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

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.bungee.BungeeCaptionKeys;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import io.leangen.geantyref.TypeToken;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Argument parser for {@link ProxiedPlayer players}
 *
 * @param <C> Command sender type
 * @since 1.1.0
 */
public final class PlayerArgument<C> extends CommandArgument<C, ProxiedPlayer> {

    private PlayerArgument(
            final @NonNull String name,
            final @Nullable SuggestionProvider<C> suggestionProvider,
            final @NonNull ArgumentDescription defaultDescription,
            final @NonNull Collection<@NonNull BiFunction<@NonNull CommandContext<C>, @NonNull CommandInput,
                    @NonNull ArgumentParseResult<Boolean>>> argumentPreprocessors
    ) {
        super(
                name,
                new PlayerParser<>(),
                TypeToken.get(ProxiedPlayer.class),
                suggestionProvider,
                defaultDescription,
                argumentPreprocessors
        );
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return new {@link Builder}
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required player argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> CommandArgument<C, ProxiedPlayer> of(
            final @NonNull String name
    ) {
        return PlayerArgument.<C>builder(name).build();
    }


    public static final class Builder<C> extends CommandArgument.TypedBuilder<C, ProxiedPlayer, Builder<C>> {

        private Builder(
                final @NonNull String name
        ) {
            super(TypeToken.get(ProxiedPlayer.class), name);
        }

        @Override
        public @NonNull CommandArgument<@NonNull C, @NonNull ProxiedPlayer> build() {
            return new PlayerArgument<>(
                    this.getName(),
                    this.suggestionProvider(),
                    this.getDefaultDescription(),
                    new LinkedList<>()
            );
        }
    }


    public static final class PlayerParser<C> implements ArgumentParser<C, ProxiedPlayer> {

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

        @Override
        public boolean isContextFree() {
            return true;
        }
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
