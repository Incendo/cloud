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

import io.leangen.geantyref.TypeToken;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionFormatter;
import org.incendo.cloud.caption.CaptionRegistry;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionsProvider;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandContextFactory;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.context.StandardCommandContextFactory;
import org.incendo.cloud.exception.handling.ExceptionController;
import org.incendo.cloud.execution.CommandExecutor;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.execution.postprocessor.AcceptingCommandPostprocessor;
import org.incendo.cloud.execution.postprocessor.CommandPostprocessingContext;
import org.incendo.cloud.execution.postprocessor.CommandPostprocessor;
import org.incendo.cloud.execution.preprocessor.AcceptingCommandPreprocessor;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessingContext;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessor;
import org.incendo.cloud.help.CommandPredicate;
import org.incendo.cloud.help.HelpHandler;
import org.incendo.cloud.help.HelpHandlerFactory;
import org.incendo.cloud.injection.ParameterInjectorRegistry;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.incendo.cloud.meta.CommandMeta;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserParameter;
import org.incendo.cloud.parser.ParserRegistry;
import org.incendo.cloud.parser.StandardParserRegistry;
import org.incendo.cloud.parser.flag.CommandFlag;
import org.incendo.cloud.permission.AndPermission;
import org.incendo.cloud.permission.OrPermission;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.permission.PermissionResult;
import org.incendo.cloud.permission.PredicatePermission;
import org.incendo.cloud.services.ServicePipeline;
import org.incendo.cloud.services.State;
import org.incendo.cloud.setting.Configurable;
import org.incendo.cloud.setting.ManagerSetting;
import org.incendo.cloud.state.RegistrationState;
import org.incendo.cloud.state.Stateful;
import org.incendo.cloud.suggestion.DelegatingSuggestionFactory;
import org.incendo.cloud.suggestion.FilteringSuggestionProcessor;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionFactory;
import org.incendo.cloud.suggestion.SuggestionMapper;
import org.incendo.cloud.suggestion.SuggestionProcessor;
import org.incendo.cloud.syntax.CommandSyntaxFormatter;
import org.incendo.cloud.syntax.StandardCommandSyntaxFormatter;
import org.incendo.cloud.type.tuple.Pair;
import org.incendo.cloud.type.tuple.Triplet;

import static java.util.Objects.requireNonNull;

/**
 * The manager is responsible for command registration, parsing delegation, etc.
 *
 * @param <C> the command sender type used to execute commands
 */
@SuppressWarnings({"unchecked", "unused"})
@API(status = API.Status.STABLE)
public abstract class CommandManager<C> implements Stateful<RegistrationState>, CommandBuilderSource<C> {

    private final Configurable<ManagerSetting> settings = Configurable.enumConfigurable(ManagerSetting.class)
            .set(ManagerSetting.ENFORCE_INTERMEDIARY_PERMISSIONS, true);
    private final ServicePipeline servicePipeline = ServicePipeline.builder().build();
    private final ParserRegistry<C> parserRegistry = new StandardParserRegistry<>();
    private final Collection<Command<C>> commands = new LinkedList<>();
    private final ParameterInjectorRegistry<C> parameterInjectorRegistry = new ParameterInjectorRegistry<>();
    private final CommandTree<C> commandTree;
    private final SuggestionFactory<C, ? extends Suggestion> suggestionFactory;
    private final Set<CloudCapability> capabilities = new HashSet<>();
    private final ExceptionController<C> exceptionController = new ExceptionController<>();
    private final CommandExecutor<C> commandExecutor;

    private CaptionFormatter<C, String> captionVariableReplacementHandler = CaptionFormatter.placeholderReplacing();
    private CommandSyntaxFormatter<C> commandSyntaxFormatter = new StandardCommandSyntaxFormatter<>(this);
    private SuggestionProcessor<C> suggestionProcessor = new FilteringSuggestionProcessor<>();
    private CommandRegistrationHandler<C> commandRegistrationHandler;
    private CaptionRegistry<C> captionRegistry;
    private HelpHandlerFactory<C> helpHandlerFactory = HelpHandlerFactory.standard(this);
    private SuggestionMapper<? extends Suggestion> mapper = SuggestionMapper.identity();
    private final AtomicReference<RegistrationState> state = new AtomicReference<>(RegistrationState.BEFORE_REGISTRATION);

