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
package cloud.commandframework.bukkit.parsers.selector;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.bukkit.arguments.selector.SingleEntitySelector;
import cloud.commandframework.context.CommandContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;

public final class SingleEntitySelectorArgument<C> extends CommandArgument<C, SingleEntitySelector> {

    private SingleEntitySelectorArgument(final boolean required,
                                         @NonNull final String name,
                                         @NonNull final String defaultValue,
                                         @Nullable final BiFunction<@NonNull CommandContext<C>, @NonNull String,
                                                 @NonNull List<@NonNull String>> suggestionsProvider) {
        super(required, name, new SingleEntitySelectorParser<>(), defaultValue, SingleEntitySelector.class, suggestionsProvider);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> SingleEntitySelectorArgument.@NonNull Builder<C> newBuilder(@NonNull final String name) {
        return new SingleEntitySelectorArgument.Builder<>(name);
    }

    /**
     * Create a new required command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, SingleEntitySelector> of(@NonNull final String name) {
        return SingleEntitySelectorArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, SingleEntitySelector> optional(@NonNull final String name) {
        return SingleEntitySelectorArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new required command argument with a default value
     *
     * @param name                  Argument name
     * @param defaultEntitySelector Default player
     * @param <C>                   Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, SingleEntitySelector> optional(@NonNull final String name,
                                                                                 @NonNull final String defaultEntitySelector) {
        return SingleEntitySelectorArgument.<C>newBuilder(name).asOptionalWithDefault(defaultEntitySelector).build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, SingleEntitySelector> {

        protected Builder(@NonNull final String name) {
            super(SingleEntitySelector.class, name);
        }

        /**
         * Builder a new argument
         *
         * @return Constructed argument
         */
        @Override
        public @NonNull SingleEntitySelectorArgument<C> build() {
            return new SingleEntitySelectorArgument<>(this.isRequired(), this.getName(), this.getDefaultValue(),
                                                      this.getSuggestionsProvider());
        }

    }


    public static final class SingleEntitySelectorParser<C> implements ArgumentParser<C, SingleEntitySelector> {

        @Override
        public @NonNull ArgumentParseResult<SingleEntitySelector> parse(@NonNull final CommandContext<C> commandContext,
                                                                        @NonNull final Queue<@NonNull String> inputQueue) {
            if (!((Set<CloudBukkitCapabilities>) commandContext.get("CloudBukkitCapabilities")).contains(
                    CloudBukkitCapabilities.BRIGADIER)) {
                return ArgumentParseResult.failure(
                        new IllegalArgumentException("Entity selector argument type not supported below Minecraft 1.13."));
            }
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NullPointerException("No input was provided"));
            }
            inputQueue.remove();

            List<Entity> entities;
            try {
                entities = Bukkit.selectEntities(commandContext.get("BukkitCommandSender"), input);
            } catch (IllegalArgumentException e) {
                return ArgumentParseResult.failure(new SelectorParseException(input));
            }

            if (entities.size() > 1) {
                return ArgumentParseResult.failure(
                        new IllegalArgumentException("More than 1 entity selected in single entity selector."));
            }

            return ArgumentParseResult.success(new SingleEntitySelector(entities));
        }
    }

}
