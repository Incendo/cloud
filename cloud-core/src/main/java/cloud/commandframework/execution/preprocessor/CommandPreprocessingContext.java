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
package cloud.commandframework.execution.preprocessor;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.internal.ImmutableImpl;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;

/**
 * Context for {@link CommandPreprocessor command preprocessors}.
 *
 * @param <C> command sender type
 * @since 2.0.0
 */
@Value.Immutable
@ImmutableImpl
@API(status = API.Status.STABLE, since = "2.0.0")
public interface CommandPreprocessingContext<C> {

    /**
     * Returns a new preprocessing context.
     *
     * @param <C>            command sender type
     * @param commandContext command context
     * @param commandInput   user-supplied input, possibly modified by prior preprocessors
     * @return the context instance
     */
    static <C> @NonNull CommandPreprocessingContext<C> of(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        return CommandPreprocessingContextImpl.of(commandContext, commandInput);
    }

    /**
     * Returns the command context.
     *
     * @return command context
     */
    @NonNull CommandContext<@NonNull C> commandContext();

    /**
     * Returns the original input.
     *
     * <p>All changes will persist and will be used during parsing.</p>
     *
     * @return command input
     */
    @NonNull CommandInput commandInput();
}
