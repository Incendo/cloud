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
package cloud.commandframework.fabric;

import cloud.commandframework.permission.PermissionResult;
import org.apiguardian.api.API;

/**
 * A {@link PermissionResult} that also contains the permission level that was required for the permission check to pass.
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public class PermissionLevelResult implements PermissionResult {

    private final boolean result;
    private final int requiredPermissionLevel;

    /**
     * Creates a new PermissionLevelResult
     *
     * @param result true if the permission check passed, otherwise false
     * @param requiredPermissionLevel the minecraft permission level that was required for the permission check to pass
     */
    public PermissionLevelResult(final boolean result, final int requiredPermissionLevel) {
        this.result = result;
        this.requiredPermissionLevel = requiredPermissionLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean toBoolean() {
        return this.result;
    }

    /**
     * Returns the minecraft permission level that was required for the permission lookup to return true.
     *
     * @return the required permission level
     */
    @SuppressWarnings("unused")
    public int requiredPermissionLevel() {
        return this.requiredPermissionLevel;
    }
}
