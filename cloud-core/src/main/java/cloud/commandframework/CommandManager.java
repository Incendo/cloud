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
import cloud.commandframework.arguments.CommandSyntaxFormatter;
import cloud.commandframework.arguments.StandardCommandSyntaxFormatter;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserParameter;
import cloud.commandframework.arguments.parser.ParserRegistry;
import cloud.commandframework.arguments.parser.StandardParserRegistry;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandContextFactory;
import cloud.commandframework.context.StandardCommandContextFactory;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.CommandResult;
import cloud.commandframework.execution.CommandSuggestionProcessor;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import cloud.commandframework.execution.postprocessor.AcceptingCommandPostprocessor;
import cloud.commandframework.execution.postprocessor.CommandPostprocessingContext;
import cloud.commandframework.execution.postprocessor.CommandPostprocessor;
import cloud.commandframework.execution.preprocessor.AcceptingCommandPreprocessor;
import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import cloud.commandframework.execution.preprocessor.CommandPreprocessor;
import cloud.commandframework.internal.CommandRegistrationHandler;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.permission.CommandPermission;
import cloud.commandframework.permission.OrPermission;
import cloud.commandframework.permission.Permission;
import cloud.commandframework.services.ServicePipeline;
import cloud.commandframework.services.State;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
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
public abstract class CommandManager<C> {

    private final Map<Class<? extends Exception>, BiConsumer<C, ? extends Exception>> exceptionHandlers = new HashMap<>();
    private final EnumSet<ManagerSettings> managerSettings = EnumSet.of(
            ManagerSettings.ENFORCE_INTERMEDIARY_PERMISSIONS);

