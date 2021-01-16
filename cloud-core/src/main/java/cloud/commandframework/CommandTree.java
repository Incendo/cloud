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
package cloud.commandframework;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.arguments.compound.CompoundArgument;
import cloud.commandframework.arguments.compound.FlagArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.AmbiguousNodeException;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoCommandInLeafException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.keys.SimpleCloudKey;
import cloud.commandframework.permission.CommandPermission;
import cloud.commandframework.permission.OrPermission;
import cloud.commandframework.types.tuples.Pair;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tree containing all commands and command paths.
 * <p>
 * All {@link Command commands} consists of unique paths made out of {@link CommandArgument arguments}.
 * These arguments may be {@link StaticArgument literals} or variables. Command may either be required
 * or optional, with the requirement that no optional argument precedes a required argument.
 * <p>
 * The {@link Command commands} are stored in this tree and the nodes of tree consists of the command
 * {@link CommandArgument arguments}. Each leaf node of the tree should containing a fully parsed
 * {@link Command}. It is thus possible to walk the tree and determine whether or not the supplied
 * input from a command sender constitutes a proper command.
 * <p>
 * When parsing input, the tree will be walked until one of four scenarios occur:
 * <ol>
 *     <li>The input queue is empty at a non-leaf node</li>
 *     <li>The input queue is not empty following a leaf node</li>
 *     <li>No child node is able to accept the input</li>
 *     <li>The input queue is empty following a leaf node</li>
 * </ol>
 * <p>
 * Scenarios one and two would result in a {@link InvalidSyntaxException} being thrown, whereas
 * scenario three would result in a {@link NoSuchCommandException} if occurring at the root node
 * or a {@link InvalidSyntaxException} otherwise. Only the fourth scenario would result in a complete
 * command being parsed.
 *
 * @param <C> Command sender type
 */
public final class CommandTree<C> {

    /**
     * Stores the index of the argument that is currently being parsed when parsing
     * a {@link CompoundArgument}
     */
    public static final CloudKey<Integer> PARSING_ARGUMENT_KEY = SimpleCloudKey.of(
            "__parsing_argument__",
            TypeToken.get(Integer.class)
    );

    private final Object commandLock = new Object();

    private final Node<CommandArgument<C, ?>> internalTree = new Node<>(null);
    private final CommandManager<C> commandManager;

    private CommandTree(final @NonNull CommandManager<C> commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Create a new command tree instance
     *
     * @param commandManager Command manager
     * @param <C>            Command sender type
     * @return New command tree
     */
    public static <C> @NonNull CommandTree<C> newTree(final @NonNull CommandManager<C> commandManager) {
        return new CommandTree<>(commandManager);
    }

    /**
     * Attempt to parse string input into a command
     *
     * @param commandContext Command context instance
     * @param args           Input
     * @return Parsed command, if one could be found
     */
    public @NonNull Pair<@Nullable Command<C>, @Nullable Exception> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull Queue<@NonNull String> args
    ) {
        final Pair<@Nullable Command<C>, @Nullable Exception> pair = this.parseCommand(
                new ArrayList<>(),
                commandContext,
                args,
                this.internalTree
        );
        if (pair.getFirst() != null) {
            final Command<C> command = pair.getFirst();
            if (command.getSenderType().isPresent() && !command.getSenderType().get()
                    .isAssignableFrom(commandContext.getSender().getClass())
            ) {
                return Pair.of(null, new InvalidCommandSenderException(
                        commandContext.getSender(),
                        command.getSenderType().get(),
                        new ArrayList<>(command.getArguments()),
                        command
                ));
            }
        }

        return pair;
    }

