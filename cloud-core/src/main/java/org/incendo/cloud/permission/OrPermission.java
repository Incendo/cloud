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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Accepts as long as at least one of the permissions is accepted
 */
@API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class OrPermission implements Permission {

    private final Set<Permission> permissions;

    OrPermission(final @NonNull Set<Permission> permissions) {
        if (permissions.isEmpty()) {
            throw new IllegalArgumentException("OrPermission may not have an empty set of permissions");
        }
        this.permissions = Collections.unmodifiableSet(permissions);
    }

    @Override
    public @NonNull Collection<@NonNull Permission> permissions() {
        return this.permissions;
    }

    @Override
    public boolean isEmpty() {
        return false; // we require the permissions to be non-empty
    }

    @Override
    public @NonNull String permissionString() {
        final StringBuilder stringBuilder = new StringBuilder();
        final Iterator<Permission> iterator = this.permissions.iterator();
        while (iterator.hasNext()) {
            final Permission permission = iterator.next();
            stringBuilder.append('(').append(permission.permissionString()).append(')');
            if (iterator.hasNext()) {
                stringBuilder.append('|');
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final OrPermission that = (OrPermission) o;
        return this.permissions.equals(that.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.permissions());
    }

    @Override
    public @NonNull String toString() {
        return this.permissionString();
    }
}
