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
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.fabric.argument.FabricArgumentParsers;
import cloud.commandframework.fabric.data.Coordinates;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.world.phys.Vec3;
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
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription,
            final boolean centerIntegers
    ) {
        super(
                required,
                name,
                FabricArgumentParsers.vec3(centerIntegers),
                defaultValue,
                Coordinates.class,
                suggestionsProvider,
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
        return Vec3dArgument.<C>builder(name).asRequired().build();
    }

    /**
     * Create a new required {@link Vec3dArgument}.
     *
     * @param name           Component name
     * @param centerIntegers whether to center integers to x.5.
     * @param <C>            Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull Vec3dArgument<C> of(final @NonNull String name, final boolean centerIntegers) {
        return Vec3dArgument.<C>builder(name).centerIntegers(centerIntegers).asRequired().build();
    }

    /**
     * Create a new optional {@link Vec3dArgument}.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull Vec3dArgument<C> optional(final @NonNull String name) {
        return Vec3dArgument.<C>builder(name).asOptional().build();
    }

    /**
     * Create a new optional {@link Vec3dArgument}.
     *
     * @param name           Component name
     * @param centerIntegers whether to center integers to x.5.
     * @param <C>            Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull Vec3dArgument<C> optional(final @NonNull String name, final boolean centerIntegers) {
        return Vec3dArgument.<C>builder(name).centerIntegers(centerIntegers).asOptional().build();
    }

    /**
     * Create a new optional {@link Vec3dArgument} with the specified default value.
     *
     * @param name         Component name
     * @param defaultValue default value
     * @param <C>          Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull Vec3dArgument<C> optional(final @NonNull String name, final @NonNull Vec3 defaultValue) {
        return Vec3dArgument.<C>builder(name).asOptionalWithDefault(defaultValue).build();
    }

    /**
     * Create a new optional {@link Vec3dArgument} with the specified default value.
     *
     * @param name           Component name
     * @param defaultValue   default value
     * @param centerIntegers whether to center integers to x.5.
     * @param <C>            Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull Vec3dArgument<C> optional(
            final @NonNull String name,
            final boolean centerIntegers,
            final @NonNull Vec3 defaultValue
    ) {
        return Vec3dArgument.<C>builder(name).centerIntegers(centerIntegers).asOptionalWithDefault(defaultValue).build();
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
         * Sets the command argument to be optional, with the specified default value.
         *
         * @param defaultValue default value
         * @return this builder
         * @see CommandArgument.Builder#asOptionalWithDefault(String)
         * @since 1.5.0
         */
        public @NonNull Builder<C> asOptionalWithDefault(final @NonNull Vec3 defaultValue) {
            return this.asOptionalWithDefault(String.format(
                    "%.10f %.10f %.10f",
                    defaultValue.x,
                    defaultValue.y,
                    defaultValue.z
            ));
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
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription(),
                    this.centerIntegers()
            );
        }
    }
}
