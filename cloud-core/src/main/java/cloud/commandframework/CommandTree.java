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
import cloud.commandframework.permission.CommandPermission;
import cloud.commandframework.permission.OrPermission;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
     * @throws NoSuchCommandException If there is no command matching the input
     * @throws NoPermissionException  If the sender lacks permission to execute the command
     * @throws InvalidSyntaxException If the command syntax is invalid
     */
    public @NonNull Optional<Command<C>> parse(final @NonNull CommandContext<C> commandContext,
                                               final @NonNull Queue<@NonNull String> args) throws
            NoSuchCommandException, NoPermissionException, InvalidSyntaxException {
        final Optional<Command<C>> commandOptional = parseCommand(new ArrayList<>(),
                                                                  commandContext,
                                                                  args,
                                                                  this.internalTree);
        commandOptional.flatMap(Command::getSenderType).ifPresent(requiredType -> {
            if (!requiredType.isAssignableFrom(commandContext.getSender().getClass())) {
                throw new InvalidCommandSenderException(commandContext.getSender(), requiredType, Collections.emptyList());
            }
        });
        return commandOptional;
    }

    private Optional<Command<C>> parseCommand(final @NonNull List<@NonNull CommandArgument<C, ?>> parsedArguments,
                                              final @NonNull CommandContext<C> commandContext,
                                              final @NonNull Queue<@NonNull String> commandQueue,
                                              final @NonNull Node<@Nullable CommandArgument<C, ?>> root) {
        CommandPermission permission = this.isPermitted(commandContext.getSender(), root);
        if (permission != null) {
            throw new NoPermissionException(permission, commandContext.getSender(), this.getChain(root)
                                                                                        .stream()
                                                                                        .map(Node::getValue)
                                                                                        .collect(Collectors.toList()));
        }

        final Optional<Command<C>> parsedChild = this.attemptParseUnambiguousChild(parsedArguments,
                                                                                   commandContext,
                                                                                   root,
                                                                                   commandQueue);
        // noinspection all
        if (parsedChild != null) {
            return parsedChild;
        }

        /* There are 0 or more static arguments as children. No variable child arguments are present */
        if (root.children.isEmpty()) {
            /* We are at the bottom. Check if there's a command attached, in which case we're done */
            if (root.getValue() != null && root.getValue().getOwningCommand() != null) {
                if (commandQueue.isEmpty()) {
                    return Optional.ofNullable(this.cast(root.getValue().getOwningCommand()));
                } else {
                    /* Too many arguments. We have a unique path, so we can send the entire context */
                    throw new InvalidSyntaxException(this.commandManager.getCommandSyntaxFormatter()
                                                                        .apply(parsedArguments, root),
                                                     commandContext.getSender(), this.getChain(root)
                                                                                     .stream()
                                                                                     .map(Node::getValue)
                                                                                     .collect(Collectors.toList()));
                }
            } else {
                /* Too many arguments. We have a unique path, so we can send the entire context */
                throw new InvalidSyntaxException(this.commandManager.getCommandSyntaxFormatter()
                                                                    .apply(parsedArguments, root),
                                                 commandContext.getSender(), this.getChain(root)
                                                                                 .stream()
                                                                                 .map(Node::getValue)
                                                                                 .collect(Collectors.toList()));
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
                throw new NoSuchCommandException(commandContext.getSender(),
                                                 getChain(root).stream().map(Node::getValue).collect(Collectors.toList()),
                                                 stringOrEmpty(commandQueue.peek()));
            }
            /* If we couldn't match a child, check if there's a command attached and execute it */
            if (root.getValue() != null && root.getValue().getOwningCommand() != null) {
                final Command<C> command = root.getValue().getOwningCommand();
                if (!this.getCommandManager().hasPermission(commandContext.getSender(),
                                                            command.getCommandPermission())) {
                    throw new NoPermissionException(command.getCommandPermission(),
                                                    commandContext.getSender(),
                                                    this.getChain(root)
                                                        .stream()
                                                        .map(Node::getValue)
                                                        .collect(Collectors.toList()));
                }
                return Optional.of(root.getValue().getOwningCommand());
            }
            /* We know that there's no command and we also cannot match any of the children */
            throw new InvalidSyntaxException(this.commandManager.getCommandSyntaxFormatter()
                                                                .apply(parsedArguments, root),
                                             commandContext.getSender(), this.getChain(root)
                                                                             .stream()
                                                                             .map(Node::getValue)
                                                                             .collect(Collectors.toList()));
        }
    }

    private @Nullable Optional<Command<C>>
    attemptParseUnambiguousChild(final @NonNull List<@NonNull CommandArgument<C, ?>> parsedArguments,
                                 final @NonNull CommandContext<C> commandContext,
                                 final @NonNull Node<@Nullable CommandArgument<C, ?>> root,
                                 final @NonNull Queue<String> commandQueue) {
        CommandPermission permission;
        final List<Node<CommandArgument<C, ?>>> children = root.getChildren();
        if (children.size() == 1 && !(children.get(0).getValue() instanceof StaticArgument)) {
            // The value has to be a variable
            final Node<CommandArgument<C, ?>> child = children.get(0);
            permission = this.isPermitted(commandContext.getSender(), child);
            if (permission != null) {
                throw new NoPermissionException(permission, commandContext.getSender(), this.getChain(child)
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
                        /* The child is not a leaf, but may have an intermediary executor, attempt to use it */
                        if (root.getValue() != null && root.getValue().getOwningCommand() != null) {
                            final Command<C> command = root.getValue().getOwningCommand();
                            if (!this.getCommandManager().hasPermission(commandContext.getSender(),
                                                                        command.getCommandPermission())) {
                                throw new NoPermissionException(command.getCommandPermission(),
                                                                commandContext.getSender(),
                                                                this.getChain(root)
                                                                    .stream()
                                                                    .map(Node::getValue)
                                                                    .collect(Collectors.toList()));
                            }
                            return Optional.of(command);
                        }
                        /* Not enough arguments */
                        throw new InvalidSyntaxException(this.commandManager.getCommandSyntaxFormatter()
                                                                            .apply(Objects.requireNonNull(
                                                                                    child.getValue().getOwningCommand())
                                                                                          .getArguments(), child),
                                                         commandContext.getSender(), this.getChain(root)
                                                                                         .stream()
                                                                                         .map(Node::getValue)
                                                                                         .collect(Collectors.toList()));
                    } else {
                        /* The child is not a leaf, but may have an intermediary executor, attempt to use it */
                        if (root.getValue() != null && root.getValue().getOwningCommand() != null) {
                            final Command<C> command = root.getValue().getOwningCommand();
                            if (!this.getCommandManager().hasPermission(commandContext.getSender(),
                                                                        command.getCommandPermission())) {
                                throw new NoPermissionException(command.getCommandPermission(),
                                                                commandContext.getSender(),
                                                                this.getChain(root)
                                                                    .stream()
                                                                    .map(Node::getValue)
                                                                    .collect(Collectors.toList()));
                            }
                            return Optional.of(command);
                        }
                        /* Child does not have a command and so we cannot proceed */
                        throw new InvalidSyntaxException(this.commandManager.getCommandSyntaxFormatter()
                                                                            .apply(parsedArguments, root),
                                                         commandContext.getSender(), this.getChain(root)
                                                                                         .stream()
                                                                                         .map(Node::getValue)
                                                                                         .collect(Collectors.toList()));
                    }
                }

                final CommandArgument<C, ?> argument = child.getValue();
                final CommandContext.ArgumentTiming argumentTiming = commandContext.createTiming(argument);

                argumentTiming.setStart(System.nanoTime());
                final ArgumentParseResult<?> result = argument.getParser().parse(commandContext, commandQueue);
                argumentTiming.setEnd(System.nanoTime(), result.getFailure().isPresent());

                if (result.getParsedValue().isPresent()) {
                    commandContext.store(child.getValue().getName(), result.getParsedValue().get());
                    if (child.isLeaf()) {
                        if (commandQueue.isEmpty()) {
                            return Optional.ofNullable(this.cast(child.getValue().getOwningCommand()));
                        } else {
                            /* Too many arguments. We have a unique path, so we can send the entire context */
                            throw new InvalidSyntaxException(this.commandManager.getCommandSyntaxFormatter()
                                                                                .apply(parsedArguments, child),
                                                             commandContext.getSender(), this.getChain(root)
                                                                                             .stream()
                                                                                             .map(Node::getValue)
                                                                                             .collect(
                                                                                                     Collectors.toList()));
                        }
                    } else {
                        parsedArguments.add(child.getValue());
                        return this.parseCommand(parsedArguments, commandContext, commandQueue, child);
                    }
                } else if (result.getFailure().isPresent()) {
                    throw new ArgumentParseException(result.getFailure().get(), commandContext.getSender(),
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
    public @NonNull List<@NonNull String> getSuggestions(final @NonNull CommandContext<C> context,
                                                         final @NonNull Queue<@NonNull String> commandQueue) {
        return getSuggestions(context, commandQueue, this.internalTree);
    }

    private @NonNull List<@NonNull String> getSuggestions(final @NonNull CommandContext<C> commandContext,
                                                          final @NonNull Queue<@NonNull String> commandQueue,
                                                          final @NonNull Node<@Nullable CommandArgument<C, ?>> root) {

        /* If the sender isn't allowed to access the root node, no suggestions are needed */
        if (this.isPermitted(commandContext.getSender(), root) != null) {
            return Collections.emptyList();
        }
        final List<Node<CommandArgument<C, ?>>> children = root.getChildren();
        if (children.size() == 1 && !(children.get(0).getValue() instanceof StaticArgument)) {
            // The value has to be a variable
            final Node<CommandArgument<C, ?>> child = children.get(0);

            // START: Compound arguments
            /* When we get in here, we need to treat compound arguments a little differently */
            if (child.getValue() instanceof CompoundArgument) {
                @SuppressWarnings("unchecked")
                final CompoundArgument<?, C, ?> compoundArgument = (CompoundArgument<?, C, ?>) child.getValue();
                /* See how many arguments it requires */
                final int requiredArguments = compoundArgument.getParserTuple().getSize();
                /* Figure out whether we even need to care about this */
                if (commandQueue.size() <= requiredArguments) {
                    /* Attempt to pop as many arguments from the stack as possible */
                    for (int i = 0; i < requiredArguments - 1 && commandQueue.size() > 1; i++) {
                        commandQueue.remove();
                        commandContext.store("__parsing_argument__", i + 2);
                    }
                }
            }
            // END: Compound arguments

            // START: Flags
            if (child.getValue() instanceof FlagArgument) {
                /* Remove all but last */
                while (commandQueue.size() > 1) {
                    commandContext.store(FlagArgument.FLAG_META, commandQueue.remove());
                }
            }
            // END: Flags

            if (child.getValue() != null) {
                if (commandQueue.isEmpty()) {
                    return Collections.emptyList();
                    // return child.getValue().getParser().suggestions(commandContext, "");
                } else if (child.isLeaf() && commandQueue.size() < 2) {
                    return child.getValue().getSuggestionsProvider().apply(commandContext, commandQueue.peek());
                } else if (child.isLeaf()) {
                    if (child.getValue() instanceof CompoundArgument) {
                        final String last = ((LinkedList<String>) commandQueue).getLast();
                        return child.getValue().getSuggestionsProvider().apply(commandContext, last);
                    }
                    return Collections.emptyList();
                } else if (commandQueue.peek().isEmpty()) {
                    return child.getValue().getSuggestionsProvider().apply(commandContext, commandQueue.remove());
                }
                final ArgumentParseResult<?> result = child.getValue().getParser().parse(commandContext, commandQueue);
                if (result.getParsedValue().isPresent()) {
                    commandContext.store(child.getValue().getName(), result.getParsedValue().get());
                    return this.getSuggestions(commandContext, commandQueue, child);
                } else if (result.getFailure().isPresent()) {
                    return child.getValue().getSuggestionsProvider().apply(commandContext, commandQueue.peek());
                }
            }
        }
        /* There are 0 or more static arguments as children. No variable child arguments are present */
        if (children.isEmpty() || commandQueue.isEmpty()) {
            return Collections.emptyList();
        } else {
            final Iterator<Node<CommandArgument<C, ?>>> childIterator = root.getChildren().iterator();
            if (childIterator.hasNext()) {
                while (childIterator.hasNext()) {
                    final Node<CommandArgument<C, ?>> child = childIterator.next();
                    if (child.getValue() != null) {
                        final ArgumentParseResult<?> result = child.getValue().getParser().parse(commandContext, commandQueue);
                        if (result.getParsedValue().isPresent()) {
                            return this.getSuggestions(commandContext, commandQueue, child);
                        }
                    }
                }
            }
            final List<String> suggestions = new LinkedList<>();
            for (final Node<CommandArgument<C, ?>> argument : root.getChildren()) {
                if (argument.getValue() == null || this.isPermitted(commandContext.getSender(), argument) != null) {
                    continue;
                }
                suggestions.addAll(argument.getValue().getSuggestionsProvider()
                                           .apply(commandContext, stringOrEmpty(commandQueue.peek())));
            }
            return suggestions;
        }
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

    private @Nullable CommandPermission isPermitted(final @NonNull C sender,
                                                    final @NonNull Node<@Nullable CommandArgument<C, ?>> node) {
        final CommandPermission permission = (CommandPermission) node.nodeMeta.get("permission");
        if (permission != null) {
            return this.commandManager.hasPermission(sender, permission) ? null : permission;
        }
        if (node.isLeaf()) {
            return this.commandManager.hasPermission(sender,
                                                     Objects.requireNonNull(
                                                             Objects.requireNonNull(node.value, "node.value").getOwningCommand(),
                                                             "owning command").getCommandPermission())
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
                final CommandPermission existingPermission = (CommandPermission) commandArgumentNode.nodeMeta.get("permission");

                CommandPermission permission;
                if (existingPermission != null) {
                    permission = OrPermission.of(Arrays.asList(commandPermission, existingPermission));
                } else {
                    permission = commandPermission;
                }

                /* Now also check if there's a command handler attached to an upper level node */
                if (commandArgumentNode.getValue() != null && commandArgumentNode.getValue().getOwningCommand() != null) {
                    final Command<C> command = commandArgumentNode.getValue().getOwningCommand();
                    if (this.getCommandManager().getSetting(CommandManager.ManagerSettings.ENFORCE_INTERMEDIARY_PERMISSIONS)) {
                        permission = command.getCommandPermission();
                    } else {
                        permission = OrPermission.of(Arrays.asList(permission, command.getCommandPermission()));
                    }
                }

                commandArgumentNode.nodeMeta.put("permission", permission);
            }
        });
    }

    private void checkAmbiguity(final @NonNull Node<@Nullable CommandArgument<C, ?>> node) throws AmbiguousNodeException {
        if (node.isLeaf()) {
            return;
        }
        final int size = node.children.size();
        for (final Node<CommandArgument<C, ?>> child : node.children) {
            if (child.getValue() != null && !child.getValue().isRequired() && size > 1) {
                throw new AmbiguousNodeException(node.getValue(),
                                                 child.getValue(),
                                                 node.getChildren().stream().map(Node::getValue).collect(Collectors.toList()));
            }
        }
        node.children.forEach(this::checkAmbiguity);
    }

    private @NonNull List<@NonNull Node<@Nullable CommandArgument<C, ?>>>
    getLeavesRaw(final @NonNull Node<@Nullable CommandArgument<C, ?>> node) {
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

    private @NonNull List<@NonNull CommandArgument<C, ?>> getLeaves(final @NonNull Node<@NonNull CommandArgument<C, ?>> node) {
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

    private @NonNull List<@NonNull Node<@Nullable CommandArgument<C, ?>>>
    getChain(final @Nullable Node<@Nullable CommandArgument<C, ?>> end) {
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