    private final CommandContextFactory<C> commandContextFactory = new StandardCommandContextFactory<>();
    private final ServicePipeline servicePipeline = ServicePipeline.builder().build();
    private final ParserRegistry<C> parserRegistry = new StandardParserRegistry<>();
    private final Collection<Command<C>> commands = new LinkedList<>();
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
            final @NonNull Function<@NonNull CommandTree<C>, @NonNull CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @NonNull CommandRegistrationHandler commandRegistrationHandler
    ) {
        this.commandTree = CommandTree.newTree(this);
        this.commandExecutionCoordinator = commandExecutionCoordinator.apply(commandTree);
        this.commandRegistrationHandler = commandRegistrationHandler;
        this.servicePipeline.registerServiceType(new TypeToken<CommandPreprocessor<C>>() {
        }, new AcceptingCommandPreprocessor<>());
        this.servicePipeline.registerServiceType(new TypeToken<CommandPostprocessor<C>>() {
        }, new AcceptingCommandPostprocessor<>());
    }

    /**
     * Tokenize an input string
     *
     * @param input Input string
     * @return List of tokens
     */
    public static @NonNull LinkedList<@NonNull String> tokenize(final @NonNull String input) {
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
    public @NonNull CompletableFuture<CommandResult<C>> executeCommand(
            final @NonNull C commandSender,
            final @NonNull String input
    ) {
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
    public @NonNull List<@NonNull String> suggest(
            final @NonNull C commandSender,
            final @NonNull String input
    ) {
        final CommandContext<C> context = this.commandContextFactory.create(true, commandSender);
        final LinkedList<String> inputQueue = tokenize(input);
        if (this.preprocessContext(context, inputQueue) == State.ACCEPTED) {
            return this.commandSuggestionProcessor.apply(
                    new CommandPreprocessingContext<>(context, inputQueue),
                    this.commandTree.getSuggestions(
                            context, inputQueue)
            );
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
    public @NonNull CommandManager<C> command(final @NonNull Command<C> command) {
        this.commandTree.insertCommand(command);
        this.commands.add(command);
        return this;
    }

    /**
     * Register a new command
     *
     * @param command Command to register. {@link Command.Builder#build()}} will be invoked.
     * @return The command manager instance
     */
    public @NonNull CommandManager<C> command(final Command.@NonNull Builder<C> command) {
        return this.command(command.manager(this).build());
    }

    /**
     * Get the command syntax formatter
     *
     * @return Command syntax formatter
     */
    public @NonNull CommandSyntaxFormatter<C> getCommandSyntaxFormatter() {
        return this.commandSyntaxFormatter;
    }

    /**
     * Set the command syntax formatter
     *
     * @param commandSyntaxFormatter New formatter
     */
    public void setCommandSyntaxFormatter(final @NonNull CommandSyntaxFormatter<C> commandSyntaxFormatter) {
        this.commandSyntaxFormatter = commandSyntaxFormatter;
    }

    /**
     * Get the command registration handler
     *
     * @return Command registration handler
     */
    public @NonNull CommandRegistrationHandler getCommandRegistrationHandler() {
        return this.commandRegistrationHandler;
    }

    protected final void setCommandRegistrationHandler(final @NonNull CommandRegistrationHandler commandRegistrationHandler) {
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
    public boolean hasPermission(
            final @NonNull C sender,
            final @NonNull CommandPermission permission
    ) {
        if (permission.toString().isEmpty()) {
            return true;
        }
        if (permission instanceof Permission) {
            return hasPermission(sender, permission.toString());
        }
        for (final CommandPermission innerPermission : permission.getPermissions()) {
            final boolean hasPermission = this.hasPermission(sender, innerPermission);
            if (permission instanceof OrPermission) {
                if (hasPermission) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if the command sender has the required permission. If the permission node is
     * empty, this should return {@code true}
     *
     * @param sender     Command sender
     * @param permission Permission node
     * @return {@code true} if the sender has the permission, else {@code false}
     */
    public abstract boolean hasPermission(@NonNull C sender, @NonNull String permission);

    /**
     * Create a new command builder
     *
     * @param name        Command name
     * @param aliases     Command aliases
     * @param description Command description
     * @param meta        Command meta
     * @return Builder instance
     */
    public Command.@NonNull Builder<C> commandBuilder(
            final @NonNull String name,
            final @NonNull Collection<String> aliases,
            final @NonNull Description description,
            final @NonNull CommandMeta meta
    ) {
        return Command.newBuilder(name, meta, description, aliases.toArray(new String[0]));
    }

    /**
     * Create a new command builder
     *
     * @param name    Command name
     * @param aliases Command aliases
     * @param meta    Command meta
     * @return Builder instance
     */
    public Command.@NonNull Builder<C> commandBuilder(
            final @NonNull String name,
            final @NonNull Collection<String> aliases,
            final @NonNull CommandMeta meta
    ) {
        return Command.newBuilder(name, meta, Description.empty(), aliases.toArray(new String[0]));
    }

    /**
     * Create a new command builder
     *
     * @param name        Command name
     * @param meta        Command meta
     * @param description Command description
     * @param aliases     Command aliases
     * @return Builder instance
     */
    public Command.@NonNull Builder<C> commandBuilder(
            final @NonNull String name,
            final @NonNull CommandMeta meta,
            final @NonNull Description description,
            final @NonNull String... aliases
    ) {
        return Command.newBuilder(name, meta, description, aliases);
    }

    /**
     * Create a new command builder
     *
     * @param name    Command name
     * @param meta    Command meta
     * @param aliases Command aliases
     * @return Builder instance
     */
    public Command.@NonNull Builder<C> commandBuilder(
            final @NonNull String name,
            final @NonNull CommandMeta meta,
            final @NonNull String... aliases
    ) {
        return Command.newBuilder(name, meta, Description.empty(), aliases);
    }

    /**
     * Create a new command builder using a default command meta instance.
     *
     * @param name        Command name
     * @param description Command description
     * @param aliases     Command aliases
     * @return Builder instance
     * @throws UnsupportedOperationException If the command manager does not support default command meta creation
     * @see #createDefaultCommandMeta() Default command meta creation
     */
    public Command.@NonNull Builder<C> commandBuilder(
            final @NonNull String name,
            final @NonNull Description description,
            final @NonNull String... aliases
    ) {
        return Command.<C>newBuilder(name, this.createDefaultCommandMeta(), description, aliases).manager(this);
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
    public Command.@NonNull Builder<C> commandBuilder(
            final @NonNull String name,
            final @NonNull String... aliases
    ) {
        return Command.<C>newBuilder(name, this.createDefaultCommandMeta(), Description.empty(), aliases).manager(this);
    }

    /**
     * Create a new command argument builder
     *
     * @param type Argument type
     * @param name Argument name
     * @param <T>  Generic argument name
     * @return Argument builder
     */
    public <T> CommandArgument.@NonNull Builder<C, T> argumentBuilder(
            final @NonNull Class<T> type,
            final @NonNull String name
    ) {
        return CommandArgument.<C, T>ofType(type, name).manager(this);
    }

    /**
     * Create a new command flag builder
     *
     * @param name Flag name
     * @return Flag builder
     */
    public CommandFlag.@NonNull Builder<Void> flagBuilder(final @NonNull String name) {
        return CommandFlag.newBuilder(name);
    }

    /**
     * Get the internal command tree. This should not be accessed unless you know what you
     * are doing
     *
     * @return Command tree
     */
    public @NonNull CommandTree<C> getCommandTree() {
        return this.commandTree;
    }

    /**
     * Construct a default command meta instance
     *
     * @return Default command meta
     * @throws UnsupportedOperationException If the command manager does not support this operation
     */
    public abstract @NonNull CommandMeta createDefaultCommandMeta();

    /**
     * Register a new command preprocessor. The order they are registered in is respected, and they
     * are called in LIFO order
     *
     * @param processor Processor to register
     * @see #preprocessContext(CommandContext, LinkedList) Preprocess a context
     */
    public void registerCommandPreProcessor(final @NonNull CommandPreprocessor<C> processor) {
        this.servicePipeline.registerServiceImplementation(new TypeToken<CommandPreprocessor<C>>() {
                                                           }, processor,
                Collections.emptyList()
        );
    }

    /**
     * Register a new command postprocessor. The order they are registered in is respected, and they
     * are called in LIFO order
     *
     * @param processor Processor to register
     * @see #preprocessContext(CommandContext, LinkedList) Preprocess a context
     */
    public void registerCommandPostProcessor(final @NonNull CommandPostprocessor<C> processor) {
        this.servicePipeline.registerServiceImplementation(new TypeToken<CommandPostprocessor<C>>() {
                                                           }, processor,
                Collections.emptyList()
        );
    }

    /**
     * Preprocess a command context instance
     *
     * @param context    Command context
     * @param inputQueue Command input as supplied by sender
     * @return {@link State#ACCEPTED} if the command should be parsed and executed, else {@link State#REJECTED}
     * @see #registerCommandPreProcessor(CommandPreprocessor) Register a command preprocessor
     */
    public State preprocessContext(
            final @NonNull CommandContext<C> context,
            final @NonNull LinkedList<@NonNull String> inputQueue
    ) {
        this.servicePipeline.pump(new CommandPreprocessingContext<>(context, inputQueue))
                .through(new TypeToken<CommandPreprocessor<C>>() {
                })
                .getResult();
        return context.<String>getOptional(AcceptingCommandPreprocessor.PROCESSED_INDICATOR_KEY).orElse("").isEmpty()
                ? State.REJECTED
                : State.ACCEPTED;
    }

    /**
     * Postprocess a command context instance
     *
     * @param context Command context
     * @param command Command instance
     * @return {@link State#ACCEPTED} if the command should be parsed and executed, else {@link State#REJECTED}
     * @see #registerCommandPostProcessor(CommandPostprocessor) Register a command postprocessor
     */
    public State postprocessContext(
            final @NonNull CommandContext<C> context,
            final @NonNull Command<C> command
    ) {
        this.servicePipeline.pump(new CommandPostprocessingContext<>(context, command))
                .through(new TypeToken<CommandPostprocessor<C>>() {
                })
                .getResult();
        return context.<String>getOptional(AcceptingCommandPostprocessor.PROCESSED_INDICATOR_KEY).orElse("").isEmpty()
                ? State.REJECTED
                : State.ACCEPTED;
    }

    /**
     * Get the command suggestions processor instance currently used in this command manager
     *
     * @return Command suggestions processor
     * @see #setCommandSuggestionProcessor(CommandSuggestionProcessor) Setting the suggestion processor
     */
    public @NonNull CommandSuggestionProcessor<C> getCommandSuggestionProcessor() {
        return this.commandSuggestionProcessor;
    }

    /**
     * Set the command suggestions processor for this command manager. This will be called every
     * time {@link #suggest(Object, String)} is called, to process the list of suggestions
     * before it's returned to the caller
     *
     * @param commandSuggestionProcessor New command suggestions processor
     */
    public void setCommandSuggestionProcessor(final @NonNull CommandSuggestionProcessor<C> commandSuggestionProcessor) {
        this.commandSuggestionProcessor = commandSuggestionProcessor;
    }

    /**
     * Get the parser registry instance. The parser registry contains default
     * mappings to {@link ArgumentParser}
     * and allows for the registration of custom mappings. The parser registry also
     * contains mappings of annotations to {@link ParserParameter}
     * which allows for annotations to be used to customize parser settings.
     * <p>
     * When creating a new parser type, it is recommended to register it in the parser
     * registry. In particular, default parser types (shipped with cloud implementations)
     * should be registered in the constructor of the platform {@link CommandManager}
     *
     * @return Parser registry instance
     */
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
    public final <E extends Exception> @Nullable BiConsumer<@NonNull C, @NonNull E>
    getExceptionHandler(final @NonNull Class<E> clazz) {
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
    public final <E extends Exception> void registerExceptionHandler(
            final @NonNull Class<E> clazz,
            final @NonNull BiConsumer<@NonNull C, @NonNull E> handler
    ) {
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
    public final <E extends Exception> void handleException(
            final @NonNull C sender,
            final @NonNull Class<E> clazz,
            final @NonNull E exception,
            final @NonNull BiConsumer<C, E> defaultHandler
    ) {
        Optional.ofNullable(this.getExceptionHandler(clazz)).orElse(defaultHandler).accept(sender, exception);
    }

    /**
     * Get a collection containing all registered commands.
     *
     * @return Unmodifiable view of all registered commands
     */
    public final @NonNull Collection<@NonNull Command<C>> getCommands() {
        return Collections.unmodifiableCollection(this.commands);
    }

    /**
     * Get a command help handler instance. This can be used to assist in the production
     * of command help menus, etc.
     *
     * @return Command help handler. A new instance will be created
     *         each time this method is called.
     */
    public final @NonNull CommandHelpHandler<C> getCommandHelpHandler() {
        return new CommandHelpHandler<>(this);
    }

    /**
     * Get a command manager setting
     *
     * @param setting Setting
     * @return {@code true} if the setting is activated or {@code false} if it's not
     */
    public boolean getSetting(final @NonNull ManagerSettings setting) {
        return this.managerSettings.contains(setting);
    }

    /**
     * Set the setting
     *
     * @param setting Setting to set
     * @param value   Value
     */
    @SuppressWarnings("unused")
    public void setSetting(
            final @NonNull ManagerSettings setting,
            final boolean value
    ) {
        if (value) {
            this.managerSettings.add(setting);
        } else {
            this.managerSettings.remove(setting);
        }
    }


    /**
     * Configurable command related settings
     */
    public enum ManagerSettings {
        /**
         * Do not create a compound permission and do not look greedily
         * for child permission values, if a preceding command in the tree path
         * has a command handler attached
         */
        ENFORCE_INTERMEDIARY_PERMISSIONS
    }

}