    private @NonNull Pair<@Nullable Command<C>, @Nullable Exception> parseCommand(
            final @NonNull List<@NonNull CommandArgument<C, ?>> parsedArguments,
            final @NonNull CommandContext<C> commandContext,
            final @NonNull Queue<@NonNull String> commandQueue,
            final @NonNull Node<@Nullable CommandArgument<C, ?>> root
    ) {
        CommandPermission permission = this.isPermitted(commandContext.getSender(), root);
        if (permission != null) {
            return Pair.of(null, new NoPermissionException(
                    permission,
                    commandContext.getSender(),
                    this.getChain(root)
                            .stream()
                            .filter(node -> node.getValue() != null)
                            .map(Node::getValue)
                            .collect(Collectors.toList())
            ));
        }

        final Pair<@Nullable Command<C>, @Nullable Exception> parsedChild = this.attemptParseUnambiguousChild(
                parsedArguments,
                commandContext,
                root,
                commandQueue
        );
        if (parsedChild.getFirst() != null || parsedChild.getSecond() != null) {
            return parsedChild;
        }

        /* There are 0 or more static arguments as children. No variable child arguments are present */
        if (root.children.isEmpty()) {
            /* We are at the bottom. Check if there's a command attached, in which case we're done */
            if (root.getValue() != null && root.getValue().getOwningCommand() != null) {
                if (commandQueue.isEmpty()) {
                    return Pair.of(this.cast(root.getValue().getOwningCommand()), null);
                } else {
                    /* Too many arguments. We have a unique path, so we can send the entire context */
                    return Pair.of(null, new InvalidSyntaxException(
                            this.commandManager.getCommandSyntaxFormatter()
                                    .apply(parsedArguments, root),
                            commandContext.getSender(), this.getChain(root)
                            .stream()
                            .filter(node -> node.getValue() != null)
                            .map(Node::getValue)
                            .collect(Collectors.toList())
                    ));
                }
            } else {
                /* Too many arguments. We have a unique path, so we can send the entire context */
                return Pair.of(null, new InvalidSyntaxException(
                        this.commandManager.getCommandSyntaxFormatter()
                                .apply(parsedArguments, root),
                        commandContext.getSender(), this.getChain(root)
                        .stream()
                        .filter(node -> node.getValue() != null)
                        .map(Node::getValue)
                        .collect(Collectors.toList())
                ));
            }
        } else {
            final Iterator<Node<CommandArgument<C, ?>>> childIterator = root.getChildren().iterator();
            if (childIterator.hasNext()) {
                while (childIterator.hasNext()) {
                    final Node<CommandArgument<C, ?>> child = childIterator.next();
                    if (child.getValue() != null) {
                        final CommandArgument<C, ?> argument = child.getValue();
                        final CommandContext.ArgumentTiming argumentTiming = commandContext.createTiming(argument);

                        argumentTiming.setStart(System.nanoTime());
                        commandContext.setCurrentArgument(argument);
                        final ArgumentParseResult<?> result = argument.getParser().parse(commandContext, commandQueue);
                        argumentTiming.setEnd(System.nanoTime(), result.getFailure().isPresent());

                        if (result.getParsedValue().isPresent()) {
                            parsedArguments.add(child.getValue());
                            return this.parseCommand(parsedArguments, commandContext, commandQueue, child);
                        }
                    }
                }
            }
            /* We could not find a match */
            if (root.equals(this.internalTree)) {
                return Pair.of(null, new NoSuchCommandException(
                        commandContext.getSender(),
                        getChain(root).stream().map(Node::getValue).collect(Collectors.toList()),
                        stringOrEmpty(commandQueue.peek())
                ));
            }
            /* If we couldn't match a child, check if there's a command attached and execute it */
            if (root.getValue() != null && root.getValue().getOwningCommand() != null && commandQueue.isEmpty()) {
                final Command<C> command = root.getValue().getOwningCommand();
                if (!this.getCommandManager().hasPermission(
                        commandContext.getSender(),
                        command.getCommandPermission()
                )) {
                    return Pair.of(null, new NoPermissionException(
                            command.getCommandPermission(),
                            commandContext.getSender(),
                            this.getChain(root)
                                    .stream()
                                    .filter(node -> node.getValue() != null)
                                    .map(Node::getValue)
                                    .collect(Collectors.toList())
                    ));
                }
                return Pair.of(root.getValue().getOwningCommand(), null);
            }
            /* We know that there's no command and we also cannot match any of the children */
            return Pair.of(null, new InvalidSyntaxException(
                    this.commandManager.getCommandSyntaxFormatter()
                            .apply(parsedArguments, root),
                    commandContext.getSender(), this.getChain(root)
                    .stream()
                    .filter(node -> node.getValue() != null)
                    .map(Node::getValue)
                    .collect(Collectors.toList())
            ));
        }
    }

