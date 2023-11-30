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
package cloud.commandframework.brigadier.suggestion;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.suggestion.SuggestionFactory;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.types.tuples.Pair;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Produces Brigadier suggestions by invoking the Cloud suggestion provider.
 *
 * @param <C> command sender type
 * @param <S> Brigadier sender type
 * @since 2.0.0
 */
@API(status = API.Status.INTERNAL, since = "2.0.0")
public final class BrigadierSuggestionFactory<C, S> {

    private final CloudBrigadierManager<C, S> cloudBrigadierManager;
    private final CommandManager<C> commandManager;
    private final Supplier<CommandContext<C>> dummyContextProvider;
    private final SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory;

    /**
     * Creates a new suggestion factory.
     *
     * @param cloudBrigadierManager the brigadier manager
     * @param commandManager        the command manager
     * @param dummyContextProvider  creates the context provided when retrieving suggestions
     * @param suggestionFactory     the suggestion factory-producing tooltip suggestions
     */
    public BrigadierSuggestionFactory(
            final @NonNull CloudBrigadierManager<C, S> cloudBrigadierManager,
            final @NonNull CommandManager<C> commandManager,
            final @NonNull Supplier<CommandContext<C>> dummyContextProvider,
            final @NonNull SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory
    ) {
        this.cloudBrigadierManager = cloudBrigadierManager;
        this.commandManager = commandManager;
        this.dummyContextProvider = dummyContextProvider;
        this.suggestionFactory = suggestionFactory;
    }

    /**
     * Builds suggestions for the given component.
     *
     * @param senderContext the brigadier context
     * @param parentNode    the parent command node
     * @param component     the command component to generate suggestions for
     * @param builder       the suggestion builder to generate suggestions with
     * @return future that completes with the suggestions
     */
    public @NonNull CompletableFuture<@NonNull Suggestions> buildSuggestions(
            final com.mojang.brigadier.context.@Nullable CommandContext<S> senderContext,
            final cloud.commandframework.internal.@Nullable CommandNode<C> parentNode,
            final @NonNull CommandComponent<C> component,
            final @NonNull SuggestionsBuilder builder
    ) {
        final CommandContext<C> commandContext;
        String command = builder.getInput();
        if (this.cloudBrigadierManager.brigadierSenderMapper() == null || senderContext == null) {
            commandContext = this.dummyContextProvider.get();
            if (command.startsWith("/") /* Minecraft specific */) {
                command = command.substring(1);
            }
        } else {
            final C cloudSender = this.cloudBrigadierManager.brigadierSenderMapper().apply(senderContext.getSource());
            commandContext = new CommandContext<>(
                    true,
                    cloudSender,
                    this.commandManager
            );
            command = command.substring(getNodes(senderContext.getLastChild()).get(0).getSecond().getStart());
        }

        /* Remove namespace */
        final String leading = command.split(" ")[0];
        if (leading.contains(":")) {
            command = command.substring(leading.split(":")[0].length() + 1);
        }

        return this.suggestionFactory.suggest(commandContext.getSender(), command).thenCompose(suggestionsUnfiltered -> {
            /* Filter suggestions that are literal arguments to avoid duplicates, except for root arguments */
            final List<TooltipSuggestion> suggestions = new ArrayList<>(suggestionsUnfiltered);
            if (parentNode != null) {
                final Set<String> siblingLiterals = parentNode.children().stream()
                        .map(cloud.commandframework.internal.CommandNode::component)
                        .filter(Objects::nonNull)
                        .flatMap(commandComponent -> commandComponent.aliases().stream())
                        .collect(Collectors.toSet());

                suggestions.removeIf(suggestion -> siblingLiterals.contains(suggestion.suggestion()));
            }

            SuggestionsBuilder suggestionsBuilder = builder;

            final int lastIndexOfSpaceInRemainingString = builder.getRemaining().lastIndexOf(' ');
            if (lastIndexOfSpaceInRemainingString != -1) {
                suggestionsBuilder = builder.createOffset(builder.getStart() + lastIndexOfSpaceInRemainingString + 1);
            }

            for (final TooltipSuggestion suggestion : suggestions) {
                suggestionsBuilder = suggestionsBuilder.suggest(suggestion.suggestion(), suggestion.tooltip());
            }

            return suggestionsBuilder.buildFuture();
        });
    }

    /**
     * Return type changed at some point, but information is essentially the same. This code works for both versions of the
     * method.
     *
     * @param commandContext command context
     * @param <S>            source type
     * @return parsed nodes
     */
    @SuppressWarnings("unchecked")
    private static <S> List<Pair<CommandNode<S>, StringRange>> getNodes(
            final com.mojang.brigadier.context.CommandContext<S> commandContext
    ) {
        try {
            final Method getNodesMethod = commandContext.getClass().getDeclaredMethod("getNodes");
            final Object nodes = getNodesMethod.invoke(commandContext);
            if (nodes instanceof List) {
                return ParsedCommandNodeHandler.toPairList((List) nodes);
            } else if (nodes instanceof Map) {
                return ((Map<CommandNode<S>, StringRange>) nodes).entrySet().stream()
                        .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
            } else {
                throw new IllegalStateException();
            }
        } catch (final ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }


    // Inner class to prevent attempting to load ParsedCommandNode when it doesn't exist
    @SuppressWarnings("unchecked")
    private static final class ParsedCommandNodeHandler {
        private ParsedCommandNodeHandler() {
        }

        private static <S> List<Pair<CommandNode<S>, StringRange>> toPairList(final List<?> nodes) {
            return ((List<ParsedCommandNode<S>>) nodes).stream()
                    .map(n -> Pair.of(n.getNode(), n.getRange()))
                    .collect(Collectors.toList());
        }
    }
}
