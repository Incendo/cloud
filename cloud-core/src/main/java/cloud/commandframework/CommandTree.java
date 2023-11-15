//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.context.ArgumentContext;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.AmbiguousNodeException;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoCommandInLeafException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.internal.CommandNode;
import cloud.commandframework.internal.SuggestionContext;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.keys.SimpleCloudKey;
import cloud.commandframework.permission.CommandPermission;
import cloud.commandframework.permission.OrPermission;
import cloud.commandframework.types.tuples.Pair;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Tree containing all commands and command paths.
 * <p>
 * All {@link Command commands} consists of unique paths made out of {@link CommandArgument arguments}.
 * These arguments may be {@link StaticArgument literals} or variables. Command may either be required
 * or optional, with the requirement that no optional argument precedes a required argument.
 * <p>
 * The {@link Command commands} are stored in this tree and the nodes of tree consists of the command
 * {@link CommandArgument arguments}. Each leaf node of the tree should contain a fully parsed
 * {@link Command}. It is thus possible to walk the tree and determine whether the supplied
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
@API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
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

    private final CommandNode<C> internalTree = new CommandNode<>(null);
    private final CommandManager<C> commandManager;

    private CommandTree(final @NonNull CommandManager<C> commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Creates a new command tree instance
     *
     * @param commandManager Command manager
     * @param <C>            Command sender type
     * @return the created command tree
     */
    public static <C> @NonNull CommandTree<C> newTree(final @NonNull CommandManager<C> commandManager) {
        return new CommandTree<>(commandManager);
    }

    /**
     * Returns the command manager that was used to create this command tree
     *
     * @return Command manager
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull CommandManager<C> commandManager() {
        return this.commandManager;
    }

    /**
     * Returns an immutable view containing of the root nodes of the command tree
     *
     * @return immutable view of the root nodes
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull Collection<@NonNull CommandNode<C>> rootNodes() {
        return this.internalTree.children();
    }

    /**
     * Returns a named root node, if it exists
     *
     * @param name root node name
     * @return the found root node, or {@code null}
     */
    @SuppressWarnings("unchecked")
    public @Nullable CommandNode<C> getNamedNode(final @Nullable String name) {
        for (final CommandNode<C> node : this.rootNodes()) {
            final CommandArgument<C, ?> argument = node.argument();
            if (!(argument instanceof StaticArgument)) {
                continue;
            }
            final StaticArgument<C> staticArgument = (StaticArgument<C>) argument;
            for (final String alias : staticArgument.getAliases()) {
                if (alias.equalsIgnoreCase(name)) {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * Attempts to parse string input into a command
     *
     * @param commandContext Command context instance
     * @param commandInput   Input
     * @return parsed command, if one could be found
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull Pair<@Nullable Command<C>, @Nullable Exception> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        // Special case for empty command trees.
        if (this.internalTree.isLeaf() && this.internalTree.component() == null) {
            return Pair.of(
                    null,
                    new NoSuchCommandException(
                            commandContext.getSender(),
                            new ArrayList<>(),
                            commandInput.peekString()
                    )
            );
        }

        final Pair<@Nullable Command<C>, @Nullable Exception> pair = this.parseCommand(
                new ArrayList<>(),
                commandContext,
                commandInput,
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
                        new ArrayList<>(command.components()),
                        command
                ));
            }
        }

        return pair;
    }

    private @NonNull Pair<@Nullable Command<C>, @Nullable Exception> parseCommand(
            final @NonNull List<@NonNull CommandComponent<C>> parsedArguments,
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput,
            final @NonNull CommandNode<C> root
    ) {
        final CommandPermission permission = this.findMissingPermission(commandContext.getSender(), root);
        if (permission != null) {
            return Pair.of(null, new NoPermissionException(
                    permission,
                    commandContext.getSender(),
                    this.getChain(root)
                            .stream()
                            .filter(node -> node.component() != null)
                            .map(CommandNode::component)
                            .collect(Collectors.toList())
            ));
        }

        final Pair<@Nullable Command<C>, @Nullable Exception> parsedChild = this.attemptParseUnambiguousChild(
                parsedArguments,
                commandContext,
                root,
                commandInput
        );
        if (parsedChild.getFirst() != null || parsedChild.getSecond() != null) {
            return parsedChild;
        }

        // There are 0 or more static arguments as children. No variable child arguments are present
        if (root.children().isEmpty()) {
            final CommandArgument<C, ?> rootArgument = root.argument();
            if (rootArgument == null || rootArgument.getOwningCommand() == null || !commandInput.isEmpty()) {
                // Too many arguments. We have a unique path, so we can send the entire context
                return Pair.of(null, new InvalidSyntaxException(
                        this.commandManager.commandSyntaxFormatter()
                                .apply(parsedArguments, root),
                        commandContext.getSender(), this.getChain(root)
                        .stream()
                        .filter(node -> node.component() != null)
                        .map(CommandNode::component)
                        .collect(Collectors.toList())
                ));
            }
            return Pair.of(rootArgument.getOwningCommand(), null);
        }

        final Iterator<CommandNode<C>> childIterator = root.children().iterator();
        if (childIterator.hasNext()) {
            while (childIterator.hasNext()) {
                final CommandNode<C> child = childIterator.next();

                if (child.argument() == null) {
                    continue;
                }

                final CommandArgument<C, ?> argument = Objects.requireNonNull(child.argument());
                final CommandComponent<C> component = Objects.requireNonNull(child.component());
                final ArgumentContext<C, ?> argumentContext = commandContext.createArgumentContext(argument);

                // Copy the current queue so that we can deduce the captured input.
                final CommandInput currentInput = commandInput.copy();

                argumentContext.markStart();
                commandContext.setCurrentArgument(argument);

                final ArgumentParseResult<?> result = argument.getParser().parse(commandContext, commandInput);
                argumentContext.markEnd();
                argumentContext.success(!result.getFailure().isPresent());

                final List<String> consumedTokens = currentInput.tokenize();
                consumedTokens.removeAll(commandInput.tokenize());
                argumentContext.consumedInput(consumedTokens);

                if (result.getParsedValue().isPresent()) {
                    parsedArguments.add(component);
                    return this.parseCommand(parsedArguments, commandContext, commandInput, child);
                } else if (result.getFailure().isPresent()) {
                    commandInput.cursor(currentInput.cursor());
                }
            }
        }

        // We could not find a match
        if (root.equals(this.internalTree)) {
            return Pair.of(null, new NoSuchCommandException(
                    commandContext.getSender(),
                    this.getChain(root).stream().map(CommandNode::component).collect(Collectors.toList()),
                    commandInput.peekString()
            ));
        }

        // If we couldn't match a child, check if there's a command attached and execute it
        final CommandArgument<C, ?> rootArgument = root.argument();
        if (rootArgument != null && rootArgument.getOwningCommand() != null && commandInput.isEmpty()) {
            final Command<C> command = rootArgument.getOwningCommand();
            if (!this.commandManager().hasPermission(
                    commandContext.getSender(),
                    command.getCommandPermission()
            )) {
                return Pair.of(null, new NoPermissionException(
                        command.getCommandPermission(),
                        commandContext.getSender(),
                        this.getChain(root)
                                .stream()
                                .filter(node -> node.component() != null)
                                .map(CommandNode::component)
                                .collect(Collectors.toList())
                ));
            }
            return Pair.of(rootArgument.getOwningCommand(), null);
        }

        // We know that there's no command, and we also cannot match any of the children
        return Pair.of(null, new InvalidSyntaxException(
                this.commandManager.commandSyntaxFormatter()
                        .apply(parsedArguments, root),
                commandContext.getSender(), this.getChain(root)
                .stream()
                .filter(node -> node.component() != null)
                .map(CommandNode::component)
                .collect(Collectors.toList())
        ));
    }

    private @NonNull Pair<@Nullable Command<C>, @Nullable Exception> attemptParseUnambiguousChild(
            final @NonNull List<@NonNull CommandComponent<C>> parsedArguments,
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandNode<C> root,
            final @NonNull CommandInput commandInput
    ) {
        final List<CommandNode<C>> children = root.children();

        // Check whether it matches any of the static arguments If so, do not attempt parsing as a dynamic argument
        if (!commandInput.isEmpty() && this.matchesLiteral(children, commandInput.peekString())) {
            return Pair.of(null, null);
        }

        // If it does not match a literal, try to find the one argument node, if it exists
        // The ambiguity check guarantees that only one will be present
        final List<CommandNode<C>> argumentNodes = children.stream()
                .filter(n -> (n.argument() != null && !(n.argument() instanceof StaticArgument)))
                .collect(Collectors.toList());
        if (argumentNodes.size() > 1) {
            throw new IllegalStateException("Unexpected ambiguity detected, number of dynamic child nodes should not exceed 1");
        } else if (argumentNodes.isEmpty()) {
            return Pair.of(null, null);
        }
        final CommandNode<C> child = argumentNodes.get(0);

        // Check if we're allowed to execute the child command. If not, exit
        final CommandPermission permission = this.findMissingPermission(commandContext.getSender(), child);
        if (!commandInput.isEmpty() && permission != null) {
            return Pair.of(null, new NoPermissionException(
                    permission,
                    commandContext.getSender(),
                    this.getChain(child)
                            .stream()
                            .filter(node -> node.component() != null)
                            .map(CommandNode::component)
                            .collect(Collectors.toList())
            ));
        }

        // If the child has no argument it cannot be executed, so we exit
        if (child.argument() == null) {
            return Pair.of(null, null);
        }

        // Flag arguments need to be skipped over, so that further defaults are handled
        if (commandInput.isEmpty() && !(child.argument() instanceof FlagArgument)) {
            final CommandComponent<C> childComponent = Objects.requireNonNull(child.component());
            if (childComponent.hasDefaultValue()) {
                return this.attemptParseUnambiguousChild(
                        parsedArguments,
                        commandContext,
                        root,
                        commandInput.appendString(childComponent.defaultValue())
                );
            } else if (!child.component().required()) {
                if (childComponent.argument().getOwningCommand() == null) {
                    // If there are multiple children with different owning commands then it's ambiguous and
                    // not allowed, therefore we're able to pick any child command, as long as we can find it
                    CommandNode<C> node = child;
                    while (!node.isLeaf()) {
                        node = node.children().get(0);
                        final CommandArgument<C, ?> nodeArgument = node.argument();
                        if (nodeArgument != null && nodeArgument.getOwningCommand() != null) {
                            childComponent.argument().setOwningCommand(nodeArgument.getOwningCommand());
                        }
                    }
                }
                return Pair.of(childComponent.argument().getOwningCommand(), null);
            } else if (child.isLeaf()) {
                final CommandArgument<C, ?> rootArgument = root.argument();
                if (rootArgument == null || rootArgument.getOwningCommand() == null) {
                    final List<CommandComponent<C>> components = Objects.requireNonNull(
                            childComponent.argument().getOwningCommand()
                    ).components();
                    return Pair.of(null, new InvalidSyntaxException(
                            this.commandManager.commandSyntaxFormatter().apply(components, child),
                            commandContext.getSender(),
                            this.getChain(root)
                                .stream()
                                .filter(node -> node.component() != null)
                                .map(CommandNode::component)
                                .collect(Collectors.toList())
                    ));
                }

                final Command<C> command = rootArgument.getOwningCommand();
                if (this.commandManager().hasPermission(commandContext.getSender(), command.getCommandPermission())) {
                    return Pair.of(command, null);
                }
                return Pair.of(null, new NoPermissionException(
                        command.getCommandPermission(),
                        commandContext.getSender(),
                        this.getChain(root)
                                .stream()
                                .filter(node -> node.component() != null)
                                .map(CommandNode::component)
                                .collect(Collectors.toList())
                ));
            } else {
                // The child is not a leaf, but may have an intermediary executor, attempt to use it
                final CommandArgument<C, ?> rootArgument = root.argument();
                if (rootArgument == null || rootArgument.getOwningCommand() == null) {
                    // Child does not have a command, and so we cannot proceed
                    return Pair.of(null, new InvalidSyntaxException(
                            this.commandManager.commandSyntaxFormatter()
                                    .apply(parsedArguments, root),
                            commandContext.getSender(),
                            this.getChain(root)
                                .stream()
                                .filter(node -> node.component() != null)
                                .map(CommandNode::component)
                                .collect(Collectors.toList())
                    ));
                }

                // If the sender has permission to use the command, then we're completely done
                final Command<C> command = Objects.requireNonNull(rootArgument.getOwningCommand());
                if (this.commandManager().hasPermission(commandContext.getSender(), command.getCommandPermission())) {
                    return Pair.of(command, null);
                }

                return Pair.of(null, new NoPermissionException(
                        command.getCommandPermission(),
                        commandContext.getSender(),
                        this.getChain(root)
                            .stream()
                            .filter(node -> node.component() != null)
                            .map(CommandNode::component)
                            .collect(Collectors.toList())
                ));
            }
        }

        final CommandArgument<C, ?> argument = Objects.requireNonNull(child.argument());
        final ArgumentParseResult<?> result = this.parseArgument(commandContext, argument, commandInput);

        if (result.getParsedValue().isPresent()) {
            commandContext.store(argument.getName(), result.getParsedValue().get());
            if (child.isLeaf()) {
                if (commandInput.isEmpty()) {
                    return Pair.of(argument.getOwningCommand(), null);
                }

                // Too many arguments. We have a unique path, so we can send the entire context
                return Pair.of(null, new InvalidSyntaxException(
                        this.commandManager.commandSyntaxFormatter().apply(parsedArguments, child),
                        commandContext.getSender(),
                        this.getChain(root)
                            .stream()
                            .filter(node -> node.component() != null)
                            .map(CommandNode::component)
                            .collect(Collectors.toList()
                        )
                ));
            }

            parsedArguments.add(Objects.requireNonNull(child.component()));
            return this.parseCommand(parsedArguments, commandContext, commandInput, child);
        } else if (result.getFailure().isPresent()) {
            return Pair.of(null, new ArgumentParseException(
                    result.getFailure().get(),
                    commandContext.getSender(),
                    this.getChain(child)
                        .stream()
                        .filter(node -> node.component() != null)
                        .map(CommandNode::component)
                        .collect(Collectors.toList())
            ));
        }

        return Pair.of(null, null);
    }

    private boolean matchesLiteral(final @NonNull List<@NonNull CommandNode<C>> children, final @NonNull String input) {
        return children.stream()
                .filter(n -> n.argument() instanceof StaticArgument)
                .map(n -> (StaticArgument<?>) n.argument())
                .filter(Objects::nonNull)
                .flatMap(arg -> Stream.concat(Stream.of(arg.getName()), arg.getAliases().stream()))
                .anyMatch(arg -> arg.equals(input));
    }

    private @NonNull ArgumentParseResult<?> parseArgument(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandArgument<C, ?> argument,
            final @NonNull CommandInput commandInput
    ) {
        final ArgumentContext<C, ?> argumentContext = commandContext.createArgumentContext(argument);
        argumentContext.markStart();

        final ArgumentParseResult<?> result;
        final ArgumentParseResult<Boolean> preParseResult = argument.preprocess(commandContext, commandInput);
        if (!preParseResult.getFailure().isPresent() && preParseResult.getParsedValue().orElse(false)) {
            commandContext.setCurrentArgument(argument);

            // Copy the current queue so that we can deduce the captured input.
            final CommandInput currentInput = commandInput.copy();

            result = argument.getParser().parse(commandContext, commandInput);

            // We remove all remaining queue, and then we'll have a list of the captured input.
            final List<String> consumedInput = currentInput.tokenize();
            consumedInput.removeAll(commandInput.tokenize());
            argumentContext.consumedInput(consumedInput);

            if (result.getFailure().isPresent()) {
                commandInput.cursor(currentInput.cursor());
            }
        } else {
            result = preParseResult;
        }

        argumentContext.markEnd();
        argumentContext.success(!result.getFailure().isPresent());

        return result;
    }

    /**
     * Returns suggestions from the input queue
     *
     * @param context      Context instance
     * @param commandInput Input
     * @return String suggestions. These should be filtered based on {@link String#startsWith(String)}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull List<@NonNull Suggestion> getSuggestions(
            final @NonNull CommandContext<C> context,
            final @NonNull CommandInput commandInput
    ) {
        final SuggestionContext<C> suggestionContext = new SuggestionContext<>(context);
        return this.getSuggestions(suggestionContext, commandInput, this.internalTree).suggestions();
    }

    @SuppressWarnings("MixedMutabilityReturnType")
    private @NonNull SuggestionContext<C> getSuggestions(
            final @NonNull SuggestionContext<C> context,
            final @NonNull CommandInput commandInput,
            final @NonNull CommandNode<C> root
    ) {
        // If the sender isn't allowed to access the root node, no suggestions are needed
        if (this.findMissingPermission(context.commandContext().getSender(), root) != null) {
            return context;
        }

        final List<CommandNode<C>> children = root.children();
        final List<CommandNode<C>> staticArguments = children.stream()
                .filter(n -> n.argument() instanceof StaticArgument)
                .collect(Collectors.toList());

        // Try to see if any of the static literals can be parsed (matches exactly)
        // If so, enter that node of the command tree for deeper suggestions
        if (!staticArguments.isEmpty() && !commandInput.isEmpty(true /* ignoringWhitespace */)) {
            final CommandInput commandInputCopy = commandInput.copy();
            for (CommandNode<C> child : staticArguments) {
                final CommandArgument<C, ?> childArgument = child.argument();
                if (childArgument == null) {
                    continue;
                }

                context.commandContext().setCurrentArgument(childArgument);
                final ArgumentParseResult<?> result = childArgument.getParser().parse(
                        context.commandContext(),
                        commandInput
                );

                if (result.getFailure().isPresent()) {
                    commandInput.cursor(commandInputCopy.cursor());
                }

                if (!result.getParsedValue().isPresent()) {
                    continue;
                }

                if (commandInput.isEmpty()) {
                    // We've already matched one exactly, no use looking further
                    break;
                }

                return this.getSuggestions(context, commandInput, child);
            }

            // Restore original queue
            commandInput.cursor(commandInputCopy.cursor());
        }

        // Calculate suggestions for the literal arguments
        if (commandInput.remainingTokens() <= 1) {
            final String literalValue = commandInput.peekString().replace(" ", "");
            for (final CommandNode<C> node : staticArguments) {
                this.addSuggestionsForLiteralArgument(context, node, literalValue);
            }
        }

        // Calculate suggestions for the variable argument, if one exists
        for (final CommandNode<C> child : root.children()) {
            if (child.argument() == null || child.argument() instanceof StaticArgument) {
                continue;
            }
            this.addSuggestionsForDynamicArgument(context, commandInput, child);
        }

        return context;
    }

    /**
     * Adds the suggestions for a static argument if they match the given {@code input}
     *
     * @param context the suggestion context
     * @param node    the node containing the static argument
     * @param input   the current input
     */
    private void addSuggestionsForLiteralArgument(
            final @NonNull SuggestionContext<C> context,
            final @NonNull CommandNode<C> node,
            final @NonNull String input
    ) {
        if (this.findMissingPermission(context.commandContext().getSender(), node) != null) {
            return;
        }
        final CommandArgument<C, ?> argument = Objects.requireNonNull(node.argument());
        context.commandContext().setCurrentArgument(argument);
        final List<Suggestion> suggestionsToAdd = argument
                .suggestionProvider()
                .suggestions(context.commandContext(), input);
        for (Suggestion suggestion : suggestionsToAdd) {
            if (suggestion.suggestion().equals(input) || !suggestion.suggestion().startsWith(input)) {
                continue;
            }
            context.addSuggestion(suggestion);
        }
    }


    @SuppressWarnings("unchecked")
    private @NonNull SuggestionContext<C> addSuggestionsForDynamicArgument(
            final @NonNull SuggestionContext<C> context,
            final @NonNull CommandInput commandInput,
            final @NonNull CommandNode<C> child
    ) {
        final CommandArgument<C, ?> argument = child.argument();
        if (argument == null) {
            return context;
        }

        if (argument instanceof CompoundArgument) {
            // If we're working with a compound argument then we attempt to pop the required arguments from the input queue.
            final CompoundArgument<?, C, ?> compoundArgument = (CompoundArgument<?, C, ?>) argument;
            this.popRequiredArguments(context.commandContext(), commandInput, compoundArgument);
        } else if (argument.getParser() instanceof FlagArgument.FlagArgumentParser) {
            // Use the flag argument parser to deduce what flag is being suggested right now
            // If empty, then no flag value is being typed, and the different flag options should
            // be suggested instead.
            final FlagArgument.FlagArgumentParser<C> parser = (FlagArgument.FlagArgumentParser<C>) argument.getParser();
            final Optional<String> lastFlag = parser.parseCurrentFlag(context.commandContext(), commandInput);
            if (lastFlag.isPresent()) {
                context.commandContext().store(FlagArgument.FLAG_META_KEY, lastFlag.get());
            } else {
                context.commandContext().remove(FlagArgument.FLAG_META_KEY);
            }
        } else if (commandInput.remainingTokens() <= argument.getParser().getRequestedArgumentCount()) {
            // If the input queue contains fewer arguments than requested by the parser, then the parser will
            // need to be given the opportunity to provide suggestions. We store all provided arguments
            // so that the parser can use these to give contextual suggestions.
            for (int i = 0; i < argument.getParser().getRequestedArgumentCount() - 1
                    && commandInput.remainingTokens() > 1; i++) {
                context.commandContext().store(
                        String.format("%s_%d", argument.getName(), i),
                        commandInput.readString()
                );
            }
        }

        if (commandInput.isEmpty()) {
            return context;
        } else if (commandInput.remainingTokens() == 1) {
            return this.addArgumentSuggestions(context, child, commandInput.peekString());
        } else if (child.isLeaf() && child.argument() instanceof CompoundArgument) {
            return this.addArgumentSuggestions(context, child, commandInput.tokenize().getLast());
        }

        // Store original input command queue before the parsers below modify it
        final CommandInput commandInputOriginal = commandInput.copy();

        // START: Preprocessing
        final ArgumentParseResult<Boolean> preParseResult = argument.preprocess(
                context.commandContext(),
                commandInput
        );
        final boolean preParseSuccess = !preParseResult.getFailure().isPresent()
                && preParseResult.getParsedValue().orElse(false);
        // END: Preprocessing

        if (preParseSuccess) {
            // START: Parsing
            context.commandContext().setCurrentArgument(child.argument());

            final CommandInput preParseInput = commandInput.copy();

            final ArgumentParseResult<?> result = child.argument().getParser().parse(context.commandContext(), commandInput);
            final Optional<?> parsedValue = result.getParsedValue();
            final boolean parseSuccess = parsedValue.isPresent();

            if (result.getFailure().isPresent()) {
                commandInput.cursor(preParseInput.cursor());
            }

            // It's the last node, we don't care for success or not as we don't need to delegate to a child
            if (child.isLeaf()) {
                if (!commandInput.isEmpty()) {
                    return context;
                }

                // Greedy parser took all the input, we can restore and just ask for suggestions
                commandInput.cursor(commandInputOriginal.cursor());
                this.addArgumentSuggestions(context, child, commandInput.remainingInput());
            }

            if (parseSuccess && !commandInput.isEmpty()) {
                // the current argument at the position is parsable and there are more arguments following
                context.commandContext().store(child.argument().getName(), parsedValue.get());
                return this.getSuggestions(context, commandInput, child);
            } else if (!parseSuccess && commandInputOriginal.remainingTokens() > 1) {
                // at this point there should normally be no need to reset the command queue as we expect
                // users to only take out an argument if the parse succeeded. Just to be sure we reset anyway
                commandInput.cursor(commandInputOriginal.cursor());

                // there are more arguments following but the current argument isn't matching - there
                // is no need to collect any further suggestions
                return context;
            }
            // END: Parsing
        }

        // Restore original command input queue
        commandInput.cursor(commandInputOriginal.cursor());

        if (!preParseSuccess && commandInput.remainingTokens() > 1) {
            // The preprocessor denied the argument, and there are more arguments following the current one
            // Therefore we shouldn't list the suggestions of the current argument, as clearly the suggestions of
            // one of the following arguments is requested
            return context;
        }

        return this.addArgumentSuggestions(context, child, commandInput.peekString());
    }

    /**
     * Removes as many arguments from the {@code commandQueue} as the given {@code compoundArgument} requires. If the
     * {@code commandQueue} fewer than the required arguments then no arguments are popped
     *
     * @param commandContext   the command context
     * @param commandInput     the input
     * @param compoundArgument the compound argument
     */
    private void popRequiredArguments(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput,
            final @NonNull CompoundArgument<?, C, ?> compoundArgument
    ) {
        /* See how many arguments it requires */
        final int requiredArguments = compoundArgument.getParserTuple().getSize();
        /* Figure out whether we even need to care about this */
        if (commandInput.remainingTokens() <= requiredArguments) {
            /* Attempt to pop as many arguments from the stack as possible */
            for (int i = 0; i < requiredArguments - 1 && commandInput.remainingTokens() > 1; i++) {
                commandInput.readString();
                commandContext.store(PARSING_ARGUMENT_KEY, i + 2);
            }
        }
    }

    /**
     * Adds the suggestions for the given {@code node} to the given {@code context}. If the {@code node} contains
     * a flag, then all children of the {@code node} will contribute with suggestions as well
     *
     * @param context the suggestion context
     * @param node    the node containing the argument to get suggestions from
     * @param text    the input from the sender
     * @return the context
     */
    private @NonNull SuggestionContext<C> addArgumentSuggestions(
            final @NonNull SuggestionContext<C> context,
            final @NonNull CommandNode<C> node,
            final @NonNull String text
    ) {
        final CommandArgument<C, ?> argument = Objects.requireNonNull(node.argument());

        this.addArgumentSuggestions(context, argument, text);

        // When suggesting a flag, potentially suggest following nodes too
        final boolean isParsingFlag = argument instanceof FlagArgument
                && !node.children().isEmpty() // Has children
                && !text.startsWith("-") // Not a flag
                && !context.commandContext().getOptional(FlagArgument.FLAG_META_KEY).isPresent();

        if (!isParsingFlag) {
            return context;
        }

        for (final CommandNode<C> child : node.children()) {
            final CommandArgument<C, ?> childArgument = Objects.requireNonNull(child.argument());
            this.addArgumentSuggestions(context, childArgument, text);
        }

        return context;
    }

    /**
     * Adds the suggestions for the given {@code argument} to the given {@code context}
     *
     * @param context  the suggestion context
     * @param argument the argument to get suggestions from
     * @param text     the input from the sender
     */
    private void addArgumentSuggestions(
            final @NonNull SuggestionContext<C> context,
            final @NonNull CommandArgument<C, ?> argument,
            final @NonNull String text
    ) {
        context.commandContext().setCurrentArgument(argument);
        context.addSuggestions(argument.suggestionProvider().suggestions(context.commandContext(), text));
    }


    /**
     * Inserts a new command into the command tree and then verifies the integrity of the tree
     *
     * @param command the command to insert
     */
    @SuppressWarnings("unchecked")
    public void insertCommand(final @NonNull Command<C> command) {
        synchronized (this.commandLock) {
            final CommandComponent<C> flagComponent = command.flagComponent();
            final List<CommandComponent<C>> nonFlagArguments = command.nonFlagArguments();
            final int flagStartIdx = this.flagStartIndex(nonFlagArguments);

            CommandNode<C> node = this.internalTree;
            for (int i = 0; i < nonFlagArguments.size(); i++) {
                final CommandComponent<C> component = nonFlagArguments.get(i);

                CommandNode<C> tempNode = node.getChild(component);
                if (tempNode == null) {
                    tempNode = node.addChild(component);
                } else if (component.argument() instanceof StaticArgument && tempNode.argument() != null) {
                    for (final String alias : ((StaticArgument<C>) component.argument()).getAliases()) {
                        ((StaticArgument<C>) Objects.requireNonNull(tempNode.argument())).registerAlias(alias);
                    }
                }
                if (!node.children().isEmpty()) {
                    node.sortChildren();
                }
                tempNode.parent(node);
                node = tempNode;

                if (flagComponent != null && i >= flagStartIdx) {
                    tempNode = node.addChild(flagComponent);
                    tempNode.parent(node);
                    node = tempNode;
                }
            }

            final CommandArgument<C, ?> nodeArgument = node.argument();
            if (nodeArgument != null) {
                if (nodeArgument.getOwningCommand() != null) {
                    throw new IllegalStateException(String.format(
                            "Duplicate command chains detected. Node '%s' already has an owning command (%s)",
                            node, nodeArgument.getOwningCommand()
                    ));
                }
                nodeArgument.setOwningCommand(command);
            }

            this.verifyAndRegister();
        }
    }

    /**
     * Returns the index of the given {@code components} list after which flags may be inserted.
     *
     * @param components the components
     * @return the index after which flags may be inserted
     */
    private int flagStartIndex(final @NonNull List<CommandComponent<C>> components) {
        // Append flags after the last static argument
        if (this.commandManager.getSetting(CommandManager.ManagerSettings.LIBERAL_FLAG_PARSING)) {
            for (int i = components.size() - 1; i >= 0; i--) {
                if (components.get(i).argument() instanceof StaticArgument) {
                    return i;
                }
            }
        }

        // Append flags after the last argument
        return components.size() - 1;
    }

    /**
     * Returns the permission that is missing from the given {@code sender} for them to execute the command attached
     * to the given {@code node}. If the {@code sender} is allowed to execute the command, the method returns {@code null}
     *
     * @param sender the command sender
     * @param node   the command node
     * @return the missing permission, or {@code null}
     */
    private @Nullable CommandPermission findMissingPermission(
            final @NonNull C sender,
            final @NonNull CommandNode<C> node
    ) {
        final CommandPermission permission = (CommandPermission) node.nodeMeta().get("permission");
        if (permission != null) {
            return this.commandManager.hasPermission(sender, permission) ? null : permission;
        }
        if (node.isLeaf()) {
            final CommandArgument<C, ?> argument = Objects.requireNonNull(node.argument(), "argument");
            final Command<C> command = Objects.requireNonNull(argument.getOwningCommand(), "command");
            return this.commandManager.hasPermission(
                    sender,
                    command.getCommandPermission()
            ) ? null : command.getCommandPermission();
        }
        /*
          if any of the children would permit the execution, then the sender has a valid
           chain to execute, and so we allow them to execute the root
         */
        final List<CommandPermission> missingPermissions = new LinkedList<>();
        for (final CommandNode<C> child : node.children()) {
            final CommandPermission check = this.findMissingPermission(sender, child);
            if (check == null) {
                return null;
            } else {
                missingPermissions.add(check);
            }
        }

        return OrPermission.of(missingPermissions);
    }

    /**
     * Goes through all commands and registers them, then verifies the integrity of the command tree.
     */
    public void verifyAndRegister() {
        // All top level commands are supposed to be registered in the command manager
        this.internalTree.children().stream().map(CommandNode::argument).forEach(commandArgument -> {
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
                this.commandManager.commandRegistrationHandler().registerCommand(owningCommand);
            }
        });

        this.getLeavesRaw(this.internalTree).forEach(this::updatePermission);
    }

    /**
     * Updates the permission of the given {@code node} by traversing the command tree and calculating the applicable permission.
     *
     * @param node the command node
     */
    private void updatePermission(final @NonNull CommandNode<C> node) {
        // noinspection all
        final CommandPermission commandPermission = node.argument().getOwningCommand().getCommandPermission();
        /* All leaves must necessarily have an owning command */
        node.nodeMeta().put("permission", commandPermission);
        // Get chain and order it tail->head then skip the tail (leaf node)
        List<CommandNode<C>> chain = this.getChain(node);
        Collections.reverse(chain);
        chain = chain.subList(1, chain.size());
        // Go through all nodes from the tail upwards until a collision occurs
        for (final CommandNode<C> commandArgumentNode : chain) {
            final CommandPermission existingPermission = (CommandPermission) commandArgumentNode.nodeMeta()
                    .get("permission");

            CommandPermission permission;
            if (existingPermission != null) {
                permission = OrPermission.of(Arrays.asList(commandPermission, existingPermission));
            } else {
                permission = commandPermission;
            }

            /* Now also check if there's a command handler attached to an upper level node */
            if (commandArgumentNode.argument() != null && commandArgumentNode
                    .argument()
                    .getOwningCommand() != null) {
                final Command<C> command = commandArgumentNode.argument().getOwningCommand();
                if (this
                        .commandManager()
                        .getSetting(CommandManager.ManagerSettings.ENFORCE_INTERMEDIARY_PERMISSIONS)) {
                    permission = command.getCommandPermission();
                } else {
                    permission = OrPermission.of(Arrays.asList(permission, command.getCommandPermission()));
                }
            }

            commandArgumentNode.nodeMeta().put("permission", permission);
        }
    }

    /**
     * Verifies that there is no illegal ambiguity in the given {@code node}.
     *
     * @param node the node
     * @throws AmbiguousNodeException if the node breaks some ambiguity contract
     */
    @SuppressWarnings("unchecked")
    private void checkAmbiguity(final @NonNull CommandNode<C> node) throws AmbiguousNodeException {
        if (node.isLeaf()) {
            return;
        }

        // List of child nodes that are not static arguments, but (parsed) variable ones
        final List<CommandNode<C>> childVariableArguments = node.children()
                .stream()
                .filter(n -> (n.argument() != null && !(n.argument() instanceof StaticArgument)))
                .collect(Collectors.toList());

        // If more than one child node exists with a variable argument, fail
        if (childVariableArguments.size() > 1) {
            final CommandNode<C> child = childVariableArguments.get(0);
            throw new AmbiguousNodeException(
                    node.argument(),
                    child.argument(),
                    node.children()
                            .stream()
                            .filter(n -> n.argument() != null)
                            .map(CommandNode::argument).collect(Collectors.toList())
            );
        }

        // List of child nodes that are static arguments, with fixed values
        final List<CommandNode<C>> childStaticArguments = node.children()
                .stream()
                .filter(n -> n.argument() instanceof StaticArgument)
                .collect(Collectors.toList());

        // Check none of the static arguments are equal to another one
        // This is done by filling a set and checking there are no duplicates
        final Set<String> checkedLiterals = new HashSet<>();
        for (final CommandNode<C> child : childStaticArguments) {
            final StaticArgument<C> staticArgument = (StaticArgument<C>) child.argument();
            for (final String nameOrAlias : staticArgument.getAliases()) {
                if (!checkedLiterals.add(nameOrAlias)) {
                    // Same literal value, ambiguity detected
                    throw new AmbiguousNodeException(
                            node.argument(),
                            child.argument(),
                            node.children()
                                    .stream()
                                    .filter(n -> n.argument() != null)
                                    .map(CommandNode::argument).collect(Collectors.toList())
                    );
                }
            }
        }

        // Recursively check child nodes as well
        node.children().forEach(this::checkAmbiguity);
    }

    /**
     * Returns all leaf nodes attached to the given {@code node} or its children
     *
     * @param node the node
     * @return the leaf nodes attached to the node
     */
    private @NonNull List<@NonNull CommandNode<C>> getLeavesRaw(
            final @NonNull CommandNode<C> node
    ) {
        final List<CommandNode<C>> leaves = new LinkedList<>();
        if (node.isLeaf()) {
            if (node.argument() != null) {
                leaves.add(node);
            }
        } else {
            node.children().forEach(child -> leaves.addAll(this.getLeavesRaw(child)));
        }
        return leaves;
    }

    /**
     * Returns all leaf nodes attached to the given {@code node} or its children
     *
     * @param node the node
     * @return the leaf nodes attached to the node
     */
    private @NonNull List<@NonNull CommandArgument<C, ?>> getLeaves(
            final @NonNull CommandNode<C> node
    ) {
        return this.getLeavesRaw(node).stream()
                .filter(n -> n.argument() != null)
                .map(CommandNode::argument)
                .collect(Collectors.toList());
    }

    /**
     * Returns an ordered list containing the chain of nodes that leads up to the given {@code end} node
     *
     * @param end the end node
     * @return the list of nodes leading up to the {@code end} node
     */
    private @NonNull List<@NonNull CommandNode<C>> getChain(
            final @Nullable CommandNode<C> end
    ) {
        final List<CommandNode<C>> chain = new LinkedList<>();
        CommandNode<C> tail = end;
        while (tail != null) {
            chain.add(tail);
            tail = tail.parent();
        }
        Collections.reverse(chain);
        return chain;
    }

    /**
     * Recursively deletes the given {@code node} and its children and performs an operation on each account encountered during
     * the deletion
     *
     * @param node            the node to delete
     * @param root            whether the node is a root node
     * @param commandConsumer consumer of encountered commands
     */
    void deleteRecursively(
        final @NonNull CommandNode<C> node,
        final boolean root,
        final Consumer<Command<C>> commandConsumer
    ) {
        for (final CommandNode<C> child : new ArrayList<>(node.children())) {
            this.deleteRecursively(child, false, commandConsumer);
        }

        final @Nullable CommandArgument<C, ?> value = node.argument();
        final @Nullable Command<C> owner = value == null ? null : value.getOwningCommand();
        if (owner != null) {
            commandConsumer.accept(owner);
        }
        this.removeNode(node, root);
    }

    /**
     * Removes the {@code node} from the tree. If {@code root} is true, the code is removed from the root node. Otherwise,
     * it is removed from its parent
     *
     * @param node the node to remove
     * @param root whether the node is a root node
     */
    private void removeNode(
        final @NonNull CommandNode<C> node,
        final boolean root
    ) {
        if (root) {
            this.internalTree.removeChild(node);
        } else {
            Objects.requireNonNull(node.parent(), "parent").removeChild(node);
        }
    }
}
