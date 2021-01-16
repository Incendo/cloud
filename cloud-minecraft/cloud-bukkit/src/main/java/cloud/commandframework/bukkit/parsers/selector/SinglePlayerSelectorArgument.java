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
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.bukkit.arguments.selector.SinglePlayerSelector;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;

public final class SinglePlayerSelectorArgument<C> extends CommandArgument<C, SinglePlayerSelector> {

    private SinglePlayerSelectorArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                new SinglePlayerSelectorParser<>(),
                defaultValue,
                SinglePlayerSelector.class,
                suggestionsProvider,
                defaultDescription
        );
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> SinglePlayerSelectorArgument.@NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new SinglePlayerSelectorArgument.Builder<>(name);
    }

    /**
     * Create a new required command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, SinglePlayerSelector> of(final @NonNull String name) {
        return SinglePlayerSelectorArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, SinglePlayerSelector> optional(final @NonNull String name) {
        return SinglePlayerSelectorArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new required command argument with a default value
     *
     * @param name                  Argument name
     * @param defaultEntitySelector Default player
     * @param <C>                   Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, SinglePlayerSelector> optional(
            final @NonNull String name,
            final @NonNull String defaultEntitySelector
    ) {
        return SinglePlayerSelectorArgument.<C>newBuilder(name).asOptionalWithDefault(defaultEntitySelector).build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, SinglePlayerSelector> {

        private Builder(final @NonNull String name) {
            super(SinglePlayerSelector.class, name);
        }

        /**
         * Builder a new argument
         *
         * @return Constructed argument
         */
        @Override
        public @NonNull SinglePlayerSelectorArgument<C> build() {
            return new SinglePlayerSelectorArgument<>(this.isRequired(), this.getName(), this.getDefaultValue(),
                    this.getSuggestionsProvider(), this.getDefaultDescription()
            );
        }

    }


    public static final class SinglePlayerSelectorParser<C> implements ArgumentParser<C, SinglePlayerSelector> {

        @Override
        public @NonNull ArgumentParseResult<SinglePlayerSelector> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        SinglePlayerSelectorParser.class,
                        commandContext
                ));
            }
            inputQueue.remove();

            if (!commandContext.<Set<CloudBukkitCapabilities>>get("CloudBukkitCapabilities").contains(
                    CloudBukkitCapabilities.BRIGADIER)) {
                @SuppressWarnings("deprecation")
                Player player = Bukkit.getPlayer(input);

                if (player == null) {
                    return ArgumentParseResult.failure(new PlayerArgument.PlayerParseException(input, commandContext));
                }
                return ArgumentParseResult.success(new SinglePlayerSelector(input, ImmutableList.of(player)));
            }

            List<Entity> entities;
            try {
                entities = Bukkit.selectEntities(commandContext.get("BukkitCommandSender"), input);
            } catch (IllegalArgumentException e) {
                return ArgumentParseResult.failure(new SelectorParseException(
                        input,
                        commandContext,
                        SelectorParseException.FailureReason.MALFORMED_SELECTOR,
                        SinglePlayerSelectorParser.class
                ));
            }

            for (Entity e : entities) {
                if (!(e instanceof Player)) {
                    return ArgumentParseResult.failure(new SelectorParseException(
                            input,
                            commandContext,
                            SelectorParseException.FailureReason.NON_PLAYER_IN_PLAYER_SELECTOR,
                            SinglePlayerSelectorParser.class
                    ));
                }
            }
            if (entities.size() > 1) {
                return ArgumentParseResult.failure(new SelectorParseException(
                        input,
                        commandContext,
                        SelectorParseException.FailureReason.TOO_MANY_PLAYERS,
                        SinglePlayerSelectorParser.class
                ));
            }

            return ArgumentParseResult.success(new SinglePlayerSelector(input, entities));
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            List<String> output = new ArrayList<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                output.add(player.getName());
            }

            return output;
        }

    }

}
