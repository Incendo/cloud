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
package org.incendo.cloud;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.context.ParsingContext;
import org.incendo.cloud.exception.AmbiguousNodeException;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoCommandInLeafException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.exception.NoSuchCommandException;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.internal.SuggestionContext;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.aggregate.AggregateParser;
import org.incendo.cloud.parser.flag.CommandFlagParser;
import org.incendo.cloud.parser.standard.LiteralParser;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.permission.PermissionResult;
import org.incendo.cloud.setting.ManagerSetting;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionMapper;
import org.incendo.cloud.suggestion.Suggestions;
import org.incendo.cloud.util.CompletableFutures;

/**
 * Tree containing all commands and command paths.
 * <p>
 * All {@link Command commands} consists of unique paths made out of {@link CommandComponent components}.
 * These arguments may be literals or variables. Command may either be required
 * or optional, with the requirement that no optional argument precedes a required argument.
 * <p>
 * The {@link Command commands} are stored in this tree and the nodes of tree consists of the command
 * {@link CommandComponent components}. Each leaf node of the tree should contain a fully parsed
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
 * @param <C> command sender type
 */
@API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class CommandTree<C> {

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
     */
    @API(status = API.Status.STABLE)
    public @NonNull CommandManager<C> commandManager() {
        return this.commandManager;
    }

    /**
     * Returns an immutable view containing of the root nodes of the command tree
     *
     * @return immutable view of the root nodes
     */
    @API(status = API.Status.STABLE)
    public @NonNull Collection<@NonNull CommandNode<C>> rootNodes() {
        return this.internalTree.children();
    }

    /**
     * Returns a named root node, if it exists
     *
     * @param name root node name
     * @return the found root node, or {@code null}
     */
    public @Nullable CommandNode<C> getNamedNode(final @Nullable String name) {
        for (final CommandNode<C> node : this.rootNodes()) {
            final CommandComponent<C> component = node.component();
            if (component == null || !(component.type() == CommandComponent.ComponentType.LITERAL)) {
                continue;
            }
            for (final String alias : component.aliases()) {
                if (alias.equalsIgnoreCase(name)) {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * Attempts to parse a command from the provided input.
     *
     * @param commandContext  command context instance
     * @param commandInput    command input
     * @param parsingExecutor executor to schedule parsing logic on
     * @return parsed command, if one could be found
     */
    @API(status = API.Status.STABLE)
    public @NonNull CompletableFuture<@Nullable Command<C>> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput,
            final @NonNull Executor parsingExecutor
    ) {
        return CompletableFutures.scheduleOn(parsingExecutor, () -> this.parseDirect(commandContext, commandInput, parsingExecutor))
                .thenApply(command -> {
                    if (command != null) {
                        commandContext.command(command);
                    }
                    return command;
                });
    }

    private @NonNull CompletableFuture<@Nullable Command<C>> parseDirect(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput,
            final @NonNull Executor parsingExecutor
    ) {
        // Special case for empty command trees.
        if (this.internalTree.isLeaf() && this.internalTree.component() == null) {
            return CompletableFutures.failedFuture(
                    new NoSuchCommandException(
                            commandContext.sender(),
                            new ArrayList<>(),
                            commandInput.peekString()
                    )
            );
        }

       return this.parseCommand(
                new ArrayList<>(),
                commandContext,
                commandInput,
               this.internalTree,
               parsingExecutor
        ).thenCompose(command -> {
            if (command != null
                    && command.senderType().isPresent()
                    && !GenericTypeReflector.isSuperType(command.senderType().get().getType(), commandContext.sender().getClass())) {
                return CompletableFutures.failedFuture(
                        new InvalidCommandSenderException(
                                commandContext.sender(),
                                command.senderType().get().getType(),
                                new ArrayList<>(command.components()),
                                command
                        )
                );
            }
            return CompletableFuture.completedFuture(command);
        });
    }

    private @NonNull CompletableFuture<@Nullable Command<C>> parseCommand(
            final @NonNull List<@NonNull CommandComponent<C>> parsedArguments,
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput,
            final @NonNull CommandNode<C> root,
            final @NonNull Executor executor
    ) {
        final Optional<PermissionResult> permissionResult = this.determineAccess(commandContext.sender(), root);
        if (!permissionResult.isPresent()) {
            return CompletableFutures.failedFuture(
                    new InvalidCommandSenderException(
                            commandContext.sender(),
                            root.nodeMeta().get(CommandNode.META_KEY_SENDER_TYPES),
                            this.getComponentChain(root),
                            null
                    )
            );
        }
        if (permissionResult.get().denied()) {
            return CompletableFutures.failedFuture(
                    new NoPermissionException(
                            permissionResult.get(),
                            commandContext.sender(),
                            this.getComponentChain(root)
                    )
            );
        }

        final CompletableFuture<@Nullable Command<C>> parsedChild = this.attemptParseUnambiguousChild(
                parsedArguments,
                commandContext,
                root,
                commandInput,
                executor
        );
        if (parsedChild != null) {
            return parsedChild;
        }

        // There are 0 or more static arguments as children. No variable child arguments are present
        if (root.children().isEmpty()) {
            final CommandComponent<C> rootComponent = root.component();
            if (rootComponent == null || root.command() == null || !commandInput.isEmpty()) {
                // Too many arguments. We have a unique path, so we can send the entire context
                return CompletableFutures.failedFuture(
                        new InvalidSyntaxException(
                                this.commandManager.commandSyntaxFormatter()
                                        .apply(commandContext.sender(), parsedArguments, root),
                                commandContext.sender(), this.getComponentChain(root)
                        )
                );
            }
            return CompletableFuture.completedFuture(root.command());
        }

        CompletableFuture<Command<C>> childCompletable = CompletableFuture.completedFuture(null);
        for (final CommandNode<C> child : new ArrayList<>(root.children())) {
            if (child.component() == null) {
                continue;
            }

            childCompletable = childCompletable.thenCompose(previousResult -> {
                if (previousResult != null) {
                    return CompletableFuture.completedFuture(previousResult);
                }

                final CommandComponent<C> component = Objects.requireNonNull(child.component());
                final ParsingContext<C> parsingContext = commandContext.createParsingContext(component);

                // Skip a single space (argument delimiter)
                commandInput.skipWhitespace(1);
                // Copy the current queue so that we can deduce the captured input.
                final CommandInput currentInput = commandInput.copy();

                parsingContext.markStart();

                return component.parser()
                        .parseFuture(commandContext, commandInput)
                        .thenComposeAsync(result -> {
                            parsingContext.markEnd();
                            parsingContext.success(!result.failure().isPresent());
                            parsingContext.consumedInput(currentInput, commandInput);

                            if (result.parsedValue().isPresent()) {
                                parsedArguments.add(component);
                                return this.parseCommand(parsedArguments, commandContext, commandInput, child, executor);
                            } else if (result.failure().isPresent()) {
                                commandInput.cursor(currentInput.cursor());
                            }
                            // We do not want to respond with a parsing error, as parsing errors are meant to propagate.
                            // Just not being able to parse is not enough.
                            return CompletableFuture.completedFuture(null);
                        }, executor);
            });
        }

        return childCompletable.thenCompose(completedCommand -> {
                    if (completedCommand != null) {
                        return CompletableFuture.completedFuture(completedCommand);
                    }

                    // We could not find a match
                    if (root.equals(this.internalTree)) {
                       return CompletableFutures.failedFuture(
                           new NoSuchCommandException(
                                   commandContext.sender(),
                                   this.getChain(root).stream().map(CommandNode::component).collect(Collectors.toList()),
                                   commandInput.peekString()
                           )
                       );
                   }

                    // If we couldn't match a child, check if there's a command attached and execute it
                    final CommandComponent<C> rootComponent = root.component();
                    if (rootComponent != null && root.command() != null && commandInput.isEmpty()) {
                        final Command<C> command = root.command();
                        final PermissionResult check = this.commandManager.testPermission(
                                commandContext.sender(),
                                command.commandPermission()
                        );
                        if (check.denied()) {
                            return CompletableFutures.failedFuture(
                                    new NoPermissionException(
                                            check,
                                            commandContext.sender(),
                                            this.getComponentChain(root)
                                    )
                            );
                        }
                        return CompletableFuture.completedFuture(root.command());
                    }

                    // We know that there's no command, and we also cannot match any of the children
                    return CompletableFutures.failedFuture(
                            new InvalidSyntaxException(
                                    this.commandManager.commandSyntaxFormatter()
                                            .apply(commandContext.sender(), parsedArguments, root),
                                    commandContext.sender(), this.getComponentChain(root)
                            )
                    );
                });
    }

    private @Nullable CompletableFuture<@Nullable Command<C>> attemptParseUnambiguousChild(
            final @NonNull List<@NonNull CommandComponent<C>> parsedArguments,
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandNode<C> root,
            final @NonNull CommandInput commandInput,
            final @NonNull Executor executor
    ) {
        final C sender = commandContext.sender();
        final List<CommandNode<C>> children = root.children();

        // Check whether it matches any of the static arguments If so, do not attempt parsing as a dynamic argument
        if (!commandInput.isEmpty() && this.matchesLiteral(children, commandInput.peekString())) {
            return null;
        }

        // If it does not match a literal, try to find the one argument node, if it exists
        // The ambiguity check guarantees that only one will be present
        final List<CommandNode<C>> argumentNodes = children.stream()
                .filter(n -> (n.component() != null && n.component().type() != CommandComponent.ComponentType.LITERAL))
                .collect(Collectors.toList());
        if (argumentNodes.size() > 1) {
            throw new IllegalStateException("Unexpected ambiguity detected, number of dynamic child nodes should not exceed 1");
        } else if (argumentNodes.isEmpty()) {
            return null;
        }
        final CommandNode<C> child = argumentNodes.get(0);

        // Check if we're allowed to execute the child command. If not, exit
        final Optional<PermissionResult> childCheck = this.determineAccess(sender, child);
        if (!childCheck.isPresent()) {
            return CompletableFutures.failedFuture(
                    new InvalidCommandSenderException(
                            sender,
                            child.nodeMeta().get(CommandNode.META_KEY_SENDER_TYPES),
                            this.getComponentChain(child),
                            null
                    )
            );
        }
        if (!commandInput.isEmpty() && childCheck.get().denied()) {
            return CompletableFutures.failedFuture(
                    new NoPermissionException(
                            childCheck.get(),
                            sender,
                            this.getComponentChain(child)
                    )
            );
        }

        // If the child has no argument it cannot be executed, so we exit
        if (child.component() == null) {
            return null;
        }

        // This stores the argument value for this argument.
        ArgumentParseResult<?> argumentValue = null;

        // Flag arguments need to be skipped over, so that further defaults are handled
        if (commandInput.isEmpty() && !(child.component().type() == CommandComponent.ComponentType.FLAG)) {
            final CommandComponent<C> childComponent = Objects.requireNonNull(child.component());
            if (childComponent.hasDefaultValue()) {
                final DefaultValue<C, ?> defaultValue = Objects.requireNonNull(childComponent.defaultValue(), "defaultValue");

                if (defaultValue instanceof DefaultValue.ParsedDefaultValue) {
                    return this.attemptParseUnambiguousChild(
                            parsedArguments,
                            commandContext,
                            root,
                            commandInput.appendString(((DefaultValue.ParsedDefaultValue<C, ?>) defaultValue).value()),
                            executor
                    );
                } else {
                    argumentValue = defaultValue.evaluateDefault(commandContext);
                }
            } else if (!child.component().required()) {
                if (child.command() == null) {
                    // If there are multiple children with different owning commands then it's ambiguous and
                    // not allowed, therefore we're able to pick any child command, as long as we can find it
                    CommandNode<C> node = child;
                    while (!node.isLeaf()) {
                        node = node.children().get(0);
                        final CommandComponent<C> nodeComponent = node.component();
                        if (nodeComponent != null && node.command() != null) {
                            child.command(node.command());
                        }
                    }
                }
                return CompletableFuture.completedFuture(child.command());
            } else if (child.isLeaf()) {
                final CommandComponent<C> rootComponent = root.component();
                if (rootComponent == null || root.command() == null) {
                    final List<CommandComponent<C>> components = Objects.requireNonNull(child.command()).components();
                    return CompletableFutures.failedFuture(
                            new InvalidSyntaxException(
                                    this.commandManager.commandSyntaxFormatter()
                                            .apply(commandContext.sender(), components, child),
                                    sender,
                                    this.getComponentChain(root)
                            )
                    );
                }

                final Command<C> command = root.command();
                final PermissionResult check = this.commandManager().testPermission(sender, command.commandPermission());
                if (check.allowed()) {
                    return CompletableFuture.completedFuture(command);
                }
                return CompletableFutures.failedFuture(
                        new NoPermissionException(
                                check,
                                sender,
                                this.getComponentChain(root)
                        )
                );
            } else {
                // The child is not a leaf, but may have an intermediary executor, attempt to use it
                final CommandComponent<C> rootComponent = root.component();
                if (rootComponent == null || root.command() == null) {
                    // Child does not have a command, and so we cannot proceed
                    return CompletableFutures.failedFuture(
                            new InvalidSyntaxException(
                                    this.commandManager.commandSyntaxFormatter()
                                            .apply(commandContext.sender(), parsedArguments, root),
                                    sender,
                                    this.getComponentChain(root)
                            )
                    );
                }

                // If the sender has permission to use the command, then we're completely done
                final Command<C> command = Objects.requireNonNull(root.command());
                final PermissionResult check = this.commandManager().testPermission(sender, command.commandPermission());
                if (check.allowed()) {
                    return CompletableFuture.completedFuture(command);
                }

                return CompletableFutures.failedFuture(
                        new NoPermissionException(
                                check,
                                sender,
                                this.getComponentChain(root)
                        )
                );
            }
        }

        final CommandComponent<C> component = Objects.requireNonNull(child.component());

        final CompletableFuture<?> parseResult;
        if (argumentValue != null) {
            if (argumentValue.parsedValue().isPresent()) {
                parseResult = CompletableFuture.completedFuture(argumentValue.parsedValue().get());
            } else {
                parseResult = CompletableFutures.failedFuture(this.argumentParseException(commandContext, child, argumentValue));
            }
        } else {
            parseResult = this.parseArgument(commandContext, child, commandInput, executor)
                    .thenApply(result -> result.parsedValue().orElse(null));
        }

        return parseResult.thenComposeAsync(value -> {
           if (value == null) {
               return CompletableFuture.completedFuture(null);
           }

           commandContext.store(component.name(), value);
           if (child.isLeaf()) {
               if (commandInput.isEmpty()) {
                   return CompletableFuture.completedFuture(child.command());
               }
               return CompletableFutures.failedFuture(
                       new InvalidSyntaxException(
                               this.commandManager.commandSyntaxFormatter()
                                       .apply(commandContext.sender(), parsedArguments, child),
                               sender,
                               this.getComponentChain(root)
                       )
               );
           }

            parsedArguments.add(Objects.requireNonNull(child.component()));
            return this.parseCommand(parsedArguments, commandContext, commandInput, child, executor);
        }, executor);
    }

    private boolean matchesLiteral(final @NonNull List<@NonNull CommandNode<C>> children, final @NonNull String input) {
        return children.stream()
                .map(CommandNode::component)
                .filter(Objects::nonNull)
                .filter(n -> n.type() == CommandComponent.ComponentType.LITERAL)
                .flatMap(arg -> Stream.concat(Stream.of(arg.name()), arg.aliases().stream()))
                .anyMatch(arg -> arg.equals(input));
    }

    private @NonNull CompletableFuture<ArgumentParseResult<?>> parseArgument(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandNode<C> node,
            final @NonNull CommandInput commandInput,
            final @NonNull Executor executor
    ) {
        final ParsingContext<C> parsingContext = commandContext.createParsingContext(node.component());
        parsingContext.markStart();

        final ArgumentParseResult<Boolean> preParseResult = node.component().preprocess(commandContext, commandInput);

        if (preParseResult.failure().isPresent() || !preParseResult.parsedValue().orElse(false)) {
            parsingContext.markEnd();
            parsingContext.success(false);
            if (preParseResult.failure().isPresent()) {
                return CompletableFutures.failedFuture(
                        this.argumentParseException(commandContext, node, preParseResult)
                );
            }
            return CompletableFuture.completedFuture(preParseResult);
        }

        // Skip a single space (argument delimiter)
        commandInput.skipWhitespace(1);
        // Copy the current queue so that we can deduce the captured input.
        final CommandInput currentInput = commandInput.copy();

        return node.component().parser()
                .parseFuture(commandContext, commandInput)
                .thenComposeAsync(result -> {
                    parsingContext.consumedInput(currentInput, commandInput);
                    parsingContext.markEnd();
                    parsingContext.success(false);

                    if (result.failure().isPresent()) {
                        commandInput.cursor(currentInput.cursor());
                        return CompletableFutures.failedFuture(
                                this.argumentParseException(commandContext, node, result)
                        );
                    } else {
                        return CompletableFuture.completedFuture(result);
                    }
                }, executor);
    }

    private @NonNull ArgumentParseException argumentParseException(
            final CommandContext<C> commandContext,
            final CommandNode<C> node,
            final ArgumentParseResult<?> result
    ) {
        return new ArgumentParseException(
                result.failure().get(),
                commandContext.sender(),
                this.getComponentChain(node)
        );
    }

    /**
     * Returns suggestions from the input queue
     *
     * @param <S>          suggestion type
     * @param context      Context instance
     * @param commandInput Input
     * @param mapper       suggestion mapper
     * @param executor     executor to schedule suggestion logic on
     * @return String suggestions. These should be filtered based on {@link String#startsWith(String)}
     */
    @API(status = API.Status.STABLE)
    public <S extends Suggestion> @NonNull CompletableFuture<@NonNull Suggestions<C, S>> getSuggestions(
            final @NonNull CommandContext<C> context,
            final @NonNull CommandInput commandInput,
            final @NonNull SuggestionMapper<S> mapper,
            final @NonNull Executor executor
    ) {
        return CompletableFutures.scheduleOn(executor, () -> this.getSuggestionsDirect(context, commandInput, mapper, executor));
    }

    private <S extends Suggestion> @NonNull CompletableFuture<@NonNull Suggestions<C, S>> getSuggestionsDirect(
            final @NonNull CommandContext<C> context,
            final @NonNull CommandInput commandInput,
            final @NonNull SuggestionMapper<S> mapper,
            final @NonNull Executor executor
    ) {
        final SuggestionContext<C, S> suggestionCtx = new SuggestionContext<>(
                this.commandManager.suggestionProcessor(),
                context,
                commandInput,
                mapper
        );
        return this.getSuggestions(suggestionCtx, commandInput, this.internalTree, executor)
                .thenApply($ -> suggestionCtx.makeSuggestions());
    }

    @SuppressWarnings("MixedMutabilityReturnType")
    private @NonNull CompletableFuture<SuggestionContext<C, ?>> getSuggestions(
            final @NonNull SuggestionContext<C, ?> context,
            final @NonNull CommandInput commandInput,
            final @NonNull CommandNode<C> root,
            final @NonNull Executor executor
    ) {
        // If the sender isn't allowed to access the root node, no suggestions are needed
        if (!this.determineAccess(context.commandContext().sender(), root).map(PermissionResult::allowed).orElse(false)) {
            return CompletableFuture.completedFuture(context);
        }

        final List<CommandNode<C>> children = root.children();
        final List<CommandNode<C>> staticArguments = children.stream()
                .filter(n -> n.component() != null)
                .filter(n -> n.component().type() == CommandComponent.ComponentType.LITERAL)
                .collect(Collectors.toList());

        if (!commandInput.isEmpty()) {
            commandInput.skipWhitespace(1);
        }

        // Try to see if any of the static literals can be parsed (matches exactly)
        // If so, enter that node of the command tree for deeper suggestions
        if (!staticArguments.isEmpty() && !commandInput.isEmpty(true /* ignoringWhitespace */)) {
            final CommandInput commandInputCopy = commandInput.copy();
            for (CommandNode<C> child : staticArguments) {
                final CommandComponent<C> childComponent = child.component();
                if (childComponent == null) {
                    continue;
                }

                final ArgumentParseResult<?> result = childComponent.parser().parse(
                        context.commandContext(),
                        commandInput
                );

                if (result.failure().isPresent()) {
                    commandInput.cursor(commandInputCopy.cursor());
                }

                if (!result.parsedValue().isPresent()) {
                    continue;
                }

                if (commandInput.isEmpty()) {
                    // We've already matched one exactly, no use looking further
                    break;
                }

                return this.getSuggestions(context, commandInput, child, executor);
            }

            // Restore original queue
            commandInput.cursor(commandInputCopy.cursor());
        }

        // Calculate suggestions for the literal arguments
        CompletableFuture<SuggestionContext<C, ?>> suggestionFuture = CompletableFuture.completedFuture(context);
        if (commandInput.remainingTokens() <= 1) {
            for (final CommandNode<C> node : staticArguments) {
                suggestionFuture = suggestionFuture
                        .thenCompose(ctx -> this.addSuggestionsForLiteralArgument(context, node, commandInput));
            }
        }

        // Calculate suggestions for the variable argument, if one exists
        for (final CommandNode<C> child : root.children()) {
            if (child.component() == null || child.component().type() == CommandComponent.ComponentType.LITERAL) {
                continue;
            }
            suggestionFuture = suggestionFuture
                    .thenCompose(ctx -> this.addSuggestionsForDynamicArgument(context, commandInput, child, executor, false));
        }

        return suggestionFuture;
    }

    /**
     * Adds the suggestions for a static argument if they match the given {@code input}
     *
     * @param context the suggestion context
     * @param node    the node containing the static argument
     * @param input   the current input
     * @return future that completes with the context
     */
    private CompletableFuture<SuggestionContext<C, ?>> addSuggestionsForLiteralArgument(
            final @NonNull SuggestionContext<C, ?> context,
            final @NonNull CommandNode<C> node,
            final @NonNull CommandInput input
    ) {
        if (!this.determineAccess(context.commandContext().sender(), node).map(PermissionResult::allowed).orElse(false)) {
            return CompletableFuture.completedFuture(context);
        }
        final CommandComponent<C> component = Objects.requireNonNull(node.component());
        return component.suggestionProvider()
                .suggestionsFuture(context.commandContext(), input.copy())
                .thenApply(suggestionsToAdd -> {
                    final String string = input.peekString();
                    for (Suggestion suggestion : suggestionsToAdd) {
                        if (suggestion.suggestion().equals(string) || !suggestion.suggestion().startsWith(string)) {
                            continue;
                        }
                        context.addSuggestion(suggestion);
                    }
                    return context;
                });
    }

    @SuppressWarnings("unchecked")
    private @NonNull CompletableFuture<SuggestionContext<C, ?>> addSuggestionsForDynamicArgument(
            final @NonNull SuggestionContext<C, ?> context,
            final @NonNull CommandInput commandInput,
            final @NonNull CommandNode<C> child,
            final @NonNull Executor executor,
            final boolean inFlag
    ) {
        final CommandComponent<C> component = child.component();
        if (component == null) {
            return CompletableFuture.completedFuture(context);
        }

        if (!inFlag && component.parser() instanceof CommandFlagParser) {
            // Use the flag argument parser to deduce what flag is being suggested right now
            // If empty, then no flag value is being typed, and the different flag options should
            // be suggested instead.
            final CommandFlagParser<C> parser = (CommandFlagParser<C>) component.parser();

            return parser.parseCurrentFlag(context.commandContext(), commandInput, executor).thenCompose(lastFlag -> {
                if (lastFlag.isPresent()) {
                    context.commandContext().store(CommandFlagParser.FLAG_META_KEY, lastFlag.get());
                } else {
                    context.commandContext().remove(CommandFlagParser.FLAG_META_KEY);
                }
                return this.addSuggestionsForDynamicArgument(context, commandInput, child, executor, true);
            });
        }

        if (commandInput.isEmpty() || commandInput.remainingTokens() == 1
                || (child.isLeaf() && child.component().parser() instanceof AggregateParser)
                || (child.isLeaf() && child.component().parser() instanceof CommandFlagParser)) {
            return this.addArgumentSuggestions(context, child, commandInput, executor);
        }

        // Store original input command queue before the parsers below modify it
        final CommandInput commandInputOriginal = commandInput.copy();

        // START: Preprocessing
        final ArgumentParseResult<Boolean> preParseResult = component.preprocess(
                context.commandContext(),
                commandInput
        );
        final boolean preParseSuccess = !preParseResult.failure().isPresent()
                && preParseResult.parsedValue().orElse(false);
        // END: Preprocessing

        final CompletableFuture<SuggestionContext<C, ?>> parsingFuture;
        if (!preParseSuccess) {
            parsingFuture = CompletableFuture.completedFuture(null);
        } else {
            // START: Parsing
            final ParsingContext<C> parsingContext = context.commandContext().createParsingContext(child.component());
            parsingContext.markStart();
            final CommandInput preParseInput = commandInput.copy();

            parsingFuture = child.component()
                    .parser()
                    .parseFuture(context.commandContext(), commandInput)
                    .thenComposeAsync(result -> {
                        final Optional<?> parsedValue = result.parsedValue();
                        final boolean parseSuccess = parsedValue.isPresent();

                        if (result.failure().isPresent()) {
                            commandInput.cursor(preParseInput.cursor());
                            return this.addArgumentSuggestions(context, child, commandInput, executor);
                        }

                        if (child.isLeaf()) {
                            if (!commandInput.isEmpty()) {
                                return CompletableFuture.completedFuture(context);
                            }

                            // Greedy parser took all the input, we can restore and just ask for suggestions
                            commandInput.cursor(commandInputOriginal.cursor());
                            this.addArgumentSuggestions(context, child, commandInput, executor);
                        }

                        if (parseSuccess && (!commandInput.isEmpty() || commandInput.input().endsWith(" "))) {
                            if (commandInput.isEmpty()) {
                                commandInput.moveCursor(-1);
                            }
                            // the current argument at the position is parsable and there are more arguments following
                            context.commandContext().store(child.component().name(), parsedValue.get());
                            parsingContext.success(true);
                            return this.getSuggestions(context, commandInput, child, executor);
                        } else if (!parseSuccess && commandInputOriginal.remainingTokens() > 1) {
                            // at this point there should normally be no need to reset the command queue as we expect
                            // users to only take out an argument if the parse succeeded. Just to be sure we reset anyway
                            commandInput.cursor(commandInputOriginal.cursor());

                            // there are more arguments following but the current argument isn't matching - there
                            // is no need to collect any further suggestions
                            return CompletableFuture.completedFuture(context);
                        }
                        return CompletableFuture.completedFuture(null);
                    }, executor);
        }

        return parsingFuture.thenCompose(previousResult -> {
            if (previousResult != null) {
                return CompletableFuture.completedFuture(previousResult);
            }

            // Restore original command input queue
            commandInput.cursor(commandInputOriginal.cursor());

            if (!preParseSuccess && commandInput.remainingTokens() > 1) {
                // The preprocessor denied the argument, and there are more arguments following the current one
                // Therefore we shouldn't list the suggestions of the current argument, as clearly the suggestions of
                // one of the following arguments is requested
                return CompletableFuture.completedFuture(context);
            }

            return this.addArgumentSuggestions(context, child, commandInput, executor);
        });
    }

    /**
     * Adds the suggestions for the given {@code node} to the given {@code context}. If the {@code node} contains
     * a flag, then all children of the {@code node} will contribute with suggestions as well
     *
     * @param context  the suggestion context
     * @param node     the node containing the argument to get suggestions from
     * @param input    the input from the sender
     * @param executor executor to schedule further suggestion logic to
     * @return the context
     */
    private @NonNull CompletableFuture<SuggestionContext<C, ?>> addArgumentSuggestions(
            final @NonNull SuggestionContext<C, ?> context,
            final @NonNull CommandNode<C> node,
            final @NonNull CommandInput input,
            final @NonNull Executor executor
    ) {
        final CommandComponent<C> component = Objects.requireNonNull(node.component());
        return this.addArgumentSuggestions(context, component, input, executor).thenCompose(ctx -> {
            // When suggesting a flag, potentially suggest following nodes too
            final boolean isParsingFlag = component.type() == CommandComponent.ComponentType.FLAG
                    && !node.children().isEmpty() // Has children
                    && !(input.hasRemainingInput() && input.peek() == '-') // Not a flag
                    && !context.commandContext().optional(CommandFlagParser.FLAG_META_KEY).isPresent();

            if (!isParsingFlag) {
                return CompletableFuture.completedFuture(ctx);
            }

            return CompletableFuture.allOf(
                    node.children()
                            .stream()
                            .map(child -> this.addArgumentSuggestions(
                                    context, Objects.requireNonNull(child.component()), input, executor))
                            .toArray(CompletableFuture[]::new)
            ).thenApply(v -> ctx);
        });
    }

    /**
     * Adds the suggestions for the given {@code argument} to the given {@code context}
     *
     * @param context   the suggestion context
     * @param component the component to get suggestions from
     * @param input     the input from the sender
     * @param executor  executor to schedule further suggestion logic to
     * @return future that completes with the context
     */
    private CompletableFuture<SuggestionContext<C, ?>> addArgumentSuggestions(
            final @NonNull SuggestionContext<C, ?> context,
            final @NonNull CommandComponent<C> component,
            final @NonNull CommandInput input,
            final @NonNull Executor executor
    ) {
        return component.suggestionProvider()
                .suggestionsFuture(context.commandContext(), input.copy())
                .thenAcceptAsync(context::addSuggestions, executor)
                .thenApply(in -> context);
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
                } else if (component.type() == CommandComponent.ComponentType.LITERAL && tempNode.component() != null) {
                    for (final String alias : component.aliases()) {
                        ((LiteralParser<C>) tempNode.component().parser()).insertAlias(alias);
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

            final CommandComponent<C> nodeComponent = node.component();
            if (nodeComponent != null) {
                if (node.command() != null) {
                    throw new IllegalStateException(String.format(
                            "Duplicate command chains detected. Node '%s' already has an owning command (%s)",
                            node, node.command()
                    ));
                }

                node.command(command);
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
        if (this.commandManager.settings().get(ManagerSetting.LIBERAL_FLAG_PARSING)) {
            for (int i = components.size() - 1; i >= 0; i--) {
                if (components.get(i).type() == CommandComponent.ComponentType.LITERAL) {
                    return i;
                }
            }
        }

        // Append flags after the last argument
        return components.size() - 1;
    }

    /**
     * Checks if the sender can access the node.
     *
     * <p>Returns {@link Optional#empty} if the sender does not match the type requirements for the node.</p>
     *
     * <p>If the sender matches a sender type-permission entry, the success result will be returned.
     * Otherwise, a failure result with all of the failed permissions wrapped by {@link Permission#anyOf(Permission...)}
     * will be returned.</p>
     *
     * @param sender command sender
     * @param node   command node
     * @return whether the sender can access the node
     */
    private Optional<PermissionResult> determineAccess(final @NonNull C sender, final @NonNull CommandNode<C> node) {
        final Map<Type, Permission> accessMap = node.nodeMeta().getOrNull(CommandNode.META_KEY_ACCESS);
        if (accessMap == null) {
            throw new IllegalStateException("Expected access requirements to be propagated");
        }
        final Set<Permission> failed = new HashSet<>();
        for (final Map.Entry<Type, Permission> entry : accessMap.entrySet()) {
            if (GenericTypeReflector.isSuperType(entry.getKey(), sender.getClass())) {
                final PermissionResult result = this.commandManager.testPermission(sender, entry.getValue());
                if (result.allowed()) {
                    return Optional.of(result);
                } else {
                    failed.add(entry.getValue());
                }
            }
        }
        if (failed.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(PermissionResult.denied(Permission.anyOf(failed)));
    }

    /**
     * Goes through all commands and registers them, then verifies the integrity of the command tree.
     */
    private void verifyAndRegister() {
        // All top level commands are supposed to be registered in the command manager
        this.internalTree.children().stream().map(CommandNode::component).forEach(component -> {
            if (component.type() != CommandComponent.ComponentType.LITERAL) {
                throw new IllegalStateException("Top level command argument cannot be a variable");
            }
        });

        this.checkAmbiguity(this.internalTree);

        // Verify that all leaf nodes have command registered
        this.getLeaves(this.internalTree).forEach(leaf -> {
            if (leaf.command() == null) {
                throw new NoCommandInLeafException(leaf.component());
            } else {
                final Command<C> owningCommand = leaf.command();
                this.commandManager.commandRegistrationHandler().registerCommand(owningCommand);
            }
        });

        this.getExecutorNodes(this.internalTree).forEach(this::propagateRequirements);
    }

    /**
     * Returns the node that all {@link #rootNodes()} share as a parent.
     *
     * @return the root node
     */
    @API(status = API.Status.INTERNAL)
    public @NonNull CommandNode<C> rootNode() {
        return this.internalTree;
    }

    /**
     * Propagates permission and sender type requirements from the {@link Command} owning the {@code leafNode}'s component down
     * the tree, merging as is appropriate for nodes shared by multiple chains.
     *
     * @param leafNode leafNode
     */
    private void propagateRequirements(final @NonNull CommandNode<C> leafNode) {
        final Permission commandPermission = leafNode.command().commandPermission();
        Type senderType = leafNode.command().senderType().map(TypeToken::getType).orElse(null);
        if (senderType == null) {
            senderType = Object.class;
        }
        // Get chain and order it tail->head
        List<CommandNode<C>> chain = this.getChain(leafNode);
        Collections.reverse(chain);
        for (final CommandNode<C> commandArgumentNode : chain) {
            final Set<Type> senderTypes = commandArgumentNode.nodeMeta()
                    .computeIfAbsent(CommandNode.META_KEY_SENDER_TYPES, $ -> new HashSet<>());
            updateSenderRequirements(senderTypes, senderType);

            final Map<Type, Permission> accessMap = commandArgumentNode.nodeMeta()
                    .computeIfAbsent(CommandNode.META_KEY_ACCESS, $ -> new HashMap<>());
            updateAccess(accessMap, senderType, commandPermission);
        }
    }

    private static void updateAccess(
            final Map<Type, Permission> senderTypes,
            final Type senderType,
            final Permission commandPermission
    ) {
        senderTypes.compute(senderType, (key, existing) -> {
           if (existing == null) {
               return commandPermission;
           }
           return Permission.anyOf(existing, commandPermission);
        });
    }

    private static void updateSenderRequirements(final Set<Type> senderTypes, final Type senderType) {
        boolean add = true;
        for (final Iterator<Type> iterator = senderTypes.iterator(); iterator.hasNext();) {
            final Type existingType = iterator.next();
            if (GenericTypeReflector.isSuperType(existingType, senderType)) {
                add = false;
                break;
            }
            if (GenericTypeReflector.isSuperType(senderType, existingType)) {
                iterator.remove();
                break;
            }
        }
        if (add) {
            senderTypes.add(senderType);
        }
    }

    /**
     * Verifies that there is no illegal ambiguity in the given {@code node}.
     *
     * @param node the node
     * @throws AmbiguousNodeException if the node breaks some ambiguity contract
     */
    private void checkAmbiguity(final @NonNull CommandNode<C> node) throws AmbiguousNodeException {
        if (node.isLeaf()) {
            return;
        }

        // List of child nodes that are not static arguments, but (parsed) variable ones
        final List<CommandNode<C>> childVariableArguments = node.children()
                .stream()
                .filter(n -> n.component() != null)
                .filter(n -> n.component().type() != CommandComponent.ComponentType.LITERAL)
                .collect(Collectors.toList());

        // If more than one child node exists with a variable argument, fail
        if (childVariableArguments.size() > 1) {
            final CommandNode<C> child = childVariableArguments.get(0);

            throw new AmbiguousNodeException(
                    node,
                    child,
                    node.children()
                            .stream()
                            .filter(n -> n.component() != null)
                            .collect(Collectors.toList())
            );
        }

        // List of child nodes that are static arguments, with fixed values
        final List<CommandNode<C>> childStaticArguments = node.children()
                .stream()
                .filter(n -> n.component() != null)
                .filter(n -> n.component().type() == CommandComponent.ComponentType.LITERAL)
                .collect(Collectors.toList());

        // Check none of the static arguments are equal to another one
        // This is done by filling a set and checking there are no duplicates
        final Set<String> checkedLiterals = new HashSet<>();
        for (final CommandNode<C> child : childStaticArguments) {
            for (final String nameOrAlias : child.component().aliases()) {
                if (!checkedLiterals.add(nameOrAlias)) {
                    // Same literal value, ambiguity detected
                    throw new AmbiguousNodeException(
                            node,
                            child,
                            node.children()
                                    .stream()
                                    .filter(n -> n.component() != null)
                                    .collect(Collectors.toList())
                    );
                }
            }
        }

        // Recursively check child nodes as well
        node.children().forEach(this::checkAmbiguity);
    }

    /**
     * Returns all leaf nodes attached to the given {@code node} or its children.
     *
     * @param node the node
     * @return the leaf nodes attached to the node
     */
    @API(status = API.Status.INTERNAL)
    public @NonNull List<@NonNull CommandNode<C>> getLeavesRaw(
            final @NonNull CommandNode<C> node
    ) {
        final List<CommandNode<C>> leaves = new LinkedList<>();
        if (node.isLeaf()) {
            if (node.component() != null) {
                leaves.add(node);
            }
        } else {
            node.children().forEach(child -> leaves.addAll(this.getLeavesRaw(child)));
        }
        return leaves;
    }

    private @NonNull List<@NonNull CommandNode<C>> getExecutorNodes(
            final @NonNull CommandNode<C> node
    ) {
        final List<CommandNode<C>> leaves = new LinkedList<>();
        if (node.command() != null) {
            leaves.add(node);
        }
        for (final CommandNode<C> child : node.children()) {
            leaves.addAll(this.getExecutorNodes(child));
        }
        return leaves;
    }

    /**
     * Returns all leaf nodes attached to the given {@code node} or its children.
     *
     * @param node the node
     * @return the leaf nodes attached to the node
     */
    @API(status = API.Status.INTERNAL)
    public @NonNull List<@NonNull CommandNode<C>> getLeaves(
            final @NonNull CommandNode<C> node
    ) {
        return this.getLeavesRaw(node).stream()
                .filter(n -> n.component() != null)
                .collect(Collectors.toList());
    }

    /**
     * Returns an ordered list containing the chain of components that leads up to the given {@code end} node.
     *
     * @param end the end node
     * @return the list of components leading up to the {@code end} node
     */
    private @NonNull List<@NonNull CommandComponent<?>> getComponentChain(
            final @NonNull CommandNode<C> end
    ) {
        return this.getChain(end).stream()
                .map(CommandNode::component)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Returns an ordered list containing the chain of nodes that leads up to the given {@code end} node.
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

        final @Nullable CommandComponent<C> component = node.component();
        final @Nullable Command<C> owner = component == null ? null : node.command();
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
