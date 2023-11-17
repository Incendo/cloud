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
package cloud.commandframework;

import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.internal.CommandNode;
import cloud.commandframework.meta.CommandMeta;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = API.Status.STABLE)
public final class CommandHelpHandler<C> {

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
     * Get exact syntax hints for all commands
     *
     * @return Syntax hints for all registered commands, order in lexicographical order
     */
    public @NonNull List<@NonNull VerboseHelpEntry<C>> getAllCommands() {
        final List<VerboseHelpEntry<C>> syntaxHints = new ArrayList<>();
        for (final Command<C> command : this.commandManager.commands()) {
            /* Check command is not filtered */
            if (!this.commandPredicate.test(command)) {
                continue;
            }

            final List<CommandComponent<C>> components = command.components();
            final String description = command.getCommandMeta().getOrDefault(CommandMeta.DESCRIPTION, "");
            syntaxHints.add(new VerboseHelpEntry<>(
                    command,
                    this.commandManager.commandSyntaxFormatter()
                            .apply(components, null),
                    description
            ));
        }
        syntaxHints.sort(Comparator.comparing(VerboseHelpEntry::getSyntaxString));
        return syntaxHints;
    }

    /**
     * Get a list of the longest shared command chains of all commands.
     * If there are two commands "foo bar 1" and "foo bar 2", this would
     * then return "foo bar 1|2"
     *
     * @return Longest shared command chains
     */
    public @NonNull List<@NonNull String> getLongestSharedChains() {
        final List<String> chains = new ArrayList<>();
        this.commandManager.commandTree().rootNodes().forEach(node ->
                chains.add(Objects.requireNonNull(node.component())
                        .name() + this.commandManager
                        .commandSyntaxFormatter()
                        .apply(
                                Collections
                                        .emptyList(),
                                node
                        )));
        chains.sort(String::compareTo);
        return chains;
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
        final List<VerboseHelpEntry<C>> commands = this.getAllCommands();
        commands.removeIf(command -> recipient != null && !this.commandManager.hasPermission(
                recipient,
                command.getCommand().getCommandPermission()
        ));
        if (query.replace(" ", "").isEmpty()) {
            return new IndexHelpTopic<>(commands);
        }

        final String[] queryFragments = query.split(" ");
        final String rootFragment = queryFragments[0];

        /* Determine which command we are querying for */
        final List<Command<C>> availableCommands = new LinkedList<>();
        final Set<String> availableCommandLabels = new HashSet<>();

        boolean exactMatch = false;

        for (final VerboseHelpEntry<C> entry : commands) {
            final Command<C> command = entry.getCommand();

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
            return new IndexHelpTopic<>(Collections.emptyList());
        } else if (!exactMatch || availableCommandLabels.size() > 1) {
            final List<VerboseHelpEntry<C>> syntaxHints = new ArrayList<>();
            for (final Command<C> command : availableCommands) {
                final List<CommandComponent<C>> components = command.components();
                final String description = command.getCommandMeta().getOrDefault(CommandMeta.DESCRIPTION, "");
                syntaxHints.add(new VerboseHelpEntry<>(
                        command,
                        this.commandManager.commandSyntaxFormatter()
                                .apply(components, null),
                        description
                ));
            }
            syntaxHints.sort(Comparator.comparing(VerboseHelpEntry::getSyntaxString));
            syntaxHints.removeIf(command -> recipient != null
                    && !this.commandManager.hasPermission(recipient, command.getCommand().getCommandPermission()));
            return new IndexHelpTopic<>(syntaxHints);
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

            if (head.component() != null && head.argument().getOwningCommand() != null) {
                if (head.isLeaf() || index == queryFragments.length) {
                    if (recipient == null || this.commandManager.hasPermission(recipient, head.argument()
                            .getOwningCommand()
                            .getCommandPermission())) {
                        return new VerboseHelpTopic<>(head.argument().getOwningCommand());
                    }
                }
            }

            if (head.children().size() == 1) {
                head = head.children().get(0);
            } else {
                if (index < queryFragments.length) {
                    /* We might still be able to match an argument */
                    CommandNode<C> potentialVariable = null;
                    for (final CommandNode<C> child : head.children()) {
                        if (!(child.argument() instanceof StaticArgument)) {
                            if (child.argument() != null) {
                                potentialVariable = child;
                            }
                            continue;
                        }
                        for (final String childAlias : child.component().aliases()) {
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
                final List<String> childSuggestions = new LinkedList<>();
                for (final CommandNode<C> child : head.children()) {
                    /* Check filtered by predicate */
                    if (!this.isNodeVisible(child)) {
                        continue;
                    }

                    final List<CommandComponent<C>> traversedNodesSub = new LinkedList<>(traversedNodes);
                    if (recipient == null
                            || child.argument() == null
                            || child.argument().getOwningCommand() == null
                            || this.commandManager.hasPermission(
                            recipient,
                            child.argument().getOwningCommand().getCommandPermission()
                    )) {
                        traversedNodesSub.add(child.component());
                        childSuggestions.add(this.commandManager.commandSyntaxFormatter().apply(traversedNodesSub, child));
                    }
                }
                return new MultiHelpTopic<>(currentDescription, childSuggestions);
            }
        }

        return new IndexHelpTopic<>(Collections.emptyList());
    }

    /* Checks using the predicate whether a command node or one of its children is visible */
    private boolean isNodeVisible(
            final @NonNull CommandNode<C> node
    ) {
        /* Check node is itself a command that is visible */
        final CommandComponent<C> component = node.component();
        if (component != null) {
            final Command<C> owningCommand = component.argument().getOwningCommand();
            if (owningCommand != null && this.commandPredicate.test(owningCommand)) {
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

        @Override
        public String toString() {
            return "VerboseHelpEntry{"
                    + "command=" + this.command
                    + ", syntaxString='" + this.syntaxString + '\''
                    + ", description='" + this.description + '\''
                    + '}';
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
        public int hashCode() {
            return Objects.hash(this.command, this.syntaxString, this.description);
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
            return this.getEntries().isEmpty();
        }

        @Override
        public String toString() {
            return "IndexHelpTopic{"
                    + "entries=" + this.entries
                    + '}';
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
        public int hashCode() {
            return Objects.hash(this.entries);
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

        @Override
        public String toString() {
            return "VerboseHelpTopic{"
                    + "command=" + this.command
                    + ", description='" + this.description + '\''
                    + '}';
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
        public int hashCode() {
            return Objects.hash(this.command, this.description);
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

        @Override
        public String toString() {
            return "MultiHelpTopic{"
                    + "longestPath='" + this.longestPath + '\''
                    + ", childSuggestions=" + this.childSuggestions
                    + '}';
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
        public int hashCode() {
            return Objects.hash(this.longestPath, this.childSuggestions);
        }
    }
}
