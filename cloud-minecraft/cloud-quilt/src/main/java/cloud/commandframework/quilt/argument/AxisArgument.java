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
import io.leangen.geantyref.TypeToken;
import net.minecraft.command.argument.SwizzleArgumentType;
import net.minecraft.util.math.Direction;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * An argument for a set of axes, described in Vanilla as a "swizzle".
 *
 * @param <C> the sender type
 * @since 1.5.0
 */
public final class AxisArgument<C> extends CommandArgument<C, EnumSet<Direction.Axis>> {

    private static final TypeToken<EnumSet<Direction.Axis>> TYPE = new TypeToken<EnumSet<Direction.Axis>>() {
    };

    AxisArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider
    ) {
        super(
                required,
                name,
                new WrappedBrigadierParser<>(SwizzleArgumentType.swizzle()),
                defaultValue,
                TYPE,
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
    public static <C> AxisArgument.@NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new AxisArgument.Builder<>(name);
    }

    /**
     * Create a new required command argument.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull AxisArgument<C> of(final @NonNull String name) {
        return AxisArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull AxisArgument<C> optional(final @NonNull String name) {
        return AxisArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new optional command argument with a default value.
     *
     * @param name          Argument name
     * @param defaultValues Default axes to include
     * @param <C>           Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull AxisArgument<C> optional(
            final @NonNull String name,
            final @NonNull Set<Direction.@NonNull Axis> defaultValues
    ) {
        final StringBuilder builder = new StringBuilder();
        for (final Direction.Axis axis : defaultValues) {
            builder.append(axis.getName());
        }
        return AxisArgument.<C>newBuilder(name).asOptionalWithDefault(builder.toString()).build();
    }


    /**
     * Builder for {@link AxisArgument}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Builder<C> extends TypedBuilder<C, EnumSet<Direction.Axis>, Builder<C>> {

        Builder(final @NonNull String name) {
            super(TYPE, name);
        }

        /**
         * Build a new axis argument.
         *
         * @return Constructed argument
         * @since 1.5.0
         */
        @Override
        public @NonNull AxisArgument<C> build() {
            return new AxisArgument<>(this.isRequired(), this.getName(), this.getDefaultValue(), this.getSuggestionsProvider());
        }

    }

}
