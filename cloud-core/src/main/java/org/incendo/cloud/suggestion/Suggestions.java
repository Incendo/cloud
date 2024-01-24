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
package org.incendo.cloud.suggestion;

import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.internal.ImmutableImpl;

@Value.Immutable
@ImmutableImpl
@API(status = API.Status.STABLE)
public interface Suggestions<C, S extends Suggestion> {

    /**
     * Returns the {@link CommandContext command context}.
     *
     * @return the command context
     */
    @NonNull CommandContext<C> commandContext();

    /**
     * Returns the list of {@link Suggestion suggestions}.
     *
     * @return suggestion list
     */
    @NonNull List<S> list();

    /**
     * Returns the {@link CommandInput} as seen by the last suggestion provider.
     *
     * @return command input
     */
    @NonNull CommandInput commandInput();

    /**
     * Create a {@link Suggestions} instance.
     *
     * @param ctx          command context
     * @param list         suggestion list
     * @param commandInput command input
     * @param <C>          command sender type
     * @param <S>          suggestion type
     * @return suggestions instance
     */
    @API(status = API.Status.INTERNAL)
    static <C, S extends Suggestion> Suggestions<C, S> create(
            final @NonNull CommandContext<C> ctx,
            final @NonNull List<S> list,
            final @NonNull CommandInput commandInput
    ) {
        return SuggestionsImpl.of(ctx, list, commandInput);
    }
}
