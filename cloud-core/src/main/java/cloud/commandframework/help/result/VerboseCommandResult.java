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

import cloud.commandframework.Command;
import cloud.commandframework.help.HelpQuery;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Verbose information about a single command.
 *
 * @param <C> the command sender type
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public class VerboseCommandResult<C> extends HelpQueryResult<C> implements CommandEntry<C> {

    private final CommandEntry<C> entry;

    /**
     * Creates a new result.
     *
     * @param query the query that prompted the result
     * @param entry the entry
     */
    public VerboseCommandResult(
            final @NonNull HelpQuery<C> query,
            final @NonNull CommandEntry<C> entry
    ) {
        super(query);
        this.entry = entry;
    }

    /**
     * Returns the command entry.
     *
     * @return the entry
     */
    public @NonNull CommandEntry<C> entry() {
        return this.entry;
    }

    @Override
    public final @NonNull Command<C> command() {
        return this.entry.command();
    }

    @Override
    public final @NonNull String syntax() {
        return this.entry.syntax();
    }

    @Override
    public final int compareTo(final @NonNull CommandEntry<C> other) {
        return this.entry.compareTo(other);
    }

    @Override
    public final boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        final VerboseCommandResult<?> that = (VerboseCommandResult<?>) object;
        return Objects.equals(this.entry, that.entry);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.entry);
    }
}
