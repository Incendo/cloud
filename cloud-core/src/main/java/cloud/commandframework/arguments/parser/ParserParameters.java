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
package cloud.commandframework.arguments.parser;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Collection of {@link ParserParameter parameter}-{@link Object object} pairs
 */
public final class ParserParameters {

    private final Map<ParserParameter<?>, Object> internalMap = new HashMap<>();

    /**
     * Get an empty {@link ParserParameters} instance
     *
     * @return Empty instance
     */
    public static @NonNull ParserParameters empty() {
        return new ParserParameters();
    }

    /**
     * Create a {@link ParserParameters} instance containing a single key-value par
     *
     * @param parameter Parameter
     * @param value     Value
     * @param <T>       Value type
     * @return Constructed instance
     */
    public static <T> @NonNull ParserParameters single(
            final @NonNull ParserParameter<T> parameter,
            final @NonNull T value
    ) {
        final ParserParameters parameters = new ParserParameters();
        parameters.store(parameter, value);
        return parameters;
    }

    /**
     * Check if this instance contains a parameter-object pair for a given parameter
     *
     * @param parameter Parameter
     * @param <T>       Parameter type
     * @return {@code true} if such a pair is stored, else {@code false}
     */
    public <T> boolean has(final @NonNull ParserParameter<T> parameter) {
        return this.internalMap.containsKey(parameter);
    }

    /**
     * Store a parameter-object pair
     *
     * @param parameter Parameter
     * @param value     Object
     * @param <T>       Parameter type
     */
    public <T> void store(
            final @NonNull ParserParameter<T> parameter,
            final @NonNull T value
    ) {
        this.internalMap.put(parameter, value);
    }

    /**
     * Get a value from the parameter map, if it is stored, else return a default value
     *
     * @param parameter    Parameter to retrieve
     * @param defaultValue Default value
     * @param <T>          Parameter type
     * @return Parameter value
     */
    @SuppressWarnings("unchecked")
    public <T> @NonNull T get(
            final @NonNull ParserParameter<T> parameter,
            final @NonNull T defaultValue
    ) {
        return (T) this.internalMap.getOrDefault(parameter, defaultValue);
    }

    /**
     * Attempt to merge two {@link ParserParameters} instances. If the instances contain conflicting
     * values, the values of the "other" instance will be preferred
     *
     * @param other Other instance
     */
    public void merge(final @NonNull ParserParameters other) {
        this.internalMap.putAll(other.internalMap);
    }

    /**
     * Get an immutable view of the internal map
     *
     * @return Immutable map
     */
    public @NonNull Map<@NonNull ParserParameter<?>, @NonNull Object> getAll() {
        return Collections.unmodifiableMap(this.internalMap);
    }

}
