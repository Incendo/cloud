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
package cloud.commandframework.arguments;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.aggregate.AggregateCommandParser;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.flags.CommandFlagParser;
import cloud.commandframework.internal.CommandNode;
import cloud.commandframework.permission.Permission;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * {@link CommandSyntaxFormatter} implementation that uses the following rules:
 * <ul>
 *     <li>static arguments are serialized as their name, without a bracket</li>
 *     <li>required arguments are serialized as their name, surrounded by angle brackets</li>
 *     <li>optional arguments are serialized as their name, surrounded by square brackets</li>
 *     <li>does not render arguments the sender does not have access to (either due to permission or sender type requirements)
 *     </li>
 * </ul>
 *
 * @param <C> command sender type
 */
@API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
public class StandardCommandSyntaxFormatter<C> implements CommandSyntaxFormatter<C> {

    private final CommandManager<C> manager;

    /**
     * Creates a new {@link StandardCommandSyntaxFormatter}.
     *
     * @param manager command manager
     */
    public StandardCommandSyntaxFormatter(final @NonNull CommandManager<C> manager) {
        this.manager = manager;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public final @NonNull String apply(
            final @Nullable C sender,
            final @NonNull List<@NonNull CommandComponent<C>> commandComponents,
            final @Nullable CommandNode<C> node
    ) {
        return this.apply(commandComponents, node, n -> {
            if (sender == null) {
                return true;
            }
            final Permission permission = (Permission) n.nodeMeta().getOrDefault(
                    CommandNode.META_KEY_PERMISSION,
                    Permission.empty()
            );
            final Set<Class<?>> senderTypes = (Set<Class<?>>) n.nodeMeta().getOrDefault(
                    CommandNode.META_KEY_SENDER_TYPES,
                    Collections.emptySet()
            );
            if (senderTypes.isEmpty()) {
                return this.manager.hasPermission(sender, permission);
            }
            for (final Class<?> senderType : senderTypes) {
                if (senderType.isInstance(sender)) {
                    return this.manager.hasPermission(sender, permission);
                }
            }
            return false;
        });
    }

    private @NonNull String apply(
            final @NonNull List<@NonNull CommandComponent<C>> commandComponents,
            final @Nullable CommandNode<C> node,
            final @NonNull Predicate<@NonNull CommandNode<C>> filter
    ) {
        final FormattingInstance formattingInstance = this.createInstance();
        final Iterator<CommandComponent<C>> iterator = commandComponents.iterator();
        while (iterator.hasNext()) {
            final CommandComponent<C> commandComponent = iterator.next();
            if (commandComponent.type() == CommandComponent.ComponentType.LITERAL) {
                formattingInstance.appendLiteral(commandComponent);
            } else if (commandComponent.parser() instanceof AggregateCommandParser<?, ?>) {
                final AggregateCommandParser<?, ?> aggregateParser = (AggregateCommandParser<?, ?>) commandComponent.parser();
                formattingInstance.appendAggregate(commandComponent, aggregateParser);
            } else if (commandComponent.type() == CommandComponent.ComponentType.FLAG) {
                formattingInstance.appendFlag((CommandFlagParser<?>) commandComponent.parser());
            } else {
                if (commandComponent.required()) {
                    formattingInstance.appendRequired(commandComponent);
                } else {
                    formattingInstance.appendOptional(commandComponent);
                }
            }
            if (iterator.hasNext()) {
                formattingInstance.appendBlankSpace();
            }
        }
        CommandNode<C> tail = node;
        while (tail != null && !tail.isLeaf() && filter.test(tail)) {
            if (tail.children().size() > 1) {
                formattingInstance.appendBlankSpace();
                final Iterator<CommandNode<C>> childIterator = tail.children().stream().filter(filter).iterator();
                while (childIterator.hasNext()) {
                    final CommandNode<C> child = childIterator.next();

                    if (child.component() == null) {
                        continue;
                    }

                    switch (child.component().type()) {
                        case LITERAL:
                            formattingInstance.appendName(child.component().name());
                            break;
                        case REQUIRED_VARIABLE:
                            formattingInstance.appendRequired(child.component());
                            break;
                        case OPTIONAL_VARIABLE:
                            formattingInstance.appendOptional(child.component());
                            break;
                        default:
                            break;
                    }

                    if (childIterator.hasNext()) {
                        formattingInstance.appendPipe();
                    }
                }
                break;
            }
            if (!filter.test(tail.children().get(0))) {
                break;
            }
            final CommandComponent<C> component = tail.children().get(0).component();
            if (component.parser() instanceof AggregateCommandParser<?, ?>) {
                final AggregateCommandParser<?, ?> aggregateParser = (AggregateCommandParser<?, ?>) component.parser();
                formattingInstance.appendBlankSpace();
                formattingInstance.appendAggregate(component, aggregateParser);
            } else if (component.type() == CommandComponent.ComponentType.FLAG) {
                formattingInstance.appendBlankSpace();
                formattingInstance.appendFlag((CommandFlagParser<?>) component.parser());
            } else if (component.type() == CommandComponent.ComponentType.LITERAL) {
                formattingInstance.appendBlankSpace();
                formattingInstance.appendLiteral(component);
            } else {
                formattingInstance.appendBlankSpace();
                if (component.required()) {
                    formattingInstance.appendRequired(component);
                } else {
                    formattingInstance.appendOptional(component);
                }
            }
            tail = tail.children().get(0);
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
    @API(status = API.Status.STABLE)
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
        public void appendLiteral(final @NonNull CommandComponent<?> literal) {
            this.appendName(literal.name());
        }

        /**
         * Append an aggregate component to the syntax string
         *
         * @param component The component that contained the argument
         * @param parser    Compound argument to append
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public void appendAggregate(
                final @NonNull CommandComponent<?> component,
                final @NonNull AggregateCommandParser<?, ?> parser
        ) {
            final String prefix = component.required() ? this.getRequiredPrefix() : this.getOptionalPrefix();
            final String suffix = component.required() ? this.getRequiredSuffix() : this.getOptionalSuffix();
            this.builder.append(prefix);

            final Iterator<? extends CommandComponent<?>> innerComponents = parser.components().iterator();
            while (innerComponents.hasNext()) {
                final CommandComponent<?> innerComponent = innerComponents.next();
                this.builder.append(prefix);
                this.appendName(innerComponent.name());
                this.builder.append(suffix);
                if (innerComponents.hasNext()) {
                    this.builder.append(' ');
                }
            }
            this.builder.append(suffix);
        }

        /**
         * Appends a flag argument
         *
         * @param flagParser flag parser
         */
        public void appendFlag(final @NonNull CommandFlagParser<?> flagParser) {
            this.builder.append(this.getOptionalPrefix());

            final Iterator<CommandFlag<?>> flagIterator = flagParser
                    .flags()
                    .iterator();

            while (flagIterator.hasNext()) {
                final CommandFlag<?> flag = flagIterator.next();
                this.appendName(String.format("--%s", flag.getName()));

                if (flag.commandComponent() != null) {
                    this.builder.append(' ');
                    this.builder.append(this.getOptionalPrefix());
                    this.appendName(flag.commandComponent().name());
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
        public void appendRequired(final @NonNull CommandComponent<?> argument) {
            this.builder.append(this.getRequiredPrefix());
            this.appendName(argument.name());
            this.builder.append(this.getRequiredSuffix());
        }

        /**
         * Append an optional argument
         *
         * @param argument Optional argument
         */
        public void appendOptional(final @NonNull CommandComponent<?> argument) {
            this.builder.append(this.getOptionalPrefix());
            this.appendName(argument.name());
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
