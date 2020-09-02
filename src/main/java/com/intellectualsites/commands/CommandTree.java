//
// MIT License
//
// Copyright (c) 2020 IntellectualSites
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

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.intellectualsites.commands.components.CommandComponent;
import com.intellectualsites.commands.components.StaticComponent;
import com.intellectualsites.commands.exceptions.NoSuchCommandException;
import com.intellectualsites.commands.parser.ComponentParseResult;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tree containing all commands and command paths
 *
 * @param <C> Command sender type
 */
public class CommandTree<C extends CommandSender> {

    private final Node<CommandComponent<?>> internalTree = new Node<>(null);

    private CommandTree() {
    }

    /**
     * Create a new command tree instance
     *
     * @param <C> Command sender type
     * @return New command tree
     */
    @Nonnull
    public static <C extends CommandSender> CommandTree<C> newTree() {
        return new CommandTree<>();
    }

    public Optional<Command> parse(@Nonnull final C commandSender, @Nonnull final String[] args) throws NoSuchCommandException {
        final Queue<String> commandQueue = new LinkedList<>(Arrays.asList(args));
        return parseCommand(commandSender, commandQueue, this.internalTree);
    }

    private Optional<Command> parseCommand(@Nonnull final C commandSender, @Nonnull final Queue<String> commandQueue,
                                           @Nonnull final Node<CommandComponent<?>> root) throws NoSuchCommandException {

        final List<Node<CommandComponent<?>>> children = root.getChildren();
        if (children.size() == 1 && !(children.get(0).getValue() instanceof StaticComponent)) {
            // The value has to be a variable
            final Node<CommandComponent<?>> child = children.get(0);
            if (child.getValue() != null) {
                final ComponentParseResult<?> result = child.getValue().getParser().parse(commandSender, commandQueue);
                if (result.getParsedValue().isPresent()) {
                    /* TODO: Add context */
                    if (child.isLeaf()) {
                        return Optional.ofNullable(child.getValue().getOwningCommand());
                    } else {
                        return this.parseCommand(commandSender, commandQueue, child);
                    }
                } else if (result.getFailure().isPresent()) {
                    /* TODO: Return error */
                }
            }
        }

        /* There are 0 or more static components as children. No variable child components are present */
        if (children.isEmpty()) {
            /* We are at the bottom. Check if there's a command attached, in which case we're done */
            if (root.getValue() != null && root.getValue().getOwningCommand() != null) {
                return Optional.of(root.getValue().getOwningCommand());
            } else {
                /* TODO: Indicate that we could not resolve the command here */
                final List<CommandComponent<?>> components = this.getChain(root).stream().map(Node::getValue).collect(Collectors.toList());
            }
        } else {
            final String popped = commandQueue.poll();
            if (popped == null) {
                /* Not enough arguments */
                /* TODO: Send correct usage */
                return Optional.empty();
            }

            int low = 0;
            int high = children.size() - 1;

            while (low <= high) {
                int mid = (low + high) / 2;

                final Node<CommandComponent<?>> node = children.get(mid);
                assert node.getValue() != null;

                final int comparison = node.getValue().getName().compareToIgnoreCase(popped);
                if (comparison < 0) {
                    low = mid + 1;
                } else if (comparison > 0) {
                    high = mid - 1;
                } else {
                    /* We found a match */
                    if (node.isLeaf()) {
                        return Optional.ofNullable(node.getValue().getOwningCommand());
                    } else {
                        return parseCommand(commandSender, commandQueue, node);
                    }
                }
            }

            /* We could not find a match */
            throw new NoSuchCommandException(commandSender, getChain(root).stream().map(Node::getValue).collect(Collectors.toList()), popped);
        }

        /*
        final Iterator<Node<CommandComponent<?>>> childIterator = root.getChildren().iterator();
        if (childIterator.hasNext()) {
            while (childIterator.hasNext()) {
                final Node<CommandComponent<?>> child = childIterator.next();
                if (child.getValue() != null) {
                    final ComponentParseResult<?> result = child.getValue().getParser().parse(commandSender, commandQueue);
                    if (result.getParsedValue().isPresent()) {
                        return this.parseCommand(commandSender, commandQueue, child);
                    } else if (result.getFailure().isPresent() && root.children.size() == 1) {
                    }
                }
            }
        }
        */

        return Optional.empty();
    }

    /**
     * Insert a new command into the command tree
     *
     * @param command Command to insert
     */
    public void insertCommand(@Nonnull final Command command) {
        Node<CommandComponent<?>> node = this.internalTree;
        for (final CommandComponent<?> component : command.getComponents()) {
            Node<CommandComponent<?>> tempNode = node.getChild(component);
            if (tempNode == null) {
                tempNode = node.addChild(component);
            }
            if (node.children.size() > 0) {
                node.children.sort(Comparator.comparing(Node::getValue));
            }
            node = tempNode;
        }
        if (node.getValue() != null) {
            node.getValue().setOwningCommand(command);
        }
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
            // TODO: Register in the command handler
        });
        this.checkAmbiguity(this.internalTree);
        // Verify that all leaf nodes have command registered
        this.getLeaves(this.internalTree).forEach(leaf -> {
            if (leaf.getOwningCommand() == null) {
                // TODO: Custom exception type
                throw new IllegalStateException("Leaf node does not have associated owning command");
            }
        });
    }

    private void checkAmbiguity(@Nonnull final Node<CommandComponent<?>> node) {
        if (node.isLeaf()) {
            return;
        }
        final int size = node.children.size();
        for (final Node<CommandComponent<?>> child : node.children) {
            if (child.getValue() != null && !child.getValue().isRequired() && size > 1) {
                // TODO: Use a custom exception type here
                throw new IllegalStateException("Ambiguous command node found: " + node.getValue());
            }
        }
        node.children.forEach(this::checkAmbiguity);
    }

    private List<CommandComponent<?>> getLeaves(@Nonnull final Node<CommandComponent<?>> node) {
        final List<CommandComponent<?>> leaves = new LinkedList<>();
        if (node.isLeaf()) {
            if (node.getValue() != null) {
                leaves.add(node.getValue());
            }
        } else {
            node.children.forEach(child -> leaves.addAll(getLeaves(child)));
        }
        return leaves;
    }

    private List<Node<CommandComponent<?>>> getChain(@Nullable final Node<CommandComponent<?>> end) {
        final List<Node<CommandComponent<?>>> chain = new LinkedList<>();
        Node<CommandComponent<?>> tail = end;
        while (tail != null) {
            chain.add(tail);
            tail = end.getParent();
        }
        return Lists.reverse(chain);
    }


    private static final class Node<T> {

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
            return Objects.equal(getChildren(), node.getChildren()) &&
                    Objects.equal(getValue(), node.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getChildren(), getValue());
        }

        public void setParent(@Nullable final Node<T> parent) {
            this.parent = parent;
        }

        @Nullable
        public Node<T> getParent() {
            return this.parent;
        }

    }

}
