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
package cloud.commandframework.permission;

import cloud.commandframework.internal.ImmutableImpl;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;

import static java.util.Objects.requireNonNull;

/**
 * A command permission representation.
 *
 * <p>The meaning of a permission is dependent on the platform, and the same permission node may represent different things in
 * different environments.</p>
 *
 * @see PredicatePermission
 * @since 2.0.0
 */
@ImmutableImpl
@Value.Immutable
@API(status = API.Status.STABLE, since = "2.0.0")
public interface Permission {

    Permission EMPTY = permission("");

    /**
     * Returns a simple permission that is represented by the given {@code permission} string.
     *
     * @param permission permission string
     * @return the permission
     */
    static @NonNull Permission permission(final @NonNull String permission) {
        return PermissionImpl.of(permission);
    }

    /**
     * Returns a simple permission that is represented by the given {@code permission} string.
     *
     * @param permission permission string
     * @return the permission
     */
    static @NonNull Permission of(final @NonNull String permission) {
        return permission(permission);
    }

    /**
     * Returns an empty permission node.
     *
     * @return an empty permission node
     */
    static @NonNull Permission empty() {
        return EMPTY;
    }

    /**
     * Creates a new AND permission that evaluates to {@code true} if all the given {@code permissions} evaluate to {@code true}.
     *
     * @param permissions permissions to join
     * @return the permission
     */
    static @NonNull Permission allOf(final @NonNull Collection<@NonNull Permission> permissions) {
        final Set<Permission> objects = new HashSet<>();
        for (final Permission permission : permissions) {
            if (permission instanceof AndPermission) {
                objects.addAll(permission.permissions());
            } else {
                objects.add(permission);
            }
        }
        return new AndPermission(objects);
    }

    /**
     * Creates a new AND permission that evaluates to {@code true} if all the given {@code permissions} evaluate to {@code true}.
     *
     * @param permissions permissions to join
     * @return the permission
     */
    static @NonNull Permission allOf(final @NonNull Permission @NonNull... permissions) {
        return allOf(Arrays.asList(permissions));
    }

    /**
     * Create a new OR permission that evaluates to {@code true} if any of the given {@code permissions}
     * evaluates to {@code true}.
     *
     * @param permissions permissions to join
     * @return the permission
     */
    static @NonNull Permission anyOf(final @NonNull Collection<@NonNull Permission> permissions) {
        final Set<Permission> objects = new HashSet<>();
        for (final Permission permission : permissions) {
            if (permission instanceof OrPermission) {
                objects.addAll(permission.permissions());
            } else {
                objects.add(permission);
            }
        }
        return new OrPermission(objects);
    }

    /**
     * Create a new OR permission that evaluates to {@code true} if any of the given {@code permissions}
     * evaluates to {@code true}.
     *
     * @param permissions permissions to join
     * @return the permission
     */
    static @NonNull Permission anyOf(final @NonNull Permission @NonNull... permissions) {
        return anyOf(Arrays.asList(permissions));
    }

    /**
     * Returns the inner permission nodes of this node.
     *
     * @return the inner permission nodes
     */
    default @NonNull Collection<@NonNull Permission> permissions() {
        return Collections.singleton(this);
    }

    /**
     * Returns the string representation of the permission.
     *
     * @return string representation
     */
    @NonNull String permissionString();


    /**
     * Returns true if a check for this permission should and will always return true.
     *
     * @return true if this permission is empty, otherwise false
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    default boolean isEmpty() {
        return this.permissionString().isEmpty();
    }

    /**
     * Returns a permission that matches either this permission or the {@code other} permission.
     *
     * @param other the other permission to test
     * @return a new {@code or} permission
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
    default @NonNull Permission or(final @NonNull Permission other) {
        requireNonNull(other, "other");
        final Set<Permission> permission = new HashSet<>(2);
        permission.add(this);
        permission.add(other);
        return Permission.anyOf(permission);
    }

    /**
     * Returns a permission that matches either this permission or any of the {@code other} permissions.
     *
     * @param other the other permission to test
     * @return a new {@code or} permission
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
    default @NonNull Permission or(final @NonNull Permission @NonNull... other) {
        requireNonNull(other, "other");
        final Set<Permission> permission = new HashSet<>(other.length + 1);
        permission.add(this);
        permission.addAll(Arrays.asList(other));
        return Permission.anyOf(permission);
    }

    /**
     * Returns a permission that matches this permission and the {@code other} permission.
     *
     * @param other the other permission to test
     * @return a new {@code and} permission
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
    default @NonNull Permission and(final @NonNull Permission other) {
        requireNonNull(other, "other");
        final Set<Permission> permission = new HashSet<>(2);
        permission.add(this);
        permission.add(other);
        return Permission.allOf(permission);
    }

    /**
     * Returns a permission that matches this permission and all of the {@code other} permissions.
     *
     * @param other the other permission to test
     * @return a new {@code and} permission
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
    default @NonNull Permission and(final @NonNull Permission @NonNull... other) {
        requireNonNull(other, "other");
        final Set<Permission> permission = new HashSet<>(other.length + 1);
        permission.add(this);
        permission.addAll(Arrays.asList(other));
        return Permission.allOf(permission);
    }
}
