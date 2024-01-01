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

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.bukkit.BukkitCaptionKeys;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.ParserException;
import java.util.ArrayList;
import java.util.List;
import org.apiguardian.api.API;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class EnchantmentParser<C> implements ArgumentParser<C, Enchantment>,
        BlockingSuggestionProvider.Strings<C> {

    /**
     * Creates a enchantment parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Enchantment> enchantmentParser() {
        return ParserDescriptor.of(new EnchantmentParser<>(), Enchantment.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #enchantmentParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Enchantment> enchantmentComponent() {
        return CommandComponent.<C, Enchantment>builder().parser(enchantmentParser());
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NonNull ArgumentParseResult<Enchantment> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.peekString();

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
    public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext,
                                                                final @NonNull CommandInput input) {
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


    public static final class EnchantmentParseException extends ParserException {

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
