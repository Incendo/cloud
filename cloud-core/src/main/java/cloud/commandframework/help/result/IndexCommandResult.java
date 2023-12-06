//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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
package cloud.commandframework.help.result;

import cloud.commandframework.help.HelpQuery;
import cloud.commandframework.internal.ImmutableImpl;
import java.util.Iterator;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;

/**
 * A list of commands.
 * <p>
 * This is the result of a query that doesn't point to a specific command or chain of commands.
 * The resulting {@link #entries()} are likely to consist of all commands available to the sender,
 * but may also be empty in the case that no results were found.
 *
 * @param <C> the command sender type
 * @since 2.0.0
 */
@ImmutableImpl
@Value.Immutable
@API(status = API.Status.STABLE, since = "2.0.0")
public interface IndexCommandResult<C> extends HelpQueryResult<C>, Iterable<CommandEntry<C>> {

    /**
     * Creates a new result.
     *
     * @param <C>     the command sender type
     * @param query   the query that prompted the result
     * @param entries the entries that were found for the query
     * @return the result
     */
    static <C> @NonNull IndexCommandResult<C> of(
            final @NonNull HelpQuery<C> query,
            final @NonNull List<@NonNull CommandEntry<C>> entries
    ) {
        return IndexCommandResultImpl.of(query, entries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull HelpQuery<C> query();

    /**
     * Returns an unmodifiable view of the entries.
     *
     * @return the entries
     */
    @NonNull List<@NonNull CommandEntry<C>> entries();

    /**
     * Returns whether the result is empty.
     *
     * @return {@code true} if there are no entries, else {@code false}
     */
    @Value.Parameter(false)
    default boolean isEmpty() {
        return this.entries().isEmpty();
    }

    /**
     * Returns an iterator that iterates over the {@link #entries()}.
     *
     * @return the iterator
     */
    @Value.Parameter(false)
    @Override
    default @NonNull Iterator<@NonNull CommandEntry<C>> iterator() {
        return this.entries().iterator();
    }
}
