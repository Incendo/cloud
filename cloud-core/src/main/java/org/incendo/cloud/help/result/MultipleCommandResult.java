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
package org.incendo.cloud.help.result;

import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;
import org.incendo.cloud.help.HelpQuery;
import org.incendo.cloud.internal.ImmutableImpl;

/**
 * A list of commands.
 * <p>
 * This is the result of a query that is not specific enough to point to a single command,
 * but unlike {@link IndexCommandResult} this is guaranteed to not be empty.
 * <p>
 * The {@link #longestPath()} is the longest command path shared between all the results, and the
 * {@link #childSuggestions()}} are the commands that share that common path.
 *
 * @param <C> the command sender type
 */
@ImmutableImpl
@Value.Immutable
@API(status = API.Status.STABLE)
public interface MultipleCommandResult<C> extends HelpQueryResult<C> {

    /**
     * Creates a new result.
     *
     * @param <C>              the command sender type
     * @param query            the query that prompted the result
     * @param longestPath      the longest shared result
     * @param childSuggestions the syntax hints for the children
     * @return the result
     */
    static <C> @NonNull MultipleCommandResult<C> of(
            final @NonNull HelpQuery<C> query,
            final @NonNull String longestPath,
            final @NonNull List<@NonNull String> childSuggestions
    ) {
        return MultipleCommandResultImpl.of(query, longestPath, childSuggestions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull HelpQuery<C> query();

    /**
     * Returns the longest path shared between the children.
     *
     * @return the longest shared path
     */
    @NonNull String longestPath();

    /**
     * Returns syntax hints for the children.
     *
     * @return child suggestions
     */
    @NonNull List<@NonNull String> childSuggestions();
}
