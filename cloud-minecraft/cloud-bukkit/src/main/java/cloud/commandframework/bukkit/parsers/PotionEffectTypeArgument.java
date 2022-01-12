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
package cloud.commandframework.bukkit.parsers;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.bukkit.BukkitCaptionKeys;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import org.bukkit.potion.PotionEffectType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.function.BiFunction;

/**
 * Parses Bukkit {@link PotionEffectType}s.
 *
 * @param <C> sender type
 */
public class PotionEffectTypeArgument<C> extends CommandArgument<C, PotionEffectType> {

    private PotionEffectTypeArgument(
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
                new Parser<>(),
                defaultValue,
                PotionEffectType.class,
                suggestionsProvider,
                defaultDescription
        );
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return new builder
     */
    public static @NonNull <C> Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required {@link PotionEffectTypeArgument}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return built argument
     */
    public static <C> @NonNull PotionEffectTypeArgument<C> of(final @NonNull String name) {
        return PotionEffectTypeArgument.<C>builder(name).asRequired().build();
    }

    /**
     * Create a new optional {@link PotionEffectTypeArgument}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return built argument
     */
    public static <C> @NonNull PotionEffectTypeArgument<C> optional(final @NonNull String name) {
        return PotionEffectTypeArgument.<C>builder(name).asOptional().build();
    }

    /**
     * Create a new optional {@link PotionEffectTypeArgument} with the specified default value.
     *
     * @param name             argument name
     * @param potionEffectType default value
     * @param <C>              sender type
     * @return built argument
     */
    public static <C> @NonNull PotionEffectTypeArgument<C> optional(
            final @NonNull String name,
            final @NonNull PotionEffectType potionEffectType
    ) {
        return PotionEffectTypeArgument.<C>builder(name).asOptionalWithDefault(potionEffectType.getName()).build();
    }

    /**
     * Builder for {@link PotionEffectTypeArgument}.
     *
     * @param <C> sender type
     */
    public static final class Builder<C> extends TypedBuilder<C, PotionEffectType, Builder<C>> {

        private Builder(final @NonNull String name) {
            super(PotionEffectType.class, name);
        }

        /**
         * Sets the command argument to be optional, with the specified default value.
         *
         * @param defaultValue default value
         * @return this builder
         * @see CommandArgument.Builder#asOptionalWithDefault(String)
         */
        public @NonNull Builder<C> asOptionalWithDefault(final @NonNull PotionEffectType defaultValue) {
            return this.asOptionalWithDefault(defaultValue.getName());
        }

        /**
         * Create a new {@link PotionEffectTypeArgument} from this builder.
         *
         * @return built argument
         */
        @Override
        public @NonNull PotionEffectTypeArgument<C> build() {
            return new PotionEffectTypeArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

    }

    /**
     * Parser for {@link PotionEffectType}.
     *
     * @param <C> sender type
     */
    public static final class Parser<C> implements ArgumentParser<C, PotionEffectType> {

        @Override
        public @NonNull ArgumentParseResult<PotionEffectType> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        Parser.class,
                        commandContext
                ));
            }

            final @Nullable PotionEffectType potionEffectType = PotionEffectType.getByName(input);
            if (potionEffectType == null) {
                return ArgumentParseResult.failure(new PotionEffectTypeParseException(input, commandContext));
            }

            inputQueue.remove();
            return ArgumentParseResult.success(potionEffectType);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            final List<String> completions = new ArrayList<>();
            for (final PotionEffectType value : PotionEffectType.values()) {
                completions.add(value.getName().toLowerCase(Locale.ROOT));
            }
            return completions;
        }

    }

    /**
     * Failure exception for {@link Parser}.
     */
    public static final class PotionEffectTypeParseException extends ParserException {

        private static final long serialVersionUID = 3319300669876276695L;
        private final String input;

        /**
         * Construct a new {@link PotionEffectTypeParseException}.
         *
         * @param input   input string
         * @param context command context
         */
        public PotionEffectTypeParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    Parser.class,
                    context,
                    BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_POTION_EFFECT_TYPE,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Get the supplied input string.
         *
         * @return input string
         */
        public @NonNull String getInput() {
            return this.input;
        }

    }

}
