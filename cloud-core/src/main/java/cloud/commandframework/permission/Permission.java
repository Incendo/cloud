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

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * {@link cloud.commandframework.arguments.CommandArgument} permission
 */
public final class Permission implements CommandPermission {

    /**
     * Empty command permission
     */
    private static final Permission EMPTY = Permission.of("");

    private final String permission;

    private Permission(final @NonNull String permission) {
        this.permission = permission;
    }

    /**
     * Get an empty command permission
     *
     * @return Command permission
     */
    public static @NonNull Permission empty() {
        return EMPTY;
    }

    /**
     * Create a command permission instance
     *
     * @param string Command permission
     * @return Created command permission
     */
    public static @NonNull Permission of(final @NonNull String string) {
        return new Permission(string);
    }

    /**
     * Get the command permission
     *
     * @return Command permission
     */
    public String getPermission() {
        return this.permission;
    }

    @Override
    public @NonNull Collection<@NonNull CommandPermission> getPermissions() {
        return Collections.singleton(this);
    }

    /**
     * Get the command permission
     *
     * @return Command permission
     */
    @Override
    public String toString() {
        return this.permission;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Permission that = (Permission) o;
        return Objects.equals(getPermission(), that.getPermission());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPermission());
    }

}
