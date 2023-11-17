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
package cloud.commandframework.bukkit.parsers;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.bukkit.BukkitCaptionKeys;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import java.util.ArrayList;
import java.util.List;
import org.apiguardian.api.API;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * cloud argument type that parses Bukkit {@link Enchantment enchantments}
 *
 * @param <C> Command sender type
 */
public class EnchantmentArgument<C> extends CommandArgument<C, Enchantment> {

    protected EnchantmentArgument(
            final @NonNull String name,
            final @Nullable SuggestionProvider<C> suggestionProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(
                name,
                new EnchantmentParser<>(),
                Enchantment.class,
                suggestionProvider,
                defaultDescription
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
        return new EnchantmentArgument.Builder<>(name);
    }

    /**
     * Create a new required argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Enchantment> of(final @NonNull String name) {
        return EnchantmentArgument.<C>builder(name).build();
    }


    public static final class Builder<C> extends CommandArgument.TypedBuilder<C, Enchantment, Builder<C>> {

        private Builder(final @NonNull String name) {
            super(Enchantment.class, name);
        }

        @Override
        public @NonNull CommandArgument<C, Enchantment> build() {
            return new EnchantmentArgument<>(
                    this.getName(),
                    this.suggestionProvider(),
                    this.getDefaultDescription()
            );
        }
    }

    public static final class EnchantmentParser<C> implements ArgumentParser<C, Enchantment> {

        @Override
        @SuppressWarnings("deprecation")
        public @NonNull ArgumentParseResult<Enchantment> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull CommandInput commandInput
        ) {
            final String input = commandInput.peekString();
            if (input.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        EnchantmentParser.class,
                        commandContext
                ));
            }

            final NamespacedKey key;
            try {
                if (input.contains(":")) {
                    key = new NamespacedKey(commandInput.readUntilAndSkip(':'), commandInput.readString());
                } else {
                    key = NamespacedKey.minecraft(commandInput.readString());
                }
            } catch (final Exception ex) {
                return ArgumentParseResult.failure(new EnchantmentParseException(input, commandContext));
            }

            final Enchantment enchantment = Enchantment.getByKey(key);
            if (enchantment == null) {
                return ArgumentParseResult.failure(new EnchantmentParseException(input, commandContext));
            }
            return ArgumentParseResult.success(enchantment);
        }

        @Override
        public @NonNull List<@NonNull String> stringSuggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            final List<String> completions = new ArrayList<>();
            for (Enchantment value : Enchantment.values()) {
                if (value.getKey().getNamespace().equals(NamespacedKey.MINECRAFT)) {
                    completions.add(value.getKey().getKey());
                } else {
                    completions.add(value.getKey().toString());
                }
            }
            return completions;
        }
    }


    public static final class EnchantmentParseException extends ParserException {

        private static final long serialVersionUID = 1415174766296065151L;
        private final String input;

        /**
         * Construct a new EnchantmentParseException
         *
         * @param input   Input
         * @param context Command context
         */
        public EnchantmentParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    EnchantmentParser.class,
                    context,
                    BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_ENCHANTMENT,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Get the input
         *
         * @return Input
         */
        public @NonNull String getInput() {
            return this.input;
        }
    }
}
