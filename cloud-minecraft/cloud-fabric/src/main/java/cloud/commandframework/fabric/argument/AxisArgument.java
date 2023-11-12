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
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import io.leangen.geantyref.TypeToken;
import java.util.EnumSet;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.core.Direction;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An argument for a set of {@link net.minecraft.core.Direction.Axis axes}, described in Vanilla as a "swizzle".
 *
 * @param <C> the sender type
 * @since 1.5.0
 */
public final class AxisArgument<C> extends CommandArgument<C, EnumSet<Direction.Axis>> {

    private static final TypeToken<EnumSet<Direction.Axis>> TYPE = new TypeToken<EnumSet<Direction.Axis>>() {
    };

    AxisArgument(
            final @NonNull String name,
            final @Nullable SuggestionProvider<C> suggestionProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(
                name,
                new WrappedBrigadierParser<>(SwizzleArgument.swizzle()),
                TYPE,
                suggestionProvider,
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
        return new Builder<>(name);
    }

    /**
     * Create a new required {@link AxisArgument}.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull AxisArgument<C> of(final @NonNull String name) {
        return AxisArgument.<C>builder(name).build();
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
         * Build a new {@link AxisArgument}.
         *
         * @return Constructed argument
         * @since 1.5.0
         */
        @Override
        public @NonNull AxisArgument<C> build() {
            return new AxisArgument<>(
                    this.getName(),
                    this.suggestionProvider(),
                    this.getDefaultDescription()
            );
        }
    }
}
