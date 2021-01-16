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
package cloud.commandframework.bukkit.parsers.location;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * Single coordinate with a type
 *
 * @since 1.1.0
 */
public final class LocationCoordinate {

    private final LocationCoordinateType type;
    private final double coordinate;

    private LocationCoordinate(
            final @NonNull LocationCoordinateType type,
            final double coordinate
    ) {
        this.type = type;
        this.coordinate = coordinate;
    }

    /**
     * Create a new location coordinate
     *
     * @param type       Coordinate type
     * @param coordinate Coordinate
     * @return Created coordinate instance
     */
    public static @NonNull LocationCoordinate of(
            final @NonNull LocationCoordinateType type,
            final double coordinate
    ) {
        return new LocationCoordinate(type, coordinate);
    }

    /**
     * Get the coordinate type
     *
     * @return Coordinate type
     */
    public @NonNull LocationCoordinateType getType() {
        return this.type;
    }

    /**
     * Get the coordinate
     *
     * @return Coordinate
     */
    public double getCoordinate() {
        return this.coordinate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final LocationCoordinate that = (LocationCoordinate) o;
        return Double.compare(that.coordinate, coordinate) == 0
                && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, coordinate);
    }

    @Override
    public String toString() {
        return String.format("LocationCoordinate{type=%s, coordinate=%f}", this.type.name().toLowerCase(), this.coordinate);
    }

}
