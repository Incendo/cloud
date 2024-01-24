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
package cloud.commandframework.exception;

import cloud.commandframework.Command;
import cloud.commandframework.component.CommandComponent;
import cloud.commandframework.permission.Permission;
import cloud.commandframework.permission.PermissionResult;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Exception thrown when a command sender misses a permission required
 * to execute a {@link Command}
 */
@SuppressWarnings({"unused", "serial"})
@API(status = API.Status.STABLE)
public class NoPermissionException extends CommandParseException {

    private final PermissionResult result;

    /**
     * Constructs a new no permission exception,
     *
     * @param permissionResult result of the permission check
     * @param commandSender    command sender
     * @param currentChain     chain leading up to the exception
     */
    @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
    public NoPermissionException(
            final @NonNull PermissionResult permissionResult,
            final @NonNull Object commandSender,
            final @NonNull List<@NonNull CommandComponent<?>> currentChain
    ) {
        super(commandSender, currentChain);
        if (permissionResult.allowed()) {
            throw new IllegalArgumentException("Provided permission result was one that succeeded instead of failed");
        }
        this.result = permissionResult;
    }

    @Override
    public final String getMessage() {
        return String.format("Missing permission '%s'", this.missingPermission());
    }

    /**
     * Returns the missing {@link Permission}
     *
     * @return the missing permission
     * @since 1.9.0
     */
    @API(status = API.Status.STABLE, since = "1.9.0")
    public @NonNull Permission missingPermission() {
        return this.result.permission();
    }

    /**
     * Returns the falsy {@link PermissionResult} that caused this exception.
     *
     * @return the falsy result that caused this exception
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull PermissionResult permissionResult() {
        return this.result;
    }

    @Override
    public final synchronized Throwable fillInStackTrace() {
        return this;
    }

    @Override
    public final synchronized Throwable initCause(final Throwable cause) {
        return this;
    }
}
