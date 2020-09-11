//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
package com.intellectualsites.commands;

import com.intellectualsites.commands.components.CommandComponent;
import com.intellectualsites.commands.components.StaticComponent;
import com.intellectualsites.commands.components.parser.ComponentParseResult;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.exceptions.AmbiguousNodeException;
import com.intellectualsites.commands.exceptions.ComponentParseException;
import com.intellectualsites.commands.exceptions.InvalidSyntaxException;
import com.intellectualsites.commands.exceptions.NoCommandInLeafException;
import com.intellectualsites.commands.exceptions.NoPermissionException;
import com.intellectualsites.commands.exceptions.NoSuchCommandException;
import com.intellectualsites.commands.internal.CommandRegistrationHandler;
import com.intellectualsites.commands.meta.CommandMeta;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Tree containing all commands and command paths
 *
 * @param <C> Command sender type
 * @param <M> Command meta type
 */
public final class CommandTree<C extends CommandSender, M extends CommandMeta> {

    private final Object commandLock = new Object();

    private final Node<CommandComponent<C, ?>> internalTree = new Node<>(null);
    private final CommandManager<C, M> commandManager;
    private final CommandRegistrationHandler<M> commandRegistrationHandler;

    private CommandTree(@Nonnull final CommandManager<C, M> commandManager,
                        @Nonnull final CommandRegistrationHandler<M> commandRegistrationHandler) {
        this.commandManager = commandManager;
        this.commandRegistrationHandler = commandRegistrationHandler;
    }

    /**
     * Create a new command tree instance
     *
     * @param commandManager             Command manager
     * @param commandRegistrationHandler Command registration handler
     * @param <C>                        Command sender type
     * @param <M>                        Command meta type
     * @return New command tree
     */
    @Nonnull
    public static <C extends CommandSender, M extends CommandMeta> CommandTree<C, M> newTree(
            @Nonnull final CommandManager<C, M> commandManager,
            @Nonnull final CommandRegistrationHandler<M> commandRegistrationHandler) {
        return new CommandTree<>(commandManager, commandRegistrationHandler);
    }

    /**
     * Attempt to parse string input into a command
     *
     * @param commandContext Command context instance
     * @param args           Input
     * @return Parsed command, if one could be found
     * @throws NoSuchCommandException If there is no command matching the input
     * @throws NoPermissionException  If the sender lacks permission to execute the command
     * @throws InvalidSyntaxException If the command syntax is invalid
     */
    public Optional<Command<C, M>> parse(@Nonnull final CommandContext<C> commandContext,
                                         @Nonnull final Queue<String> args) throws
            NoSuchCommandException, NoPermissionException, InvalidSyntaxException {
        return parseCommand(commandContext, args, this.internalTree);
    }

