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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.arguments.CommandSyntaxFormatter;
import com.intellectualsites.commands.arguments.StandardCommandSyntaxFormatter;
import com.intellectualsites.commands.arguments.parser.ParserRegistry;
import com.intellectualsites.commands.arguments.parser.StandardParserRegistry;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.context.CommandContextFactory;
import com.intellectualsites.commands.context.StandardCommandContextFactory;
import com.intellectualsites.commands.execution.CommandExecutionCoordinator;
import com.intellectualsites.commands.execution.CommandResult;
import com.intellectualsites.commands.execution.CommandSuggestionProcessor;
import com.intellectualsites.commands.execution.FilteringCommandSuggestionProcessor;
import com.intellectualsites.commands.execution.preprocessor.AcceptingCommandPreprocessor;
import com.intellectualsites.commands.execution.preprocessor.CommandPreprocessingContext;
import com.intellectualsites.commands.execution.preprocessor.CommandPreprocessor;
import com.intellectualsites.commands.internal.CommandRegistrationHandler;
import com.intellectualsites.commands.meta.CommandMeta;
import com.intellectualsites.services.ServicePipeline;
import com.intellectualsites.services.State;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * The manager is responsible for command registration, parsing delegation, etc.
 *
 * @param <C> Command sender type
 */
@SuppressWarnings("unused")
public abstract class CommandManager<C> {

    private final CommandContextFactory<C> commandContextFactory = new StandardCommandContextFactory<>();
    private final ServicePipeline servicePipeline = ServicePipeline.builder().build();
    private final ParserRegistry<C> parserRegistry = new StandardParserRegistry<>();
    private final Map<Class<? extends Exception>, BiConsumer<C, ? extends Exception>> exceptionHandlers = Maps.newHashMap();
    private final Collection<Command<C>> commands = Lists.newLinkedList();

    private final CommandExecutionCoordinator<C> commandExecutionCoordinator;
    private final CommandTree<C> commandTree;

    private CommandSyntaxFormatter<C> commandSyntaxFormatter = new StandardCommandSyntaxFormatter<>();
    private CommandSuggestionProcessor<C> commandSuggestionProcessor = new FilteringCommandSuggestionProcessor<>();
    private CommandRegistrationHandler commandRegistrationHandler;

    /**
     * Create a new command manager instance
     *
     * @param commandExecutionCoordinator Execution coordinator instance
     * @param commandRegistrationHandler  Command registration handler
     */
    public CommandManager(
            @Nonnull final Function<CommandTree<C>, CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            @Nonnull final CommandRegistrationHandler commandRegistrationHandler) {
        this.commandTree = CommandTree.newTree(this);
        this.commandExecutionCoordinator = commandExecutionCoordinator.apply(commandTree);
        this.commandRegistrationHandler = commandRegistrationHandler;
        this.servicePipeline.registerServiceType(new TypeToken<CommandPreprocessor<C>>() {
        }, new AcceptingCommandPreprocessor<>());
    }

    /**
     * Tokenize an input string
     *
     * @param input Input string
     * @return List of tokens
     */
    @Nonnull
    public static LinkedList<String> tokenize(@Nonnull final String input) {
        final StringTokenizer stringTokenizer = new StringTokenizer(input, " ");
        final LinkedList<String> tokens = new LinkedList<>();
        while (stringTokenizer.hasMoreElements()) {
            tokens.add(stringTokenizer.nextToken());
        }
        if (input.endsWith(" ")) {
            tokens.add("");
        }
        return tokens;
    }

