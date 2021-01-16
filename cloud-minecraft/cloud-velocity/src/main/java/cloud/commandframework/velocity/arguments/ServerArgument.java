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
package cloud.commandframework.velocity.arguments;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.velocity.VelocityCaptionKeys;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
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
 * Argument parser for {@link RegisteredServer servers}
 *
 * @param <C> Command sender type
 * @since 1.1.0
 */
public final class ServerArgument<C> extends CommandArgument<C, RegisteredServer> {

    private ServerArgument(
            final boolean required,
            final @NonNull String name,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription,
            final @NonNull Collection<@NonNull BiFunction<@NonNull CommandContext<C>, @NonNull Queue<@NonNull String>,
                    @NonNull ArgumentParseResult<Boolean>>> argumentPreprocessors
    ) {
        super(
                required,
                name,
                new ServerParser<>(),
                "",
                TypeToken.get(RegisteredServer.class),
                suggestionsProvider,
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
     */
    public static <C> CommandArgument.@NonNull Builder<C, RegisteredServer> newBuilder(
            final @NonNull String name
    ) {
        return new Builder<C>(
                name
        ).withParser(
                new ServerParser<>()
        );
    }

    /**
     * Create a new required player argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, RegisteredServer> of(
            final @NonNull String name
    ) {
        return ServerArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional server argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, RegisteredServer> optional(final @NonNull String name) {
        return ServerArgument.<C>newBuilder(name).asOptional().build();
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, RegisteredServer> {

        private Builder(final @NonNull String name) {
            super(TypeToken.get(RegisteredServer.class), name);
        }

        @Override
        public @NonNull CommandArgument<@NonNull C, @NonNull RegisteredServer> build() {
            return new ServerArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription(),
                    new LinkedList<>()
            );
        }

    }

    public static final class ServerParser<C> implements ArgumentParser<C, RegisteredServer> {

        @Override
        public @NonNull ArgumentParseResult<@NonNull RegisteredServer> parse(
                final @NonNull CommandContext<@NonNull C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        ServerParser.class,
                        commandContext
                ));
            }
            final RegisteredServer server = commandContext.<ProxyServer>get("ProxyServer").getServer(input).orElse(null);
            if (server == null) {
                return ArgumentParseResult.failure(
                        new ServerParseException(
                                input,
                                commandContext
                        )
                );
            }
            inputQueue.remove();
            return ArgumentParseResult.success(server);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return commandContext.<ProxyServer>get("ProxyServer")
                    .getAllServers()
                    .stream()
                    .map(s -> s.getServerInfo().getName())
                    .collect(Collectors.toList());
        }

    }

    public static final class ServerParseException extends ParserException {

        private static final long serialVersionUID = 9168156226853233788L;

        private ServerParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    ServerParser.class,
                    context,
                    VelocityCaptionKeys.ARGUMENT_PARSE_FAILURE_SERVER,
                    CaptionVariable.of("input", input)
            );
        }

    }

}
