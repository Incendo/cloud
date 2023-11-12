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
package cloud.commandframework.fabric.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.context.CommandContext;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.OperationArgument.Operation;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An argument for selecting any of the logical operations in {@link Operation}.
 *
 * <p>These operations can be used to compare scores on a {@link net.minecraft.world.scores.Scoreboard}.</p>
 *
 * @param <C> the sender type
 * @since 1.5.0
 */
public final class ScoreboardOperationArgument<C> extends CommandArgument<C, Operation> {

    ScoreboardOperationArgument(
            final @NonNull String name,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(
                name,
                new WrappedBrigadierParser<>(OperationArgument.operation()),
                Operation.class,
                suggestionsProvider,
                defaultDescription
        );
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     * @since 1.5.0
     */
    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new ScoreboardOperationArgument.Builder<>(name);
    }

    /**
     * Create a new required {@link ScoreboardOperationArgument}.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull ScoreboardOperationArgument<C> of(final @NonNull String name) {
        return ScoreboardOperationArgument.<C>builder(name).build();
    }


    /**
     * Builder for {@link ScoreboardOperationArgument}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Builder<C> extends TypedBuilder<C, Operation, Builder<C>> {

        Builder(final @NonNull String name) {
            super(Operation.class, name);
        }

        /**
         * Build a new {@link ScoreboardOperationArgument}.
         *
         * @return Constructed argument
         * @since 1.5.0
         */
        @Override
        public @NonNull ScoreboardOperationArgument<C> build() {
            return new ScoreboardOperationArgument<>(
                    this.getName(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }
    }
}
