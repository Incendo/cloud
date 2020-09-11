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

import com.intellectualsites.commands.components.CommandComponent;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Exception thrown when a {@link CommandSender} misses a permission required
 * to execute a {@link com.intellectualsites.commands.Command}
 */
public class NoPermissionException extends CommandParseException {

    private final String missingPermission;

    public NoPermissionException(@Nonnull final String missingPermission,
                                 @Nonnull final CommandSender commandSender,
                                 @Nonnull final List<CommandComponent<?, ?>> currentChain) {
        super(commandSender, currentChain);
        this.missingPermission = missingPermission;
    }

    @Override
    public String getMessage() {
        return String.format("Missing permission '%s'", this.missingPermission);
    }

    @Nonnull
    public String getMissingPermission() {
        return this.missingPermission;
    }

}
