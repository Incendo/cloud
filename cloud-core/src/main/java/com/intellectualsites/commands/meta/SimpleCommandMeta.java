//
// MIT License
//
// Copyright (c) 2020 IntellectualSites
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
package com.intellectualsites.commands.meta;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A simple immutable string-string map containing command meta
 */
public class SimpleCommandMeta extends CommandMeta {

    private final Map<String, String> metaMap;

    protected SimpleCommandMeta(@Nonnull final Map<String, String> metaMap) {
        this.metaMap = Collections.unmodifiableMap(metaMap);
    }

    /**
     * Create a new meta builder
     *
     * @return Builder instance
     */
    @Nonnull
    public static SimpleCommandMeta.Builder builder() {
        return new Builder();
    }

    /**
     * Create an empty simple command meta instance
     *
     * @return Empty instance
     */
    @Nonnull public static SimpleCommandMeta empty() {
        return SimpleCommandMeta.builder().build();
    }

    /**
     * Get the value associated with a key
     *
     * @param key Key
     * @return Optional that may contain the associated value
     */
    @Nonnull
    public Optional<String> getValue(@Nonnull final String key) {
        return Optional.ofNullable(this.metaMap.get(key));
    }

    /**
     * Get the value if it exists, else return the default value
     *
     * @param key          Key
     * @param defaultValue Default value
     * @return Value, or default value
     */
    @Nonnull
    public String getOrDefault(@Nonnull final String key, @Nonnull final String defaultValue) {
        return this.getValue(key).orElse(defaultValue);
    }

    /**
     * Get a copy of the meta map
     *
     * @return Copy of meta map
     */
    @Nonnull
    public Map<String, String> getAll() {
        return new HashMap<>(this.metaMap);
    }

    @Override
    public boolean equals(final Object o) {
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
    public int hashCode() {
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
         * Store a new key-value pair in the meta map
         *
         * @param key   Key
         * @param value Value
         * @return Builder instance
         */
        @Nonnull
        public Builder with(@Nonnull final String key, @Nonnull final String value) {
            this.map.put(key, value);
            return this;
        }

        /**
         * Construct a new meta instance
         *
         * @return Meta instance
         */
        @Nonnull
        public SimpleCommandMeta build() {
            return new SimpleCommandMeta(this.map);
        }

    }

}
