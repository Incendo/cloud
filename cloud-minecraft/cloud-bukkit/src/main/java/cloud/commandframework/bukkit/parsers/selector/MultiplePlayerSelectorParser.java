//
// MIT License
//
// Copyright (c) 2024 Incendo
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
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.bukkit.data.MultiplePlayerSelector;
import cloud.commandframework.bukkit.parsers.PlayerParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Parser for {@link MultiplePlayerSelector}. On Minecraft 1.13+
 * this argument uses Minecraft's built-in entity selector argument for parsing
 * and suggestions. On prior versions, this argument behaves similarly to
 * {@link PlayerParser}.
 *
 * @param <C> sender type
 */
public final class MultiplePlayerSelectorParser<C> extends SelectorUtils.PlayerSelectorParser<C, MultiplePlayerSelector> {

    /**
     * Creates a new multiple player selector parser that allows empty results.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, MultiplePlayerSelector> multiplePlayerSelectorParser() {
        return multiplePlayerSelectorParser(true /* allowEmpty */);
    }

    /**
     * Creates a new multiple player selector parser.
     *
     * @param allowEmpty whether to allow empty selections
     * @param <C>        command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, MultiplePlayerSelector> multiplePlayerSelectorParser(final boolean allowEmpty) {
        return ParserDescriptor.of(new MultiplePlayerSelectorParser<>(allowEmpty), MultiplePlayerSelector.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #multiplePlayerSelectorParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, MultiplePlayerSelector> multiplePlayerSelectorComponent() {
        return CommandComponent.<C, MultiplePlayerSelector>builder().parser(multiplePlayerSelectorParser());
    }

    private final boolean allowEmpty;

    /**
     * Creates a new {@link MultiplePlayerSelectorParser}.
     *
     * @param allowEmpty Whether to allow an empty result
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    public MultiplePlayerSelectorParser(final boolean allowEmpty) {
        super(false);
        this.allowEmpty = allowEmpty;
    }

    /**
     * Creates a new {@link MultiplePlayerSelectorParser}.
     */
    public MultiplePlayerSelectorParser() {
        this(true);
    }

    @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
    @Override
    public MultiplePlayerSelector mapResult(
            final @NonNull String input,
            final SelectorUtils.@NonNull EntitySelectorWrapper wrapper
    ) {
        final List<Player> players = wrapper.players();
        if (players.isEmpty() && !this.allowEmpty) {
            new Thrower(NO_PLAYERS_EXCEPTION_TYPE.get()).throwIt();
        }
        return new MultiplePlayerSelector() {
            @Override
            public @NonNull String inputString() {
                return input;
            }

            @Override
            public @NonNull Collection<Player> values() {
                return Collections.unmodifiableCollection(players);
            }
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    protected @NonNull CompletableFuture<ArgumentParseResult<MultiplePlayerSelector>> legacyParse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.peekString();
        final @Nullable Player player = Bukkit.getPlayer(input);

        if (player == null) {
            return CompletableFuture.completedFuture(
                    ArgumentParseResult.failure(new PlayerParser.PlayerParseException(input, commandContext)));
        }

        final String pop = commandInput.readString();
        return ArgumentParseResult.successFuture(new MultiplePlayerSelector() {
            @Override
            public @NonNull String inputString() {
                return pop;
            }

            @Override
            public @NonNull Collection<Player> values() {
                return Collections.singletonList(player);
            }
        });
    }
}
