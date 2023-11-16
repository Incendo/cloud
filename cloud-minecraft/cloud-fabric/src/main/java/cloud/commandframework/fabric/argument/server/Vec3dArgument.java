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
package cloud.commandframework.fabric.argument.server;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.fabric.argument.FabricArgumentParsers;
import cloud.commandframework.fabric.data.Coordinates;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An argument for resolving {@link Coordinates} from 3 doubles.
 *
 * @param <C> the sender type
 * @since 1.5.0
 */
public final class Vec3dArgument<C> extends CommandArgument<C, Coordinates> {

    private final boolean centerIntegers;

    Vec3dArgument(
            final @NonNull String name,
            final @Nullable SuggestionProvider<C> suggestionProvider,
            final @NonNull ArgumentDescription<C> defaultDescription,
            final boolean centerIntegers
    ) {
        super(
                name,
                FabricArgumentParsers.vec3(centerIntegers),
                Coordinates.class,
                suggestionProvider,
                defaultDescription
        );
        this.centerIntegers = centerIntegers;
    }

    /**
     * Get whether integers will be centered to x.5. Defaults to false.
     *
     * @return whether integers will be centered
     * @since 1.5.0
     */
    public boolean centerIntegers() {
        return this.centerIntegers;
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
     * Create a new required {@link Vec3dArgument}.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull Vec3dArgument<C> of(final @NonNull String name) {
        return Vec3dArgument.<C>builder(name).build();
    }


    /**
     * Builder for {@link Vec3dArgument}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Builder<C> extends TypedBuilder<C, Coordinates, Builder<C>> {

        private boolean centerIntegers = false;

        Builder(final @NonNull String name) {
            super(Coordinates.class, name);
        }

        /**
         * Set whether integers will be centered to x.5. Will be false by default if unset.
         *
         * @param centerIntegers whether integers will be centered
         * @return this builder
         * @since 1.5.0
         */
        public @NonNull Builder<C> centerIntegers(final boolean centerIntegers) {
            this.centerIntegers = centerIntegers;
            return this;
        }

        /**
         * Get whether integers will be centered to x.5. Defaults to false.
         *
         * @return whether integers will be centered
         * @since 1.5.0
         */
        public boolean centerIntegers() {
            return this.centerIntegers;
        }

        /**
         * Build a new {@link Vec3dArgument}.
         *
         * @return Constructed argument
         * @since 1.5.0
         */
        @Override
        public @NonNull Vec3dArgument<C> build() {
            return new Vec3dArgument<>(
                    this.getName(),
                    this.suggestionProvider(),
                    this.getDefaultDescription(),
                    this.centerIntegers()
            );
        }
    }
}
