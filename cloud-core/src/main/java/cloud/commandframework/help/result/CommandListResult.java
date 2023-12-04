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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A list of commands.
 *
 * @param <C> the command sender type
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public class CommandListResult<C> extends HelpQueryResult<C> implements Iterable<CommandEntry<C>> {

    private final List<CommandEntry<C>> entries;

    /**
     * Creates a new result.
     *
     * @param query   the query that prompted the result
     * @param entries the entries that were found for the query
     */
    public CommandListResult(
            final @NonNull HelpQuery<C> query,
            final @NonNull List<@NonNull CommandEntry<C>> entries
    ) {
        super(query);
        this.entries = Collections.unmodifiableList(entries);
    }

    /**
     * Returns an unmodifiable view of the entries.
     *
     * @return the entries
     */
    public @NonNull List<@NonNull CommandEntry<C>> entries() {
        return this.entries;
    }

    /**
     * Returns whether the result is empty.
     *
     * @return {@code true} if there are no entries, else {@code false}
     */
    public boolean isEmpty() {
        return this.entries().isEmpty();
    }

    @Override
    public final @NonNull Iterator<@NonNull CommandEntry<C>> iterator() {
        return this.entries.iterator();
    }

    @Override
    public final boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        final CommandListResult<?> that = (CommandListResult<?>) object;
        return Objects.equals(this.entries, that.entries);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.entries);
    }
}
