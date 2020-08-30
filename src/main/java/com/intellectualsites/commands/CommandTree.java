package com.intellectualsites.commands;

import com.google.common.base.Objects;
import com.intellectualsites.commands.components.CommandComponent;
import com.intellectualsites.commands.components.StaticComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Tree containing all commands and command paths
 */
public class CommandTree {

    private final Node<CommandComponent<?>> internalTree = new Node<>(null);

    private CommandTree() {
    }

    /**
     * Create a new command tree instance
     *
     * @return New command tree
     */
    @Nonnull public static CommandTree newTree() {
        return new CommandTree();
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


    private static final class Node<T> {

        private final List<Node<T>> children = new LinkedList<>();
        private final T value;

        private Node(@Nullable final T value) {
            this.value = value;
        }

        private List<Node<T>> getChildren() {
            return Collections.unmodifiableList(this.children);
        }

        @Nonnull private Node<T> addChild(@Nonnull final T child) {
            final Node<T> node = new Node<>(child);
            this.children.add(node);
            return node;
        }

        @Nullable private Node<T> getChild(@Nonnull final T type) {
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

        @Nullable public T getValue() {
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
    }

}
