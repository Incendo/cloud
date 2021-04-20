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

import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.temporal.TemporalUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * An element of in-game time.
 *
 * <p>The basic unit is 1 <em>tick</em>, which aims to be {@code 50ms}</p>
 *
 * @since 1.5.0
 */
public final class MinecraftTime {

    private static final MinecraftTime ZERO = new MinecraftTime(0);

    private final long ticks;

    /**
     * Get the time instance for the specified number of ticks.
     *
     * @param ticks the number of ticks
     * @return a time holder
     * @since 1.5.0
     */
    public static @NonNull MinecraftTime of(final long ticks) {
        return ticks == 0 ? ZERO : new MinecraftTime(ticks);
    }

    /**
     * Given an amount of time in another unit, create a game time holding the number of ticks expected to pass in that time.
     *
     * @param amount the amount of time
     * @param unit   the unit
     * @return a time holder
     * @since 1.5.0
     */
    public static @NonNull MinecraftTime of(final long amount, final @NonNull TemporalUnit unit) {
        requireNonNull(unit, "unit");
        return new MinecraftTime(Math.round(amount / 50d * unit.getDuration().toMillis()));
    }

    /**
     * Given an amount of time in another unit, create a game time holding the number of ticks expected to pass in that time.
     *
     * @param amount the amount of time
     * @param unit   the unit
     * @return a time holder
     * @since 1.5.0
     */
    public static @NonNull MinecraftTime of(final long amount, final @NonNull TimeUnit unit) {
        requireNonNull(unit, "unit");
        return amount == 0 ? ZERO : new MinecraftTime(TimeUnit.MILLISECONDS.convert(amount, unit) / 50);
    }

    MinecraftTime(final long ticks) {
        this.ticks = ticks;
    }

    /**
     * Get the number of in-game ticks represented by this time.
     *
     * <p>This time will be truncated to the maximum value of an integer.
     * See {@link #getLongTicks()} for the full contents.</p>
     *
     * @return the time in ticks
     * @since 1.5.0
     */
    public int getTicks() {
        return (int) this.ticks;
    }

    /**
     * Get the number of in-game ticks represented by this time.
     *
     * @return the time in ticks
     * @since 1.5.0
     */
    public long getLongTicks() {
        return this.ticks;
    }

    /**
     * Convert this to another time unit.
     *
     * @param unit the target unit
     * @return the target duration, as represented by the provided unit
     * @since 1.5.0
     */
    public long convertTo(final @NonNull TemporalUnit unit) {
        return this.ticks * 50 / unit.getDuration().toMillis();
    }

    /**
     * Convert this to another time unit.
     *
     * @param unit the target unit
     * @return the target duration, as represented by the provided unit
     * @since 1.5.0
     */
    public long convertTo(final @NonNull TimeUnit unit) {
        return unit.convert(this.ticks * 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }

        return this.ticks == ((MinecraftTime) other).ticks;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.ticks);
    }

    @Override
    public String toString() {
        return Long.toString(this.ticks);
    }

}
