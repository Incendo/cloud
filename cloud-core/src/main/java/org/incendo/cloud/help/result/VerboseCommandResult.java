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

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;
import org.incendo.cloud.help.HelpQuery;
import org.incendo.cloud.internal.ImmutableImpl;

/**
 * Verbose information about a single command.
 * <p>
 * This is displayed when the help query is precise enough to point to a single command.
 *
 * @param <C> the command sender type
 */
@ImmutableImpl
@Value.Immutable
@API(status = API.Status.STABLE)
public interface VerboseCommandResult<C> extends HelpQueryResult<C> {

    /**
     * Creates a new result.
     *
     * @param <C>   the command sender type
     * @param query the query that prompted the result
     * @param entry the entry
     * @return the result
     */
    static <C> @NonNull VerboseCommandResult<C> of(
            final @NonNull HelpQuery<C> query,
            final @NonNull CommandEntry<C> entry
    ) {
        return VerboseCommandResultImpl.of(query, entry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull HelpQuery<C> query();

    /**
     * Returns the command entry.
     *
     * @return the entry
     */
    @NonNull CommandEntry<C> entry();
}
