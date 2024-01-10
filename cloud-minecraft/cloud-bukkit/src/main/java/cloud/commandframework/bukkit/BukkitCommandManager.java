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
package cloud.commandframework.bukkit;

import cloud.commandframework.CloudCapability;
import cloud.commandframework.CommandManager;
import cloud.commandframework.SenderMapper;
import cloud.commandframework.SenderMapperHolder;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionMapper;
import cloud.commandframework.brigadier.BrigadierManagerHolder;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.brigadier.suggestion.TooltipSuggestion;
import cloud.commandframework.bukkit.annotation.specifier.AllowEmptySelection;
import cloud.commandframework.bukkit.annotation.specifier.DefaultNamespace;
import cloud.commandframework.bukkit.annotation.specifier.RequireExplicitNamespace;
import cloud.commandframework.bukkit.data.MultipleEntitySelector;
import cloud.commandframework.bukkit.data.MultiplePlayerSelector;
import cloud.commandframework.bukkit.internal.CraftBukkitReflection;
import cloud.commandframework.bukkit.parsers.BlockPredicateParser;
import cloud.commandframework.bukkit.parsers.EnchantmentParser;
import cloud.commandframework.bukkit.parsers.ItemStackParser;
import cloud.commandframework.bukkit.parsers.ItemStackPredicateParser;
import cloud.commandframework.bukkit.parsers.MaterialParser;
import cloud.commandframework.bukkit.parsers.NamespacedKeyParser;
import cloud.commandframework.bukkit.parsers.OfflinePlayerParser;
import cloud.commandframework.bukkit.parsers.PlayerParser;
import cloud.commandframework.bukkit.parsers.WorldParser;
import cloud.commandframework.bukkit.parsers.location.Location2DParser;
import cloud.commandframework.bukkit.parsers.location.LocationParser;
import cloud.commandframework.bukkit.parsers.selector.MultipleEntitySelectorParser;
import cloud.commandframework.bukkit.parsers.selector.MultiplePlayerSelectorParser;
import cloud.commandframework.bukkit.parsers.selector.SingleEntitySelectorParser;
import cloud.commandframework.bukkit.parsers.selector.SinglePlayerSelectorParser;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.execution.ExecutionCoordinator;
import cloud.commandframework.state.RegistrationState;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import org.apiguardian.api.API;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Command manager for the Bukkit platform
 *
 * @param <C> command sender type
 */
