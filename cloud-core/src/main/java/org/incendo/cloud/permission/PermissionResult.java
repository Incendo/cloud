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
package org.incendo.cloud.permission;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;
import org.incendo.cloud.internal.ImmutableImpl;

/**
 * The cached result of a permission check, representing whether a command may be executed.
 *
 * <p>Implementations must be immutable. Most importantly, {@link #allowed()} must always return the same value as previous
 * invocations.</p>
 *
 * <p>Custom implementations may be used in order to provide more information.
 * For example, the reason that the permission lookup returned false.</p>
 *
 */
@ImmutableImpl
@Value.Immutable
@API(status = API.Status.STABLE)
public interface PermissionResult {

    /**
     * Returns {@code true} if the command may be executed.
     *
     * <p>This always returns the opposite of {@link #denied()}.</p>
     *
     * @return {@code true} if the command may be executed
     */
    boolean allowed();

    /**
     * Returns {@code true} if the command may not be executed.
     *
     * <p>This always returns the opposite of {@link #allowed()}.</p>
     *
     * @return {@code true} if the command may not be executed
     */
    default boolean denied() {
        return !this.allowed();
    }

    /**
     * Returns the permission that this result came from.
     *
     * @return the permission that this result came from
     */
    @NonNull Permission permission();

    /**
     * Creates a result that wraps the given boolean result.
     *
     * @param result true if the command may be executed, false otherwise
     * @param permission the permission that this result came from
     * @return a PermissionResult of the boolean result
     */
    static @NonNull PermissionResult of(final boolean result, final @NonNull Permission permission) {
        return PermissionResultImpl.of(result, permission);
    }

    /**
     * Creates a successful result for the given permission.
     *
     * @param permission the permission that this result came from
     * @return a successful PermissionResult
     */
    static @NonNull PermissionResult allowed(final @NonNull Permission permission) {
        return PermissionResultImpl.of(true, permission);
    }

    /**
     * Creates a failed result for the given permission.
     *
     * @param permission the permission that this result came from
     * @return a failed PermissionResult
     */
    static @NonNull PermissionResult denied(final @NonNull Permission permission) {
        return PermissionResultImpl.of(false, permission);
    }
}
