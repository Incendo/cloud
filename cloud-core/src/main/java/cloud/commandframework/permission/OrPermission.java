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
package cloud.commandframework.permission;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Accepts as long as at least one of the permissions is accepted
 */
public final class OrPermission implements CommandPermission {

    private final Collection<CommandPermission> permissions;

    private OrPermission(final @NonNull Collection<CommandPermission> permissions) {
        this.permissions = permissions;
    }

    /**
     * Create a new OR permission
     *
     * @param permissions Permissions to join
     * @return Constructed permission
     */
    public static @NonNull CommandPermission of(final @NonNull Collection<CommandPermission> permissions) {
        final Set<CommandPermission> permissionSet = new HashSet<>();
        for (final CommandPermission permission : permissions) {
            permissionSet.addAll(permission.getPermissions());
        }
        return new OrPermission(permissionSet);
    }

    @Override
    public @NonNull Collection<@NonNull CommandPermission> getPermissions() {
        return this.permissions;
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        final Iterator<CommandPermission> iterator = this.permissions.iterator();
        while (iterator.hasNext()) {
            final CommandPermission permission = iterator.next();
            stringBuilder.append('(').append(permission.toString()).append(')');
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OrPermission that = (OrPermission) o;
        final List<CommandPermission> local = new ArrayList<>(this.getPermissions());
        final List<CommandPermission> foreign = new ArrayList<>(that.getPermissions());
        return local.equals(foreign);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPermissions());
    }

}
