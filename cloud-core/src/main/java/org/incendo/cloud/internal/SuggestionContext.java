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
package org.incendo.cloud.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessingContext;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionMapper;
import org.incendo.cloud.suggestion.SuggestionProcessor;
import org.incendo.cloud.suggestion.Suggestions;

@API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class SuggestionContext<C, S extends Suggestion> {

    private final List<S> suggestions = new ArrayList<>();
    private final CommandPreprocessingContext<C> preprocessingContext;
    private final SuggestionMapper<S> mapper;
    private final SuggestionProcessor<C> processor;
    private final CommandContext<C> commandContext;

    /**
     * Creates a new suggestion context
     *
     * @param processor      the suggestion processor
     * @param commandContext the command context
     * @param commandInput   the command input
     * @param mapper         the suggestion mapper
     */
    public SuggestionContext(
            final @NonNull SuggestionProcessor<C> processor,
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput,
            final @NonNull SuggestionMapper<S> mapper
    ) {
        this.processor = processor;
        this.commandContext = commandContext;
        this.preprocessingContext = CommandPreprocessingContext.of(this.commandContext, commandInput);
        this.mapper = mapper;
    }

    /**
     * Create {@link Suggestions} from the current context.
     *
     * @return suggestions
     */
    @SuppressWarnings("unchecked")
    public @NonNull Suggestions<C, S> makeSuggestions() {
        final Stream<S> stream = this.suggestions.stream();
        final Stream<Suggestion> processedStream = this.processor.process(this.preprocessingContext, (Stream<Suggestion>) stream);
        final List<S> list;
        if (stream == processedStream) {
            // don't re-collect with a pass-through processor
            list = Collections.unmodifiableList(this.suggestions);
        } else {
            list = Collections.unmodifiableList(
                    processedStream
                            .peek(obj -> Objects.requireNonNull(obj, "suggestion"))
                            .map(this.mapper::map)
                            .collect(Collectors.toList())
            );
        }
        return Suggestions.create(this.commandContext, list, this.preprocessingContext.commandInput());
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
    public void addSuggestions(final @NonNull Iterable<? extends @NonNull Suggestion> suggestions) {
        suggestions.forEach(this::addSuggestion);
    }

    /**
     * Adds the given {@code suggestion} to the stored suggestions
     *
     * @param suggestion the suggestion to add
     */
    public void addSuggestion(final @NonNull Suggestion suggestion) {
        Objects.requireNonNull(suggestion, "suggestion");
        this.suggestions.add(this.mapper.map(suggestion));
    }
}