    private Optional<Command<C, M>> parseCommand(@Nonnull final CommandContext<C> commandContext,
                                                 @Nonnull final Queue<String> commandQueue,
                                                 @Nonnull final Node<CommandComponent<C, ?>> root) {
        String permission = this.isPermitted(commandContext.getCommandSender(), root);
        if (permission != null) {
            throw new NoPermissionException(permission, commandContext.getCommandSender(), this.getChain(root)
                                                                                               .stream()
                                                                                               .map(Node::getValue)
                                                                                               .collect(Collectors.toList()));
        }

        final Optional<Command<C, M>> parsedChild = this.attemptParseUnambiguousChild(commandContext, root, commandQueue);
        // noinspection all
        if (parsedChild != null) {
            return parsedChild;
        }

        /* There are 0 or more static components as children. No variable child components are present */
        if (root.children.isEmpty()) {
            /* We are at the bottom. Check if there's a command attached, in which case we're done */
            if (root.getValue() != null && root.getValue().getOwningCommand() != null) {
                if (commandQueue.isEmpty()) {
                    return Optional.ofNullable(this.cast(root.getValue().getOwningCommand()));
                } else {
                    /* Too many arguments. We have a unique path, so we can send the entire context */
                    throw new InvalidSyntaxException(this.commandManager.getCommandSyntaxFormatter()
                                                                        .apply(root.getValue()
                                                                                   .getOwningCommand()
                                                                                   .getComponents()),
                                                     commandContext.getCommandSender(), this.getChain(root)
                                                                                            .stream()
                                                                                            .map(Node::getValue)
                                                                                            .collect(Collectors.toList()));
                }
            } else {
                /* Too many arguments. We have a unique path, so we can send the entire context */
                throw new InvalidSyntaxException(this.commandManager.getCommandSyntaxFormatter()
                                                                    .apply(Objects.requireNonNull(
                                                                            Objects.requireNonNull(root.getValue())
                                                                                   .getOwningCommand())
                                                                                  .getComponents()),
                                                 commandContext.getCommandSender(), this.getChain(root)
                                                                                        .stream()
                                                                                        .map(Node::getValue)
                                                                                        .collect(Collectors.toList()));
            }
        } else {
            final Iterator<Node<CommandComponent<C, ?>>> childIterator = root.getChildren().iterator();
            if (childIterator.hasNext()) {
                while (childIterator.hasNext()) {
                    final Node<CommandComponent<C, ?>> child = childIterator.next();
                    if (child.getValue() != null) {
                        final ComponentParseResult<?> result = child.getValue().getParser().parse(commandContext, commandQueue);
                        if (result.getParsedValue().isPresent()) {
                            return this.parseCommand(commandContext, commandQueue, child);
                        } /*else if (result.getFailure().isPresent() && root.children.size() == 1) {
                        }*/
                    }
                }
            }
            /* We could not find a match */
            throw new NoSuchCommandException(commandContext.getCommandSender(),
                                             getChain(root).stream().map(Node::getValue).collect(Collectors.toList()),
                                             stringOrEmpty(commandQueue.peek()));
        }
    }

    @Nullable
    private Optional<Command<C, M>> attemptParseUnambiguousChild(@Nonnull final CommandContext<C> commandContext,
                                                                 @Nonnull final Node<CommandComponent<C, ?>> root,
                                                                 @Nonnull final Queue<String> commandQueue) {
        String permission;
        final List<Node<CommandComponent<C, ?>>> children = root.getChildren();
        if (children.size() == 1 && !(children.get(0).getValue() instanceof StaticComponent)) {
            // The value has to be a variable
            final Node<CommandComponent<C, ?>> child = children.get(0);
            permission = this.isPermitted(commandContext.getCommandSender(), child);
            if (permission != null) {
                throw new NoPermissionException(permission, commandContext.getCommandSender(), this.getChain(child)
                                                                                                   .stream()
                                                                                                   .map(Node::getValue)
                                                                                                   .collect(Collectors.toList()));
            }
            if (child.getValue() != null) {
                if (commandQueue.isEmpty()) {
                    if (child.getValue().hasDefaultValue()) {
                        commandQueue.add(child.getValue().getDefaultValue());
                    } else if (!child.getValue().isRequired()) {
                        return Optional.ofNullable(this.cast(child.getValue().getOwningCommand()));
                    } else if (child.isLeaf()) {
                        /* Not enough arguments */
                        throw new InvalidSyntaxException(this.commandManager.getCommandSyntaxFormatter()
                                                                            .apply(Objects.requireNonNull(
                                                                                    child.getValue().getOwningCommand())
                                                                                          .getComponents()),
                                                         commandContext.getCommandSender(), this.getChain(root)
                                                                                                .stream()
                                                                                                .map(Node::getValue)
                                                                                                .collect(Collectors.toList()));
                    } else {
                        throw new NoSuchCommandException(commandContext.getCommandSender(),
                                                         this.getChain(root)
                                                             .stream()
                                                             .map(Node::getValue)
                                                             .collect(Collectors.toList()),
                                                         "");
                    }
                }
                final ComponentParseResult<?> result = child.getValue().getParser().parse(commandContext, commandQueue);
                if (result.getParsedValue().isPresent()) {
                    commandContext.store(child.getValue().getName(), result.getParsedValue().get());
                    if (child.isLeaf()) {
                        if (commandQueue.isEmpty()) {
                            return Optional.ofNullable(this.cast(child.getValue().getOwningCommand()));
                        } else {
                            /* Too many arguments. We have a unique path, so we can send the entire context */
                            throw new InvalidSyntaxException(this.commandManager.getCommandSyntaxFormatter()
                                                                                .apply(Objects.requireNonNull(child.getValue()
                                                                                                             .getOwningCommand())
                                                                                              .getComponents()),
                                                             commandContext.getCommandSender(), this.getChain(root)
                                                                                                    .stream()
                                                                                                    .map(Node::getValue)
                                                                                                    .collect(
                                                                                                            Collectors.toList()));
                        }
                    } else {
                        return this.parseCommand(commandContext, commandQueue, child);
                    }
                } else if (result.getFailure().isPresent()) {
                    throw new ComponentParseException(result.getFailure().get(), commandContext.getCommandSender(),
                                                      this.getChain(child)
                                                          .stream()
                                                          .map(Node::getValue)
                                                          .collect(Collectors.toList()));
                }
            }
        }
        // noinspection all
        return null;
    }

