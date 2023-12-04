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
public class MultipleCommandResult<C> extends HelpQueryResult<C> {

    private final String longestPath;
    private final List<@NonNull String> childSuggestions;

    /**
     * Creates a new result.
     *
     * @param query            the query that prompted the result
     * @param longestPath      the longest shared result
     * @param childSuggestions the syntax hints for the children
     */
    public MultipleCommandResult(
            final @NonNull HelpQuery<C> query,
            final @NonNull String longestPath,
            final @NonNull List<@NonNull String> childSuggestions
    ) {
        super(query);
        this.longestPath = longestPath;
        this.childSuggestions = Collections.unmodifiableList(childSuggestions);
    }

    /**
     * Returns the longest path shared between the children.
     *
     * @return the longest shared path
     */
    public @NonNull String longestPath() {
        return this.longestPath;
    }

    /**
     * Returns syntax hints for the children.
     *
     * @return child suggestions
     */
    public @NonNull List<@NonNull String> childSuggestions() {
        return this.childSuggestions;
    }

    @Override
    public final boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        final MultipleCommandResult<?> that = (MultipleCommandResult<?>) object;
        return Objects.equals(this.longestPath, that.longestPath)
                && Objects.equals(this.childSuggestions, that.childSuggestions);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.longestPath, this.childSuggestions);
    }
}
