//
// MIT License
//
// Copyright (c) 2024 Incendo
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Flag value mappings
 */
@API(status = API.Status.STABLE)
@SuppressWarnings({"rawtypes", "unchecked"})
public final class FlagContext {

    /**
     * Dummy object stored as a flag value when the flag has no associated parser
     */
    public static final Object FLAG_PRESENCE_VALUE = new Object();

    private final Map<String, List> flagValues;

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
        ((List<Object>) this.flagValues.computeIfAbsent(
                flag.getName(),
                $ -> new ArrayList<>()
        )).add(FLAG_PRESENCE_VALUE);
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
        ((List<T>) this.flagValues.computeIfAbsent(
                flag.getName(),
                $ -> new ArrayList<>()
        )).add(value);
    }

    /**
     * Returns the number of values associated with the given {@code flag}.
     *
     * @param flag the flag
     * @param <T>  the flag value type
     * @return the number of values associated with the flag
     * @since 1.7.0
     */
    @API(status = API.Status.STABLE, since = "1.7.0")
    public <T> int count(final @NonNull CommandFlag<T> flag) {
        return this.getAll(flag).size();
    }

    /**
     * Returns the number of values associated with the given {@code flag}.
     *
     * @param flag the flag
     * @return the number of values associated with the flag
     * @since 1.7.0
     */
    @API(status = API.Status.STABLE, since = "1.7.0")
    public int count(final @NonNull String flag) {
        return this.getAll(flag).size();
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
        final List value = this.flagValues.get(flag);
        return value != null && !value.isEmpty();
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
    @API(status = API.Status.STABLE, since = "1.4.0")
    public boolean isPresent(final @NonNull CommandFlag<Void> flag) {
        return this.isPresent(flag.getName());
    }

    /**
     * Returns a flag value.
     * <p>
     * If using {@link cloud.commandframework.arguments.flags.CommandFlag.FlagMode#SINGLE}
     * then this returns the only value, if it has been specified. If using
     * {@link cloud.commandframework.arguments.flags.CommandFlag.FlagMode#REPEATABLE} then
     * it'll return the first value.
     *
     * @param name Flag name
     * @param <T>  Value type
     * @return Optional containing stored value if present
     * @since 1.2.0
     */
    @API(status = API.Status.STABLE, since = "1.2.0")
    public <T> @NonNull Optional<T> getValue(
            final @NonNull String name
    ) {
        final List value = this.flagValues.get(name);
        if (value == null || value.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of((T) value.get(0));
    }

    /**
     * Returns a flag value.
     * <p>
     * If using {@link cloud.commandframework.arguments.flags.CommandFlag.FlagMode#SINGLE}
     * then this returns the only value, if it has been specified. If using
     * {@link cloud.commandframework.arguments.flags.CommandFlag.FlagMode#REPEATABLE} then
     * it'll return the first value.
     *
     * @param flag Flag type
     * @param <T>  Value type
     * @return Optional containing stored value if present
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
    public <T> @NonNull Optional<T> getValue(
            final @NonNull CommandFlag<T> flag
    ) {
        return this.getValue(flag.getName());
    }

    /**
     * Returns a flag value.
     * <p>
     * If using {@link cloud.commandframework.arguments.flags.CommandFlag.FlagMode#SINGLE}
     * then this returns the only value, if it has been specified. If using
     * {@link cloud.commandframework.arguments.flags.CommandFlag.FlagMode#REPEATABLE} then
     * it'll return the first value.
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
     * Returns a flag value.
     * <p>
     * If using {@link cloud.commandframework.arguments.flags.CommandFlag.FlagMode#SINGLE}
     * then this returns the only value, if it has been specified. If using
     * {@link cloud.commandframework.arguments.flags.CommandFlag.FlagMode#REPEATABLE} then
     * it'll return the first value.
     *
     * @param name         Flag value
     * @param defaultValue Default value
     * @param <T>          Value type
     * @return Stored value, or the supplied default value
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
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
    @API(status = API.Status.STABLE, since = "1.2.0")
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
    @API(status = API.Status.STABLE, since = "1.4.0")
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
    @API(status = API.Status.STABLE, since = "1.3.0")
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
    @API(status = API.Status.STABLE, since = "1.4.0")
    public boolean contains(final @NonNull CommandFlag<?> flag) {
        return this.hasFlag(flag);
    }

    /**
     * Returns a flag value.
     * <p>
     * If using {@link cloud.commandframework.arguments.flags.CommandFlag.FlagMode#SINGLE}
     * then this returns the only value, if it has been specified. If using
     * {@link cloud.commandframework.arguments.flags.CommandFlag.FlagMode#REPEATABLE} then
     * it'll return the first value.
     *
     * @param name Flag name
     * @param <T>  Value type
     * @return Stored value if present, else {@code null}
     * @since 1.3.0
     */
    @SuppressWarnings("TypeParameterUnusedInFormals")
    @API(status = API.Status.STABLE, since = "1.3.0")
    public <T> @Nullable T get(
            final @NonNull String name
    ) {
        return this.<T>getValue(name).orElse(null);
    }

    /**
     * Returns a flag value.
     * <p>
     * If using {@link cloud.commandframework.arguments.flags.CommandFlag.FlagMode#SINGLE}
     * then this returns the only value, if it has been specified. If using
     * {@link cloud.commandframework.arguments.flags.CommandFlag.FlagMode#REPEATABLE} then
     * it'll return the first value.
     *
     * @param flag Flag name
     * @param <T>  Value type
     * @return Stored value if present, else {@code null}
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
    public <T> @Nullable T get(
            final @NonNull CommandFlag<T> flag
    ) {
        return this.getValue(flag).orElse(null);
    }

    /**
     * Returns all supplied flag values for the given {@code flag}.
     *
     * @param flag the flag
     * @param <T>  the flag value type
     * @return unmodifiable view of all stored flag values, or {@link Collections#emptyList()}.
     * @since 1.7.0
     */
    @API(status = API.Status.STABLE, since = "1.7.0")
    public <T> @NonNull Collection<T> getAll(
            final @NonNull CommandFlag<T> flag
    ) {
        final List values = this.flagValues.get(flag.getName());
        if (values != null) {
            return Collections.unmodifiableList((List<T>) values);
        }
        return Collections.emptyList();
    }

    /**
     * Returns all supplied flag values for the given {@code flag}.
     *
     * @param flag the flag
     * @param <T>  the flag value type
     * @return unmodifiable view of all stored flag values, or {@link Collections#emptyList()}.
     * @since 1.7.0
     */
    @API(status = API.Status.STABLE, since = "1.7.0")
    public <T> @NonNull Collection<T> getAll(
            final @NonNull String flag
    ) {
        final List values = this.flagValues.get(flag);
        if (values != null) {
            return Collections.unmodifiableList((List<T>) values);
        }
        return Collections.emptyList();
    }
}