    /**
     * Get suggestions from the input queue
     *
     * @param context      Context instance
     * @param commandQueue Input queue
     * @return String suggestions. These should be filtered based on {@link String#startsWith(String)}
     */
    @Nonnull
    public List<String> getSuggestions(@Nonnull final CommandContext<C> context, @Nonnull final Queue<String> commandQueue) {
        return getSuggestions(context, commandQueue, this.internalTree);
    }

    @Nonnull
    private List<String> getSuggestions(@Nonnull final CommandContext<C> commandContext,
                                       @Nonnull final Queue<String> commandQueue,
                                       @Nonnull final Node<CommandComponent<C, ?>> root) {

        /* If the sender isn't allowed to access the root node, no suggestions are needed */
        if (this.isPermitted(commandContext.getCommandSender(), root) != null) {
            return Collections.emptyList();
        }
        final List<Node<CommandComponent<C, ?>>> children = root.getChildren();
        if (children.size() == 1 && !(children.get(0).getValue() instanceof StaticComponent)) {
            // The value has to be a variable
            final Node<CommandComponent<C, ?>> child = children.get(0);
            if (child.getValue() != null) {
                if (commandQueue.isEmpty()) {
                    if (child.isLeaf()) {
                        /* Child is leaf, and so no suggestions should be sent */
                        return Collections.emptyList();
                    } else {
                        /* Send all suggestions */
                        return child.getValue().getParser().suggestions(commandContext, "");
                    }
                }
                final ComponentParseResult<?> result = child.getValue().getParser().parse(commandContext, commandQueue);
                if (result.getParsedValue().isPresent()) {
                    if (child.isLeaf()) {
                        /* Child is leaf, and so no suggestions should be sent */
                        return Collections.emptyList();
                    }
                    commandContext.store(child.getValue().getName(), result.getParsedValue().get());
                    return this.getSuggestions(commandContext, commandQueue, child);
                } else if (result.getFailure().isPresent()) {
                    /* I need to return ze error */
                    return Collections.emptyList();
                }
            }
        }
        /* There are 0 or more static components as children. No variable child components are present */
        if (children.isEmpty()) {
            return Collections.emptyList();
        } else {
            final Iterator<Node<CommandComponent<C, ?>>> childIterator = root.getChildren().iterator();
            if (childIterator.hasNext()) {
                while (childIterator.hasNext()) {
                    final Node<CommandComponent<C, ?>> child = childIterator.next();
                    if (child.getValue() != null) {
                        final ComponentParseResult<?> result = child.getValue().getParser().parse(commandContext, commandQueue);
                        if (result.getParsedValue().isPresent()) {
                            return this.getSuggestions(commandContext, commandQueue, child);
                        } /* else if (result.getFailure().isPresent() && root.children.size() == 1) {
                        }*/
                    }
                }
            }
            final List<String> suggestions = new LinkedList<>();
            for (final Node<CommandComponent<C, ?>> component : root.getChildren()) {
                if (component.getValue() == null || this.isPermitted(commandContext.getCommandSender(), component) != null) {
                    continue;
                }
                suggestions.addAll(
                        component.getValue().getParser().suggestions(commandContext, stringOrEmpty(commandQueue.peek())));
            }
            return suggestions;
        }
    }

