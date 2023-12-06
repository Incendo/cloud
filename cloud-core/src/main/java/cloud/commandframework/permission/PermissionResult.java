//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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

import org.apiguardian.api.API;

/**
 * The cached result of a permission check, representing whether a command may be executed.
 * <p>
 * Implementations must be immutable. Most importantly, {@link #toBoolean()} must always return the same value as previous
 * invocations.
 * <p>
 * Custom implementations may be used in order to provide more information.
 * For example, the reason that the permission lookup returned false.
 *
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public interface PermissionResult {

    /**
     * Result that returns true
     */
    PermissionResult TRUE = () -> true;

    /**
     * Result that returns false
     */
    PermissionResult FALSE = () -> false;

    /**
     * Returns the result of a permission check
     *
     * @return true if the command may be executed, false otherwise
     */
    boolean toBoolean();

    /**
     * Creates a result that wraps the given boolean result
     *
     * @param result true if the command may be executed, false otherwise
     * @return a PermissionResult of the boolean result
     */
    static PermissionResult of(boolean result) {
        return result ? TRUE : FALSE;
    }
}
