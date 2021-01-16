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
package cloud.commandframework.bungee.arguments;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.bungee.BungeeCaptionKeys;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import io.leangen.geantyref.TypeToken;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Argument parser for {@link ProxiedPlayer players}
 *
 * @param <C> Command sender type
 * @since 1.1.0
 */
public final class PlayerArgument<C> extends CommandArgument<C, ProxiedPlayer> {

    private PlayerArgument(
            final boolean required,
            final @NonNull String name,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionProvider,
            final @NonNull ArgumentDescription defaultDescription,
            final @NonNull Collection<@NonNull BiFunction<@NonNull CommandContext<C>, @NonNull Queue<@NonNull String>,
                    @NonNull ArgumentParseResult<Boolean>>> argumentPreprocessors
    ) {
        super(
                required,
                name,
                new PlayerParser<>(),
                "",
                TypeToken.get(ProxiedPlayer.class),
                suggestionProvider,
                defaultDescription,
                argumentPreprocessors
        );
    }

    /**
     * Create a new argument builder
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Constructed builder
     **/
    public static <C> CommandArgument.@NonNull Builder<C, ProxiedPlayer> newBuilder(
            final @NonNull String name
    ) {
        return new Builder<C>(
                name
        ).withParser(
                new PlayerParser<>()
        );
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
        return PlayerArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional player argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> CommandArgument<C, ProxiedPlayer> optional(
            final @NonNull String name
    ) {
        return PlayerArgument.<C>newBuilder(name).asOptional().build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, ProxiedPlayer> {

        private Builder(
                final @NonNull String name
        ) {
            super(TypeToken.get(ProxiedPlayer.class), name);
        }

        @Override
        public @NonNull CommandArgument<@NonNull C, @NonNull ProxiedPlayer> build() {
            return new PlayerArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription(),
                    new LinkedList<>()
            );
        }

    }


    public static final class PlayerParser<C> implements ArgumentParser<C, ProxiedPlayer> {

        @Override
        public @NonNull ArgumentParseResult<@NonNull ProxiedPlayer> parse(
                final @NonNull CommandContext<@NonNull C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        PlayerParser.class,
                        commandContext
                ));
            }
            final ProxiedPlayer player = commandContext.<ProxyServer>get("ProxyServer").getPlayer(input);
            if (player == null) {
                return ArgumentParseResult.failure(
                        new PlayerParseException(
                                input,
                                commandContext
                        )
                );
            }
            inputQueue.remove();
            return ArgumentParseResult.success(player);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
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
