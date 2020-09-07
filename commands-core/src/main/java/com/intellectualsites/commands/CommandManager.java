//
// MIT License
//
// Copyright (c) 2020 IntellectualSites
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

import com.intellectualsites.commands.components.CommandSyntaxFormatter;
import com.intellectualsites.commands.components.StandardCommandSyntaxFormatter;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.execution.CommandExecutionCoordinator;
import com.intellectualsites.commands.execution.CommandResult;
import com.intellectualsites.commands.internal.CommandRegistrationHandler;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * The manager is responsible for command registration, parsing delegation, etc.
 *
 * @param <C> Command sender type
 */
public abstract class CommandManager<C extends CommandSender> {

    private final CommandExecutionCoordinator<C> commandExecutionCoordinator;
    private final CommandRegistrationHandler commandRegistrationHandler;
    private final CommandTree<C> commandTree;

    private CommandSyntaxFormatter<C> commandSyntaxFormatter = new StandardCommandSyntaxFormatter<>();

    protected CommandManager(@Nonnull final Function<CommandTree<C>, CommandExecutionCoordinator<C>> commandExecutionCoordinator,
                             @Nonnull final CommandRegistrationHandler commandRegistrationHandler) {
        this.commandTree = CommandTree.newTree(this, commandRegistrationHandler);
        this.commandExecutionCoordinator = commandExecutionCoordinator.apply(commandTree);
        this.commandRegistrationHandler = commandRegistrationHandler;
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
        final CommandContext<C> context = new CommandContext<>(commandSender);
        return this.commandExecutionCoordinator.coordinateExecution(context, tokenize(input));
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
        final CommandContext<C> context = new CommandContext<>(commandSender);
        return this.commandTree.getSuggestions(context, tokenize(input));
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
    public CommandManager<C> registerCommand(@Nonnull final Command<C> command) {
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
    protected CommandRegistrationHandler getCommandRegistrationHandler() {
        return this.commandRegistrationHandler;
    }

    /**
     * Create a new command builder
     *
     * @param name Command name
     * @return Builder instance
     */
    @Nonnull
    public Command.Builder<C> commandBuilder(@Nonnull final String name) {
        return Command.newBuilder(name);
    }

}
