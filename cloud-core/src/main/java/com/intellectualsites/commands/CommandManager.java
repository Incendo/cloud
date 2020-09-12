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
package com.intellectualsites.commands;

import com.google.common.reflect.TypeToken;
import com.intellectualsites.commands.components.CommandSyntaxFormatter;
import com.intellectualsites.commands.components.StandardCommandSyntaxFormatter;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.context.CommandContextFactory;
import com.intellectualsites.commands.context.StandardCommandContextFactory;
import com.intellectualsites.commands.execution.CommandExecutionCoordinator;
import com.intellectualsites.commands.execution.CommandResult;
import com.intellectualsites.commands.execution.preprocessor.CommandPreProcessor;
import com.intellectualsites.commands.execution.preprocessor.CommandPreprocessingContext;
import com.intellectualsites.commands.execution.preprocessor.NullCommandPreProcessor;
import com.intellectualsites.commands.internal.CommandRegistrationHandler;
import com.intellectualsites.commands.meta.CommandMeta;
import com.intellectualsites.commands.sender.CommandSender;
import com.intellectualsites.services.ServicePipeline;
import com.intellectualsites.services.State;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * The manager is responsible for command registration, parsing delegation, etc.
 *
 * @param <C> Command sender type
 * @param <M> Command meta type
 */
@SuppressWarnings("unused")
public abstract class CommandManager<C extends CommandSender, M extends CommandMeta> {

    private final CommandContextFactory<C> commandContextFactory = new StandardCommandContextFactory<>();

    private final ServicePipeline servicePipeline = ServicePipeline.builder().build();
    private final CommandExecutionCoordinator<C, M> commandExecutionCoordinator;
    private final CommandRegistrationHandler<M> commandRegistrationHandler;
    private final CommandTree<C, M> commandTree;

    private CommandSyntaxFormatter<C> commandSyntaxFormatter = new StandardCommandSyntaxFormatter<>();

    /**
     * Create a new command manager instance
     *
     * @param commandExecutionCoordinator Execution coordinator instance
     * @param commandRegistrationHandler  Command registration handler
     */
    public CommandManager(
            @Nonnull final Function<CommandTree<C, M>, CommandExecutionCoordinator<C, M>> commandExecutionCoordinator,
            @Nonnull final CommandRegistrationHandler<M> commandRegistrationHandler) {
        this.commandTree = CommandTree.newTree(this, commandRegistrationHandler);
        this.commandExecutionCoordinator = commandExecutionCoordinator.apply(commandTree);
        this.commandRegistrationHandler = commandRegistrationHandler;
        this.servicePipeline.registerServiceType(new TypeToken<CommandPreProcessor<C>>() {
        }, new NullCommandPreProcessor<>());
    }

    /**
     * Execute a command and get a future that completes with the result
     *
     * @param commandSender Sender of the command
     * @param input         Input provided by the sender
     * @return Command result
     */
    @Nonnull
    public CompletableFuture<CommandResult> executeCommand(@Nonnull final C commandSender, @Nonnull final String input) {
        return this.commandExecutionCoordinator.coordinateExecution(this.commandContextFactory.create(commandSender),
                                                                    tokenize(input));
    }

    /**
     * Get command suggestions for the "next" argument that would yield a correctly
     * parsing command input
     *
     * @param commandSender Sender of the command
     * @param input         Input provided by the sender
     * @return List of suggestions
     */
    @Nonnull
    public List<String> suggest(@Nonnull final C commandSender, @Nonnull final String input) {
        return this.commandTree.getSuggestions(this.commandContextFactory.create(commandSender), tokenize(input));
    }

    @Nonnull
    private Queue<String> tokenize(@Nonnull final String input) {
        final StringTokenizer stringTokenizer = new StringTokenizer(input, " ");
        final Queue<String> tokens = new LinkedList<>();
        while (stringTokenizer.hasMoreElements()) {
            tokens.add(stringTokenizer.nextToken());
        }
        return tokens;
    }

    /**
     * Register a new command
     *
     * @param command Command to register
     * @return The command manager instance
     */
    public CommandManager<C, M> registerCommand(@Nonnull final Command<C, M> command) {
        this.commandTree.insertCommand(command);
        return this;
    }

    /**
     * Get the command syntax formatter
     *
     * @return Command syntax formatter
     */
    @Nonnull
    public CommandSyntaxFormatter<C> getCommandSyntaxFormatter() {
        return this.commandSyntaxFormatter;
    }

    /**
     * Set the command syntax formatter
     *
     * @param commandSyntaxFormatter New formatter
     */
    public void setCommandSyntaxFormatter(@Nonnull final CommandSyntaxFormatter<C> commandSyntaxFormatter) {
        this.commandSyntaxFormatter = commandSyntaxFormatter;
    }

    /**
     * Get the command registration handler
     *
     * @return Command registration handler
     */
    @Nonnull
    protected CommandRegistrationHandler<M> getCommandRegistrationHandler() {
        return this.commandRegistrationHandler;
    }

    /**
     * Create a new command builder
     *
     * @param name Command name
     * @param meta Command meta
     * @return Builder instance
     */
    @Nonnull
    public Command.Builder<C, M> commandBuilder(@Nonnull final String name, @Nonnull final M meta) {
        return Command.newBuilder(name, meta);
    }

    /**
     * Create a new command builder using a default command meta instance.
     *
     * @param name Command name
     * @return Builder instance
     * @throws UnsupportedOperationException If the command manager does not support default command meta creation
     * @see #createDefaultCommandMeta() Default command meta creation
     */
    @Nonnull
    public Command.Builder<C, M> commandBuilder(@Nonnull final String name) {
        return Command.newBuilder(name, this.createDefaultCommandMeta());
    }


    /**
     * Get the internal command tree. This should not be accessed unless you know what you
     * are doing
     *
     * @return Command tree
     */
    @Nonnull
    CommandTree<C, M> getCommandTree() {
        return this.commandTree;
    }

    /**
     * Construct a default command meta instance
     *
     * @return Default command meta
     * @throws UnsupportedOperationException If the command manager does not support this operation
     */
    @Nonnull
    public abstract M createDefaultCommandMeta();

    /**
     * Register a new command preprocessor. The order they are registered in is respected, and they
     * are called in LIFO order
     *
     * @param processor Processor to register
     */
    public void registerCommandPreProcessor(@Nonnull final CommandPreProcessor<C> processor) {
        this.servicePipeline.registerServiceImplementation(new TypeToken<CommandPreProcessor<C>>() {
                                                           }, processor,
                                                           Collections.emptyList());
    }

    /**
     * Preprocess a command context instance
     *
     * @param context    Command context
     * @param inputQueue Command input as supplied by sender
     * @return {@link State#ACCEPTED} if the command should be parsed and executed, else {@link State#REJECTED}
     */
    public State preprocessContext(@Nonnull final CommandContext<C> context, @Nonnull final Queue<String> inputQueue) {
        return this.servicePipeline.pump(new CommandPreprocessingContext<>(context, inputQueue))
                                   .through(new TypeToken<CommandPreProcessor<C>>() {
                                   })
                                   .getResult() == State.ACCEPTED ? State.REJECTED : State.ACCEPTED;
    }

}
