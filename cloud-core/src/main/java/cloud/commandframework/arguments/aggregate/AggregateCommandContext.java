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
package cloud.commandframework.arguments.aggregate;

import cloud.commandframework.keys.CloudKey;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE, since = "2.0.0")
public interface AggregateCommandContext<C> {

    /**
     * Returns a new argument context instance that accepts values for the inner parsers of the given {@code parser}.
     *
     * @param <C>    the comment sender type
     * @param parser the parser that the context is used by
     * @return the command context
     */
    static <C> @NonNull AggregateCommandContext<C> argumentContext(
            final @NonNull AggregateCommandParser<C, ?> parser
    ) {
        return new AggregateCommandContextImpl<>(parser);
    }

    /**
     * Stores the given {@code value} identified by the given {@code key} in the context.
     *
     * @param key   the key
     * @param value the value
     */
    void store(@NonNull CloudKey<?> key, @NonNull Object value);

    /**
     * Returns the value identified by the given {@code key} that was identified by the aggregate parser.
     *
     * @param <T> the type of the value
     * @param key the key
     * @return the value
     * @throws NullPointerException if the value is not stored in the context
     */
    <T> @NonNull T get(@NonNull CloudKey<T> key);

    /**
     * Returns the value identified by the given {@code key} that was identified by the aggregate parser.
     *
     * @param <T> the type of the value
     * @param key the key
     * @return the value
     * @throws NullPointerException if the value is not stored in the context
     */
    @SuppressWarnings("TypeParameterUnusedInFormals")
    <T> @NonNull T get(@NonNull String key);

    /**
     * Returns whether the aggregate parser has stored a value identified by the given {@code key}.
     *
     * @param key the key
     * @return {@code true} if the value exists, or {@code false} if not
     */
    boolean contains(@NonNull CloudKey<?> key);

    /**
     * Returns whether the aggregate parser has stored a value identified by the given {@code key}.
     *
     * @param key the key
     * @return {@code true} if the value exists, or {@code false} if not
     */
    boolean contains(@NonNull String key);
}
