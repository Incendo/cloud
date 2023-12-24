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
package cloud.commandframework.internal;

import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.execution.CommandSuggestionProcessor;
import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*", since = "2.0.0")
public final class SuggestionContext<C> {

    private final List<Suggestion> suggestions = new ArrayList<>();
    private final CommandPreprocessingContext<C> preprocessingContext;
    private final CommandSuggestionProcessor<C> processor;
    private final CommandContext<C> commandContext;

    /**
     * Creates a new suggestion context
     *
     * @param processor      the suggestion processor
     * @param commandContext the command context
     * @param commandInput   the command input
     */
    public SuggestionContext(
            final CommandSuggestionProcessor<C> processor,
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        this.processor = processor;
        this.commandContext = commandContext;
        this.preprocessingContext = CommandPreprocessingContext.of(this.commandContext, commandInput);
    }

    /**
     * Returns an unmodifiable view of the suggestions
     *
     * @return list of suggestions
     */
    public @NonNull List<@NonNull Suggestion> suggestions() {
        return Collections.unmodifiableList(this.suggestions);
    }

    /**
     * Returns the command context
     *
     * @return command context
     */
    public @NonNull CommandContext<C> commandContext() {
        return this.commandContext;
    }

    /**
     * Adds all the given {@code suggestions} to the stored suggestions
     *
     * @param suggestions the suggestions to add
     */
    public void addSuggestions(final @NonNull Iterable<@NonNull Suggestion> suggestions) {
        suggestions.forEach(this::addSuggestion);
    }

    /**
     * Adds the given {@code suggestion} to the stored suggestions
     *
     * @param suggestion the suggestion to add
     */
    public void addSuggestion(final @NonNull Suggestion suggestion) {
        final Suggestion result = this.processor.process(this.preprocessingContext, suggestion);
        if (result == null) {
            return;
        }
        this.suggestions.add(result);
    }
}