    @Nonnull
    private String stringOrEmpty(@Nullable final String string) {
        if (string == null) {
            return "";
        }
        return string;
    }

    /**
     * Insert a new command into the command tree
     *
     * @param command Command to insert
     */
    public void insertCommand(@Nonnull final Command<C, M> command) {
        synchronized (this.commandLock) {
            Node<CommandComponent<C, ?>> node = this.internalTree;
            for (final CommandComponent<C, ?> component : command.getComponents()) {
                Node<CommandComponent<C, ?>> tempNode = node.getChild(component);
                if (tempNode == null) {
                    tempNode = node.addChild(component);
                }
                if (node.children.size() > 0) {
                    node.children.sort(Comparator.comparing(Node::getValue));
                }
                tempNode.setParent(node);
                node = tempNode;
            }
            if (node.getValue() != null) {
                node.getValue().setOwningCommand(command);
            }
            // Verify the command structure every time we add a new command
            this.verifyAndRegister();
        }
    }

    @Nullable
    private String isPermitted(@Nonnull final C sender, @Nonnull final Node<CommandComponent<C, ?>> node) {
        final String permission = node.nodeMeta.get("permission");
        if (permission != null) {
            return sender.hasPermission(permission) ? null : permission;
        }
        if (node.isLeaf()) {
            return sender.hasPermission(
                    Objects.requireNonNull(Objects.requireNonNull(node.value, "node.value").getOwningCommand(),
                                           "owning command").getCommandPermission())
                   ? null : Objects.requireNonNull(node.value.getOwningCommand(), "owning command").getCommandPermission();
        }
        /*
          if any of the children would permit the execution, then the sender has a valid
           chain to execute, and so we allow them to execute the root
         */
        final List<String> missingPermissions = new LinkedList<>();
        for (final Node<CommandComponent<C, ?>> child : node.getChildren()) {
            final String check = this.isPermitted(sender, child);
            if (check == null) {
                return null;
            } else {
                missingPermissions.add(check);
            }
        }
        return String.join(", ", missingPermissions);
    }

    /**
     * Go through all commands and register them, and verify the
     * command tree contracts
     */
    public void verifyAndRegister() {
        // All top level commands are supposed to be registered in the command manager
        this.internalTree.children.stream().map(Node::getValue).forEach(commandComponent -> {
            if (!(commandComponent instanceof StaticComponent)) {
                throw new IllegalStateException("Top level command component cannot be a variable");
            }
        });

        this.checkAmbiguity(this.internalTree);

        // Verify that all leaf nodes have command registered
        this.getLeaves(this.internalTree).forEach(leaf -> {
            if (leaf.getOwningCommand() == null) {
                throw new NoCommandInLeafException(leaf);
            } else {
                // noinspection unchecked
                final Command<C, M> owningCommand = (Command<C, M>) leaf.getOwningCommand();
                this.commandRegistrationHandler.registerCommand(owningCommand);
            }
        });

        // Register command permissions
        this.getLeavesRaw(this.internalTree).forEach(node -> {
            /* All leaves must necessarily have an owning command */
            // noinspection all
            node.nodeMeta.put("permission", node.getValue().getOwningCommand().getCommandPermission());
            // Get chain and order it tail->head then skip the tail (leaf node)
            List<Node<CommandComponent<C, ?>>> chain = this.getChain(node);
            Collections.reverse(chain);
            chain = chain.subList(1, chain.size());
            // Go through all nodes from the tail upwards until a collision occurs
            for (final Node<CommandComponent<C, ?>> commandComponentNode : chain) {
                if (commandComponentNode.nodeMeta.containsKey("permission") && !commandComponentNode.nodeMeta.get("permission")
                                                                                                             .equalsIgnoreCase(
                                                                                                                     node.nodeMeta
                                                                                                           .get("permission"))) {
                    commandComponentNode.nodeMeta.put("permission", "");
                } else {
                    commandComponentNode.nodeMeta.put("permission", node.nodeMeta.get("permission"));
                }
            }
        });
    }

