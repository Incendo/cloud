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
package cloud.commandframework.arguments;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.Completion;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import cloud.commandframework.internal.CommandInputTokenizer;
import cloud.commandframework.services.State;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Command suggestion engine that delegates to a {@link cloud.commandframework.CommandTree}
 *
 * @param <C> Command sender type
 */
@API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
public final class DelegatingCommandSuggestionEngine<C> implements CommandSuggestionEngine<C> {

    private static final List<Completion> SINGLE_EMPTY_COMPLETION = Collections.singletonList(Completion.of(""));

    private final CommandManager<C> commandManager;
    private final CommandTree<C> commandTree;

    /**
     * Create a new delegating command suggestion engine
     *
     * @param commandManager Command manager
     * @param commandTree    Command tree
     */
    DelegatingCommandSuggestionEngine(
            final @NonNull CommandManager<C> commandManager,
            final @NonNull CommandTree<C> commandTree
    ) {
        this.commandManager = commandManager;
        this.commandTree = commandTree;
    }

    @Override
    public @NonNull List<@NonNull String> getSuggestions(@NonNull final CommandContext<C> context, @NonNull final String input) {
        return this.getCompletions(context, input).stream().map(Completion::completion).collect(Collectors.toList());
    }

    @Override
    public @NonNull List<@NonNull Completion> getCompletions(
            final @NonNull CommandContext<C> context,
            final @NonNull String input
    ) {
        final @NonNull LinkedList<@NonNull String> inputQueue = new CommandInputTokenizer(input).tokenize();
        /* Store a copy of the input queue in the context */
        context.store("__raw_input__", new LinkedList<>(inputQueue));
        final List<Completion> completions;
        if (this.commandManager.preprocessContext(context, inputQueue) == State.ACCEPTED) {
            completions = this.commandManager.commandCompletionProcessor().apply(
                    new CommandPreprocessingContext<>(context, inputQueue),
                    this.commandTree.getCompletions(
                            context,
                            inputQueue
                    )
            );
        } else {
            completions = Collections.emptyList();
        }
        if (this.commandManager.getSetting(CommandManager.ManagerSettings.FORCE_SUGGESTION) && completions.isEmpty()) {
            return SINGLE_EMPTY_COMPLETION;
        }
        return completions;
    }
}
