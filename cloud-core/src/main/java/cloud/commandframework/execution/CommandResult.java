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
package cloud.commandframework.execution;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.internal.ImmutableImpl;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;

/**
 * The result of a command execution.
 *
 * @param <C> command sender type
 */
@ImmutableImpl
@Value.Immutable
@API(status = API.Status.STABLE, since = "2.0.0")
public interface CommandResult<C> {

    /**
     * Creates a {@link CommandResult} using the given {@code context}.
     *
     * @param <C>     command sender type
     * @param context command context
     * @return the result instance
     */
    static <C> @NonNull CommandResult<C> of(final @NonNull CommandContext<C> context) {
        return CommandResultImpl.of(context);
    }

    /**
     * Returns the command context.
     *
     * @return the command context
     */
    @NonNull CommandContext<C> commandContext();
}
