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
package cloud.commandframework.sponge.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.sponge.NodeSupplyingArgumentParser;
import cloud.commandframework.sponge.SpongeCommandContextKeys;
import cloud.commandframework.sponge.data.MultiplePlayerSelector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKeys;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.command.selector.Selector;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public final class MultiplePlayerSelectorArgument<C> extends CommandArgument<C, MultiplePlayerSelector> {

    private MultiplePlayerSelectorArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                new Parser<>(),
                defaultValue,
                MultiplePlayerSelector.class,
                suggestionsProvider,
                defaultDescription
        );
    }

    public static <C> @NonNull MultiplePlayerSelectorArgument<C> optional(final @NonNull String name) {
        return MultiplePlayerSelectorArgument.<C>builder(name).asOptional().build();
    }

    public static <C> @NonNull MultiplePlayerSelectorArgument<C> of(final @NonNull String name) {
        return MultiplePlayerSelectorArgument.<C>builder(name).build();
    }

    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    public static final class Parser<C> implements NodeSupplyingArgumentParser<C, MultiplePlayerSelector> {

        @Override
        public @NonNull ArgumentParseResult<@NonNull MultiplePlayerSelector> parse(
                @NonNull final CommandContext<@NonNull C> commandContext,
                @NonNull final Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            final Selector selector;
            try {
                selector = Selector.parse(input);
            } catch (final IllegalArgumentException ex) {
                return SelectorUtil.selectorParseFailure(ex);
            }
            if (!selector.playersOnly()) {
                return SelectorUtil.onlyPlayersAllowed();
            }
            final CommandCause cause = commandContext.get(SpongeCommandContextKeys.COMMAND_CAUSE_KEY);
            final Collection<Player> result = selector.select(cause).stream()
                    .map(e -> (Player) e).collect(Collectors.toList());
            if (result.isEmpty()) {
                return SelectorUtil.noPlayersFound();
            }
            inputQueue.remove();
            return ArgumentParseResult.success(new MultiplePlayerSelectorImpl(
                    selector,
                    input,
                    result
            ));
        }

        @Override
        public CommandTreeNode.@NonNull Argument<? extends CommandTreeNode.Argument<?>> node() {
            return ClientCompletionKeys.ENTITY.get().createNode().playersOnly();
        }

    }

    public static final class Builder<C> extends TypedBuilder<C, MultiplePlayerSelector, Builder<C>> {

        Builder(final @NonNull String name) {
            super(MultiplePlayerSelector.class, name);
        }

        @Override
        public @NonNull MultiplePlayerSelectorArgument<C> build() {
            return new MultiplePlayerSelectorArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

    }

    private static final class MultiplePlayerSelectorImpl implements MultiplePlayerSelector {

        private final Selector selector;
        private final String inputString;
        private final Collection<Player> result;

        private MultiplePlayerSelectorImpl(
                final Selector selector,
                final String inputString,
                final Collection<Player> result
        ) {
            this.selector = selector;
            this.inputString = inputString;
            this.result = result;
        }

        @Override
        public @NonNull Selector selector() {
            return this.selector;
        }

        @Override
        public @NonNull String inputString() {
            return this.inputString;
        }

        @Override
        public @NonNull Collection<Player> get() {
            return this.result;
        }

    }

}
