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
package cloud.commandframework.meta;

import io.leangen.geantyref.GenericTypeReflector;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A simple immutable string-string map containing command meta
 */
@SuppressWarnings("unused")
public class SimpleCommandMeta extends CommandMeta {

    private final Map<String, Object> metaMap;

    @Deprecated
    protected SimpleCommandMeta(final @NonNull Map<@NonNull String, @NonNull String> metaMap) {
        this.metaMap = Collections.unmodifiableMap(metaMap);
    }

    protected SimpleCommandMeta(final SimpleCommandMeta source) {
        this.metaMap = source.metaMap;
    }

    // Constructor needs an extra flag to distinguish it from the old one (for reified generics)
    SimpleCommandMeta(final @NonNull Map<@NonNull String, @NonNull Object> metaMap, final boolean unusedMarkerForNew) {
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
    @Deprecated
    public final @NonNull Optional<String> getValue(final @NonNull String key) {
        final Object result = this.metaMap.get(key);
        if (result != null && !(result instanceof String)) {
            throw new IllegalArgumentException("Key '" + key + "' has been used for a new typed command meta and contains a "
                    + "non-string value!");
        }
        return Optional.ofNullable((String) result);
    }

    @Override
    @Deprecated
    public final @NonNull String getOrDefault(final @NonNull String key, final @NonNull String defaultValue) {
        return this.getValue(key).orElse(defaultValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final @NonNull <V> Optional<V> get(final @NonNull Key<V> key) {
        final Object value = this.metaMap.get(key.getName());
        if (value == null) {
            // Attempt to use a fallback legacy type
            if (key.getFallbackDerivation() != null) {
                return Optional.ofNullable(key.getFallbackDerivation().apply(this));
            }

            return Optional.empty();
        }
        if (!GenericTypeReflector.isSuperType(key.getValueType().getType(), value.getClass())) {
            throw new IllegalArgumentException("Conflicting argument types between key type of "
                    + key.getValueType().getType() + " and value type of " + value.getClass());
        }

        return Optional.of((V) value);
    }

    @Override
    public final <V> @NonNull V getOrDefault(final @NonNull Key<V> key, final @NonNull V defaultValue) {
        return this.get(key).orElse(defaultValue);
    }

    @Override
    @Deprecated
    public final @NonNull Map<@NonNull String, @NonNull String> getAll() {
        return this.metaMap.entrySet()
                .stream().filter(ent -> ent.getValue() instanceof String)
                .collect(Collectors.<Map.Entry<String, Object>, String, String>toMap(
                        Map.Entry::getKey,
                        ent -> ent.getValue().toString()
                ));
    }

    @Override
    public final @NonNull Map<@NonNull String, @NonNull ?> getAllValues() {
        return new HashMap<>(this.metaMap);
    }

    @Override
    public final boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final SimpleCommandMeta that = (SimpleCommandMeta) other;
        return Objects.equals(this.metaMap, that.metaMap);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(this.metaMap);
    }

    /**
     * Builder for {@link SimpleCommandMeta}
     */
    public static final class Builder {

        private final Map<String, Object> map = new HashMap<>();

        private Builder() {
        }

        /**
         * Copy all values from another command meta instance
         *
         * @param commandMeta Existing instance
         * @return Builder instance
         */
        public @NonNull Builder with(final @NonNull CommandMeta commandMeta) {
            if (commandMeta instanceof SimpleCommandMeta) {
                this.map.putAll(((SimpleCommandMeta) commandMeta).metaMap);
            } else {
                this.map.putAll(commandMeta.getAllValues());
            }
            return this;
        }

        /**
         * Store a new key-value pair in the meta map
         *
         * @param key   Key
         * @param value Value
         * @return Builder instance
         * @deprecated For removal since 1.3.0, use {@link #with(Key, Object) the typesafe alternative} instead
         */
        @Deprecated
        public @NonNull Builder with(
                final @NonNull String key,
                final @NonNull String value
        ) {
            this.map.put(key, value);
            return this;
        }

        /**
         * Store a new key-value pair in the meta map
         *
         * @param <V>   Value type
         * @param key   Key
         * @param value Value
         * @return Builder instance
         * @since 1.3.0
         */
        public <V> @NonNull Builder with(
                final @NonNull Key<V> key,
                final @NonNull V value
        ) {
           this.map.put(key.getName(), value);
           return this;
        }

        /**
         * Construct a new meta instance
         *
         * @return Meta instance
         */
        public @NonNull SimpleCommandMeta build() {
            return new SimpleCommandMeta(this.map, false);
        }

    }

}
