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

import cloud.commandframework.CommandComponent;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.parser.ArgumentParseResult;
import cloud.commandframework.suggestion.Suggestion;
import cloud.commandframework.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.INTERNAL, since = "2.0.0")
final class AggregateSuggestionProvider<C> implements SuggestionProvider<C> {

    private final AggregateParser<C, ?> parser;

    AggregateSuggestionProvider(final @NonNull AggregateParser<C, ?> parser) {
        this.parser = parser;
    }

    @Override
    public @NonNull CompletableFuture<@NonNull Iterable<@NonNull Suggestion>> suggestionsFuture(
            final @NonNull CommandContext<C> context,
            final @NonNull CommandInput input
    ) {
        // We store a copy of the original input so that we can calculate the difference.
        final CommandInput originalInput = input.copy();

        return new ParsingInstance(context, input).parseComponent()
                .thenCompose(component -> component.suggestionProvider().suggestionsFuture(
                        context,
                        input.skipWhitespace(1, false).copy() // There might be whitespace left over from the parsing.

                )).thenApply(suggestions -> {
                    final String prefix = originalInput.difference(input, true /* includeTrailingWhitespace */);
                    final List<Suggestion> prefixedSuggestions = new ArrayList<>();
                    for (final Suggestion suggestion : suggestions) {
                        prefixedSuggestions.add(suggestion.withSuggestion(
                                String.format("%s%s", prefix, suggestion.suggestion())));
                    }
                    return prefixedSuggestions;
                });
    }

    private final class ParsingInstance {

        private final Iterator<CommandComponent<C>> components = AggregateSuggestionProvider.this.parser.components().iterator();
        private final CommandContext<C> context;
        private final CommandInput input;

        private CommandComponent<C> component;
        private int previousCursor;

        private ParsingInstance(final @NonNull CommandContext<C> context, final @NonNull CommandInput input) {
            this.context = context;
            this.input = input;
        }

        private @NonNull CompletableFuture<CommandComponent<C>> parseComponent() {
            if (!this.components.hasNext()) {
                return CompletableFuture.completedFuture(this.component);
            }

            // We store the current component and cursor position so that we can invoke the component for suggestions.
            // The cursor is stored so that we can restore the cursor once we're done with the parsing.
            this.component = this.components.next();
            this.previousCursor = this.input.cursor();

            return this.component.parser()
                    .parseFuture(this.context, this.input.skipWhitespace(1))
                    .thenCompose(this::handleResult);
        }

        private @NonNull CompletableFuture<CommandComponent<C>> handleResult(final @NonNull ArgumentParseResult<?> result) {
            // We store this before resetting the cursor.
            final boolean consumedAll = this.input.isEmpty();

            if (result.failure().isPresent() || !this.components.hasNext() || this.input.isEmpty()) {
                // We reset the cursor to whatever it was before parsing in the instance that this is the component we want
                // to provide suggestions for. We do this if it's the last component, or if the parsing failed. We also need
                // to reset the cursor if the input was empty, so that we can keep providing suggestions for the current
                // component.
                this.input.cursor(this.previousCursor);
            }

            // If the parsing failed then we know that we want to provide suggestions for this component.
            if (result.failure().isPresent()) {
                return CompletableFuture.completedFuture(this.component);
            }

            // We store the value in the context so that it can be accessed from the suggestion providers.
            result.parsedValue().ifPresent(value -> this.context.store(this.component.name(), value));

            // This means that there's no more input to parse, so we stop.
            if (consumedAll) {
                return CompletableFuture.completedFuture(this.component);
            }

            // If the parsing succeeded then we'll attempt to parse the subsequent component as well.
            return this.parseComponent();
        }
    }
}