    private @NonNull Pair<@Nullable Command<C>, @Nullable Exception> attemptParseUnambiguousChild(
            final @NonNull List<@NonNull CommandArgument<C, ?>> parsedArguments,
            final @NonNull CommandContext<C> commandContext,
            final @NonNull Node<@Nullable CommandArgument<C, ?>> root,
            final @NonNull Queue<String> commandQueue
    ) {
        CommandPermission permission;
        final List<Node<CommandArgument<C, ?>>> children = root.getChildren();

        // Check whether it matches any of the static arguments
        // If so, do not attempt parsing as a dynamic argument
        if (!commandQueue.isEmpty()) {
            final String literal = commandQueue.peek();
            final boolean matchesLiteral = children.stream()
                .filter(n -> n.getValue() instanceof StaticArgument)
                .map(n -> (StaticArgument<?>) n.getValue())
                .flatMap(arg -> Stream.concat(Stream.of(arg.getName()), arg.getAliases().stream()))
                .anyMatch(arg -> arg.equals(literal));

            if (matchesLiteral) {
                return Pair.of(null, null);
            }
        }

        // If it does not match a literal, try to find the one argument node, if it exists
        // The ambiguity check guarantees that only one will be present
        final List<Node<CommandArgument<C, ?>>> argumentNodes = children.stream()
                .filter(n -> (n.getValue() != null && !(n.getValue() instanceof StaticArgument)))
                .collect(Collectors.toList());

        if (argumentNodes.size() > 1) {
            throw new IllegalStateException("Unexpected ambiguity detected, number of "
                    + "dynamic child nodes should not exceed 1");
        } else if (!argumentNodes.isEmpty()) {
            final Node<CommandArgument<C, ?>> child = argumentNodes.get(0);

            // The value has to be a variable
            permission = this.isPermitted(commandContext.getSender(), child);
            if (!commandQueue.isEmpty() && permission != null) {
                return Pair.of(null, new NoPermissionException(
                        permission,
                        commandContext.getSender(),
                        this.getChain(child)
                                .stream()
                                .filter(node -> node.getValue() != null)
                                .map(Node::getValue)
                                .collect(Collectors.toList())
                ));
            }
            if (child.getValue() != null) {
                if (commandQueue.isEmpty()) {
                    if (child.getValue().hasDefaultValue()) {
                        commandQueue.add(child.getValue().getDefaultValue());
                    } else if (!child.getValue().isRequired()) {
                        if (child.getValue().getOwningCommand() == null) {
                            /*
                             * If there are multiple children with different owning commands then it's ambiguous and
                             * not allowed, therefore we're able to pick any child command, as long as we can find it
                             */
                            Node<CommandArgument<C, ?>> node = child;
                            while (!node.isLeaf()) {
                                node = node.getChildren().get(0);
                                if (node.getValue() != null && node.getValue().getOwningCommand() != null) {
                                    child.getValue().setOwningCommand(node.getValue().getOwningCommand());
                                }
                            }
                        }
                        return Pair.of(child.getValue().getOwningCommand(), null);
                    } else if (child.isLeaf()) {
                        if (root.getValue() != null && root.getValue().getOwningCommand() != null) {
                            final Command<C> command = root.getValue().getOwningCommand();
                            if (!this.getCommandManager().hasPermission(
                                    commandContext.getSender(),
                                    command.getCommandPermission()
                            )) {
                                return Pair.of(null, new NoPermissionException(
                                        command.getCommandPermission(),
                                        commandContext.getSender(),
                                        this.getChain(root)
                                                .stream()
                                                .filter(node -> node.getValue() != null)
                                                .map(Node::getValue)
                                                .collect(Collectors.toList())
                                ));
                            }
                            return Pair.of(command, null);
                        }
                        /* Not enough arguments */
                        return Pair.of(null, new InvalidSyntaxException(
                                this.commandManager.getCommandSyntaxFormatter()
                                        .apply(Objects.requireNonNull(
                                                child.getValue()
                                                        .getOwningCommand())
                                                .getArguments(), child),
                                commandContext.getSender(), this.getChain(root)
                                .stream()
                                .filter(node -> node.getValue() != null)
                                .map(Node::getValue)
                                .collect(Collectors.toList())
                        ));
                    } else {
                        /* The child is not a leaf, but may have an intermediary executor, attempt to use it */
                        if (root.getValue() != null && root.getValue().getOwningCommand() != null) {
                            final Command<C> command = root.getValue().getOwningCommand();
                            if (!this.getCommandManager().hasPermission(
                                    commandContext.getSender(),
                                    command.getCommandPermission()
                            )) {
                                return Pair.of(null, new NoPermissionException(
                                        command.getCommandPermission(),
                                        commandContext.getSender(),
                                        this.getChain(root)
                                                .stream()
                                                .filter(node -> node.getValue() != null)
                                                .map(Node::getValue)
                                                .collect(Collectors.toList())
                                ));
                            }
                            return Pair.of(command, null);
                        }
                        /* Child does not have a command and so we cannot proceed */
                        return Pair.of(null, new InvalidSyntaxException(
                                this.commandManager.getCommandSyntaxFormatter()
                                        .apply(parsedArguments, root),
                                commandContext.getSender(), this.getChain(root)
                                .stream()
                                .filter(node -> node.getValue() != null)
                                .map(Node::getValue)
                                .collect(Collectors.toList())
                        ));
                    }
                }

                final CommandArgument<C, ?> argument = child.getValue();
                final CommandContext.ArgumentTiming argumentTiming = commandContext.createTiming(argument);

                // START: Parsing
                argumentTiming.setStart(System.nanoTime());
                final ArgumentParseResult<?> result;
                final ArgumentParseResult<Boolean> preParseResult = child.getValue().preprocess(
                        commandContext,
                        commandQueue
                );
                if (!preParseResult.getFailure().isPresent() && preParseResult.getParsedValue().orElse(false)) {
                    commandContext.setCurrentArgument(argument);
                    result = argument.getParser().parse(commandContext, commandQueue);
                } else {
                    result = preParseResult;
                }
                argumentTiming.setEnd(System.nanoTime(), result.getFailure().isPresent());
                // END: Parsing

                if (result.getParsedValue().isPresent()) {
                    commandContext.store(child.getValue().getName(), result.getParsedValue().get());
                    if (child.isLeaf()) {
                        if (commandQueue.isEmpty()) {
                            return Pair.of(this.cast(child.getValue().getOwningCommand()), null);
                        } else {
                            /* Too many arguments. We have a unique path, so we can send the entire context */
                            return Pair.of(null, new InvalidSyntaxException(
                                    this.commandManager.getCommandSyntaxFormatter()
                                            .apply(parsedArguments, child),
                                    commandContext.getSender(), this.getChain(root)
                                    .stream()
                                    .filter(node -> node.getValue() != null)
                                    .map(Node::getValue)
                                    .collect(Collectors.toList())
                            ));
                        }
                    } else {
                        parsedArguments.add(child.getValue());
                        return this.parseCommand(parsedArguments, commandContext, commandQueue, child);
                    }
                } else if (result.getFailure().isPresent()) {
                    return Pair.of(null, new ArgumentParseException(
                            result.getFailure().get(), commandContext.getSender(),
                            this.getChain(child)
                                    .stream()
                                    .filter(node -> node.getValue() != null)
                                    .map(Node::getValue)
                                    .collect(Collectors.toList())
                    ));
                }
            }
        }

        return Pair.of(null, null);
    }

