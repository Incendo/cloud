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
package cloud.commandframework.examples.jda;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PermissionRegistry {

    private final Map<Long, Set<String>> permissions = new HashMap<>();

    /**
     * Add a permission to a user
     *
     * @param userId     Users id
     * @param permission Permission to add
     */
    public void add(final @NonNull Long userId, final @NonNull String permission) {
        this.getPermissions(userId).add(permission.toLowerCase());
    }

    /**
     * Remove a permission from a user
     *
     * @param userId     Users id
     * @param permission Permission to remove
     */
    public void remove(final @NonNull Long userId, final @NonNull String permission) {
        this.getPermissions(userId).remove(permission.toLowerCase());
    }

    /**
     * Check if a user has a specific permission
     *
     * @param userId     Users id
     * @param permission Permission to check
     * @return True if the user has a permission
     */
    public boolean hasPermission(final @NonNull Long userId, final @Nullable String permission) {
        if (permission == null) {
            return true;
        }

        return this.getPermissions(userId).contains(permission.toLowerCase());
    }

    private Set<String> getPermissions(final @NonNull Long userId) {
        this.permissions.putIfAbsent(userId, new HashSet<>());
        return this.permissions.get(userId);
    }

}
