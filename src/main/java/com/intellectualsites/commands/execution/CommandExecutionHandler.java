//
// MIT License
//
// Copyright (c) 2020 IntellectualSites
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
package com.intellectualsites.commands.execution;

import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;

/**
 * Handler that is invoked whenever a {@link com.intellectualsites.commands.Command} is executed
 * by a {@link com.intellectualsites.commands.sender.CommandSender}
 *
 * @param <C> Command sender type
 */
@FunctionalInterface public interface CommandExecutionHandler<C extends CommandSender> {

    /**
     * Handle command execution
     *
     * @param commandContext Command context
     */
    void execute(@Nonnull final CommandContext<C> commandContext);


    class NullCommandExecutionHandler<C extends CommandSender> implements CommandExecutionHandler<C> {

        @Override
        public void execute(@Nonnull final CommandContext<C> commandContext) {

        }

    }

}