    /**
     * Create a new command manager instance.
     *
     * @param executionCoordinator       Execution coordinator instance. When choosing the appropriate coordinator for your
     *                                   project, be sure to consider any limitations noted by the platform documentation.
     * @param commandRegistrationHandler Command registration handler. This will get called every time a new command is
     *                                   registered to the command manager. This may be used to forward command registration
     *                                   to the platform.
     */
    protected CommandManager(
            final @NonNull ExecutionCoordinator<C> executionCoordinator,
            final @NonNull CommandRegistrationHandler<C> commandRegistrationHandler
    ) {
        final CommandContextFactory<C> commandContextFactory = new StandardCommandContextFactory<>(this);
        this.commandTree = CommandTree.newTree(this);
        this.commandRegistrationHandler = commandRegistrationHandler;
        this.suggestionFactory = new DelegatingSuggestionFactory<>(
                this,
                this.commandTree,
                commandContextFactory,
                executionCoordinator,
                suggestion -> this.mapper.map(suggestion)
        );
        this.commandExecutor = new StandardCommandExecutor<>(
                this,
                executionCoordinator,
                commandContextFactory
        );
        /* Register service types */
        this.servicePipeline.registerServiceType(new TypeToken<CommandPreprocessor<C>>() {
        }, new AcceptingCommandPreprocessor<>());
        this.servicePipeline.registerServiceType(new TypeToken<CommandPostprocessor<C>>() {
        }, new AcceptingCommandPostprocessor<>());
        /* Create the caption registry */
        this.captionRegistry = CaptionRegistry.captionRegistry();
        this.captionRegistry.registerProvider(new StandardCaptionsProvider<>());
        /* Register default injectors */
        this.parameterInjectorRegistry().registerInjector(
                CommandContext.class,
                (context, annotationAccessor) -> context
        );
    }

    /**
     * Returns the command executor.
     *
     * <p>The executor is used to parse &amp; execute commands.</p>
     *
     * @return the command executor
     */
    @API(status = API.Status.STABLE)
    public @NonNull CommandExecutor<C> commandExecutor() {
        return this.commandExecutor;
    }

    /**
     * Returns the suggestion factory.
     *
     * <p>Will map results using {@link #suggestionMapper()}.</p>
     *
     * @return the suggestion factory
     */
    @API(status = API.Status.STABLE)
    public @NonNull SuggestionFactory<C, ? extends Suggestion> suggestionFactory() {
        return this.suggestionFactory;
    }

    /**
     * Returns the suggestion mapper for {@link #suggestionFactory()}.
     *
     * <p>Platform command managers may replace the default mapper to better support
     * platform suggestion types. Therefore it's encouraged to chain any additional mappers using
     * {@link SuggestionMapper#then(SuggestionMapper)} or {@link #appendSuggestionMapper(SuggestionMapper)},
     * rather than replacing the mapper directly.</p>
     *
     * @return the suggestion mapper
     */
    public @NonNull SuggestionMapper<? extends Suggestion> suggestionMapper() {
        return this.mapper;
    }

    /**
     * Sets the suggestion mapper for {@link #suggestionFactory()} to the result of appending the provided mapper to the
     * current mapper using {@link SuggestionMapper#then(SuggestionMapper)}.
     *
     * @param mapper suggestion mapper
     */
    public void appendSuggestionMapper(final @NonNull SuggestionMapper<? extends  Suggestion> mapper) {
        this.suggestionMapper(this.suggestionMapper().then(mapper));
    }

    /**
     * Sets the suggestion mapper for {@link #suggestionFactory()}.
     *
     * @param mapper suggestion mapper
     * @see #suggestionMapper()
     */
    public void suggestionMapper(final @NonNull SuggestionMapper<? extends  Suggestion> mapper) {
        this.mapper = requireNonNull(mapper, "mapper");
    }

