//
// MIT License
//
// Copyright (c) 2023 Alexander Söderberg & Contributors
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
package cloud.commandframework;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.meta.CommandMeta;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = API.Status.STABLE)
public class CommandHelpHandler<C> {

    private final CommandManager<C> commandManager;
    private final Predicate<Command<C>> commandPredicate;

    CommandHelpHandler(
            final @NonNull CommandManager<C> commandManager,
            final @NonNull Predicate<Command<C>> commandPredicate
    ) {
        this.commandManager = commandManager;
        this.commandPredicate = commandPredicate;
    }

    /**
     * Query for help
     *
     * @param query Query string
     * @return Help topic, will return an empty {@link IndexHelpTopic} if no results were found
     */
    public @NonNull HelpTopic<C> queryHelp(final @NonNull String query) {
        return this.queryHelp(null, query);
    }

    /**
     * Query a root index help topic. This is the topic returned when querying {@link #queryHelp(Object, String)} with
     * an empty string, or when there are no results.
     *
     * @param recipient The recipient of this help query to check permissions against (if Non-Null)
     * @return index help topic
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    public @NonNull IndexHelpTopic<C> queryRootIndex(final @Nullable C recipient) {
        return (IndexHelpTopic<C>) this.queryHelp(recipient, "");
    }

    /**
     * Query for help
     *
     * @param recipient The recipient of this help query to check permissions against (if Non-Null)
     * @param query     Query string
     * @return Help topic, will return an empty {@link IndexHelpTopic} if no results were found
     */
    public @NonNull HelpTopic<C> queryHelp(
            final @Nullable C recipient,
            final @NonNull String query
    ) {
        final List<VerboseHelpEntry<C>> commands = this.getAllCommands().stream()
                .filter(command -> {
                    if (recipient == null) {
                        return true;
                    } else {
                        return !this.commandManager.hasPermission(recipient, command.getCommand().getCommandPermission());
                    }
                }).collect(Collectors.toList());

        if (query.trim().isEmpty()) { // query is empty, return all commands
            return new IndexHelpTopic<>(commands);
        } else {
            return this.queryHelpForCommands(recipient, query, commands);
        }
    }

    /**
     * Query for help against a list of commands.
     * Note: does not check for empty query.
     * <p>
     * Invoked as part of {@link #queryHelp(Object, String)}
     *
     * @param recipient The recipient of this help query to check permissions against (if Non-Null)
     * @param query     Query string
     * @param commands  List of commands to query against
     * @return The help topic, only including help info for commands in the list provided.
     */
    protected HelpTopic<C> queryHelpForCommands(
            final @Nullable C recipient,
            final @NonNull String query,
            final @NonNull List<VerboseHelpEntry<C>> commands
    ) {
        final String[] queryFragments = query.split(" ");
        final String rootFragment = queryFragments[0];

        /* Determine which command we are querying for */
        final List<VerboseHelpEntry<C>> availableCommands = new LinkedList<>();
        final Set<String> availableCommandLabels = new HashSet<>();

        boolean inexactMatch = true;

        for (final VerboseHelpEntry<C> entry : commands) {
            @SuppressWarnings("unchecked")
            final StaticArgument<C> staticArgument = (StaticArgument<C>) entry
                    .getCommand()
                    .getArguments()
                    .get(0);

            for (final String alias : staticArgument.getAliases()) {
                final String aliasLower = alias.toLowerCase(Locale.ENGLISH);
                final String rootLower = rootFragment.toLowerCase(Locale.ENGLISH);

                if (alias.equalsIgnoreCase(rootFragment)) {
                    inexactMatch = false;
                    availableCommands.add(entry);
                    availableCommandLabels.add(staticArgument.getName());
                    break;
                } else if (aliasLower.startsWith(rootLower)) {
                    availableCommands.add(entry);
                    availableCommandLabels.add(staticArgument.getName());
                    break;
                }
            }

            if (rootFragment.equalsIgnoreCase(staticArgument.getName())) {
                availableCommandLabels.clear();
                availableCommands.clear();
                availableCommandLabels.add(staticArgument.getName());
                availableCommands.add(entry);
                break;
            }
        }

        if (availableCommands.isEmpty()) { // No command found, return all possible commands
            return new IndexHelpTopic<>(Collections.emptyList());
        } else if (inexactMatch || availableCommandLabels.size() > 1) { // Found >1 command, return info for them
            final List<VerboseHelpEntry<C>> syntaxHints = availableCommands.stream()
                    .sorted(Comparator.comparing(VerboseHelpEntry::getSyntaxString))
                    .collect(Collectors.toList());
            return new IndexHelpTopic<>(syntaxHints);
        } else { // Traverse command to find the most specific help topic
            return this.querySpecificHelp(recipient, queryFragments, availableCommandLabels);
        }
    }

