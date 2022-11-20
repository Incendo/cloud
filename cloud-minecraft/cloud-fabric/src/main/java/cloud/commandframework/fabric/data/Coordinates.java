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
package cloud.commandframework.fabric.data;

import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A {@link net.minecraft.commands.arguments.coordinates.Coordinates} wrapper for easier use with cloud commands.
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
    @NonNull Vec3 position();

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
    net.minecraft.commands.arguments.coordinates.@NonNull Coordinates wrappedCoordinates();

    /**
     * A specialized version of {@link Coordinates} for representing the result of the vanilla {@link Vec2Argument},
     * which accepts two doubles for the x and z coordinate, always defaulting to 0 for the y coordinate.
     *
     * @since 1.5.0
     */
    interface CoordinatesXZ extends Coordinates {

    }

    /**
     * A specialized version of {@link Coordinates} for representing the result of the vanilla {@link BlockPosArgument}.
     *
     * @since 1.5.0
     */
    interface BlockCoordinates extends Coordinates {

    }

    /**
     * A specialized version of {@link Coordinates} for representing the result of the vanilla {@link ColumnPosArgument}.
     *
     * @since 1.5.0
     */
    interface ColumnCoordinates extends Coordinates {

    }
}
