//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import cloud.commandframework.internal.CommandInputTokenizer;
import cloud.commandframework.services.State;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Command suggestion engine that delegates to a {@link cloud.commandframework.CommandTree}
 *
 * @param <C> Command sender type
 */
public final class DelegatingCommandSuggestionEngine<C> implements CommandSuggestionEngine<C> {

    private static final List<String> SINGLE_EMPTY_SUGGESTION = Collections.unmodifiableList(Collections.singletonList(""));

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
    public @NonNull List<@NonNull String> getSuggestions(
            final @NonNull CommandContext<C> context,
            final @NonNull String input
    ) {
        final @NonNull LinkedList<@NonNull String> inputQueue = new CommandInputTokenizer(input).tokenize();
        /* Store a copy of the input queue in the context */
        context.store("__raw_input__", new LinkedList<>(inputQueue));
        final List<String> suggestions;
        if (this.commandManager.preprocessContext(context, inputQueue) == State.ACCEPTED) {
            suggestions = this.commandManager.getCommandSuggestionProcessor().apply(
                    new CommandPreprocessingContext<>(context, inputQueue),
                    this.commandTree.getSuggestions(
                            context,
                            inputQueue
                    )
            );
        } else {
            suggestions = Collections.emptyList();
        }
        if (this.commandManager.getSetting(CommandManager.ManagerSettings.FORCE_SUGGESTION) && suggestions.isEmpty()) {
            return SINGLE_EMPTY_SUGGESTION;
        }
        return suggestions;
    }

}
