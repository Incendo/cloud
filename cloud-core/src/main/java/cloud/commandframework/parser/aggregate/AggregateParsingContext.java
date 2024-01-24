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
package cloud.commandframework.parser.aggregate;

import cloud.commandframework.key.MutableCloudKeyContainer;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Context that stores the individual result of invoking the child parsers of a {@link AggregateParser}.
 * <p>
 * This is used in {@link AggregateResultMapper} to map to the output type using the intermediate results.
 *
 * @param <C> the command sender type
 */
@API(status = API.Status.STABLE)
public interface AggregateParsingContext<C> extends MutableCloudKeyContainer {

    /**
     * Returns a new argument context instance that accepts values for the inner parsers of the given {@code parser}.
     *
     * @param <C>    the comment sender type
     * @param parser the parser that the context is used by
     * @return the command context
     */
    static <C> @NonNull AggregateParsingContext<C> argumentContext(
            final @NonNull AggregateParser<C, ?> parser
    ) {
        return new AggregateParsingContextImpl<>(parser);
    }
}
