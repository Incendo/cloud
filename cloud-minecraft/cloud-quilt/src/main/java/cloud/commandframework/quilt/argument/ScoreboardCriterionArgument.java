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

package cloud.commandframework.quilt.argument;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.context.CommandContext;
import net.minecraft.command.argument.ObjectiveCriteriaArgumentType;
import net.minecraft.scoreboard.ScoreboardCriterion;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.function.BiFunction;

/**
 * An argument for a {@linkplain ScoreboardCriterion criterion} in a scoreboard.
 *
 * @param <C> the sender type
 * @since 1.5.0
 */
public final class ScoreboardCriterionArgument<C> extends CommandArgument<C, ScoreboardCriterion> {

    ScoreboardCriterionArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider
    ) {
        super(
                required,
                name,
                new WrappedBrigadierParser<>(ObjectiveCriteriaArgumentType.objectiveCriteria()),
                defaultValue,
                ScoreboardCriterion.class,
                suggestionsProvider
        );
    }

    /**
     * Create a new builder.
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     * @since 1.5.0
     */
    public static <C> ScoreboardCriterionArgument.@NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new ScoreboardCriterionArgument.Builder<>(name);
    }

    /**
     * Create a new required command argument.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull ScoreboardCriterionArgument<C> of(final @NonNull String name) {
        return ScoreboardCriterionArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull ScoreboardCriterionArgument<C> optional(final @NonNull String name) {
        return ScoreboardCriterionArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new optional command argument with a default value.
     *
     * @param name             Argument name
     * @param defaultCriterion Default criterion
     * @param <C>              Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull ScoreboardCriterionArgument<C> optional(
            final @NonNull String name,
            final @NonNull ScoreboardCriterion defaultCriterion
    ) {
        return ScoreboardCriterionArgument.<C>newBuilder(name).asOptionalWithDefault(defaultCriterion.getName()).build();
    }


    /**
     * Builder for {@link ScoreboardCriterionArgument}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Builder<C> extends TypedBuilder<C, ScoreboardCriterion, Builder<C>> {

        Builder(final @NonNull String name) {
            super(ScoreboardCriterion.class, name);
        }

        /**
         * Build a new criterion argument.
         *
         * @return Constructed argument
         * @since 1.5.0
         */
        @Override
        public @NonNull ScoreboardCriterionArgument<C> build() {
            return new ScoreboardCriterionArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider()
            );
        }

    }

}
