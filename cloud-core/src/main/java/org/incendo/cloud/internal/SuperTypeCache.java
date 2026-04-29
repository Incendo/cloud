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
package org.incendo.cloud.internal;

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Global cache for {@link GenericTypeReflector#isSuperType(Type, Type)} calls.
 *
 * <p>{@code isSuperType} is called multiple times in cloud-core and platform-specific implementations.
 * Caching the supertype check results permanently is safe (java types aren't mutable during runtime)
 *
 * @since 2.0.0
 */
@API(status = API.Status.INTERNAL, since = "2.0.0")
public final class SuperTypeCache {

    private static final Map<Type, Map<Class<?>, Boolean>> TYPE_CACHE = new ConcurrentHashMap<>();

    private SuperTypeCache() {
    }

    /**
     * Returns whether {@code superType} is a supertype of {@code subType}.
     * All results of calls to this method are cached (for efficiency).
     *
     * @param superType the supertype
     * @param subType the subtype to test
     * @return true if the {@code superType} is a supertype pf {@code subType}
     */
    public static boolean isSuperType(final @NonNull Type superType, final @NonNull Class<?> subType) {
        return TYPE_CACHE
                .computeIfAbsent(superType, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(
                        subType,
                        k -> GenericTypeReflector.isSuperType(superType, subType)
                );
    }
}
