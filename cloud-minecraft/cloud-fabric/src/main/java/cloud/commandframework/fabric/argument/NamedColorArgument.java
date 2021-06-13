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
package cloud.commandframework.fabric.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.context.CommandContext;
import net.minecraft.ChatFormatting;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.function.BiFunction;

/**
 * An argument for named colors in the {@link ChatFormatting} enum.
 *
 * @param <C> the sender type
 * @since 1.5.0
 */
public final class NamedColorArgument<C> extends CommandArgument<C, ChatFormatting> {

    NamedColorArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                new WrappedBrigadierParser<>(net.minecraft.commands.arguments.ColorArgument.color()),
                defaultValue,
                ChatFormatting.class,
                suggestionsProvider,
                defaultDescription
        );
    }

    /**
     * Create a new {@link NamedColorArgument.Builder}.
     *
     * @param name Name of the component
     * @param <C>  Command sender type
     * @return Created builder
     * @since 1.5.0
     */
    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required {@link NamedColorArgument}.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     * @since 1.5.0
     */
    public static <C> @NonNull NamedColorArgument<C> of(final @NonNull String name) {
        return NamedColorArgument.<C>builder(name).asRequired().build();
    }

    /**
     * Create a new optional {@link NamedColorArgument}.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     * @since 1.5.0
     */
    public static <C> @NonNull NamedColorArgument<C> optional(final @NonNull String name) {
        return NamedColorArgument.<C>builder(name).asOptional().build();
    }

    /**
     * Create a new optional {@link NamedColorArgument} with the specified default value.
     *
     * @param name         Component name
     * @param defaultColor Default colour, must be {@link ChatFormatting#isColor() a color}
     * @param <C>          Command sender type
     * @return Created component
     * @since 1.5.0
     */
    public static <C> @NonNull NamedColorArgument<C> optional(
            final @NonNull String name,
            final @NonNull ChatFormatting defaultColor
    ) {
        return NamedColorArgument.<C>builder(name).asOptionalWithDefault(defaultColor).build();
    }


    /**
     * Builder for {@link NamedColorArgument}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Builder<C> extends CommandArgument.TypedBuilder<C, ChatFormatting, Builder<C>> {

        Builder(final @NonNull String name) {
            super(ChatFormatting.class, name);
        }

        /**
         * Build a new {@link NamedColorArgument}.
         *
         * @return Constructed argument
         * @since 1.5.0
         */
        @Override
        public @NonNull NamedColorArgument<C> build() {
            return new NamedColorArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

        /**
         * Sets the command argument to be optional, with the specified default value.
         *
         * @param defaultColor default value, must be {@link ChatFormatting#isColor() a color}
         * @return this builder
         * @see CommandArgument.Builder#asOptionalWithDefault(String)
         * @since 1.5.0
         */
        public @NonNull Builder<C> asOptionalWithDefault(final @NonNull ChatFormatting defaultColor) {
            if (!defaultColor.isColor()) {
                throw new IllegalArgumentException("Only color types are allowed but " + defaultColor + " was provided");
            }
            return this.asOptionalWithDefault(defaultColor.toString());
        }

    }

}
