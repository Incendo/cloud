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
package cloud.commandframework.exception;

import cloud.commandframework.CommandTree;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.internal.CommandNode;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Exception thrown when a {@link CommandContext}
 * is being inserted into a {@link CommandTree} and an ambiguity
 * is detected.
 */
@SuppressWarnings({"unused", "serial"})
@API(status = API.Status.STABLE)
public final class AmbiguousNodeException extends IllegalStateException {

    private final CommandNode<?> parentNode;
    private final CommandNode<?> ambiguousNode;
    private final List<CommandNode<?>> children;

    /**
     * Construct a new ambiguous node exception
     *
     * @param parentNode    Parent node
     * @param ambiguousNode Node that caused exception
     * @param children      All children of the parent
     */
    @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
    public AmbiguousNodeException(
            final @Nullable CommandNode<?> parentNode,
            final @NonNull CommandNode<?> ambiguousNode,
            final @NonNull List<@NonNull CommandNode<?>> children
    ) {
        this.parentNode = parentNode;
        this.ambiguousNode = ambiguousNode;
        this.children = children;
    }

    /**
     * Returns the parent node.
     *
     * @return parent node
     */
    public @Nullable CommandNode<?> parentNode() {
        return this.parentNode;
    }

    /**
     * Returns the ambiguous node.
     *
     * @return ambiguous node
     */
    public @NonNull CommandNode<?> ambiguousNode() {
        return this.ambiguousNode;
    }

    /**
     * Returns all children of the parent.
     *
     * @return child nodes
     */
    public @NonNull List<@NonNull CommandNode<?>> children() {
        return Collections.unmodifiableList(this.children);
    }

    @Override
    public String getMessage() {
        final StringBuilder stringBuilder = new StringBuilder("Ambiguous Node: ")
                .append(this.ambiguousNode.component().name())
                .append(" cannot be added as a child to ")
                .append(this.parentNode == null ? "<root>" : this.parentNode.component().name())
                .append(" (All children: ");
        final Iterator<CommandNode<?>> childIterator = this.children.iterator();
        while (childIterator.hasNext()) {
            stringBuilder.append(childIterator.next().component().name());
            if (childIterator.hasNext()) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.append(")")
                .toString();
    }
}
