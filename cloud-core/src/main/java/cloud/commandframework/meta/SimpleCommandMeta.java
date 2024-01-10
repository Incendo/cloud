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
package cloud.commandframework.meta;

import cloud.commandframework.keys.CloudKey;
import io.leangen.geantyref.GenericTypeReflector;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A simple immutable string-string map containing command meta
 */
@SuppressWarnings("unused")
@API(status = API.Status.STABLE)
public class SimpleCommandMeta extends CommandMeta {

    private final Map<CloudKey<?>, Object> metaMap;

    protected SimpleCommandMeta(final @NonNull Map<@NonNull CloudKey<?>, @NonNull Object> metaMap) {
        this.metaMap = Collections.unmodifiableMap(new HashMap<>(metaMap));
    }

    @Override
    @SuppressWarnings("unchecked")
    public final @NonNull <V> Optional<V> optional(final @NonNull CloudKey<V> key) {
        final Object value = this.metaMap.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (!GenericTypeReflector.isSuperType(key.type().getType(), value.getClass())) {
            throw new IllegalArgumentException("Conflicting argument types between key type of "
                    + key.type().getType().getTypeName() + " and value type of " + value.getClass());
        }

        return Optional.of((V) value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <V> Optional<V> optional(final @NonNull String key) {
        final Object value = this.metaMap.get(CloudKey.of(key));
        if (value == null) {
            return Optional.empty();
        }

        return Optional.of((V) value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final @NonNull CloudKey<?> key) {
        return this.metaMap.containsKey(key);
    }

    @Override
    public final @NonNull Map<@NonNull CloudKey<?>, @NonNull ?> all() {
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
}
