//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
import cloud.commandframework.arguments.compound.FlagArgument;
import cloud.commandframework.arguments.flags.CommandFlag;
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

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public final @NonNull String apply(
            final @NonNull List<@NonNull CommandArgument<C, ?>> commandArguments,
            final CommandTree.@Nullable Node<@Nullable CommandArgument<C, ?>> node
    ) {
        final FormattingInstance formattingInstance = this.createInstance();
        final Iterator<CommandArgument<C, ?>> iterator = commandArguments.iterator();
        while (iterator.hasNext()) {
            final CommandArgument<?, ?> commandArgument = iterator.next();
            if (commandArgument instanceof StaticArgument) {
                formattingInstance.appendLiteral((StaticArgument<C>) commandArgument);
            } else if (commandArgument instanceof CompoundArgument) {
                formattingInstance.appendCompound((CompoundArgument<?, ?, ?>) commandArgument);
            } else if (commandArgument instanceof FlagArgument) {
                formattingInstance.appendFlag((FlagArgument<?>) commandArgument);
            } else {
                if (commandArgument.isRequired()) {
                    formattingInstance.appendRequired(commandArgument);
                } else {
                    formattingInstance.appendOptional(commandArgument);
                }
            }
            if (iterator.hasNext()) {
                formattingInstance.appendBlankSpace();
            }
        }
        CommandTree.Node<CommandArgument<C, ?>> tail = node;
        while (tail != null && !tail.isLeaf()) {
            if (tail.getChildren().size() > 1) {
                formattingInstance.appendBlankSpace();
                final Iterator<CommandTree.Node<CommandArgument<C, ?>>> childIterator = tail.getChildren().iterator();
                while (childIterator.hasNext()) {
                    final CommandTree.Node<CommandArgument<C, ?>> child = childIterator.next();

                    if (child.getValue() instanceof StaticArgument) {
                        formattingInstance.appendName(child.getValue().getName());
                    } else if (child.getValue().isRequired()) {
                        formattingInstance.appendRequired(child.getValue());
                    } else {
                        formattingInstance.appendOptional(child.getValue());
                    }

                    if (childIterator.hasNext()) {
                        formattingInstance.appendPipe();
                    }
                }
                break;
            }
            final CommandArgument<C, ?> argument = tail.getChildren().get(0).getValue();
            if (argument instanceof CompoundArgument) {
                formattingInstance.appendBlankSpace();
                formattingInstance.appendCompound((CompoundArgument<?, ?, ?>) argument);
            } else if (argument instanceof FlagArgument) {
                formattingInstance.appendBlankSpace();
                formattingInstance.appendFlag((FlagArgument<?>) argument);
            } else {
                formattingInstance.appendBlankSpace();
                if (argument.isRequired()) {
                    formattingInstance.appendRequired(argument);
                } else {
                    formattingInstance.appendOptional(argument);
                }
            }
            tail = tail.getChildren().get(0);
        }
        return formattingInstance.toString();
    }

    /**
     * Create a new formatting instance
     *
     * @return Formatting instance
     */
    protected @NonNull FormattingInstance createInstance() {
        return new FormattingInstance();
    }


    /**
     * Instance that is used when building command syntax
     */
    public static class FormattingInstance {

        private final StringBuilder builder;

        /**
         * Create a new formatting instance
         */
        protected FormattingInstance() {
            this.builder = new StringBuilder();
        }

        @Override
        public final @NonNull String toString() {
            return this.builder.toString();
        }

        /**
         * Append a literal to the syntax string
         *
         * @param literal Literal to append
         */
        public void appendLiteral(final @NonNull StaticArgument<?> literal) {
            this.appendName(literal.getName());
        }

        /**
         * Append a compound argument to the syntax string
         *
         * @param argument Compound argument to append
         */
        public void appendCompound(final @NonNull CompoundArgument<?, ?, ?> argument) {
            final String prefix = argument.isRequired() ? this.getRequiredPrefix() : this.getOptionalPrefix();
            final String suffix = argument.isRequired() ? this.getRequiredSuffix() : this.getOptionalSuffix();
            this.builder.append(prefix);
            final Object[] names = argument.getNames().toArray();
            for (int i = 0; i < names.length; i++) {
                this.builder.append(prefix);
                this.appendName(names[i].toString());
                this.builder.append(suffix);
                if ((i + 1) < names.length) {
                    this.builder.append(' ');
                }
            }
            this.builder.append(suffix);
        }

        /**
         * Append a flag argument
         *
         * @param flagArgument Flag argument
         */
        public void appendFlag(final @NonNull FlagArgument<?> flagArgument) {
            this.builder.append(this.getOptionalPrefix());

            final Iterator<CommandFlag<?>> flagIterator = flagArgument
                    .getFlags()
                    .iterator();

            while (flagIterator.hasNext()) {
                final CommandFlag<?> flag = flagIterator.next();
                this.appendName(String.format("--%s", flag.getName()));

                if (flag.getCommandArgument() != null) {
                    this.builder.append(' ');
                    this.builder.append(this.getOptionalPrefix());
                    this.appendName(flag.getCommandArgument().getName());
                    this.builder.append(this.getOptionalSuffix());
                }

                if (flagIterator.hasNext()) {
                    this.appendBlankSpace();
                    this.appendPipe();
                    this.appendBlankSpace();
                }
            }

            this.builder.append(this.getOptionalSuffix());
        }

        /**
         * Append a required argument
         *
         * @param argument Required argument
         */
        public void appendRequired(final @NonNull CommandArgument<?, ?> argument) {
            this.builder.append(this.getRequiredPrefix());
            this.appendName(argument.getName());
            this.builder.append(this.getRequiredSuffix());
        }

        /**
         * Append an optional argument
         *
         * @param argument Optional argument
         */
        public void appendOptional(final @NonNull CommandArgument<?, ?> argument) {
            this.builder.append(this.getOptionalPrefix());
            this.appendName(argument.getName());
            this.builder.append(this.getOptionalSuffix());
        }

        /**
         * Append the pipe (|) character
         */
        public void appendPipe() {
            this.builder.append("|");
        }

        /**
         * Append an argument name
         *
         * @param name Name to append
         */
        public void appendName(final @NonNull String name) {
            this.builder.append(name);
        }

        /**
         * Get the required argument prefix
         *
         * @return Required argument prefix
         */
        public @NonNull String getRequiredPrefix() {
            return "<";
        }

        /**
         * Get the required argument suffix
         *
         * @return Required argument suffix
         */
        public @NonNull String getRequiredSuffix() {
            return ">";
        }

        /**
         * Get the optional argument prefix
         *
         * @return Optional argument prefix
         */
        public @NonNull String getOptionalPrefix() {
            return "[";
        }

        /**
         * Get the optional argument suffix
         *
         * @return Optional argument suffix
         */
        public @NonNull String getOptionalSuffix() {
            return "]";
        }

        /**
         * Append a blank space
         */
        public void appendBlankSpace() {
            this.builder.append(' ');
        }

    }

}
