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

import cloud.commandframework.Description;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Argument parser for {@link net.md_5.bungee.api.config.ServerInfo servers}
 *
 * @param <C> Command sender type
 * @since 1.1.0
 */
public final class ServerArgument<C> extends CommandArgument<C, ServerInfo> {

    private ServerArgument(
            final @NonNull String name,
            final @Nullable SuggestionProvider<C> suggestionProvider,
            final @NonNull Description defaultDescription,
            final @NonNull Collection<@NonNull BiFunction<@NonNull CommandContext<C>, @NonNull CommandInput,
                    @NonNull ArgumentParseResult<Boolean>>> argumentPreprocessors
    ) {
        super(
                name,
                new ServerParser<>(),
                TypeToken.get(ServerInfo.class),
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
     * Create a new required server argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, ServerInfo> of(
            final @NonNull String name
    ) {
        return ServerArgument.<C>builder(name).build();
    }


    public static final class Builder<C> extends CommandArgument.TypedBuilder<C, ServerInfo, Builder<C>> {

        private Builder(
                final @NonNull String name
        ) {
            super(TypeToken.get(ServerInfo.class), name);
        }

        @Override
        public @NonNull CommandArgument<@NonNull C, @NonNull ServerInfo> build() {
            return new ServerArgument<>(
                    this.getName(),
                    this.suggestionProvider(),
                    this.getDefaultDescription(),
                    new LinkedList<>()
            );
        }
    }

    public static final class ServerParser<C> implements ArgumentParser<C, ServerInfo> {

        @Override
        public @NonNull ArgumentParseResult<@NonNull ServerInfo> parse(
                final @NonNull CommandContext<@NonNull C> commandContext,
                final @NonNull CommandInput commandInput
        ) {
            final String input = commandInput.peekString();
            if (input.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        ServerParser.class,
                        commandContext
                ));
            }
            final ServerInfo server = commandContext.<ProxyServer>get("ProxyServer").getServerInfo(commandInput.readString());
            if (server == null) {
                return ArgumentParseResult.failure(
                        new ServerParseException(
                                input,
                                commandContext
                        )
                );
            }
            return ArgumentParseResult.success(server);
        }

        @Override
        public @NonNull List<@NonNull String> stringSuggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return new ArrayList<>(commandContext.<ProxyServer>get("ProxyServer").getServers().keySet());
        }
    }

    public static final class ServerParseException extends ParserException {

        private static final long serialVersionUID = -3825941611365494659L;

        private ServerParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    ServerParser.class,
                    context,
                    BungeeCaptionKeys.ARGUMENT_PARSE_FAILURE_SERVER,
                    CaptionVariable.of("input", input)
            );
        }
    }
}