    /**
     * Register a new command to the command manager and insert it into the underlying command tree. The command will be
     * forwarded to the {@link CommandRegistrationHandler} and will, depending on the platform, be forwarded to the platform.
     * <p>
     * Different command manager implementations have different requirements for the command registration. It is possible
     * that a command manager may only allow registration during certain stages of the application lifetime. Read the platform
     * command manager documentation to find out more about your particular platform
     *
     * @param command Command to register
     * @return The command manager instance. This is returned so that these method calls may be chained. This will always
     *         return {@code this}.
     */
    @SuppressWarnings("unchecked")
    public @This @NonNull CommandManager<C> command(final @NonNull Command<? extends C> command) {
        if (!(this.transitionIfPossible(RegistrationState.BEFORE_REGISTRATION, RegistrationState.REGISTERING)
                || this.isCommandRegistrationAllowed())) {
            throw new IllegalStateException("Unable to register commands because the manager is no longer in a registration "
                    + "state. Your platform may allow unsafe registrations by enabling the appropriate manager setting.");
        }
        this.commandTree.insertCommand((Command<C>) command);
        this.commands.add((Command<C>) command);
        return this;
    }

    /**
     * Creates a command using the given {@code commandFactory} and inserts it into the underlying command tree. The command
     * will be forwarded to the {@link CommandRegistrationHandler} and will, depending on the platform, be forwarded to the
     * platform.
     * <p>
     * Different command manager implementations have different requirements for the command registration. It is possible
     * that a command manager may only allow registration during certain stages of the application lifetime. Read the platform
     * command manager documentation to find out more about your particular platform
     *
     * @param commandFactory the command factory to register
     * @return The command manager instance. This is returned so that these method calls may be chained. This will always
     *         return {@code this}
     */
    @API(status = API.Status.STABLE)
    public @This @NonNull CommandManager<C> command(final @NonNull CommandFactory<C> commandFactory) {
        commandFactory.createCommands(this).forEach(this::command);
        return this;
    }

    /**
     * Register a new command
     *
     * @param command Command to register. {@link Command.Builder#build()}} will be invoked.
     * @return The command manager instance
     */
    @SuppressWarnings("unchecked")
    public @NonNull CommandManager<C> command(final Command.@NonNull Builder<? extends C> command) {
        return this.command(((Command.Builder<C>) command).manager(this).build());
    }

    /**
     * Returns the string-producing caption formatter.
     *
     * @return the formatter
     * @see #captionFormatter(CaptionFormatter)
     */
    @API(status = API.Status.STABLE)
    public @NonNull CaptionFormatter<C, String> captionFormatter() {
        return this.captionVariableReplacementHandler;
    }

    /**
     * Sets the string-producing caption formatter.
     *
     * @param captionFormatter the new formatter
     * @see #captionFormatter()
     */
    @API(status = API.Status.STABLE)
    public void captionFormatter(final @NonNull CaptionFormatter<C, String> captionFormatter) {
        this.captionVariableReplacementHandler = captionFormatter;
    }

    /**
     * Returns the command syntax formatter.
     *
     * @return the syntax formatter
     * @see #commandSyntaxFormatter(CommandSyntaxFormatter)
     */
    @API(status = API.Status.STABLE)
    public @NonNull CommandSyntaxFormatter<C> commandSyntaxFormatter() {
        return this.commandSyntaxFormatter;
    }

    /**
     * Sets the command syntax formatter.
     * <p>
     * The command syntax formatter is used to format the command syntax hints that are used in help and error messages.
     *
     * @param commandSyntaxFormatter new formatter
     * @see #commandSyntaxFormatter()
     */
    @API(status = API.Status.STABLE)
    public void commandSyntaxFormatter(final @NonNull CommandSyntaxFormatter<C> commandSyntaxFormatter) {
        this.commandSyntaxFormatter = commandSyntaxFormatter;
    }

