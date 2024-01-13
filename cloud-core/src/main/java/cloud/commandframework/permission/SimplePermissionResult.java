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

import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Simple implementation of {@link PermissionResult}
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public class SimplePermissionResult implements PermissionResult {

    private final boolean result;
    private final Permission permission;

    /**
     * Creates a result that wraps the given boolean result.
     *
     * @param result true if the command may be executed, false otherwise
     * @param permission the permission that this result came from
     */
    protected SimplePermissionResult(final boolean result, final @NonNull Permission permission) {
        this.result = result;
        this.permission = Objects.requireNonNull(permission, "permission");
    }

    @Override
    public final boolean allowed() {
        return this.result;
    }

    @Override
    public final boolean denied() {
        // this doesn't need to be overridden, but we do it so that we can make it final
        return !this.result;
    }

    @Override
    public final @NonNull Permission permission() {
        return this.permission;
    }

    /**
     * This is just going to be @Immutable oh my
     *
     * @param o This is just going to be @Immutable oh my
     * @return This is just going to be @Immutable oh my
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SimplePermissionResult that = (SimplePermissionResult) o;
        return this.result == that.result && Objects.equals(this.permission, that.permission);
    }

    /**
     * This is just going to be @Immutable oh my
     * @return This is just going to be @Immutable oh my
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.result, this.permission);
    }

    /**
     * This is just going to be @Immutable oh my
     * @return This is just going to be @Immutable oh my
     */
    @Override
    public String toString() {
        final String result = this.result ? "Successful" : "Failed";
        return result + "PermissionResult[" + this.permission + "]";
    }
}
