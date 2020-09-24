//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
package com.intellectualsites.commands.exceptions;

import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.permission.CommandPermission;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Exception thrown when a command sender misses a permission required
 * to execute a {@link com.intellectualsites.commands.Command}
 */
@SuppressWarnings("unused")
public class NoPermissionException extends CommandParseException {

    private final CommandPermission missingPermission;

    /**
     * Construct a new no permission exception
     *
     * @param missingPermission Missing permission node
     * @param commandSender     Command sender
     * @param currentChain      Chain leading up to the exception
     */
    public NoPermissionException(@Nonnull final CommandPermission missingPermission,
                                 @Nonnull final Object commandSender,
                                 @Nonnull final List<CommandArgument<?, ?>> currentChain) {
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
    @Nonnull
    public String getMissingPermission() {
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
