//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
package cloud.commandframework.arguments;

import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.compound.CompoundArgument;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Iterator;
import java.util.List;

/**
 * {@link CommandSyntaxFormatter} implementation that uses the following rules:
 * <ul>
 *     <li>static arguments are serialized as their name, without a bracket</li>
 *     <li>required arguments are serialized as their name, surrounded by angle brackets</li>
 *     <li>optional arguments are serialized as their name, surrounded by square brackets</li>
 * </ul>
 *
 * @param <C> Command sender type
 */
public class StandardCommandSyntaxFormatter<C> implements CommandSyntaxFormatter<C> {

    @Override
    public final @NonNull String apply(@NonNull final List<@NonNull CommandArgument<C, ?>> commandArguments,
                                       final CommandTree.@Nullable Node<@Nullable CommandArgument<C, ?>> node) {
        final StringBuilder stringBuilder = new StringBuilder();
        final Iterator<CommandArgument<C, ?>> iterator = commandArguments.iterator();
        while (iterator.hasNext()) {
            final CommandArgument<?, ?> commandArgument = iterator.next();
            if (commandArgument instanceof StaticArgument) {
                stringBuilder.append(commandArgument.getName());
            } else {
                if (commandArgument instanceof CompoundArgument) {
                    final char prefix = commandArgument.isRequired() ? '<' : '[';
                    final char suffix = commandArgument.isRequired() ? '>' : ']';
                    stringBuilder.append(prefix);
                    // noinspection all
                    final Object[] names = ((CompoundArgument<?, C, ?>) commandArgument).getNames().toArray();
                    for (int i = 0; i < names.length; i++) {
                        stringBuilder.append(prefix).append(names[i]).append(suffix);
                        if ((i + 1) < names.length) {
                            stringBuilder.append(' ');
                        }
                    }
                    stringBuilder.append(suffix);
                } else if (commandArgument.isRequired()) {
                    stringBuilder.append("<").append(commandArgument.getName()).append(">");
                } else {
                    stringBuilder.append("[").append(commandArgument.getName()).append("]");
                }
            }
            if (iterator.hasNext()) {
                stringBuilder.append(" ");
            }
        }
        CommandTree.Node<CommandArgument<C, ?>> tail = node;
        while (tail != null && !tail.isLeaf()) {
            if (tail.getChildren().size() > 1) {
                stringBuilder.append(" ");
                final Iterator<CommandTree.Node<CommandArgument<C, ?>>> childIterator = tail.getChildren().iterator();
                while (childIterator.hasNext()) {
                    final CommandTree.Node<CommandArgument<C, ?>> child = childIterator.next();
                    stringBuilder.append(child.getValue().getName());
                    if (childIterator.hasNext()) {
                        stringBuilder.append("|");
                    }
                }
                break;
            }
            final CommandArgument<C, ?> argument = tail.getChildren().get(0).getValue();
            final String prefix;
            final String suffix;
            if (argument instanceof StaticArgument) {
                prefix = "";
                suffix = "";
            } else if (argument.isRequired()) {
                prefix = "<";
                suffix = ">";
            } else {
                prefix = "[";
                suffix = "]";
            }

            if (argument instanceof CompoundArgument) {
                stringBuilder.append(" ").append(prefix);
                // noinspection all
                final Object[] names = ((CompoundArgument<?, C, ?>) argument).getNames().toArray();
                for (int i = 0; i < names.length; i++) {
                    stringBuilder.append(prefix).append(names[i]).append(suffix);
                    if ((i + 1) < names.length) {
                        stringBuilder.append(' ');
                    }
                }
                stringBuilder.append(suffix);
            } else {
                stringBuilder.append(" ")
                             .append(prefix)
                             .append(argument.getName())
                             .append(suffix);
            }
            tail = tail.getChildren().get(0);
        }
        return stringBuilder.toString();
    }

}
