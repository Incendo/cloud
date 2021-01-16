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
package cloud.commandframework.exceptions;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.permission.CommandPermission;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

/**
 * Exception thrown when a command sender misses a permission required
 * to execute a {@link Command}
 */
@SuppressWarnings("unused")
public class NoPermissionException extends CommandParseException {

    private static final long serialVersionUID = 7103413337750692843L;
    private final CommandPermission missingPermission;

    /**
     * Construct a new no permission exception
     *
     * @param missingPermission Missing permission node
     * @param commandSender     Command sender
     * @param currentChain      Chain leading up to the exception
     */
    public NoPermissionException(
            final @NonNull CommandPermission missingPermission,
            final @NonNull Object commandSender,
            final @NonNull List<@NonNull CommandArgument<?, ?>> currentChain
    ) {
        super(commandSender, currentChain);
        this.missingPermission = missingPermission;
    }

    @Override
    public final String getMessage() {
        return String.format("Missing permission '%s'", this.missingPermission);
    }

    /**
     * Get the missing permission node
     *
     * @return Get the missing permission node
     */
    public @NonNull String getMissingPermission() {
        return this.missingPermission.toString();
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
