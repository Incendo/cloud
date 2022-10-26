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
package cloud.commandframework.bukkit.parsers.selector;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.bukkit.arguments.selector.MultiplePlayerSelector;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Argument type for parsing {@link MultiplePlayerSelector}. On Minecraft 1.13+
 * this argument uses Minecraft's built-in entity selector argument for parsing
 * and suggestions. On prior versions, this argument behaves similarly to
 * {@link PlayerArgument}.
 *
 * @param <C> sender type
 */
public final class MultiplePlayerSelectorArgument<C> extends CommandArgument<C, MultiplePlayerSelector> {

    private MultiplePlayerSelectorArgument(
            final boolean allowEmpty,
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new MultiplePlayerSelectorParser<>(allowEmpty), defaultValue, MultiplePlayerSelector.class,
                suggestionsProvider, defaultDescription
        );
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     * @deprecated prefer {@link #builder(String)}
     */
    @API(status = API.Status.DEPRECATED, since = "1.8.0")
    @Deprecated
    public static <C> Builder<C> newBuilder(final @NonNull String name) {
        return builder(name);
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return new builder
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull MultiplePlayerSelectorArgument<C> of(final @NonNull String name) {
        return MultiplePlayerSelectorArgument.<C>builder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull MultiplePlayerSelectorArgument<C> optional(final @NonNull String name) {
        return MultiplePlayerSelectorArgument.<C>builder(name).asOptional().build();
    }

    /**
     * Create a new required command argument with a default value
     *
     * @param name                  Argument name
     * @param defaultEntitySelector Default player
     * @param <C>                   Command sender type
     * @return Created argument
     */
    public static <C> @NonNull MultiplePlayerSelectorArgument<C> optional(
            final @NonNull String name,
            final @NonNull String defaultEntitySelector
    ) {
        return MultiplePlayerSelectorArgument.<C>builder(name).asOptionalWithDefault(defaultEntitySelector).build();
    }


    public static final class Builder<C> extends CommandArgument.TypedBuilder<C, MultiplePlayerSelector, Builder<C>> {

        private boolean allowEmpty = true;

        private Builder(final @NonNull String name) {
            super(MultiplePlayerSelector.class, name);
        }

        /**
         * Set whether to allow empty results.
         *
         * @param allowEmpty whether to allow empty results
         * @return builder instance
         * @since 1.8.0
         */
        @API(status = API.Status.STABLE, since = "1.8.0")
        public @NonNull Builder<C> allowEmpty(final boolean allowEmpty) {
            this.allowEmpty = allowEmpty;
            return this;
        }

        /**
         * Builder a new argument
         *
         * @return Constructed argument
         */
        @Override
        public @NonNull MultiplePlayerSelectorArgument<C> build() {
            return new MultiplePlayerSelectorArgument<>(
                    this.allowEmpty,
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }
    }


    public static final class MultiplePlayerSelectorParser<C> extends SelectorUtils.PlayerSelectorParser<C, MultiplePlayerSelector> {

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

        @Override
        public MultiplePlayerSelector mapResult(
                final @NonNull String input,
                final SelectorUtils.@NonNull EntitySelectorWrapper wrapper
        ) throws Exception {
            final List<Player> players = wrapper.players();
            if (players.isEmpty() && !this.allowEmpty) {
                throw ((SimpleCommandExceptionType) NO_PLAYERS_EXCEPTION_TYPE.get()).create();
            }
            return new MultiplePlayerSelector(input, new ArrayList<>(players));
        }

        @Override
        protected @NonNull ArgumentParseResult<MultiplePlayerSelector> legacyParse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            @SuppressWarnings("deprecation") final @Nullable Player player = Bukkit.getPlayer(input);

            if (player == null) {
                return ArgumentParseResult.failure(new PlayerArgument.PlayerParseException(input, commandContext));
            }
            inputQueue.remove();
            return ArgumentParseResult.success(new MultiplePlayerSelector(input, ImmutableList.of(player)));
        }
    }
}
