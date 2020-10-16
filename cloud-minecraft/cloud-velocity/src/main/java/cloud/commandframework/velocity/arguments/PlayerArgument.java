//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
package cloud.commandframework.velocity.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.velocity.VelocityCaptionKeys;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Argument parser for {@link Player players}
 *
 * @param <C> Command sender type
 */
public final class PlayerArgument<C> extends CommandArgument<C, Player> {

    private PlayerArgument(
            final @NonNull ProxyServer proxyServer,
            final boolean required,
            final @NonNull String name,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull Collection<@NonNull BiFunction<@NonNull CommandContext<C>, @NonNull Queue<@NonNull String>,
                    @NonNull ArgumentParseResult<Boolean>>> argumentPreprocessors
    ) {
        super(
                required,
                name,
                new PlayerParser<>(proxyServer),
                "",
                TypeToken.get(Player.class),
                suggestionsProvider,
                argumentPreprocessors
        );
    }

    /**
     * Create a new argument builder
     *
     * @param name        Argument name
     * @param proxyServer Proxy server instance
     * @param <C>         Command sender type
     * @return Constructed builder
     */
    public static <C> CommandArgument.@NonNull Builder<C, Player> newBuilder(
            final @NonNull String name,
            final @NonNull ProxyServer proxyServer
    ) {
        return new Builder<C>(
                name,
                proxyServer
        ).withParser(
                new PlayerParser<>(
                        proxyServer
                )
        );
    }

    /**
     * Create a new required player argument
     *
     * @param name        Argument name
     * @param proxyServer Proxy server instance
     * @param <C>         Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Player> of(
            final @NonNull String name,
            final @NonNull ProxyServer proxyServer
    ) {
        return PlayerArgument.<C>newBuilder(name, proxyServer).asRequired().build();
    }

    /**
     * Create a new optional player argument
     *
     * @param name        Argument name
     * @param proxyServer Proxy server instance
     * @param <C>         Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Player> optional(
            final @NonNull String name,
            final @NonNull ProxyServer proxyServer
    ) {
        return PlayerArgument.<C>newBuilder(name, proxyServer).asOptional().build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, Player> {

        private final ProxyServer proxyServer;

        private Builder(
                final @NonNull String name,
                final @NonNull ProxyServer proxyServer
        ) {
            super(TypeToken.get(Player.class), name);
            this.proxyServer = proxyServer;
        }

        @Override
        public @NonNull CommandArgument<@NonNull C, @NonNull Player> build() {
            return new PlayerArgument<>(
                    this.proxyServer,
                    this.isRequired(),
                    this.getName(),
                    this.getSuggestionsProvider(),
                    new LinkedList<>()
            );
        }

    }


    public static final class PlayerParser<C> implements ArgumentParser<C, Player> {

        private final ProxyServer proxyServer;

        /**
         * Create a new player parser
         *
         * @param proxyServer Proxy server instance
         */
        public PlayerParser(
                @NonNull final ProxyServer proxyServer
        ) {
            this.proxyServer = proxyServer;
        }

        @Override
        public @NonNull ArgumentParseResult<@NonNull Player> parse(
                @NonNull final CommandContext<@NonNull C> commandContext,
                @NonNull final Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        PlayerParser.class,
                        commandContext
                ));
            }
            final Player player = this.proxyServer.getPlayer(input).orElse(null);
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
            return this.proxyServer.getAllPlayers().stream().map(Player::getUsername).collect(Collectors.toList());
        }

        @Override
        public boolean isContextFree() {
            return true;
        }

    }


    public static final class PlayerParseException extends ParserException {

        private PlayerParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    PlayerParser.class,
                    context,
                    VelocityCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER,
                    CaptionVariable.of("input", input)
            );
        }

    }

}
