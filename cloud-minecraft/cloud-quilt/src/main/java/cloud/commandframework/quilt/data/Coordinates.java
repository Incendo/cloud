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
package cloud.commandframework.quilt.data;

import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.ColumnPosArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec2ArgumentType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A {@link PosArgument} wrapper for easier use with cloud commands.
 *
 * @since 1.5.0
 */
public interface Coordinates {

    /**
     * Resolve a position from the parsed coordinates.
     *
     * @return position
     * @since 1.5.0
     */
    @NonNull Vec3d position();

    /**
     * Resolve a block position from the parsed coordinates.
     *
     * @return block position
     * @since 1.5.0
     */
    @NonNull BlockPos blockPos();

    /**
     * Get whether the x coordinate is relative.
     *
     * @return whether the x coordinate is relative
     * @since 1.5.0
     */
    boolean isXRelative();

    /**
     * Get whether the y coordinate is relative.
     *
     * @return whether the y coordinate is relative
     * @since 1.5.0
     */
    boolean isYRelative();

    /**
     * Get whether the z coordinate is relative.
     *
     * @return whether the z coordinate is relative
     * @since 1.5.0
     */
    boolean isZRelative();

    /**
     * Get the coordinates wrapped by this {@link Coordinates}.
     *
     * @return the base coordinates
     * @since 1.5.0
     */
    @NonNull PosArgument getWrappedCoordinates();

    /**
     * A specialized version of {@link Coordinates} for representing the result of the vanilla {@link Vec2ArgumentType},
     * which accepts two doubles for the x and z coordinate, always defaulting to 0 for the y coordinate.
     *
     * @since 1.5.0
     */
    interface CoordinatesXZ extends Coordinates {

    }

    /**
     * A specialized version of {@link Coordinates} for representing the result of the vanilla {@link BlockPosArgumentType}.
     *
     * @since 1.5.0
     */
    interface BlockCoordinates extends Coordinates {

    }

    /**
     * A specialized version of {@link Coordinates} for representing the result of the vanilla {@link ColumnPosArgumentType}.
     *
     * @since 1.5.0
     */
    interface ColumnCoordinates extends Coordinates {

    }

}