    private void checkAmbiguity(@Nonnull final Node<CommandComponent<C, ?>> node) throws AmbiguousNodeException {
        if (node.isLeaf()) {
            return;
        }
        final int size = node.children.size();
        for (final Node<CommandComponent<C, ?>> child : node.children) {
            if (child.getValue() != null && !child.getValue().isRequired() && size > 1) {
                throw new AmbiguousNodeException(node.getValue(),
                                                 child.getValue(),
                                                 node.getChildren().stream().map(Node::getValue).collect(Collectors.toList()));
            }
        }
        node.children.forEach(this::checkAmbiguity);
    }

    private List<Node<CommandComponent<C, ?>>> getLeavesRaw(@Nonnull final Node<CommandComponent<C, ?>> node) {
        final List<Node<CommandComponent<C, ?>>> leaves = new LinkedList<>();
        if (node.isLeaf()) {
            if (node.getValue() != null) {
                leaves.add(node);
            }
        } else {
            node.children.forEach(child -> leaves.addAll(getLeavesRaw(child)));
        }
        return leaves;
    }

    private List<CommandComponent<C, ?>> getLeaves(@Nonnull final Node<CommandComponent<C, ?>> node) {
        final List<CommandComponent<C, ?>> leaves = new LinkedList<>();
        if (node.isLeaf()) {
            if (node.getValue() != null) {
                leaves.add(node.getValue());
            }
        } else {
            node.children.forEach(child -> leaves.addAll(getLeaves(child)));
        }
        return leaves;
    }

    private List<Node<CommandComponent<C, ?>>> getChain(@Nullable final Node<CommandComponent<C, ?>> end) {
        final List<Node<CommandComponent<C, ?>>> chain = new LinkedList<>();
        Node<CommandComponent<C, ?>> tail = end;
        while (tail != null) {
            chain.add(tail);
            tail = tail.getParent();
        }
        Collections.reverse(chain);
        return chain;
    }

    @Nullable
    private Command<C, M> cast(@Nullable final Command<C, ?> command) {
        if (command == null) {
            return null;
        }
        @SuppressWarnings("unchecked") final Command<C, M> casted = (Command<C, M>) command;
        return casted;
    }


    /**
     * Very simple tree structure
     *
     * @param <T> Node value type
     */
    private static final class Node<T> {

        private final Map<String, String> nodeMeta = new HashMap<>();
        private final List<Node<T>> children = new LinkedList<>();
        private final T value;
        private Node<T> parent;

        private Node(@Nullable final T value) {
            this.value = value;
        }

        private List<Node<T>> getChildren() {
            return Collections.unmodifiableList(this.children);
        }

        @Nonnull
        private Node<T> addChild(@Nonnull final T child) {
            final Node<T> node = new Node<>(child);
            this.children.add(node);
            return node;
        }

        @Nullable
        private Node<T> getChild(@Nonnull final T type) {
            for (final Node<T> child : this.children) {
                if (type.equals(child.getValue())) {
                    return child;
                }
            }
            return null;
        }

        private boolean isLeaf() {
            return this.children.isEmpty();
        }

        @Nullable
        public T getValue() {
            return this.value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Node<?> node = (Node<?>) o;
            return Objects.equals(getValue(), node.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getValue());
        }

        @Nullable
        public Node<T> getParent() {
            return this.parent;
        }

        public void setParent(@Nullable final Node<T> parent) {
            this.parent = parent;
        }

        @Override
        public String toString() {
            return "Node{value=" + value + '}';
        }
    }

}
