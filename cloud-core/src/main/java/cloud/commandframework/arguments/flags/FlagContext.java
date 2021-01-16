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
package cloud.commandframework.arguments.flags;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Flag value mappings
 */
public final class FlagContext {

    /**
     * Dummy object stored as a flag value when the flag has no associated parser
     */
    public static final Object FLAG_PRESENCE_VALUE = new Object();

    private final Map<String, Object> flagValues;

    private FlagContext() {
        this.flagValues = new HashMap<>();
    }

    /**
     * Create a new flag context instance
     *
     * @return Constructed instance
     */
    public static @NonNull FlagContext create() {
        return new FlagContext();
    }

    /**
     * Indicate that a presence flag was supplied
     *
     * @param flag Flag instance
     */
    public void addPresenceFlag(final @NonNull CommandFlag<?> flag) {
        this.flagValues.put(flag.getName(), FLAG_PRESENCE_VALUE);
    }

    /**
     * Store a value associated with a value flag
     *
     * @param flag  Value flag
     * @param value Flag value
     * @param <T>   Value type
     */
    public <T> void addValueFlag(
            final @NonNull CommandFlag<T> flag,
            final @NonNull T value
    ) {
        this.flagValues.put(flag.getName(), value);
    }

    /**
     * Check whether a presence flag is present. This will return {@code false}
     * for all value flags.
     *
     * @param flag Flag name
     * @return {@code true} if the flag is a presence flag and is present,
     *         else {@code false}
     */
    public boolean isPresent(final @NonNull String flag) {
        final Object value = this.flagValues.get(flag);
        return FLAG_PRESENCE_VALUE.equals(value);
    }

    /**
     * Check whether a presence flag is present. This will return {@code false}
     * for all value flags.
     *
     * @param flag A presence flag instance
     * @return {@code true} if the flag is a presence flag and is present,
     *         else {@code false}
     * @since 1.4.0
     */
    public boolean isPresent(final @NonNull CommandFlag<Void> flag) {
        return this.isPresent(flag.getName());
    }

    /**
     * Get a flag value as an optional. Will be empty if the value is not present.
     *
     * @param name Flag name
     * @param <T>  Value type
     * @return Optional containing stored value if present
     * @since 1.2.0
     */
    public <T> @NonNull Optional<T> getValue(
            final @NonNull String name
    ) {
        final Object value = this.flagValues.get(name);
        if (value == null) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked") final T casted = (T) value;
        return Optional.of(casted);
    }

    /**
     * Get a flag value as an optional. Will be empty if the value is not present.
     *
     * @param flag Flag type
     * @param <T>  Value type
     * @return Optional containing stored value if present
     * @since 1.4.0
     */
    public <T> @NonNull Optional<T> getValue(
            final @NonNull CommandFlag<T> flag
    ) {
        return this.getValue(flag.getName());
    }

    /**
     * Get a flag value
     *
     * @param name         Flag name
     * @param defaultValue Default value
     * @param <T>          Value type
     * @return Stored value, or the supplied default value
     */
    public <T> @Nullable T getValue(
            final @NonNull String name,
            final @Nullable T defaultValue
    ) {
        return this.<T>getValue(name).orElse(defaultValue);
    }

    /**
     * Get a flag value
     *
     * @param name         Flag value
     * @param defaultValue Default value
     * @param <T>          Value type
     * @return Stored value, or the supplied default value
     * @since 1.4.0
     */
    public <T> @Nullable T getValue(
            final @NonNull CommandFlag<T> name,
            final @Nullable T defaultValue
    ) {
        return this.getValue(name).orElse(defaultValue);
    }

    /**
     * Check whether a flag is present. This will return {@code true} if the flag
     * is a presence flag and is present, or if the flag is a value flag and has
     * a value provided.
     *
     * @param name Flag name
     * @return whether the flag is present
     * @since 1.2.0
     */
    public boolean hasFlag(
            final @NonNull String name
    ) {
        return this.getValue(name).isPresent();
    }

    /**
     * Check whether a flag is present. This will return {@code true} if the flag
     * is a presence flag and is present, or if the flag is a value flag and has
     * a value provided.
     *
     * @param flag The flag instance
     * @return whether the flag is present
     * @since 1.4.0
     */
    public boolean hasFlag(
            final @NonNull CommandFlag<?> flag
    ) {
        return this.getValue(flag).isPresent();
    }

    /**
     * Check whether a flag is present. This will return {@code true} if the flag
     * is a presence flag and is present, or if the flag is a value flag and has
     * a value provided.
     *
     * @param name Flag name
     * @return whether the flag is present
     * @since 1.3.0
     */
    public boolean contains(final @NonNull String name) {
        return this.hasFlag(name);
    }

    /**
     * Check whether a flag is present. This will return {@code true} if the flag
     * is a presence flag and is present, or if the flag is a value flag and has
     * a value provided.
     *
     * @param flag Flag instance
     * @return whether the flag is present
     * @since 1.4.0
     */
    public boolean contains(final @NonNull CommandFlag<?> flag) {
        return this.hasFlag(flag);
    }

    /**
     * Get a flag value
     *
     * @param name         Flag name
     * @param <T>          Value type
     * @return Stored value if present, else {@code null}
     * @since 1.3.0
     */
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public <T> @Nullable T get(
            final @NonNull String name
    ) {
        return this.<T>getValue(name).orElse(null);
    }

    /**
     * Get a flag value
     *
     * @param flag         Flag name
     * @param <T>          Value type
     * @return Stored value if present, else {@code null}
     * @since 1.4.0
     */
    public <T> @Nullable T get(
            final @NonNull CommandFlag<T> flag
    ) {
        return this.getValue(flag).orElse(null);
    }

}
