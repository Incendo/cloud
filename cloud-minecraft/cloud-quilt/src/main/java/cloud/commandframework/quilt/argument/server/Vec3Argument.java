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
import cloud.commandframework.quilt.data.Coordinates;
import net.minecraft.util.math.Vec3d;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.function.BiFunction;

/**
 * An argument for resolving coordinates from 3 doubles.
 *
 * @param <C> the sender type
 * @since 1.5.0
 */
public final class Vec3Argument<C> extends CommandArgument<C, Coordinates> {

    private final boolean centerIntegers;

    Vec3Argument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final boolean centerIntegers
    ) {
        super(
                required,
                name,
                QuiltArgumentParsers.vec3(centerIntegers),
                defaultValue,
                Coordinates.class,
                suggestionsProvider
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
     * Create a new builder.
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     * @since 1.5.0
     */
    public static <C> Vec3Argument.@NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new Vec3Argument.Builder<>(name);
    }

    /**
     * Create a new required command argument.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull Vec3Argument<C> of(final @NonNull String name) {
        return Vec3Argument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new required command argument.
     *
     * @param name           Component name
     * @param centerIntegers whether to center integers to x.5.
     * @param <C>            Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull Vec3Argument<C> of(final @NonNull String name, final boolean centerIntegers) {
        return Vec3Argument.<C>newBuilder(name).centerIntegers(centerIntegers).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull Vec3Argument<C> optional(final @NonNull String name) {
        return Vec3Argument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name           Component name
     * @param centerIntegers whether to center integers to x.5.
     * @param <C>            Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull Vec3Argument<C> optional(final @NonNull String name, final boolean centerIntegers) {
        return Vec3Argument.<C>newBuilder(name).centerIntegers(centerIntegers).asOptional().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name         Component name
     * @param defaultValue default value
     * @param <C>          Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull Vec3Argument<C> optional(final @NonNull String name, final @NonNull Vec3d defaultValue) {
        return Vec3Argument.<C>newBuilder(name)
                .asOptionalWithDefault(serializeVec3(defaultValue))
                .build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name           Component name
     * @param defaultValue   default value
     * @param centerIntegers whether to center integers to x.5.
     * @param <C>            Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull Vec3Argument<C> optional(
            final @NonNull String name,
            final boolean centerIntegers,
            final @NonNull Vec3d defaultValue
    ) {
        return Vec3Argument.<C>newBuilder(name)
                .centerIntegers(centerIntegers)
                .asOptionalWithDefault(serializeVec3(defaultValue))
                .build();
    }

    private static @NonNull String serializeVec3(final @NonNull Vec3d vec3d) {
        return String.format(
                "%.10f %.10f %.10f",
                vec3d.x,
                vec3d.y,
                vec3d.z
        );
    }

    /**
     * Builder for {@link Vec3Argument}.
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
         * Build a vec3 argument.
         *
         * @return Constructed argument
         * @since 1.5.0
         */
        @Override
        public @NonNull Vec3Argument<C> build() {
            return new Vec3Argument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.centerIntegers()
            );
        }

    }

}
