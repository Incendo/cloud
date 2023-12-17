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
package cloud.commandframework.bukkit.parsers.selector;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.bukkit.data.SinglePlayerSelector;
import cloud.commandframework.bukkit.parsers.PlayerParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Parser for {@link SinglePlayerSelector}. On Minecraft 1.13+
 * this argument uses Minecraft's built-in entity selector argument for parsing
 * and suggestions. On prior versions, this argument behaves similarly to
 * {@link PlayerParser}.
 *
 * @param <C> sender type
 */
public final class SinglePlayerSelectorParser<C> extends SelectorUtils.PlayerSelectorParser<C, SinglePlayerSelector> {

    /**
     * Creates a new single player selector parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, SinglePlayerSelector> singlePlayerSelectorParser() {
        return ParserDescriptor.of(new SinglePlayerSelectorParser<>(), SinglePlayerSelector.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #singlePlayerSelectorParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, SinglePlayerSelector> singlePlayerSelectorComponent() {
        return CommandComponent.<C, SinglePlayerSelector>builder().parser(singlePlayerSelectorParser());
    }

    /**
     * Creates a new {@link SinglePlayerSelectorParser}.
     */
    public SinglePlayerSelectorParser() {
        super(true);
    }

    @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
    @Override
    public SinglePlayerSelector mapResult(
            final @NonNull String input,
            final SelectorUtils.@NonNull EntitySelectorWrapper wrapper
    ) {
        final Player player = wrapper.singlePlayer();
        return new SinglePlayerSelector() {
            @Override
            public @NonNull Player single() {
                return player;
            }

            @Override
            public @NonNull String inputString() {
                return input;
            }
        };
    }

    @Override
    protected @NonNull CompletableFuture<SinglePlayerSelector> legacyParse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final CompletableFuture<SinglePlayerSelector> result = new CompletableFuture<>();
        final String input = commandInput.peekString();
        @SuppressWarnings("deprecation") final @Nullable Player player = Bukkit.getPlayer(input);

        if (player == null) {
            result.completeExceptionally(new PlayerParser.PlayerParseException(input, commandContext));
        } else {
            final String pop = commandInput.readString();
            result.complete(new SinglePlayerSelector() {
                @Override
                public @NonNull Player single() {
                    return player;
                }

                @Override
                public @NonNull String inputString() {
                    return pop;
                }
            });
        }

        return result;
    }
}