public class BukkitCommandManager<C> extends CommandManager<C>
        implements BrigadierManagerHolder<C, Object>, SenderMapperHolder<CommandSender, C> {

    private static final String MESSAGE_INTERNAL_ERROR = ChatColor.RED
            + "An internal error occurred while attempting to perform this command.";
    private static final String MESSAGE_NO_PERMS = ChatColor.RED
            + "I'm sorry, but you do not have permission to perform this command. "
            + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command. Type \"/help\" for help.";

    private final Plugin owningPlugin;
    private final SenderMapper<CommandSender, C> senderMapper;

    private boolean splitAliases = false;

    /**
     * Create a new Bukkit command manager.
     *
     * @param owningPlugin                Plugin constructing the manager. Used when registering commands to the command map,
     *                                    registering event listeners, etc.
     * @param commandExecutionCoordinator Execution coordinator instance. Due to Bukkit blocking the main thread for
     *                                    suggestion requests, it's potentially unsafe to use anything other than
     *                                    {@link ExecutionCoordinator#nonSchedulingExecutor()} for
     *                                    {@link ExecutionCoordinator.Builder#suggestionsExecutor(Executor)}. Once the
     *                                    coordinator, a suggestion provider, parser, or similar routes suggestion logic
     *                                    off of the calling (main) thread, it won't be possible to schedule further logic
     *                                    back to the main thread without a deadlock. When Brigadier support is active, this issue
     *                                    is avoided, as it allows for non-blocking suggestions.
     * @param senderMapper                Mapper between Bukkit's {@link CommandSender} and the command sender type {@code C}.
     * @see #registerBrigadier()
     * @throws InitializationException if construction of the manager fails
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public BukkitCommandManager(
            final @NonNull Plugin owningPlugin,
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull SenderMapper<CommandSender, C> senderMapper
    ) throws InitializationException {
        super(commandExecutionCoordinator, new BukkitPluginRegistrationHandler<>());
        try {
            ((BukkitPluginRegistrationHandler<C>) this.commandRegistrationHandler()).initialize(this);
        } catch (final ReflectiveOperationException exception) {
            throw new InitializationException("Failed to initialize command registration handler", exception);
        }
        this.owningPlugin = owningPlugin;
        this.senderMapper = senderMapper;

        /* Register capabilities */
        CloudBukkitCapabilities.CAPABLE.forEach(this::registerCapability);
        this.registerCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION);

        /* Register Bukkit Preprocessor */
        this.registerCommandPreProcessor(new BukkitCommandPreprocessor<>(this));

        /* Register Bukkit Parsers */
        this.parserRegistry()
                .registerParser(WorldParser.worldParser())
                .registerParser(MaterialParser.materialParser())
                .registerParser(PlayerParser.playerParser())
                .registerParser(OfflinePlayerParser.offlinePlayerParser())
                .registerParser(EnchantmentParser.enchantmentParser())
                .registerParser(LocationParser.locationParser())
                .registerParser(Location2DParser.location2DParser())
                .registerParser(ItemStackParser.itemStackParser())
                .registerParser(SingleEntitySelectorParser.singleEntitySelectorParser())
                .registerParser(SinglePlayerSelectorParser.singlePlayerSelectorParser());

        /* Register Entity Selector Parsers */
        this.parserRegistry().registerAnnotationMapper(
                AllowEmptySelection.class,
                (annotation, type) -> ParserParameters.single(
                        BukkitParserParameters.ALLOW_EMPTY_SELECTOR_RESULT,
                        annotation.value()
                )
        );
        this.parserRegistry().registerParserSupplier(
                TypeToken.get(MultipleEntitySelector.class),
                parserParameters -> new MultipleEntitySelectorParser<>(
                        parserParameters.get(BukkitParserParameters.ALLOW_EMPTY_SELECTOR_RESULT, true)
                )
        );
        this.parserRegistry().registerParserSupplier(
                TypeToken.get(MultiplePlayerSelector.class),
                parserParameters -> new MultiplePlayerSelectorParser<>(
                        parserParameters.get(BukkitParserParameters.ALLOW_EMPTY_SELECTOR_RESULT, true)
                )
        );

        if (CraftBukkitReflection.classExists("org.bukkit.NamespacedKey")) {
            this.registerParserSupplierFor(NamespacedKeyParser.class);
            this.parserRegistry().registerAnnotationMapper(
                    RequireExplicitNamespace.class,
                    (annotation, type) -> ParserParameters.single(BukkitParserParameters.REQUIRE_EXPLICIT_NAMESPACE, true)
            );
            this.parserRegistry().registerAnnotationMapper(
                    DefaultNamespace.class,
                    (annotation, type) -> ParserParameters.single(BukkitParserParameters.DEFAULT_NAMESPACE, annotation.value())
            );
        }

        /* Register MC 1.13+ parsers */
        if (this.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            this.registerParserSupplierFor(ItemStackPredicateParser.class);
            this.registerParserSupplierFor(BlockPredicateParser.class);
        }

        /* Register suggestion and state listener */
        this.owningPlugin.getServer().getPluginManager().registerEvents(
                new CloudBukkitListener<>(this),
                this.owningPlugin
        );

        this.registerDefaultExceptionHandlers();
        this.captionRegistry(new BukkitCaptionRegistryFactory<C>().create());
    }

    /**
     * Returns the plugin that owns the manager.
     *
     * @return owning plugin
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public final @NonNull Plugin owningPlugin() {
        return this.owningPlugin;
    }

    @Override
    public final @NonNull SenderMapper<CommandSender, C> senderMapper() {
        return this.senderMapper;
    }

    /**
     * Sets the suggestion mapper.
     * <p>
     * If you're using Brigadier and you want to be able to use suggestions with tooltips, then
     * your suggestion mapper should output {@link TooltipSuggestion}.
     *
     * @param <S>              the custom type
     * @param suggestionMapper the suggestion mapper
     * @since 2.0.0
     */
    @Override
    public <S extends Suggestion> void suggestionMapper(final @NonNull SuggestionMapper<S> suggestionMapper) {
        super.suggestionMapper(suggestionMapper);
    }

    @Override
    public final boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        if (permission.isEmpty()) {
            return true;
        }
        return this.senderMapper.reverse(sender).hasPermission(permission);
    }

    @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
    protected final boolean getSplitAliases() {
        return this.splitAliases;
    }

    @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
    protected final void setSplitAliases(final boolean value) {
        this.requireState(RegistrationState.BEFORE_REGISTRATION);
        this.splitAliases = value;
    }

    /**
     * Check whether Brigadier can be used on the server instance
     *
     * @throws BrigadierInitializationException An exception is thrown if Brigadier isn't available. The exception
     *                                   will contain the reason for this.
     */
    protected final void checkBrigadierCompatibility() throws BrigadierInitializationException {
        if (!this.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            throw new BrigadierInitializationException(
                    "Missing capability " + CloudBukkitCapabilities.class.getSimpleName() + "."
                            + CloudBukkitCapabilities.BRIGADIER + " (Minecraft version too old? Brigadier was added in 1.13). "
                            + "See the Javadocs for more details"
            );
        }
    }

    /**
     * Attempts to enable Brigadier command registration through Commodore.
     *
     * <p>Callers should check for {@link CloudBukkitCapabilities#COMMODORE_BRIGADIER} first
     * to avoid exceptions.</p>
     *
     * @see #hasCapability(CloudCapability)
     * @throws BrigadierInitializationException when the prerequisite capabilities are not present or some other issue occurs
     * during registration of Brigadier support
     */
    public void registerBrigadier() throws BrigadierInitializationException {
        this.requireState(RegistrationState.BEFORE_REGISTRATION);
        this.checkBrigadierCompatibility();
        if (!this.hasCapability(CloudBukkitCapabilities.COMMODORE_BRIGADIER)) {
            throw new BrigadierInitializationException(
                    "Missing capability " + CloudBukkitCapabilities.class.getSimpleName() + "."
                            + CloudBukkitCapabilities.COMMODORE_BRIGADIER + " (Minecraft version too new). "
                            + "See the Javadocs for more details"
            );
        }
        try {
            final CloudCommodoreManager<C> cloudCommodoreManager = new CloudCommodoreManager<>(this);
            cloudCommodoreManager.initialize(this);
            this.commandRegistrationHandler(cloudCommodoreManager);
            this.setSplitAliases(true);
        } catch (final Exception e) {
            throw new BrigadierInitializationException(
                    "Unexpected exception initializing " + CloudCommodoreManager.class.getSimpleName(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @Override
    public boolean hasBrigadierManager() {
        return this.commandRegistrationHandler() instanceof CloudCommodoreManager;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws BrigadierManagerNotPresent when {@link #hasBrigadierManager()} is false
     * @since 1.2.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @Override
    public @NonNull CloudBrigadierManager<C, ?> brigadierManager() {
        if (this.commandRegistrationHandler() instanceof CloudCommodoreManager) {
            return ((CloudCommodoreManager<C>) this.commandRegistrationHandler()).brigadierManager();
        }
        throw new BrigadierManagerHolder.BrigadierManagerNotPresent("The CloudBrigadierManager is either not supported in the "
                + "current environment, or it is not enabled.");
    }

    /**
     * Strip the plugin namespace from a plugin namespaced command. This
     * will also strip the leading '/' if it's present
     *
     * @param command Command
     * @return Stripped command
     */
    @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
    public final @NonNull String stripNamespace(final @NonNull String command) {
        @NonNull String input;

        /* Remove leading '/' */
        if (command.charAt(0) == '/') {
            input = command.substring(1);
        } else {
            input = command;
        }

        /* Remove leading plugin namespace */
        final String namespace = String.format("%s:", this.owningPlugin().getName().toLowerCase(Locale.ROOT));
        if (input.startsWith(namespace)) {
            input = input.substring(namespace.length());
        }

        return input;
    }

    /**
     * Attempts to call the method on the provided class matching the signature
     * <p>{@code private static void registerParserSupplier(BukkitCommandManager)}</p>
     * using reflection.
     *
     * @param argumentClass argument class
     */
    private void registerParserSupplierFor(final @NonNull Class<?> argumentClass) {
        try {
            final Method registerParserSuppliers = argumentClass
                    .getDeclaredMethod("registerParserSupplier", BukkitCommandManager.class);
            registerParserSuppliers.setAccessible(true);
            registerParserSuppliers.invoke(null, this);
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerDefaultExceptionHandlers() {
        this.exceptionController().registerHandler(Throwable.class, context -> {
            this.senderMapper.reverse(context.context().sender()).sendMessage(MESSAGE_INTERNAL_ERROR);
            this.owningPlugin.getLogger().log(
                    Level.SEVERE,
                    "An unhandled exception was thrown during command execution",
                    context.exception()
            );
        }).registerHandler(CommandExecutionException.class, context -> {
            this.senderMapper.reverse(context.context().sender()).sendMessage(MESSAGE_INTERNAL_ERROR);
            this.owningPlugin.getLogger().log(
                    Level.SEVERE,
                    "Exception executing command handler",
                    context.exception().getCause()
            );
        }).registerHandler(ArgumentParseException.class, context -> {
            this.senderMapper.reverse(context.context().sender()).sendMessage(
                    ChatColor.RED + "Invalid Command Argument: " + ChatColor.GRAY + context.exception().getCause().getMessage()
            );
        }).registerHandler(NoSuchCommandException.class, context -> {
            this.senderMapper.reverse(context.context().sender()).sendMessage(MESSAGE_UNKNOWN_COMMAND);
        }).registerHandler(NoPermissionException.class, context -> {
            this.senderMapper.reverse(context.context().sender()).sendMessage(MESSAGE_NO_PERMS);
        }).registerHandler(InvalidCommandSenderException.class, context -> {
            this.senderMapper.reverse(context.context().sender()).sendMessage(
                    ChatColor.RED + context.exception().getMessage()
            );
        }).registerHandler(InvalidSyntaxException.class, context -> {
            this.senderMapper.reverse(context.context().sender()).sendMessage(
                    ChatColor.RED + "Invalid Command Syntax. Correct command syntax is: "
                            + ChatColor.GRAY + context.exception().getCorrectSyntax()
            );
        });
    }

    final void lockIfBrigadierCapable() {
        if (this.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            this.lockRegistration();
        }
    }


    /**
     * Exception thrown when the command manager could not be initialized.
     *
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static final class InitializationException extends IllegalStateException {

        /**
         * Create a new {@link InitializationException}.
         *
         * @param message message
         * @param cause   cause
         */
        @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
        public InitializationException(final String message, final @Nullable Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Exception thrown when Brigadier mappings fail to initialize.
     *
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static final class BrigadierInitializationException extends IllegalStateException {

        /**
         * Creates a new Brigadier failure exception.
         *
         * @param reason Reason
         */
        @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
        public BrigadierInitializationException(final @NonNull String reason) {
            super(reason);
        }

        /**
         * Creates a new Brigadier failure exception.
         *
         * @param reason Reason
         * @param cause  Cause
         */
        @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
        public BrigadierInitializationException(final @NonNull String reason, final @Nullable Throwable cause) {
            super(reason, cause);
        }
    }
}