    /**
     * Execute a command and get a future that completes with the result
     *
     * @param commandSender Sender of the command
     * @param input         Input provided by the sender
     * @return Command result
     */
    @Nonnull
    public CompletableFuture<CommandResult<C>> executeCommand(@Nonnull final C commandSender, @Nonnull final String input) {
        final CommandContext<C> context = this.commandContextFactory.create(false, commandSender);
        final LinkedList<String> inputQueue = tokenize(input);
        try {
            if (this.preprocessContext(context, inputQueue) == State.ACCEPTED) {
                return this.commandExecutionCoordinator.coordinateExecution(context, inputQueue);
            }
        } catch (final Exception e) {
            final CompletableFuture<CommandResult<C>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
        /* Wasn't allowed to execute the command */
        return CompletableFuture.completedFuture(null);
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
        final CommandContext<C> context = this.commandContextFactory.create(true, commandSender);
        final LinkedList<String> inputQueue = tokenize(input);
        if (this.preprocessContext(context, inputQueue) == State.ACCEPTED) {
            return this.commandSuggestionProcessor.apply(new CommandPreprocessingContext<>(context, inputQueue),
                                                         this.commandTree.getSuggestions(
                                                                 context, inputQueue));
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Register a new command
     *
     * @param command Command to register
     * @return The command manager instance
     */
    public CommandManager<C> command(@Nonnull final Command<C> command) {
        this.commandTree.insertCommand(command);
        this.commands.add(command);
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

    protected final void setCommandRegistrationHandler(@Nonnull final CommandRegistrationHandler commandRegistrationHandler) {
        this.commandRegistrationHandler = commandRegistrationHandler;
    }

    /**
     * Check if the command sender has the required permission. If the permission node is
     * empty, this should return {@code true}
     *
     * @param sender     Command sender
     * @param permission Permission node
     * @return {@code true} if the sender has the permission, else {@code false}
     */
    public abstract boolean hasPermission(@Nonnull C sender, @Nonnull String permission);

    /**
     * Create a new command builder
     *
     * @param name    Command name
     * @param aliases Command aliases
     * @param meta    Command meta
     * @return Builder instance
     */
    @Nonnull
    public Command.Builder<C> commandBuilder(@Nonnull final String name,
                                             @Nonnull final Collection<String> aliases,
                                             @Nonnull final CommandMeta meta) {
        return Command.newBuilder(name, meta, aliases.toArray(new String[0]));
    }

    /**
     * Create a new command builder
     *
     * @param name    Command name
     * @param meta    Command meta
     * @param aliases Command aliases
     * @return Builder instance
     */
    @Nonnull
    public Command.Builder<C> commandBuilder(@Nonnull final String name,
                                             @Nonnull final CommandMeta meta,
                                             @Nonnull final String... aliases) {
        return Command.newBuilder(name, meta, aliases);
    }

    /**
     * Create a new command builder using a default command meta instance.
     *
     * @param name    Command name
     * @param aliases Command aliases
     * @return Builder instance
     * @throws UnsupportedOperationException If the command manager does not support default command meta creation
     * @see #createDefaultCommandMeta() Default command meta creation
     */
    @Nonnull
    public Command.Builder<C> commandBuilder(@Nonnull final String name, @Nonnull final String... aliases) {
        return Command.<C>newBuilder(name, this.createDefaultCommandMeta(), aliases).manager(this);
    }

    /**
     * Create a new command argument builder
     *
     * @param type Argument type
     * @param name Argument name
     * @param <T>  Generic argument name
     * @return Argument builder
     */
    @Nonnull
    public <T> CommandArgument.Builder<C, T> argumentBuilder(@Nonnull final Class<T> type, @Nonnull final String name) {
        return CommandArgument.<C, T>ofType(type, name).manager(this);
    }

    /**
     * Get the internal command tree. This should not be accessed unless you know what you
     * are doing
     *
     * @return Command tree
     */
    @Nonnull
    public CommandTree<C> getCommandTree() {
        return this.commandTree;
    }

    /**
     * Construct a default command meta instance
     *
     * @return Default command meta
     * @throws UnsupportedOperationException If the command manager does not support this operation
     */
    @Nonnull
    public abstract CommandMeta createDefaultCommandMeta();

    /**
     * Register a new command preprocessor. The order they are registered in is respected, and they
     * are called in LIFO order
     *
     * @param processor Processor to register
     * @see #preprocessContext(CommandContext, LinkedList) Preprocess a context
     */
    public void registerCommandPreProcessor(@Nonnull final CommandPreprocessor<C> processor) {
        this.servicePipeline.registerServiceImplementation(new TypeToken<CommandPreprocessor<C>>() {
                                                           }, processor,
                                                           Collections.emptyList());
    }

    /**
     * Preprocess a command context instance
     *
     * @param context    Command context
     * @param inputQueue Command input as supplied by sender
     * @return {@link State#ACCEPTED} if the command should be parsed and executed, else {@link State#REJECTED}
     * @see #registerExceptionHandler(Class, BiConsumer) Register a command preprocessor
     */
    public State preprocessContext(@Nonnull final CommandContext<C> context, @Nonnull final LinkedList<String> inputQueue) {
        this.servicePipeline.pump(new CommandPreprocessingContext<>(context, inputQueue))
                            .through(new TypeToken<CommandPreprocessor<C>>() {
                            })
                            .getResult();
        return context.<String>get(AcceptingCommandPreprocessor.PROCESSED_INDICATOR_KEY).orElse("").isEmpty()
               ? State.REJECTED
               : State.ACCEPTED;
    }

    /**
     * Get the command suggestions processor instance currently used in this command manager
     *
     * @return Command suggestions processor
     * @see #setCommandSuggestionProcessor(CommandSuggestionProcessor) Setting the suggestion processor
     */
    @Nonnull
    public CommandSuggestionProcessor<C> getCommandSuggestionProcessor() {
        return this.commandSuggestionProcessor;
    }

    /**
     * Set the command suggestions processor for this command manager. This will be called every
     * time {@link #suggest(Object, String)} is called, to process the list of suggestions
     * before it's returned to the caller
     *
     * @param commandSuggestionProcessor New command suggestions processor
     */
    public void setCommandSuggestionProcessor(@Nonnull final CommandSuggestionProcessor<C> commandSuggestionProcessor) {
        this.commandSuggestionProcessor = commandSuggestionProcessor;
    }

    /**
     * Get the parser registry instance. The parser registry contains default
     * mappings to {@link com.intellectualsites.commands.arguments.parser.ArgumentParser}
     * and allows for the registration of custom mappings. The parser registry also
     * contains mappings of annotations to {@link com.intellectualsites.commands.arguments.parser.ParserParameter}
     * which allows for annotations to be used to customize parser settings.
     * <p>
     * When creating a new parser type, it is recommended to register it in the parser
     * registry. In particular, default parser types (shipped with cloud implementations)
     * should be registered in the constructor of the platform {@link CommandManager}
     *
     * @return Parser registry instance
     */
    @Nonnull
    public ParserRegistry<C> getParserRegistry() {
        return this.parserRegistry;
    }

    /**
     * Get the exception handler for an exception type, if one has been registered
     *
     * @param clazz Exception class
     * @param <E>   Exception type
     * @return Exception handler, or {@code null}
     * @see #registerCommandPreProcessor(CommandPreprocessor) Registering an exception handler
     */
    @Nullable
    public final <E extends Exception> BiConsumer<C, E> getExceptionHandler(@Nullable final Class<E> clazz) {
        final BiConsumer<C, ? extends Exception> consumer = this.exceptionHandlers.get(clazz);
        if (consumer == null) {
            return null;
        }
        //noinspection unchecked
        return (BiConsumer<C, E>) consumer;
    }

    /**
     * Register an exception handler for an exception type. This will then be used
     * when {@link #handleException(Object, Class, Exception, BiConsumer)} is called
     * for the particular exception type
     *
     * @param clazz   Exception class
     * @param handler Exception handler
     * @param <E>     Exception type
     */
    public final <E extends Exception> void registerExceptionHandler(@Nonnull final Class<E> clazz,
                                                                     @Nonnull final BiConsumer<C, E> handler) {
        this.exceptionHandlers.put(clazz, handler);
    }

    /**
     * Handle an exception using the registered exception handler for the exception type, or using the
     * provided default handler if no exception handler has been registered for the exception type
     *
     * @param sender         Executing command sender
     * @param clazz          Exception class
     * @param exception      Exception instance
     * @param defaultHandler Default exception handler. Will be called if there is no exception
     *                       handler stored for the exception type
     * @param <E>            Exception type
     */
    public final <E extends Exception> void handleException(@Nonnull final C sender,
                                                            @Nonnull final Class<E> clazz,
                                                            @Nonnull final E exception,
                                                            @Nonnull final BiConsumer<C, E> defaultHandler) {
        Optional.ofNullable(this.getExceptionHandler(clazz)).orElse(defaultHandler).accept(sender, exception);
    }

    /**
     * Get a collection containing all registered commands.
     *
     * @return Unmodifiable view of all registered commands
     */
    @Nonnull
    public final Collection<Command<C>> getCommands() {
        return Collections.unmodifiableCollection(this.commands);
    }

    /**
     * Get a command help handler instance. This can be used to assist in the production
     * of command help menus, etc.
     *
     * @return Command help handler. A new instance will be created
     * each time this method is called.
     */
    @Nonnull
    public final CommandHelpHandler<C> getCommandHelpHandler() {
        return new CommandHelpHandler<>(this);
    }

}
