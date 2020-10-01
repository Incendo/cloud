//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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

import java.util.HashMap;
import java.util.Map;

/**
 * Flag value mappings
 */
public class FlagContext {

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
    public void addPresenceFlag(@NonNull final CommandFlag<?> flag) {
        this.flagValues.put(flag.getName(), FLAG_PRESENCE_VALUE);
    }

    /**
     * Store a value associated with a value flag
     *
     * @param flag  Value flag
     * @param value Flag value
     * @param <T>   Value type
     */
    public <T> void addValueFlag(@NonNull final CommandFlag<T> flag, @NonNull final T value) {
        this.flagValues.put(flag.getName(), value);
    }

    /**
     * Check whether or not a flag is present. This will return {@code false}
     * for all value flags.
     *
     * @param flag Flag name
     * @return {@code true} if the flag is presence and the flag is a presence flag,
     * else {@code false}
     */
    public boolean isPresent(@NonNull final String flag) {
        final Object value = this.flagValues.get(flag);
        return FLAG_PRESENCE_VALUE.equals(value);
    }

    /**
     * Get a flag value
     *
     * @param name         Flag name
     * @param defaultValue Default value
     * @param <T>          Value type
     * @return Stored value, or the supplied default value
     */
    public <T> T getValue(@NonNull final String name, @NonNull final T defaultValue) {
        final Object value = this.flagValues.get(name);
        if (value == null) {
            return defaultValue;
        }
        @SuppressWarnings("unchecked") final T casted = (T) value;
        return casted;
    }

}
