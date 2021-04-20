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
import cloud.commandframework.quilt.data.Coordinates.BlockCoordinates;
import net.minecraft.util.math.BlockPos;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.function.BiFunction;

/**
 * An argument for resolving block coordinates.
 *
 * @param <C> the sender type
 * @since 1.5.0
 */
public final class BlockPosArgument<C> extends CommandArgument<C, BlockCoordinates> {

    BlockPosArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider
    ) {
        super(
                required,
                name,
                QuiltArgumentParsers.blockPos(),
                defaultValue,
                BlockCoordinates.class,
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
    public static <C> BlockPosArgument.@NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new BlockPosArgument.Builder<>(name);
    }

    /**
     * Create a new required command argument.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull BlockPosArgument<C> of(final @NonNull String name) {
        return BlockPosArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull BlockPosArgument<C> optional(final @NonNull String name) {
        return BlockPosArgument.<C>newBuilder(name).asOptional().build();
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
    public static <C> @NonNull BlockPosArgument<C> optional(final @NonNull String name, final @NonNull BlockPos defaultValue) {
        return BlockPosArgument.<C>newBuilder(name)
                .asOptionalWithDefault(serializeBlockPos(defaultValue))
                .build();
    }

    private static @NonNull String serializeBlockPos(final @NonNull BlockPos pos) {
        return String.format(
                "%s %s %s",
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );
    }

    /**
     * Builder for {@link BlockPosArgument}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Builder<C> extends TypedBuilder<C, BlockCoordinates, Builder<C>> {

        Builder(final @NonNull String name) {
            super(BlockCoordinates.class, name);
        }

        /**
         * Build a block position argument.
         *
         * @return Constructed argument
         * @since 1.5.0
         */
        @Override
        public @NonNull BlockPosArgument<C> build() {
            return new BlockPosArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider()
            );
        }

    }

}