    /**
     * Returns the command registration handler.
     * <p>
     * The command registration handler is able to intercept newly created/deleted commands, in order to propagate
     * these changes to the native command handler of the platform.
     * <p>
     * In platforms without a native command concept, this is likely to return
     * {@link CommandRegistrationHandler#nullCommandRegistrationHandler()}.
     *
     * @return the command registration handler
     */
    public @NonNull CommandRegistrationHandler<C> commandRegistrationHandler() {
        return this.commandRegistrationHandler;
    }

    @API(status = API.Status.STABLE)
    protected final void commandRegistrationHandler(final @NonNull CommandRegistrationHandler<C> commandRegistrationHandler) {
        this.requireState(RegistrationState.BEFORE_REGISTRATION);
        this.commandRegistrationHandler = commandRegistrationHandler;
    }

    /**
     * Registers the given {@code capability}.
     *
     * @param capability the capability
     * @see #hasCapability(CloudCapability)
     * @see #capabilities()
     */
    @API(status = API.Status.STABLE)
    protected final void registerCapability(final @NonNull CloudCapability capability) {
        this.capabilities.add(capability);
    }

    /**
     * Checks whether the cloud implementation has the given {@code capability}.
     *
     * @param capability the capability
     * @return {@code true} if the implementation has the {@code capability}, {@code false} if not
     * @see #capabilities()
     */
    @API(status = API.Status.STABLE)
    public boolean hasCapability(final @NonNull CloudCapability capability) {
        return this.capabilities.contains(capability);
    }

    /**
     * Returns an unmodifiable snapshot of the currently registered {@link CloudCapability capabilities}.
     *
     * @return the currently registered capabilities
     * @see #hasCapability(CloudCapability)
     */
    @API(status = API.Status.STABLE)
    public @NonNull Collection<@NonNull CloudCapability> capabilities() {
        return Collections.unmodifiableSet(new HashSet<>(this.capabilities));
    }

    /**
     * Checks if the command sender has the required permission and returns the result.
     *
     * @param sender     the command sender
     * @param permission the permission
     * @return a {@link PermissionResult} representing whether the sender has the permission
     */
    @API(status = API.Status.STABLE)
    @SuppressWarnings("unchecked")
    public @NonNull PermissionResult testPermission(
            final @NonNull C sender,
            final @NonNull Permission permission
    ) {
        if (permission instanceof PredicatePermission) {
            return ((PredicatePermission<C>) permission).testPermission(sender);
        } else if (permission instanceof OrPermission) {
            for (final Permission innerPermission : permission.permissions()) {
                final PermissionResult result = this.testPermission(sender, innerPermission);
                if (result.allowed()) {
                    return result; // short circuit the first true result
                }
            }
            return PermissionResult.denied(permission); // none returned true
        } else if (permission instanceof AndPermission) {
            for (final Permission innerPermission : permission.permissions()) {
                final PermissionResult result = this.testPermission(sender, innerPermission);
                if (!result.allowed()) {
                    return result; // short circuit the first false result
                }
            }
            return PermissionResult.allowed(permission); // all returned true
        }
        return PermissionResult.of(permission.isEmpty() || this.hasPermission(sender, permission.permissionString()), permission);
    }

    /**
     * Returns the caption registry.
     *
     * @return the caption registry
     * @see #captionRegistry(CaptionRegistry)
     */
    @API(status = API.Status.STABLE)
    public final @NonNull CaptionRegistry<C> captionRegistry() {
        return this.captionRegistry;
    }

