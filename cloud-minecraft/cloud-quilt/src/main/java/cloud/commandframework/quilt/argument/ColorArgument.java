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
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.util.Formatting;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.function.BiFunction;

/**
 * An argument for named colors in the {@link Formatting} enum.
 *
 * @param <C> the sender type
 * @since 1.5.0
 */
public final class ColorArgument<C> extends CommandArgument<C, Formatting> {

    ColorArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider
    ) {
        super(required, name, new WrappedBrigadierParser<>(ColorArgumentType.color()), defaultValue, Formatting.class,
                suggestionsProvider
        );
    }

    /**
     * Create a new builder.
     *
     * @param name Name of the component
     * @param <C>  Command sender type
     * @return Created builder
     * @since 1.5.0
     */
    public static <C> ColorArgument.@NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new ColorArgument.Builder<>(name);
    }

    /**
     * Create a new required command argument.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     * @since 1.5.0
     */
    public static <C> @NonNull ColorArgument<C> of(final @NonNull String name) {
        return ColorArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     * @since 1.5.0
     */
    public static <C> @NonNull ColorArgument<C> optional(final @NonNull String name) {
        return ColorArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new optional command argument with a default value.
     *
     * @param name         Component name
     * @param defaultColor Default colour, must be {@link Formatting#isColor() a color}
     * @param <C>          Command sender type
     * @return Created component
     * @since 1.5.0
     */
    public static <C> @NonNull ColorArgument<C> optional(
            final @NonNull String name,
            final @NonNull Formatting defaultColor
    ) {
        if (!defaultColor.isColor()) {
            throw new IllegalArgumentException("Only color types are allowed but " + defaultColor + " was provided");
        }
        return ColorArgument.<C>newBuilder(name).asOptionalWithDefault(defaultColor.toString()).build();
    }


    /**
     * Builder for {@link ColorArgument}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Builder<C> extends CommandArgument.TypedBuilder<C, Formatting, Builder<C>> {

        Builder(final @NonNull String name) {
            super(Formatting.class, name);
        }

        /**
         * Build a new color argument.
         *
         * @return Constructed argument
         * @since 1.5.0
         */
        @Override
        public @NonNull ColorArgument<C> build() {
            return new ColorArgument<>(this.isRequired(), this.getName(), this.getDefaultValue(), this.getSuggestionsProvider());
        }

    }

}