    /**
     * Query for help against a list of commands.
     * Note: does not check for empty query.
     * <p>
     * Invoked as part of {@link #queryHelp(Object, String)}
     *
     * @param recipient              The recipient of this help query to check permissions against (if Non-Null)
     * @param queryFragments         Query fragments string array
     * @param availableCommandLabels List of command labels to query against
     * @return The help topic, only including help info for commands in the list provided.
     */
    protected HelpTopic<C> querySpecificHelp(
            final @Nullable C recipient,
            final @NonNull String[] queryFragments,
            final Set<String> availableCommandLabels
    ) {
        // The complexity of this code is way too high. What can we do to make it easier to understand?

        final CommandTree.Node<CommandArgument<C, ?>> node = this.commandManager.commandTree()
                .getNamedNode(availableCommandLabels.iterator().next());

        final List<CommandArgument<C, ?>> traversedNodes = new LinkedList<>();
        CommandTree.Node<CommandArgument<C, ?>> head = node;
        int index = 0;

        outer:
        while (head != null && this.isNodeVisible(head)) {
            ++index;
            traversedNodes.add(head.getValue());

            if (head.getValue() != null && head.getValue().getOwningCommand() != null) {
                if (head.isLeaf() || index == queryFragments.length) {
                    if (recipient == null || this.commandManager.hasPermission(recipient, head.getValue()
                            .getOwningCommand()
                            .getCommandPermission())) {
                        return new VerboseHelpTopic<>(head.getValue().getOwningCommand());
                    }
                }
            }

            if (head.getChildren().size() == 1) {
                head = head.getChildren().get(0);
            } else {
                if (index < queryFragments.length) {
                    /* We might still be able to match an argument */
                    CommandTree.Node<CommandArgument<C, ?>> potentialVariable = null;
                    for (final CommandTree.Node<CommandArgument<C, ?>> child : head.getChildren()) {
                        if (!(child.getValue() instanceof StaticArgument)) {
                            if (child.getValue() != null) {
                                potentialVariable = child;
                            }
                            continue;
                        }
                        @SuppressWarnings("unchecked")
                        final StaticArgument<C> childArgument = (StaticArgument<C>) child
                                .getValue();
                        for (final String childAlias : childArgument.getAliases()) {
                            if (childAlias.equalsIgnoreCase(queryFragments[index])) {
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
                final String currentDescription = this.commandManager.commandSyntaxFormatter().apply(traversedNodes, null);
                /* Attempt to parse the longest possible description for the children */
                final List<String> childSuggestions = head.getChildren().stream()
                        .filter(this::isNodeVisible)
                        .filter(child -> {
                                    if (recipient == null || child.getValue() == null || child.getValue().getOwningCommand() == null) {
                                        return true;
                                    } else {
                                        return this.commandManager.hasPermission(
                                                recipient,
                                                child.getValue().getOwningCommand().getCommandPermission()
                                        );
                                    }
                                }
                        ).map((child) -> {
                            final List<CommandArgument<C, ?>> traversedNodesSub = new LinkedList<>(traversedNodes);
                            traversedNodesSub.add(child.getValue());
                            return this.commandManager.commandSyntaxFormatter().apply(traversedNodesSub, child);
                        }).collect(Collectors.toList());
                return new MultiHelpTopic<>(currentDescription, childSuggestions);
            }
        }

        return new IndexHelpTopic<>(Collections.emptyList());
    }

    /**
     * Get the command manager
     *
     * @return The command manager
     */
    public CommandManager<C> getCommandManager() {
        return this.commandManager;
    }

    /**
     * Get exact syntax hints for all commands
     *
     * @return Syntax hints for all registered commands, order in lexicographical order
     */
    public @NonNull List<@NonNull VerboseHelpEntry<C>> getAllCommands() {
        return this.commandManager.commands().stream()
                .filter(this.commandPredicate)
                .map((command) -> {
                    final List<CommandArgument<C, ?>> arguments = command.getArguments();
                    final String description = command.getCommandMeta().getOrDefault(CommandMeta.DESCRIPTION, "");
                    return new VerboseHelpEntry<>(
                            command,
                            this.commandManager.commandSyntaxFormatter()
                                    .apply(arguments, null),
                            description
                    );
                }).sorted(Comparator.comparing(VerboseHelpEntry::getSyntaxString))
                .collect(Collectors.toList());
    }

    /**
     * Get a list of the longest shared command chains of all commands.
     * If there are two commands "foo bar 1" and "foo bar 2", this would
     * then return "foo bar 1|2"
     *
     * @return Longest shared command chains
     */
    public @NonNull List<@NonNull String> getLongestSharedChains() {
        return this.commandManager.commandTree().getRootNodes().stream()
                .filter((node) -> node.getValue() != null)
                .map(node -> node.getValue().getName() + this.commandManager
                        .commandSyntaxFormatter()
                        .apply(Collections.emptyList(), node))
                .sorted(String::compareTo)
                .collect(Collectors.toList());
    }

    /* Checks using the predicate whether a command node or one of its children is visible */
    private boolean isNodeVisible(final CommandTree.@NonNull Node<CommandArgument<C, ?>> node) {
        /* Check node is itself a command that is visible */
        final CommandArgument<C, ?> argument = node.getValue();
        if (argument != null) {
            final Command<C> owningCommand = argument.getOwningCommand();
            if (owningCommand != null && this.commandPredicate.test(owningCommand)) {
                return true;
            }
        }

        /* Query the children recursively */
        for (final CommandTree.Node<CommandArgument<C, ?>> childNode : node.getChildren()) {
            if (this.isNodeVisible(childNode)) {
                return true;
            }
        }

        return false;
    }


    /**
     * Something that can be returned as the result of a help query
     * <p>
     * Implementations:
     * <ul>
     *     <li>{@link IndexHelpTopic}</li>
     *     <li>{@link VerboseHelpTopic}</li>
     *     <li>{@link MultiHelpTopic}</li>
     * </ul>
     *
     * @param <C> Command sender type
     */
    @API(status = API.Status.STABLE)
    public interface HelpTopic<C> {

    }


    @API(status = API.Status.STABLE)
    public static final class VerboseHelpEntry<C> {

        private final Command<C> command;
        private final String syntaxString;
        private final String description;

        private VerboseHelpEntry(
                final @NonNull Command<C> command,
                final @NonNull String syntaxString,
                final @NonNull String description
        ) {
            this.command = command;
            this.syntaxString = syntaxString;
            this.description = description;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.command, this.syntaxString, this.description);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final VerboseHelpEntry<?> that = (VerboseHelpEntry<?>) o;
            return this.command.equals(that.command)
                    && this.syntaxString.equals(that.syntaxString)
                    && this.description.equals(that.description);
        }

        @Override
        public String toString() {
            return "VerboseHelpEntry{"
                    + "command=" + this.command
                    + ", syntaxString='" + this.syntaxString + '\''
                    + ", description='" + this.description + '\''
                    + '}';
        }

        /**
         * Get the command
         *
         * @return Command
         */
        public @NonNull Command<C> getCommand() {
            return this.command;
        }

        /**
         * Get the syntax string
         *
         * @return Syntax string
         */
        public @NonNull String getSyntaxString() {
            return this.syntaxString;
        }

        /**
         * Get the command description
         *
         * @return Command description
         */
        public @NonNull String getDescription() {
            return this.description;
        }
    }


    /**
     * Index of available commands
     *
     * @param <C> Command sender type
     */
    @API(status = API.Status.STABLE)
    public static final class IndexHelpTopic<C> implements HelpTopic<C> {

        private final List<VerboseHelpEntry<C>> entries;

        private IndexHelpTopic(final @NonNull List<@NonNull VerboseHelpEntry<C>> entries) {
            this.entries = entries;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.entries);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final IndexHelpTopic<?> that = (IndexHelpTopic<?>) o;
            return this.entries.equals(that.entries);
        }

        @Override
        public String toString() {
            return "IndexHelpTopic{"
                    + "entries=" + this.entries
                    + '}';
        }

        /**
         * Get help entries
         *
         * @return Entries
         */
        public @NonNull List<@NonNull VerboseHelpEntry<C>> getEntries() {
            return this.entries;
        }

        /**
         * Check if the help topic is entry
         *
         * @return {@code true} if the topic is entry, else {@code false}
         */
        public boolean isEmpty() {
            return this.entries.isEmpty();
        }
    }


    /**
     * Verbose information about a specific {@link Command}
     *
     * @param <C> Command sender type
     */
    @API(status = API.Status.STABLE)
    public static final class VerboseHelpTopic<C> implements HelpTopic<C> {

        private final Command<C> command;
        private final String description;

        private VerboseHelpTopic(final @NonNull Command<C> command) {
            this.command = command;
            final String shortDescription = command.getCommandMeta().getOrDefault(CommandMeta.DESCRIPTION, "No description");
            this.description = command.getCommandMeta().getOrDefault(CommandMeta.LONG_DESCRIPTION, shortDescription);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.command, this.description);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final VerboseHelpTopic<?> that = (VerboseHelpTopic<?>) o;
            return this.command.equals(that.command) && this.description.equals(that.description);
        }

        @Override
        public String toString() {
            return "VerboseHelpTopic{"
                    + "command=" + this.command
                    + ", description='" + this.description + '\''
                    + '}';
        }

        /**
         * Get the command
         *
         * @return Command
         */
        public @NonNull Command<C> getCommand() {
            return this.command;
        }

        /**
         * Get the command description
         *
         * @return Command description
         */
        public @NonNull String getDescription() {
            return this.description;
        }
    }


    /**
     * Help topic with multiple semi-verbose command descriptions
     *
     * @param <C> Command sender type
     */
    @API(status = API.Status.STABLE)
    public static final class MultiHelpTopic<C> implements HelpTopic<C> {

        private final String longestPath;
        private final List<String> childSuggestions;

        private MultiHelpTopic(
                final @NonNull String longestPath,
                final @NonNull List<@NonNull String> childSuggestions
        ) {
            this.longestPath = longestPath;
            this.childSuggestions = childSuggestions;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.longestPath, this.childSuggestions);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final MultiHelpTopic<?> that = (MultiHelpTopic<?>) o;
            return this.longestPath.equals(that.longestPath) && this.childSuggestions.equals(that.childSuggestions);
        }

        @Override
        public String toString() {
            return "MultiHelpTopic{"
                    + "longestPath='" + this.longestPath + '\''
                    + ", childSuggestions=" + this.childSuggestions
                    + '}';
        }

        /**
         * Get the longest shared path
         *
         * @return Longest path
         */
        public @NonNull String getLongestPath() {
            return this.longestPath;
        }

        /**
         * Get syntax hints for the node's children
         *
         * @return Child suggestions
         */
        public @NonNull List<@NonNull String> getChildSuggestions() {
            return this.childSuggestions;
        }
    }
}