    /**
     * Replaces the caption registry.
     * <p>
     * Some platforms may inject their own captions into the default caption registry,
     * and so you may need to insert these captions yourself, if you do decide to replace the caption registry.
     *
     * @param captionRegistry new caption registry.
     * @see #captionRegistry()
     */
    @API(status = API.Status.STABLE)
    public final void captionRegistry(final @NonNull CaptionRegistry<C> captionRegistry) {
        this.captionRegistry = captionRegistry;
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
     * Deletes the given {@code rootCommand}.
     * <p>
     * This will delete all chains that originate at the root command.
     *
     * @param rootCommand The root command to delete
     * @throws CloudCapability.CloudCapabilityMissingException If {@link CloudCapability.StandardCapabilities#ROOT_COMMAND_DELETION} is missing
     */
    @API(status = API.Status.EXPERIMENTAL)
    public void deleteRootCommand(final @NonNull String rootCommand) throws CloudCapability.CloudCapabilityMissingException {
        if (!this.hasCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION)) {
            throw new CloudCapability.CloudCapabilityMissingException(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION);
        }

        // Mark the command for deletion.
        final CommandNode<C> node = this.commandTree.getNamedNode(rootCommand);
        if (node == null || node.component() == null) {
            // If the node doesn't exist, we don't really need to delete it...
            return;
        }

        // The registration handler gets to act before we destruct the command.
        this.commandRegistrationHandler.unregisterRootCommand(node.component());

        // We then delete it from the tree.
        this.commandTree.deleteRecursively(node, true, this.commands::remove);

        // And lastly we re-build the entire tree.
        this.commandTree.verifyAndRegister();
    }

    /**
     * Returns all root command names.
     *
     * @return Root command names.
     */
    @API(status = API.Status.STABLE)
    public @NonNull Collection<@NonNull String> rootCommands() {
        return this.commandTree.rootNodes()
                .stream()
                .map(CommandNode::component)
                .filter(Objects::nonNull)
                .filter(component -> component.type() == CommandComponent.ComponentType.LITERAL)
                .map(CommandComponent::name)
                .collect(Collectors.toList());
    }

    /**
     * Invokes {@link Command.Builder#manager(CommandManager)} with {@code this} instance and returns the updated builder.
     *
     * @param builder builder to decorate
     * @return the decorated builder
     */
    @Override
    public final Command.@NonNull Builder<C> decorateBuilder(final Command.@NonNull Builder<C> builder) {
        return builder.manager(this);
    }

    /**
     * Create a new command component builder.
     * <p>
     * This will also invoke {@link CommandComponent.Builder#commandManager(CommandManager)}
     * so that the argument is associated with the calling command manager. This allows for parser inference based on
     * the type, with the help of the {@link ParserRegistry parser registry}.
     *
     * @param type Argument type
     * @param name Argument name
     * @param <T>  Generic argument name
     * @return Component builder
     */
    @API(status = API.Status.STABLE)
    public <T> CommandComponent.@NonNull Builder<C, T> componentBuilder(
            final @NonNull Class<T> type,
            final @NonNull String name
    ) {
        return CommandComponent.<C, T>ofType(type, name).commandManager(this);
    }

    /**
     * Create a new command flag builder
     *
     * @param name Flag name
     * @return Flag builder
     */
    public CommandFlag.@NonNull Builder<Void> flagBuilder(final @NonNull String name) {
        return CommandFlag.builder(name);
    }

    /**
     * Returns the internal command tree.
     * <p>
     * Be careful when accessing the command tree. Do not interact with it, unless you
     * absolutely know what you're doing.
     *
     * @return the command tree
     */
    @API(status = API.Status.STABLE)
    public @NonNull CommandTree<C> commandTree() {
        return this.commandTree;
    }

    /**
     * Constructs a default {@link CommandMeta} instance.
     *
     * @return default command meta
     */
    @Override
    public @NonNull CommandMeta createDefaultCommandMeta() {
        return CommandMeta.empty();
    }

    /**
     * Register a new command preprocessor. The order they are registered in is respected, and they
     * are called in LIFO order
     *
     * @param processor Processor to register
     * @see #preprocessContext(CommandContext, CommandInput) Preprocess a context
     */
    public void registerCommandPreProcessor(final @NonNull CommandPreprocessor<C> processor) {
        this.servicePipeline.registerServiceImplementation(
                new TypeToken<CommandPreprocessor<C>>() {
                },
                processor,
                Collections.emptyList()
        );
    }

    /**
     * Register a new command postprocessor. The order they are registered in is respected, and they
     * are called in LIFO order
     *
     * @param processor Processor to register
     * @see #preprocessContext(CommandContext, CommandInput) Preprocess a context
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
     * @param context      Command context
     * @param commandInput Command input as supplied by sender
     * @return {@link State#ACCEPTED} if the command should be parsed and executed, else {@link State#REJECTED}
     * @see #registerCommandPreProcessor(CommandPreprocessor) Register a command preprocessor
     */
    @API(status = API.Status.STABLE)
    public State preprocessContext(
            final @NonNull CommandContext<C> context,
            final @NonNull CommandInput commandInput
    ) {
        this.servicePipeline.pump(CommandPreprocessingContext.of(context, commandInput))
                .through(new TypeToken<CommandPreprocessor<C>>() {
                })
                .complete();
        return context.<String>optional(AcceptingCommandPreprocessor.PROCESSED_INDICATOR_KEY).orElse("").isEmpty()
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
        this.servicePipeline.pump(CommandPostprocessingContext.of(context, command))
                .through(new TypeToken<CommandPostprocessor<C>>() {
                })
                .complete();
        return context.<String>optional(AcceptingCommandPostprocessor.PROCESSED_INDICATOR_KEY).orElse("").isEmpty()
                ? State.REJECTED
                : State.ACCEPTED;
    }

    /**
     * Returns the command suggestion processor used in this command manager.
     *
     * @return the command suggestion processor
     * @see #suggestionProcessor(SuggestionProcessor)
     */
    public @NonNull SuggestionProcessor<C> suggestionProcessor() {
        return this.suggestionProcessor;
    }

    /**
     * Sets the command suggestion processor.
     * <p>
     * This will be called every time {@link SuggestionFactory#suggest(CommandContext, String)} is called, to process the list
     * of suggestions before it's returned to the caller.
     *
     * @param suggestionProcessor the new command suggestion processor
     * @see #suggestionProcessor()
     */
    public void suggestionProcessor(final @NonNull SuggestionProcessor<C> suggestionProcessor) {
        this.suggestionProcessor = suggestionProcessor;
    }

    /**
     * Returns the parser registry instance.
     * <p>
     * The parser registry contains default mappings to {@link ArgumentParser argument parsers} and
     * allows for the registration of custom mappings. The parser registry also contains mappings between
     * annotations and {@link ParserParameter}, which allows for the customization of parser settings by
     * using annotations.
     * <p>
     * When creating a new parser type, it is highly recommended to register it in the parser registry.
     * In particular, default parser types (shipped with cloud implementations) should be registered in the
     * constructor of the platform {@link CommandManager}.
     *
     * @return the parser registry instance
     */
    @API(status = API.Status.STABLE)
    public @NonNull ParserRegistry<C> parserRegistry() {
        return this.parserRegistry;
    }

    /**
     * Get the parameter injector registry instance
     *
     * @return Parameter injector registry
     */
    public final @NonNull ParameterInjectorRegistry<C> parameterInjectorRegistry() {
        return this.parameterInjectorRegistry;
    }

    /**
     * Returns the exception controller.
     * <p>
     * The exception controller is responsible for exception handler registration.
     *
     * @return the exception controller
     */
    @API(status = API.Status.STABLE)
    public final @NonNull ExceptionController<C> exceptionController() {
        return this.exceptionController;
    }

    /**
     * Returns an unmodifiable view of all registered commands.
     *
     * @return unmodifiable view of all registered commands
     */
    @API(status = API.Status.STABLE)
    public final @NonNull Collection<@NonNull Command<C>> commands() {
        return Collections.unmodifiableCollection(this.commands);
    }

    /**
     * Creates a new command help handler instance.
     * <p>
     * The command helper handler can be used to assist in the production of command help menus, etc.
     * <p>
     * This command help handler instance will display all commands registered in this command manager.
     *
     * @return a new command helper handler instance
     */
    @API(status = API.Status.STABLE)
    public final @NonNull HelpHandler<C> createHelpHandler() {
        return this.helpHandlerFactory.createHelpHandler(cmd -> true);
    }

    /**
     * Creates a new command help handler instance.
     * <p>
     * The command helper handler can be used to assist in the production of commad help menus, etc.
     * <p>
     * A filter can be specified to filter what commands
     * registered in this command manager are visible in the help menu.
     *
     * @param filter predicate that filters what commands are displayed in the help menu.
     * @return a new command helper handler instance
     */
    @API(status = API.Status.STABLE)
    public final @NonNull HelpHandler<C> createHelpHandler(
            final @NonNull CommandPredicate<C> filter
    ) {
        return this.helpHandlerFactory.createHelpHandler(filter);
    }

    /**
     * Returns the help handler factory.
     *
     * @return the help handler factory
     */
    @API(status = API.Status.STABLE)
    public final @NonNull HelpHandlerFactory<C> helpHandlerFactory() {
        return this.helpHandlerFactory;
    }

    /**
     * Sets the new help handler factory.
     * <p>
     * The help handler factory is used to create {@link org.incendo.cloud.help.HelpHandler} instances.
     *
     * @param helpHandlerFactory the new factory instance
     */
    @API(status = API.Status.STABLE)
    public final void helpHandlerFactory(final @NonNull HelpHandlerFactory<C> helpHandlerFactory) {
        this.helpHandlerFactory = helpHandlerFactory;
    }

    /**
     * Returns a {@link Configurable} instance that can be used to modify the settings for this command manager instance.
     *
     * @return settings instance
     */
    @API(status = API.Status.STABLE)
    public @NonNull Configurable<ManagerSetting> settings() {
        return this.settings;
    }

    @Override
    public final @NonNull RegistrationState state() {
        return this.state.get();
    }

    @Override
    public final boolean transitionIfPossible(final @NonNull RegistrationState in, final @NonNull RegistrationState out) {
        return this.state.compareAndSet(in, out) || this.state.get() == out;
    }

    /**
     * Transition the command manager from either {@link RegistrationState#BEFORE_REGISTRATION} or
     * {@link RegistrationState#REGISTERING} to {@link RegistrationState#AFTER_REGISTRATION}.
     *
     * @throws IllegalStateException if the manager is not in the expected state
     */
    @API(status = API.Status.STABLE)
    protected final void lockRegistration() {
        if (this.state() == RegistrationState.BEFORE_REGISTRATION) {
            this.transitionOrThrow(RegistrationState.BEFORE_REGISTRATION, RegistrationState.AFTER_REGISTRATION);
            return;
        }
        this.transitionOrThrow(RegistrationState.REGISTERING, RegistrationState.AFTER_REGISTRATION);
    }

    /**
     * Check if command registration is allowed.
     * <p>
     * On platforms where unsafe registration is possible, this can be overridden by enabling the
     * {@link ManagerSetting#ALLOW_UNSAFE_REGISTRATION} setting.
     *
     * @return {@code true} if the registration is allowed, else {@code false}
     */
    @API(status = API.Status.STABLE)
    public boolean isCommandRegistrationAllowed() {
        return this.settings().get(ManagerSetting.ALLOW_UNSAFE_REGISTRATION)
                || this.state.get() != RegistrationState.AFTER_REGISTRATION;
    }

    /**
     * Registers the default exception handlers.
     *
     * @param messageSender consumer that gets invoked when a message should be sent to the command sender
     * @param logger        consumer that gets invoked when a message should be logged
     */
    protected void registerDefaultExceptionHandlers(
            final @NonNull Consumer<Triplet<CommandContext<C>, Caption, List<@NonNull CaptionVariable>>> messageSender,
            final @NonNull Consumer<Pair<String, Throwable>> logger
    ) {
        final DefaultExceptionHandlers<C> defaultExceptionHandlers = new DefaultExceptionHandlers<>(
                messageSender,
                logger,
                this.exceptionController
        );
        defaultExceptionHandlers.register();
    }
}
