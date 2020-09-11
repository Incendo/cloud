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
package com.intellectualsites.commands.exceptions;

import com.intellectualsites.commands.components.CommandComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

/**
 * Exception thrown when a {@link com.intellectualsites.commands.context.CommandContext}
 * is being inserted into a {@link com.intellectualsites.commands.CommandTree} and an ambiguity
 * is detected.
 */
@SuppressWarnings("unused")
public final class AmbiguousNodeException extends IllegalStateException {

    private final CommandComponent<?, ?> parentNode;
    private final CommandComponent<?, ?> ambiguousNode;
    private final List<CommandComponent<?, ?>> children;

    /**
     * Construct a new ambiguous node exception
     *
     * @param parentNode    Parent node
     * @param ambiguousNode Node that caused exception
     * @param children      All children of the parent
     */
    public AmbiguousNodeException(@Nullable final CommandComponent<?, ?> parentNode,
                                  @Nonnull final CommandComponent<?, ?> ambiguousNode,
                                  @Nonnull final List<CommandComponent<?, ?>> children) {
        this.parentNode = parentNode;
        this.ambiguousNode = ambiguousNode;
        this.children = children;
    }

    /**
     * Get the parent node
     *
     * @return Parent node
     */
    @Nullable
    public CommandComponent<?, ?> getParentNode() {
        return this.parentNode;
    }

    /**
     * Get the ambiguous node
     *
     * @return Ambiguous node
     */
    @Nonnull
    public CommandComponent<?, ?> getAmbiguousNode() {
        return this.ambiguousNode;
    }

    /**
     * Get all children of the parent
     *
     * @return Child nodes
     */
    @Nonnull
    public List<CommandComponent<?, ?>> getChildren() {
        return this.children;
    }

    @Override
    public String getMessage() {
        final StringBuilder stringBuilder = new StringBuilder("Ambiguous Node: ")
                .append(ambiguousNode.getName())
                .append(" cannot be added as a child to ")
                .append(parentNode == null ? "<root>" : parentNode.getName())
                .append(" (All children: ");
        final Iterator<CommandComponent<?, ?>> childIterator = this.children.iterator();
        while (childIterator.hasNext()) {
            stringBuilder.append(childIterator.next().getName());
            if (childIterator.hasNext()) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.append(")")
                            .toString();
    }

}
