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
package org.incendo.cloud.suggestion;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.CommandTree;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandContextFactory;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.services.State;
import org.incendo.cloud.setting.ManagerSetting;

/**
 * Command suggestion engine that delegates to a {@link org.incendo.cloud.CommandTree}
 *
 * @param <C> command sender type
 * @param <S> suggestion type
 */
@API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class DelegatingSuggestionFactory<C, S extends Suggestion> implements SuggestionFactory<C, S> {

    private final List<S> singleEmptySuggestion;
    private final CommandManager<C> commandManager;
    private final CommandTree<C> commandTree;
    private final CommandContextFactory<C> contextFactory;
    private final ExecutionCoordinator<C> executionCoordinator;
    private final SuggestionMapper<S> mapper;

    /**
     * Creates a new {@link DelegatingSuggestionFactory}.
     *
     * @param commandManager       the command manager
     * @param commandTree          the command tree
     * @param contextFactory       the context factory
     * @param executionCoordinator the execution coordinator
     * @param mapper               the suggestion mapper
     */
    public DelegatingSuggestionFactory(
            final @NonNull CommandManager<C> commandManager,
            final @NonNull CommandTree<C> commandTree,
            final @NonNull CommandContextFactory<C> contextFactory,
            final @NonNull ExecutionCoordinator<C> executionCoordinator,
            final @NonNull SuggestionMapper<S> mapper
    ) {
        this.commandManager = commandManager;
        this.commandTree = commandTree;
        this.contextFactory = contextFactory;
        this.executionCoordinator = executionCoordinator;
        this.mapper = mapper;
        this.singleEmptySuggestion = Collections.singletonList(mapper.map(Suggestion.suggestion("")));
    }

    @Override
    public @NonNull CompletableFuture<@NonNull Suggestions<C, S>> suggest(
            final @NonNull CommandContext<C> context,
            final @NonNull String input
    ) {
        return this.suggestFromTree(context, input);
    }

    @Override
    public @NonNull CompletableFuture<@NonNull Suggestions<C, S>> suggest(
            final @NonNull C sender,
            final @NonNull String input
    ) {
        return this.suggest(this.contextFactory.create(true /* suggestions */, sender), input);
    }

    @Override
    public <S2 extends Suggestion> @NonNull SuggestionFactory<C, S2> mapped(final @NonNull SuggestionMapper<S2> mapper) {
        return new DelegatingSuggestionFactory<>(
                this.commandManager,
                this.commandTree,
                this.contextFactory,
                this.executionCoordinator,
                this.mapper.then(mapper)
        );
    }

    private @NonNull CompletableFuture<@NonNull Suggestions<C, S>> suggestFromTree(
            final @NonNull CommandContext<C> context,
            final @NonNull String input
    ) {
        final @NonNull CommandInput commandInput = CommandInput.of(input);
        /* Store a copy of the input queue in the context */
        context.store("__raw_input__", commandInput.copy());

        if (this.commandManager.preprocessContext(context, commandInput) != State.ACCEPTED) {
            if (this.commandManager.settings().get(ManagerSetting.FORCE_SUGGESTION)) {
                return CompletableFuture.completedFuture(Suggestions.create(context, this.singleEmptySuggestion, commandInput));
            }
            return CompletableFuture.completedFuture(Suggestions.create(context, Collections.emptyList(), commandInput));
        }

        return this.executionCoordinator.coordinateSuggestions(this.commandTree, context, commandInput, this.mapper)
                .thenApply(suggestions -> {
                    if (this.commandManager.settings().get(ManagerSetting.FORCE_SUGGESTION) && suggestions.list().isEmpty()) {
                        return Suggestions.create(suggestions.commandContext(), this.singleEmptySuggestion, commandInput);
                    }
                    return suggestions;
                });
    }
}
