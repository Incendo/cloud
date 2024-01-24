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
package cloud.commandframework.suggestion;

import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.INTERNAL)
final class ChainedSuggestionProcessor<C> implements SuggestionProcessor<C> {

    private final List<SuggestionProcessor<C>> links;

    /**
     * Creates a chained processor from the provided processors. Chained processors will be
     * recursively flattened.
     *
     * @param links processors to chain
     */
    ChainedSuggestionProcessor(final List<SuggestionProcessor<C>> links) {
        final List<SuggestionProcessor<C>> list = new ArrayList<>();
        flattenChain(list, links);
        this.links = Collections.unmodifiableList(list);
    }

    private static <C> void flattenChain(
            final @NonNull List<SuggestionProcessor<C>> into,
            final @NonNull Collection<SuggestionProcessor<C>> links
    ) {
        for (final SuggestionProcessor<C> link : links) {
            if (link instanceof ChainedSuggestionProcessor) {
                flattenChain(into, ((ChainedSuggestionProcessor<C>) link).links);
            } else {
                into.add(link);
            }
        }
    }

    @Override
    public @NonNull Stream<@NonNull Suggestion> process(
            final @NonNull CommandPreprocessingContext<C> context,
            final @NonNull Stream<@NonNull Suggestion> suggestions
    ) {
        Stream<Suggestion> currentLink = suggestions;
        for (final SuggestionProcessor<C> link : this.links) {
            currentLink = link.process(context, currentLink);
        }
        return currentLink;
    }
}
