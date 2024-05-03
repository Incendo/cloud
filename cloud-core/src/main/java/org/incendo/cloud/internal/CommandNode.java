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
package org.incendo.cloud.internal;

import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.key.SimpleMutableCloudKeyContainer;
import org.incendo.cloud.permission.Permission;

import static org.incendo.cloud.key.CloudKey.cloudKey;

/**
 * Very simple tree structure
 *
 * @param <C> command sender type
 */
@API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class CommandNode<C> {

    /*
    Flattened view of the allowed sender types for a node.

    For example, with manager sender type Object and structure
      root a does not have executor
        child b has executor requires sender A (extends Object)
        child c has executor requires sender B (extends Object)
        child d has executor requires sender AA (extends A)

    the root node would have Set.of(A, B)

    when a leaf does not require a specific sender, Object is used as a substitute
     */
    public static final CloudKey<Set<Type>> META_KEY_SENDER_TYPES = cloudKey("senderTypes", new TypeToken<Set<Type>>() {});
    // Map of allowed sender types (not flattened) to the permission required to execute the chain for that type.
    public static final CloudKey<Map<Type, Permission>> META_KEY_ACCESS = cloudKey(
            "access",
            new TypeToken<Map<Type, Permission>>() {}
    );

    private final SimpleMutableCloudKeyContainer nodeMeta = new SimpleMutableCloudKeyContainer(new HashMap<>());
    private final List<CommandNode<C>> children = new LinkedList<>();
    private final CommandComponent<C> component;
    private CommandNode<C> parent;
    private Command<C> command;

    /**
     * Creates a new command node
     *
     * @param component the component contained in the node, or {@code null}
     */
    public CommandNode(final @Nullable CommandComponent<C> component) {
        this.component = component;
    }

    /**
     * Returns an immutable view of the node's children
     *
     * @return list of children
     */
    public @NonNull List<@NonNull CommandNode<C>> children() {
        return Collections.unmodifiableList(this.children);
    }

    /**
     * Adds the given {@code component} as a child of this node
     *
     * @param component the child component
     * @return the node containing the given {@code component}
     */
    public @NonNull CommandNode<C> addChild(final @NonNull CommandComponent<C> component) {
        final CommandNode<C> node = new CommandNode<>(component);
        this.children.add(node);
        return node;
    }

    /**
     * Returns the node containing the given {@code component}
     *
     * @param component the child component
     * @return the node containing the given {@code component}, or {@code null}
     */
    public @Nullable CommandNode<C> getChild(final @NonNull CommandComponent<C> component) {
        for (final CommandNode<C> child : this.children) {
            if (component.equals(child.component())) {
                return child;
            }
        }
        return null;
    }

    /**
     * Removes the given {@code child} as a child from this node
     *
     * @param child the child to remove
     * @return {@code true} if the child node was removed, {@code false} if not
     */
    public boolean removeChild(final @NonNull CommandNode<C> child) {
        return this.children.remove(child);
    }

    /**
     * Returns whether the node is a leaf node
     *
     * @return {@code true} if the node is a leaf node, else {@code false}
     */
    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    /**
     * Returns the node meta instance
     *
     * @return Node meta
     */
    public @NonNull SimpleMutableCloudKeyContainer nodeMeta() {
        return this.nodeMeta;
    }

    /**
     * Returns the component contained in this node.
     *
     * @return the component
     */
    public @MonotonicNonNull CommandComponent<C> component() {
        return this.component;
    }

    /**
     * Returns the command that the {@link #component()} belongs to, if the {@link #component()} is executable.
     *
     * @return the command
     */
    public @MonotonicNonNull Command<C> command() {
        return this.command;
    }

    /**
     * Sets the executable command of this node.
     *
     * @param command command
     */
    public void command(final @NonNull Command<C> command) {
        if (this.command != null) {
            throw new IllegalStateException("Cannot replace owning command");
        }
        this.command = command;
    }

    /**
     * Returns the parent node
     *
     * @return Parent node
     */
    public @Nullable CommandNode<C> parent() {
        return this.parent;
    }

    /**
     * Updates the parent node
     *
     * @param parent new parent node
     */
    public void parent(final @Nullable CommandNode<C> parent) {
        this.parent = parent;
    }

    /**
     * Sorts the child nodes using their {@link #component() components}.
     */
    public void sortChildren() {
        this.children.sort(Comparator.comparing(CommandNode::component));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final CommandNode<?> node = (CommandNode<?>) o;
        return Objects.equals(this.component(), node.component());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.component());
    }

    @Override
    public String toString() {
        return "Node{value=" + this.component + '}';
    }
}
