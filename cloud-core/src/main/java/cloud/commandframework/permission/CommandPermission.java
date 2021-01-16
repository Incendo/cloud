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
package cloud.commandframework.permission;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * A command permission representation.
 */
public interface CommandPermission {

    /**
     * Get the permission nodes
     *
     * @return Permission nodes
     */
    @NonNull Collection<@NonNull CommandPermission> getPermissions();

    /**
     * Get a string representation of the permission
     *
     * @return String representation of the permission node
     */
    @Override
    String toString();

    /**
     * Return a permission that matches either this permission or the {@code other} permission.
     *
     * @param other the other permission to test
     * @return a new {@code or} permission
     * @since 1.4.0
     */
    default CommandPermission or(final CommandPermission other) {
        requireNonNull(other, "other");
        final Set<CommandPermission> permission = new HashSet<>(2);
        permission.add(this);
        permission.add(other);
        return new OrPermission(permission);
    }

    /**
     * Return a permission that matches either this permission or any of the {@code other} permissions.
     *
     * @param other the other permission to test
     * @return a new {@code or} permission
     * @since 1.4.0
     */
    default CommandPermission or(final CommandPermission... other) {
        requireNonNull(other, "other");
        final Set<CommandPermission> permission = new HashSet<>(other.length + 1);
        permission.add(this);
        permission.addAll(Arrays.asList(other));
        return new OrPermission(permission);
    }

    /**
     * Return a permission that matches this permission and the {@code other} permission.
     *
     * @param other the other permission to test
     * @return a new {@code and} permission
     * @since 1.4.0
     */
    default CommandPermission and(final CommandPermission other) {
        requireNonNull(other, "other");
        final Set<CommandPermission> permission = new HashSet<>(2);
        permission.add(this);
        permission.add(other);
        return new AndPermission(permission);
    }

    /**
     * Return a permission that matches this permission and all of the {@code other} permissions.
     *
     * @param other the other permission to test
     * @return a new {@code and} permission
     * @since 1.4.0
     */
    default CommandPermission and(final CommandPermission... other) {
        requireNonNull(other, "other");
        final Set<CommandPermission> permission = new HashSet<>(other.length + 1);
        permission.add(this);
        permission.addAll(Arrays.asList(other));
        return new AndPermission(permission);
    }

}
