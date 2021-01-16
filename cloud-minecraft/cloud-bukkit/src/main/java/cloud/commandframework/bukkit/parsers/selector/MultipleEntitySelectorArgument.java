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
import cloud.commandframework.bukkit.arguments.selector.MultipleEntitySelector;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;

public final class MultipleEntitySelectorArgument<C> extends CommandArgument<C, MultipleEntitySelector> {

    private MultipleEntitySelectorArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new MultipleEntitySelectorParser<>(), defaultValue, MultipleEntitySelector.class,
                suggestionsProvider, defaultDescription
        );
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> MultipleEntitySelectorArgument.@NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new MultipleEntitySelectorArgument.Builder<>(name);
    }

    /**
     * Create a new required command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, MultipleEntitySelector> of(final @NonNull String name) {
        return MultipleEntitySelectorArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, MultipleEntitySelector> optional(final @NonNull String name) {
        return MultipleEntitySelectorArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new required command argument with a default value
     *
     * @param name                  Argument name
     * @param defaultEntitySelector Default player
     * @param <C>                   Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, MultipleEntitySelector> optional(
            final @NonNull String name,
            final @NonNull String defaultEntitySelector
    ) {
        return MultipleEntitySelectorArgument.<C>newBuilder(name).asOptionalWithDefault(defaultEntitySelector).build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, MultipleEntitySelector> {

        private Builder(final @NonNull String name) {
            super(MultipleEntitySelector.class, name);
        }

        /**
         * Builder a new argument
         *
         * @return Constructed argument
         */
        @Override
        public @NonNull MultipleEntitySelectorArgument<C> build() {
            return new MultipleEntitySelectorArgument<>(this.isRequired(), this.getName(), this.getDefaultValue(),
                    this.getSuggestionsProvider(), this.getDefaultDescription()
            );
        }

    }


    public static final class MultipleEntitySelectorParser<C> implements ArgumentParser<C, MultipleEntitySelector> {

        @Override
        public @NonNull ArgumentParseResult<MultipleEntitySelector> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            if (!commandContext.<Set<CloudBukkitCapabilities>>get("CloudBukkitCapabilities").contains(
                    CloudBukkitCapabilities.BRIGADIER)) {
                return ArgumentParseResult.failure(new SelectorParseException(
                        "",
                        commandContext,
                        SelectorParseException.FailureReason.UNSUPPORTED_VERSION,
                        MultipleEntitySelectorParser.class
                ));
            }
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        MultipleEntitySelectorParser.class,
                        commandContext
                ));
            }
            inputQueue.remove();

            List<Entity> entities;
            try {
                entities = Bukkit.selectEntities(commandContext.get("BukkitCommandSender"), input);
            } catch (IllegalArgumentException e) {
                return ArgumentParseResult.failure(new SelectorParseException(
                        input,
                        commandContext,
                        SelectorParseException.FailureReason.MALFORMED_SELECTOR,
                        MultipleEntitySelectorParser.class
                ));
            }

            return ArgumentParseResult.success(new MultipleEntitySelector(input, entities));
        }

    }

}