    /**
     * Get suggestions from the input queue
     *
     * @param context      Context instance
     * @param commandQueue Input queue
     * @return String suggestions. These should be filtered based on {@link String#startsWith(String)}
     */
    public @NonNull List<@NonNull String> getSuggestions(
            final @NonNull CommandContext<C> context,
            final @NonNull Queue<@NonNull String> commandQueue
    ) {
        return getSuggestions(context, commandQueue, this.internalTree);
    }

    @SuppressWarnings("MixedMutabilityReturnType")
    private @NonNull List<@NonNull String> getSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull Queue<@NonNull String> commandQueue,
            final @NonNull Node<@Nullable CommandArgument<C, ?>> root
    ) {
        /* If the sender isn't allowed to access the root node, no suggestions are needed */
        if (this.isPermitted(commandContext.getSender(), root) != null) {
            return Collections.emptyList();
        }
        final List<Node<CommandArgument<C, ?>>> children = root.getChildren();

        /* Calculate a list of arguments that are static literals */
        final List<Node<CommandArgument<C, ?>>> staticArguments = children.stream()
                .filter(n -> n.getValue() instanceof StaticArgument)
                .collect(Collectors.toList());

        /*
         * Try to see if any of the static literals can be parsed (matches exactly)
         * If so, enter that node of the command tree for deeper suggestions
         */
        if (!staticArguments.isEmpty() && !commandQueue.isEmpty()) {
            final Queue<String> commandQueueCopy = new LinkedList<String>(commandQueue);
            final Iterator<Node<CommandArgument<C, ?>>> childIterator = staticArguments.iterator();
            if (childIterator.hasNext()) {
                while (childIterator.hasNext()) {
                    final Node<CommandArgument<C, ?>> child = childIterator.next();
                    if (child.getValue() != null) {
                        commandContext.setCurrentArgument(child.getValue());
                        final ArgumentParseResult<?> result = child.getValue().getParser().parse(
                                commandContext,
                                commandQueue
                        );
                        if (result.getParsedValue().isPresent()) {
                            // If further arguments are specified, dive into this literal
                            if (!commandQueue.isEmpty()) {
                                return this.getSuggestions(commandContext, commandQueue, child);
                            }

                            // We've already matched one exactly, no use looking further
                            break;
                        }
                    }
                }
            }

            // Restore original queue
            commandQueue.clear();
            commandQueue.addAll(commandQueueCopy);
        }

        /* Calculate suggestions for the literal arguments */
        final List<String> suggestions = new LinkedList<>();
        if (commandQueue.size() <= 1) {
            final String literalValue = stringOrEmpty(commandQueue.peek());
            for (final Node<CommandArgument<C, ?>> argument : staticArguments) {
                if (this.isPermitted(commandContext.getSender(), argument) != null) {
                    continue;
                }
                commandContext.setCurrentArgument(argument.getValue());
                final List<String> suggestionsToAdd = argument.getValue().getSuggestionsProvider()
                        .apply(commandContext, literalValue);
                for (String suggestion : suggestionsToAdd) {
                    if (suggestion.equals(literalValue) || !suggestion.startsWith(literalValue)) {
                        continue;
                    }
                    suggestions.add(suggestion);
                }
            }
        }

        /* Calculate suggestions for the variable argument, if one exists */
        for (final Node<CommandArgument<C, ?>> child : root.getChildren()) {
            if (child.getValue() != null && !(child.getValue() instanceof StaticArgument)) {
                suggestions.addAll(this.suggestionsForDynamicArgument(commandContext, commandQueue, child));
            }
        }

        return suggestions;
    }

    private @NonNull List<@NonNull String> suggestionsForDynamicArgument(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull Queue<@NonNull String> commandQueue,
            final @NonNull Node<@Nullable CommandArgument<C, ?>> child
    ) {
        /* If argument has no value associated, break out early */
        if (child.getValue() == null) {
            return Collections.emptyList();
        }

        /* When we get in here, we need to treat compound arguments a little differently */
        if (child.getValue() instanceof CompoundArgument) {
            @SuppressWarnings("unchecked") final CompoundArgument<?, C, ?> compoundArgument = (CompoundArgument<?, C, ?>) child
                    .getValue();
            /* See how many arguments it requires */
            final int requiredArguments = compoundArgument.getParserTuple().getSize();
            /* Figure out whether we even need to care about this */
            if (commandQueue.size() <= requiredArguments) {
                /* Attempt to pop as many arguments from the stack as possible */
                for (int i = 0; i < requiredArguments - 1 && commandQueue.size() > 1; i++) {
                    commandQueue.remove();
                    commandContext.store(PARSING_ARGUMENT_KEY, i + 2);
                }
            }
        } else if (child.getValue().getParser() instanceof FlagArgument.FlagArgumentParser) {

            /*
             * Use the flag argument parser to deduce what flag is being suggested right now
             * If empty, then no flag value is being typed, and the different flag options should
             * be suggested instead.
             *
             * Note: the method parseCurrentFlag() will remove all but the last element from
             * the queue!
             */
            @SuppressWarnings("unchecked")
            FlagArgument.FlagArgumentParser<C> parser = (FlagArgument.FlagArgumentParser<C>) child.getValue().getParser();
            Optional<String> lastFlag = parser.parseCurrentFlag(commandContext, commandQueue);
            lastFlag.ifPresent(s -> commandContext.store(FlagArgument.FLAG_META_KEY, s));
        } else if (GenericTypeReflector.erase(child.getValue().getValueType().getType()).isArray()) {
            while (commandQueue.size() > 1) {
                commandQueue.remove();
            }
        } else if (commandQueue.size() <= child.getValue().getParser().getRequestedArgumentCount()) {
            for (int i = 0; i < child.getValue().getParser().getRequestedArgumentCount() - 1
                    && commandQueue.size() > 1; i++) {
                commandContext.store(
                        String.format("%s_%d", child.getValue().getName(), i),
                        commandQueue.remove()
                );
            }
        }

        if (commandQueue.isEmpty()) {
            return Collections.emptyList();
        } else if (child.isLeaf() && commandQueue.size() < 2) {
            commandContext.setCurrentArgument(child.getValue());
            return child.getValue().getSuggestionsProvider().apply(commandContext, commandQueue.peek());
        } else if (child.isLeaf()) {
            if (child.getValue() instanceof CompoundArgument) {
                final String last = ((LinkedList<String>) commandQueue).getLast();
                commandContext.setCurrentArgument(child.getValue());
                return child.getValue().getSuggestionsProvider().apply(commandContext, last);
            }
            return Collections.emptyList();
        } else if (commandQueue.peek().isEmpty()) {
            commandContext.setCurrentArgument(child.getValue());
            return child.getValue().getSuggestionsProvider().apply(commandContext, commandQueue.remove());
        }

        // Store original input command queue before the parsers below modify it
        final Queue<String> commandQueueOriginal = new LinkedList<>(commandQueue);

        // START: Preprocessing
        final ArgumentParseResult<Boolean> preParseResult = child.getValue().preprocess(
                commandContext,
                commandQueue
        );
        final boolean preParseSuccess = !preParseResult.getFailure().isPresent()
                && preParseResult.getParsedValue().orElse(false);
        // END: Preprocessing

        if (preParseSuccess) {
            // START: Parsing
            commandContext.setCurrentArgument(child.getValue());
            final ArgumentParseResult<?> result = child.getValue().getParser().parse(commandContext, commandQueue);
            if (result.getParsedValue().isPresent() && !commandQueue.isEmpty()) {
                commandContext.store(child.getValue().getName(), result.getParsedValue().get());
                return this.getSuggestions(commandContext, commandQueue, child);
            }
            // END: Parsing
        }

        // Restore original command input queue
        commandQueue.clear();
        commandQueue.addAll(commandQueueOriginal);

        // Fallback: use suggestion provider of argument
        commandContext.setCurrentArgument(child.getValue());
        return child.getValue().getSuggestionsProvider().apply(commandContext, stringOrEmpty(commandQueue.peek()));
    }

    private @NonNull String stringOrEmpty(final @Nullable String string) {
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
    @SuppressWarnings("unchecked")
    public void insertCommand(final @NonNull Command<C> command) {
        synchronized (this.commandLock) {
            Node<CommandArgument<C, ?>> node = this.internalTree;
            for (final CommandArgument<C, ?> argument : command.getArguments()) {
                Node<CommandArgument<C, ?>> tempNode = node.getChild(argument);
                if (tempNode == null) {
                    tempNode = node.addChild(argument);
                } else if (argument instanceof StaticArgument && tempNode.getValue() != null) {
                    for (final String alias : ((StaticArgument<C>) argument).getAliases()) {
                        ((StaticArgument<C>) tempNode.getValue()).registerAlias(alias);
                    }
                }
                if (node.children.size() > 0) {
                    node.children.sort(Comparator.comparing(Node::getValue));
                }
                tempNode.setParent(node);
                node = tempNode;
            }
            if (node.getValue() != null) {
                if (node.getValue().getOwningCommand() != null) {
                    throw new IllegalStateException(String.format(
                            "Duplicate command chains detected. Node '%s' already has an owning command (%s)",
                            node.toString(), node.getValue().getOwningCommand().toString()
                    ));
                }
                node.getValue().setOwningCommand(command);
            }
            // Verify the command structure every time we add a new command
            this.verifyAndRegister();
        }
    }

    private @Nullable CommandPermission isPermitted(
            final @NonNull C sender,
            final @NonNull Node<@Nullable CommandArgument<C, ?>> node
    ) {
        final CommandPermission permission = (CommandPermission) node.nodeMeta.get("permission");
        if (permission != null) {
            return this.commandManager.hasPermission(sender, permission) ? null : permission;
        }
        if (node.isLeaf()) {
            return this.commandManager.hasPermission(
                    sender,
                    Objects.requireNonNull(
                            Objects.requireNonNull(
                                    node.value,
                                    "node.value"
                            ).getOwningCommand(),
                            "owning command"
                    ).getCommandPermission()
            )
                    ? null : Objects.requireNonNull(node.value.getOwningCommand(), "owning command")
                    .getCommandPermission();
        }
        /*
          if any of the children would permit the execution, then the sender has a valid
           chain to execute, and so we allow them to execute the root
         */
        final List<CommandPermission> missingPermissions = new LinkedList<>();
        for (final Node<CommandArgument<C, ?>> child : node.getChildren()) {
            final CommandPermission check = this.isPermitted(sender, child);
            if (check == null) {
                return null;
            } else {
                missingPermissions.add(check);
            }
        }

        return OrPermission.of(missingPermissions);
    }

    /**
     * Go through all commands and register them, and verify the
     * command tree contracts
     */
    public void verifyAndRegister() {
        // All top level commands are supposed to be registered in the command manager
        this.internalTree.children.stream().map(Node::getValue).forEach(commandArgument -> {
            if (!(commandArgument instanceof StaticArgument)) {
                throw new IllegalStateException("Top level command argument cannot be a variable");
            }
        });

        this.checkAmbiguity(this.internalTree);

        // Verify that all leaf nodes have command registered
        this.getLeaves(this.internalTree).forEach(leaf -> {
            if (leaf.getOwningCommand() == null) {
                throw new NoCommandInLeafException(leaf);
            } else {
                final Command<C> owningCommand = leaf.getOwningCommand();
                this.commandManager.getCommandRegistrationHandler().registerCommand(owningCommand);
            }
        });

        // Register command permissions
        this.getLeavesRaw(this.internalTree).forEach(node -> {
            // noinspection all
            final CommandPermission commandPermission = node.getValue().getOwningCommand().getCommandPermission();
            /* All leaves must necessarily have an owning command */
            node.nodeMeta.put("permission", commandPermission);
            // Get chain and order it tail->head then skip the tail (leaf node)
            List<Node<CommandArgument<C, ?>>> chain = this.getChain(node);
            Collections.reverse(chain);
            chain = chain.subList(1, chain.size());
            // Go through all nodes from the tail upwards until a collision occurs
            for (final Node<CommandArgument<C, ?>> commandArgumentNode : chain) {
                final CommandPermission existingPermission = (CommandPermission) commandArgumentNode.nodeMeta
                        .get("permission");

                CommandPermission permission;
                if (existingPermission != null) {
                    permission = OrPermission.of(Arrays.asList(commandPermission, existingPermission));
                } else {
                    permission = commandPermission;
                }

                /* Now also check if there's a command handler attached to an upper level node */
                if (commandArgumentNode.getValue() != null && commandArgumentNode
                        .getValue()
                        .getOwningCommand() != null) {
                    final Command<C> command = commandArgumentNode.getValue().getOwningCommand();
                    if (this
                            .getCommandManager()
                            .getSetting(CommandManager.ManagerSettings.ENFORCE_INTERMEDIARY_PERMISSIONS)) {
                        permission = command.getCommandPermission();
                    } else {
                        permission = OrPermission.of(Arrays.asList(permission, command.getCommandPermission()));
                    }
                }

                commandArgumentNode.nodeMeta.put("permission", permission);
            }
        });
    }

    private void checkAmbiguity(final @NonNull Node<@Nullable CommandArgument<C, ?>> node) throws
            AmbiguousNodeException {
        if (node.isLeaf()) {
            return;
        }

        // List of child nodes that are not static arguments, but (parsed) variable ones
        final List<Node<CommandArgument<C, ?>>> childVariableArguments = node.children.stream()
                .filter(n -> (n.getValue() != null && !(n.getValue() instanceof StaticArgument)))
                .collect(Collectors.toList());

        // If more than one child node exists with a variable argument, fail
        if (childVariableArguments.size() > 1) {
            Node<CommandArgument<C, ?>> child = childVariableArguments.get(0);
            throw new AmbiguousNodeException(
                    node.getValue(),
                    child.getValue(),
                    node.getChildren()
                            .stream()
                            .filter(n -> n.getValue() != null)
                            .map(Node::getValue).collect(Collectors.toList())
            );
        }

        // List of child nodes that are static arguments, with fixed values
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final List<Node<StaticArgument<?>>> childStaticArguments = node.children.stream()
                .filter(n -> n.getValue() instanceof StaticArgument)
                .map(n -> (Node<StaticArgument<?>>) ((Node) n))
                .collect(Collectors.toList());

        // Check none of the static arguments are equal to another one
        // This is done by filling a set and checking there are no duplicates
        final Set<String> checkedLiterals = new HashSet<>();
        for (final Node<StaticArgument<?>> child : childStaticArguments) {
            for (final String nameOrAlias : child.getValue().getAliases()) {
                if (!checkedLiterals.add(nameOrAlias)) {
                    // Same literal value, ambiguity detected
                    throw new AmbiguousNodeException(
                            node.getValue(),
                            child.getValue(),
                            node.getChildren()
                                    .stream()
                                    .filter(n -> n.getValue() != null)
                                    .map(Node::getValue).collect(Collectors.toList())
                    );
                }
            }
        }

        // Recursively check child nodes as well
        node.children.forEach(this::checkAmbiguity);
    }

    private @NonNull List<@NonNull Node<@Nullable CommandArgument<C, ?>>> getLeavesRaw(
            final @NonNull Node<@Nullable CommandArgument<C, ?>> node
    ) {
        final List<Node<CommandArgument<C, ?>>> leaves = new LinkedList<>();
        if (node.isLeaf()) {
            if (node.getValue() != null) {
                leaves.add(node);
            }
        } else {
            node.children.forEach(child -> leaves.addAll(getLeavesRaw(child)));
        }
        return leaves;
    }

    private @NonNull List<@NonNull CommandArgument<C, ?>> getLeaves(
            final @NonNull Node<@NonNull CommandArgument<C, ?>> node
    ) {
        final List<CommandArgument<C, ?>> leaves = new LinkedList<>();
        if (node.isLeaf()) {
            if (node.getValue() != null) {
                leaves.add(node.getValue());
            }
        } else {
            node.children.forEach(child -> leaves.addAll(getLeaves(child)));
        }
        return leaves;
    }

    private @NonNull List<@NonNull Node<@Nullable CommandArgument<C, ?>>> getChain(
            final @Nullable Node<@Nullable CommandArgument<C, ?>> end
    ) {
        final List<Node<CommandArgument<C, ?>>> chain = new LinkedList<>();
        Node<CommandArgument<C, ?>> tail = end;
        while (tail != null) {
            chain.add(tail);
            tail = tail.getParent();
        }
        Collections.reverse(chain);
        return chain;
    }

    private @Nullable Command<C> cast(final @Nullable Command<C> command) {
        return command;
    }

    /**
     * Get an immutable collection containing all of the root nodes
     * in the tree
     *
     * @return Root nodes
     */
    public @NonNull Collection<@NonNull Node<@Nullable CommandArgument<C, ?>>> getRootNodes() {
        return this.internalTree.getChildren();
    }

    /**
     * Get a named root node, if it exists
     *
     * @param name Root node name
     * @return Root node, or {@code null}
     */
    public @Nullable Node<@Nullable CommandArgument<C, ?>> getNamedNode(final @Nullable String name) {
        for (final Node<CommandArgument<C, ?>> node : this.getRootNodes()) {
            if (node.getValue() != null && node.getValue() instanceof StaticArgument) {
                @SuppressWarnings("unchecked") final StaticArgument<C> staticArgument = (StaticArgument<C>) node.getValue();
                for (final String alias : staticArgument.getAliases()) {
                    if (alias.equalsIgnoreCase(name)) {
                        return node;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get the command manager
     *
     * @return Command manager
     */
    public @NonNull CommandManager<C> getCommandManager() {
        return this.commandManager;
    }

    /**
     * Very simple tree structure
     *
     * @param <T> Node value type
     */
    public static final class Node<T> {

        private final Map<String, Object> nodeMeta = new HashMap<>();
        private final List<Node<T>> children = new LinkedList<>();
        private final T value;
        private Node<T> parent;

        private Node(final @Nullable T value) {
            this.value = value;
        }

        /**
         * Get an immutable copy of the node's child list
         *
         * @return Children
         */
        public @NonNull List<@NonNull Node<@Nullable T>> getChildren() {
            return Collections.unmodifiableList(this.children);
        }

        private @NonNull Node<@Nullable T> addChild(final @NonNull T child) {
            final Node<T> node = new Node<>(child);
            this.children.add(node);
            return node;
        }

        private @Nullable Node<@Nullable T> getChild(final @NonNull T type) {
            for (final Node<T> child : this.children) {
                if (type.equals(child.getValue())) {
                    return child;
                }
            }
            return null;
        }

        /**
         * Check if the node is a leaf node
         *
         * @return {@code true} if the node is a leaf node, else {@code false}
         */
        public boolean isLeaf() {
            return this.children.isEmpty();
        }

        /**
         * Get the node meta instance
         *
         * @return Node meta
         */
        public @NonNull Map<@NonNull String, @NonNull Object> getNodeMeta() {
            return this.nodeMeta;
        }

        /**
         * Get the node value
         *
         * @return Node value
         */
        public @Nullable T getValue() {
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

        /**
         * Get the parent node
         *
         * @return Parent node
         */
        public @Nullable Node<@Nullable T> getParent() {
            return this.parent;
        }

        /**
         * Set the parent node
         *
         * @param parent new parent node
         */
        public void setParent(final @Nullable Node<@Nullable T> parent) {
            this.parent = parent;
        }

        @Override
        public String toString() {
            return "Node{value=" + value + '}';
        }

    }

}
