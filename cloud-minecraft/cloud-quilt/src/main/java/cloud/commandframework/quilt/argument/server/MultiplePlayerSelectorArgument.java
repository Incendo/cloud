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
package cloud.commandframework.quilt.argument.server;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.quilt.argument.QuiltArgumentParsers;
import cloud.commandframework.quilt.data.MultiplePlayerSelector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.function.BiFunction;

/**
 * An argument for selecting multiple players
 *
 * @param <C> the sender type
 * @since 1.5.0
 */
public final class MultiplePlayerSelectorArgument<C> extends CommandArgument<C, MultiplePlayerSelector> {

    MultiplePlayerSelectorArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider
    ) {
        super(
                required,
                name,
                QuiltArgumentParsers.multiplePlayerSelector(),
                defaultValue,
                MultiplePlayerSelector.class,
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
    public static <C> MultiplePlayerSelectorArgument.@NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new MultiplePlayerSelectorArgument.Builder<>(name);
    }

    /**
     * Create a new required command argument.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull MultiplePlayerSelectorArgument<C> of(final @NonNull String name) {
        return MultiplePlayerSelectorArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull MultiplePlayerSelectorArgument<C> optional(final @NonNull String name) {
        return MultiplePlayerSelectorArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Builder for {@link MultiplePlayerSelectorArgument}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Builder<C> extends TypedBuilder<C, MultiplePlayerSelector, Builder<C>> {

        Builder(final @NonNull String name) {
            super(MultiplePlayerSelector.class, name);
        }

        /**
         * Build a multiple player selector argument.
         *
         * @return Constructed argument
         * @since 1.5.0
         */
        @Override
        public @NonNull MultiplePlayerSelectorArgument<C> build() {
            return new MultiplePlayerSelectorArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider()
            );
        }

    }

}
