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
package cloud.commandframework.meta;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A simple immutable string-string map containing command meta
 */
@SuppressWarnings("unused")
public class SimpleCommandMeta extends CommandMeta {

    private final Map<String, String> metaMap;

    protected SimpleCommandMeta(final @NonNull Map<@NonNull String, @NonNull String> metaMap) {
        this.metaMap = Collections.unmodifiableMap(metaMap);
    }

    /**
     * Create a new meta builder
     *
     * @return Builder instance
     */
    public static SimpleCommandMeta.@NonNull Builder builder() {
        return new Builder();
    }

    /**
     * Create an empty simple command meta instance
     *
     * @return Empty instance
     */
    public static @NonNull SimpleCommandMeta empty() {
        return SimpleCommandMeta.builder().build();
    }

    @Override
    public final @NonNull Optional<String> getValue(final @NonNull String key) {
        return Optional.ofNullable(this.metaMap.get(key));
    }

    @Override
    public final @NonNull String getOrDefault(final @NonNull String key, final @NonNull String defaultValue) {
        return this.getValue(key).orElse(defaultValue);
    }

    @Override
    public final @NonNull Map<@NonNull String, @NonNull String> getAll() {
        return new HashMap<>(this.metaMap);
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SimpleCommandMeta that = (SimpleCommandMeta) o;
        return Objects.equals(metaMap, that.metaMap);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(metaMap);
    }

    /**
     * Builder for {@link SimpleCommandMeta}
     */
    public static final class Builder {

        private final Map<String, String> map = new HashMap<>();

        private Builder() {
        }

        /**
         * Copy all values from another command meta instance
         *
         * @param commandMeta Existing instance
         * @return Builder instance
         */
        public @NonNull Builder with(final @NonNull CommandMeta commandMeta) {
            commandMeta.getAll().forEach(this::with);
            return this;
        }

        /**
         * Store a new key-value pair in the meta map
         *
         * @param key   Key
         * @param value Value
         * @return Builder instance
         */
        public @NonNull Builder with(final @NonNull String key,
                                     final @NonNull String value) {
            this.map.put(key, value);
            return this;
        }

        /**
         * Construct a new meta instance
         *
         * @return Meta instance
         */
        public @NonNull SimpleCommandMeta build() {
            return new SimpleCommandMeta(this.map);
        }

    }

}
