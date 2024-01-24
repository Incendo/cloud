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
package org.incendo.cloud.help;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.help.result.CommandEntry;
import org.incendo.cloud.help.result.HelpQueryResult;
import org.incendo.cloud.help.result.IndexCommandResult;
import org.incendo.cloud.help.result.MultipleCommandResult;
import org.incendo.cloud.help.result.VerboseCommandResult;
import org.incendo.cloud.internal.CommandInputTokenizer;
import org.incendo.cloud.internal.CommandNode;

@API(status = API.Status.STABLE)
public class StandardHelpHandler<C> implements HelpHandler<C> {

    private final CommandManager<C> commandManager;
    private final CommandPredicate<C> commandFilter;

    /**
     * Creates a new help handler.
     *
     * @param commandManager the command manager to get commands from
     * @param commandPredicate  filter that determines which commands are applicable
     */
    public StandardHelpHandler(
            final @NonNull CommandManager<C> commandManager,
            final @NonNull CommandPredicate<C> commandPredicate
    ) {
        this.commandManager = commandManager;
        this.commandFilter = commandPredicate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull HelpQueryResult<C> query(final @NonNull HelpQuery<C> query) {
        final List<CommandEntry<C>> commands = this.commands(query.sender());

        // If the query is empty, then we return all commands that they have permission to see.
        if (query.query().replace(" ", "").isEmpty()) {
            return IndexCommandResult.of(query, commands);
        }

        final List<String> queryFragments = new CommandInputTokenizer(query.query()).tokenize();
        final String rootFragment = queryFragments.get(0);

        /* Determine which command we are querying for */
        final List<Command<C>> availableCommands = new LinkedList<>();
        final Set<String> availableCommandLabels = new HashSet<>();

        boolean exactMatch = false;

        for (final CommandEntry<C> entry : commands) {
            final Command<C> command = entry.command();

            final CommandComponent<C> component = command.rootComponent();
            for (final String alias : component.aliases()) {
                if (alias.toLowerCase(Locale.ENGLISH).startsWith(rootFragment.toLowerCase(Locale.ENGLISH))) {
                    availableCommands.add(command);
                    availableCommandLabels.add(component.name());
                    break;
                }
            }

            for (final String alias : component.aliases()) {
                if (alias.equalsIgnoreCase(rootFragment)) {
                    exactMatch = true;
                    break;
                }
            }

            if (rootFragment.equalsIgnoreCase(component.name())) {
                availableCommandLabels.clear();
                availableCommands.clear();
                availableCommandLabels.add(component.name());
                availableCommands.add(command);
                break;
            }
        }

        /* No command found, return all possible commands */
        if (availableCommands.isEmpty()) {
            return IndexCommandResult.of(query, Collections.emptyList());
        } else if (!exactMatch || availableCommandLabels.size() > 1) {
            return IndexCommandResult.of(
                    query,
                    availableCommands.stream()
                            .map(command -> CommandEntry.of(command, this.commandManager.commandSyntaxFormatter()
                                    .apply(query.sender(), command.components(), null)))
                            .sorted()
                            .filter(entry -> this.commandManager.testPermission(query.sender(),
                                    entry.command().commandPermission()).allowed())
                            .collect(Collectors.toList())
            );
        }

        /* Traverse command to find the most specific help topic */
        final CommandNode<C> node = this.commandManager.commandTree()
                .getNamedNode(availableCommandLabels.iterator().next());

        final List<CommandComponent<C>> traversedNodes = new LinkedList<>();
        CommandNode<C> head = node;
        int index = 0;

        outer:
        while (head != null && this.isNodeVisible(head)) {
            ++index;
            traversedNodes.add(head.component());

            if (head.component() != null && head.command() != null) {
                if (head.isLeaf() || index == queryFragments.size()) {
                    if (this.commandManager.testPermission(query.sender(), head.command().commandPermission()).allowed()) {
                        return VerboseCommandResult.of(
                                query,
                                CommandEntry.of(
                                        head.command(),
                                        this.commandManager.commandSyntaxFormatter()
                                                .apply(query.sender(), head.command().components(), null)
                                )
                        );
                    }
                }
            }

            if (head.children().size() == 1) {
                head = head.children().get(0);
            } else {
                if (index < queryFragments.size()) {
                    /* We might still be able to match an argument */
                    CommandNode<C> potentialVariable = null;
                    for (final CommandNode<C> child : head.children()) {
                        if (child.component() == null || child.component().type() != CommandComponent.ComponentType.LITERAL) {
                            if (child.component() != null) {
                                potentialVariable = child;
                            }
                            continue;
                        }
                        for (final String childAlias : child.component().aliases()) {
                            if (childAlias.equalsIgnoreCase(queryFragments.get(index))) {
                                head = child;
                                continue outer;
                            }
                        }
                    }
                    if (potentialVariable != null) {
                        head = potentialVariable;
                        continue;
                    }
                }
                final String currentDescription = this.commandManager.commandSyntaxFormatter()
                        .apply(query.sender(), traversedNodes, null);
                /* Attempt to parse the longest possible description for the children */
                final List<String> childSuggestions = new LinkedList<>();
                for (final CommandNode<C> child : head.children()) {
                    /* Check filtered by predicate */
                    if (!this.isNodeVisible(child)) {
                        continue;
                    }

                    final List<CommandComponent<C>> traversedNodesSub = new LinkedList<>(traversedNodes);
                    if (child.component() == null || child.command() == null
                            || this.commandManager.testPermission(query.sender(),
                            child.command().commandPermission()).allowed()
                    ) {
                        traversedNodesSub.add(child.component());
                        childSuggestions.add(this.commandManager.commandSyntaxFormatter()
                                .apply(query.sender(), traversedNodesSub, child));
                    }
                }
                return MultipleCommandResult.of(query, currentDescription, childSuggestions);
            }
        }

        // No result :(
        return IndexCommandResult.of(query, Collections.emptyList());
    }

    /**
     * Returns entries for all commands that are applicable to this handler.
     *
     * @param sender the sender of the query
     * @return the help entries
     */
    protected @NonNull List<@NonNull CommandEntry<C>> commands(
            final @NonNull C sender
    ) {
        return this.commandManager.commands()
                .stream()
                .filter(this.commandFilter)
                .filter(command -> this.commandManager.testPermission(sender, command.commandPermission()).allowed())
                .map(command -> CommandEntry.of(
                        command,
                        this.commandManager.commandSyntaxFormatter()
                                .apply(sender, command.components(), null))
                ).sorted()
                .collect(Collectors.toList());
    }

    /**
     * Checks using the predicate whether a command node or one of its children is visible
     *
     * @param node the node
     * @return {@code true} if the node is visible, {@code false} if it isn't
     */
    protected boolean isNodeVisible(final @NonNull CommandNode<C> node) {
        /* Check node is itself a command that is visible */
        final CommandComponent<C> component = node.component();
        if (component != null) {
            final Command<C> owningCommand = node.command();
            if (owningCommand != null && this.commandFilter.test(owningCommand)) {
                return true;
            }
        }

        /* Query the children recursively */
        for (CommandNode<C> childNode : node.children()) {
            if (this.isNodeVisible(childNode)) {
                return true;
            }
        }

        return false;
    }
}
