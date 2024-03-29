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
package org.incendo.cloud.exception;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.CommandComponent;

/**
 * Thrown when a {@link CommandComponent}
 * that is registered as a leaf node, does not contain an owning {@link Command}
 */
@SuppressWarnings({"unused", "serial"})
@API(status = API.Status.STABLE)
public final class NoCommandInLeafException extends IllegalStateException {

    private final CommandComponent<?> commandComponent;

    /**
     * Create a new no command in leaf exception instance
     *
     * @param commandComponent Command argument that caused the exception
     */
    @API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.*")
    public NoCommandInLeafException(final @NonNull CommandComponent<?> commandComponent) {
        super(String.format("Leaf node '%s' does not have associated owning command", commandComponent.name()));
        this.commandComponent = commandComponent;
    }

    /**
     * Returns the command component.
     *
     * @return command component
     */
    public @NonNull CommandComponent<?> commandComponent() {
        return this.commandComponent;
    }
}
