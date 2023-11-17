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
package cloud.commandframework.bukkit;

import cloud.commandframework.CloudCapability;
import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.brigadier.BrigadierManagerHolder;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.annotation.specifier.AllowEmptySelection;
import cloud.commandframework.bukkit.annotation.specifier.DefaultNamespace;
import cloud.commandframework.bukkit.annotation.specifier.RequireExplicitNamespace;
import cloud.commandframework.bukkit.argument.NamespacedKeyArgument;
import cloud.commandframework.bukkit.arguments.selector.MultipleEntitySelector;
import cloud.commandframework.bukkit.arguments.selector.MultiplePlayerSelector;
import cloud.commandframework.bukkit.arguments.selector.SingleEntitySelector;
import cloud.commandframework.bukkit.arguments.selector.SinglePlayerSelector;
import cloud.commandframework.bukkit.data.ProtoItemStack;
import cloud.commandframework.bukkit.internal.CraftBukkitReflection;
import cloud.commandframework.bukkit.parsers.BlockPredicateArgument;
import cloud.commandframework.bukkit.parsers.EnchantmentArgument;
import cloud.commandframework.bukkit.parsers.ItemStackArgument;
import cloud.commandframework.bukkit.parsers.ItemStackPredicateArgument;
import cloud.commandframework.bukkit.parsers.MaterialArgument;
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.bukkit.parsers.WorldArgument;
import cloud.commandframework.bukkit.parsers.location.Location2D;
import cloud.commandframework.bukkit.parsers.location.Location2DArgument;
import cloud.commandframework.bukkit.parsers.location.LocationArgument;
import cloud.commandframework.bukkit.parsers.selector.MultipleEntitySelectorArgument;
import cloud.commandframework.bukkit.parsers.selector.MultiplePlayerSelectorArgument;
import cloud.commandframework.bukkit.parsers.selector.SingleEntitySelectorArgument;
import cloud.commandframework.bukkit.parsers.selector.SinglePlayerSelectorArgument;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.tasks.TaskFactory;
import cloud.commandframework.tasks.TaskRecipe;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Command manager for the Bukkit platform
 *
 * @param <C> Command sender type
 */
@SuppressWarnings("unchecked")
public class BukkitCommandManager<C> extends CommandManager<C> implements BrigadierManagerHolder<C> {

    private final Plugin owningPlugin;

    private final Function<CommandSender, C> commandSenderMapper;
    private final Function<C, CommandSender> backwardsCommandSenderMapper;

    private final TaskFactory taskFactory;

    private boolean splitAliases = false;

    /**
     * Construct a new Bukkit command manager
     *
     * @param owningPlugin                 Plugin that is constructing the manager. This will be used when registering the
     *                                     commands to the Bukkit command map.
     * @param commandExecutionCoordinator  Execution coordinator instance. The coordinator is in charge of executing incoming
     *                                     commands. Some considerations must be made when picking a suitable execution
     *                                     coordinator. For example, an entirely asynchronous coordinator is not suitable
     *                                     when the parsers used in your commands are not thread safe. If you have
     *                                     commands that perform blocking operations, however, it might not be a good idea to
     *                                     use a synchronous execution coordinator. In most cases you will want to pick between
     *                                     {@link CommandExecutionCoordinator#simpleCoordinator()} and
     *                                     {@link cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator}.
     *                                     <p>
     *                                     A word of caution: When using the asynchronous command executor in Bukkit, it is very
     *                                     likely that you will have to perform manual synchronization when executing the commands
     *                                     in many cases, as Bukkit makes no guarantees of thread safety in common classes. To
     *                                     make this easier, {@link #taskRecipe()} is provided. Furthermore, it may be unwise to
     *                                     use asynchronous command parsing, especially when dealing with things such as players
     *                                     and entities. To make this more safe, the asynchronous command execution allows you
     *                                     to state that you want synchronous command parsing.
     * @param commandSenderMapper          Function that maps {@link CommandSender} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link CommandSender}
     * @throws Exception If the construction of the manager fails
     */
    @SuppressWarnings("unchecked")
    public BukkitCommandManager(
            final @NonNull Plugin owningPlugin,
            final @NonNull Function<@NonNull CommandTree<@NonNull C>,
                    @NonNull CommandExecutionCoordinator<@NonNull C>> commandExecutionCoordinator,
            final @NonNull Function<@NonNull CommandSender, @NonNull C> commandSenderMapper,
            final @NonNull Function<@NonNull C, @NonNull CommandSender> backwardsCommandSenderMapper
    )
            throws Exception {
        super(commandExecutionCoordinator, new BukkitPluginRegistrationHandler<>());
        ((BukkitPluginRegistrationHandler<C>) this.commandRegistrationHandler()).initialize(this);
        this.owningPlugin = owningPlugin;
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;

        final BukkitSynchronizer bukkitSynchronizer = new BukkitSynchronizer(owningPlugin);
        this.taskFactory = new TaskFactory(bukkitSynchronizer);

        this.commandSuggestionProcessor(new FilteringCommandSuggestionProcessor<>(
                FilteringCommandSuggestionProcessor.Filter.<C>startsWith(true).andTrimBeforeLastSpace()
        ));

        /* Register capabilities */
        CloudBukkitCapabilities.CAPABLE.forEach(this::registerCapability);
        this.registerCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION);

        /* Register Bukkit Preprocessor */
        this.registerCommandPreProcessor(new BukkitCommandPreprocessor<>(this));

        /* Register Bukkit Parsers */
        this.parserRegistry().registerParserSupplier(TypeToken.get(World.class), parserParameters ->
                new WorldArgument.WorldParser<>());
        this.parserRegistry().registerParserSupplier(TypeToken.get(Material.class), parserParameters ->
                new MaterialArgument.MaterialParser<>());
        this.parserRegistry().registerParserSupplier(TypeToken.get(Player.class), parserParameters ->
                new PlayerArgument.PlayerParser<>());
        this.parserRegistry().registerParserSupplier(TypeToken.get(OfflinePlayer.class), parserParameters ->
                new OfflinePlayerArgument.OfflinePlayerParser<>());
        this.parserRegistry().registerParserSupplier(TypeToken.get(Enchantment.class), parserParameters ->
                new EnchantmentArgument.EnchantmentParser<>());
        this.parserRegistry().registerParserSupplier(TypeToken.get(Location.class), parserParameters ->
                new LocationArgument.LocationParser<>());
        this.parserRegistry().registerParserSupplier(TypeToken.get(Location2D.class), parserParameters ->
                new Location2DArgument.Location2DParser<>());
        this.parserRegistry().registerParserSupplier(TypeToken.get(ProtoItemStack.class), parserParameters ->
                new ItemStackArgument.Parser<>());

        /* Register Entity Selector Parsers */
        this.parserRegistry().registerParserSupplier(TypeToken.get(SingleEntitySelector.class), parserParameters ->
                new SingleEntitySelectorArgument.SingleEntitySelectorParser<>());
        this.parserRegistry().registerParserSupplier(TypeToken.get(SinglePlayerSelector.class), parserParameters ->
                new SinglePlayerSelectorArgument.SinglePlayerSelectorParser<>());
        this.parserRegistry().registerAnnotationMapper(
                AllowEmptySelection.class,
                (annotation, type) -> ParserParameters.single(BukkitParserParameters.ALLOW_EMPTY_SELECTOR_RESULT, annotation.value())
        );
        this.parserRegistry().registerParserSupplier(
                TypeToken.get(MultipleEntitySelector.class),
                parserParameters -> new MultipleEntitySelectorArgument.MultipleEntitySelectorParser<>(
                        parserParameters.get(BukkitParserParameters.ALLOW_EMPTY_SELECTOR_RESULT, true)
                )
        );
        this.parserRegistry().registerParserSupplier(
                TypeToken.get(MultiplePlayerSelector.class),
                parserParameters -> new MultiplePlayerSelectorArgument.MultiplePlayerSelectorParser<>(
                        parserParameters.get(BukkitParserParameters.ALLOW_EMPTY_SELECTOR_RESULT, true)
                )
        );

        if (CraftBukkitReflection.classExists("org.bukkit.NamespacedKey")) {
            this.registerParserSupplierFor(NamespacedKeyArgument.class);
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
            this.registerParserSupplierFor(ItemStackPredicateArgument.class);
            this.registerParserSupplierFor(BlockPredicateArgument.class);
        }

        /* Register suggestion and state listener */
        this.owningPlugin.getServer().getPluginManager().registerEvents(
                new CloudBukkitListener<>(this),
                this.owningPlugin
        );

        this.captionRegistry(new BukkitCaptionRegistryFactory<C>().create());
    }

    /**
     * Create a command manager using Bukkit's {@link CommandSender} as the sender type.
     *
     * @param owningPlugin                plugin owning the command manager
     * @param commandExecutionCoordinator execution coordinator instance
     * @return a new command manager
     * @throws Exception If the construction of the manager fails
     * @see #BukkitCommandManager(Plugin, Function, Function, Function) for a more thorough explanation
     * @since 1.5.0
     */
    public static @NonNull BukkitCommandManager<@NonNull CommandSender> createNative(
            final @NonNull Plugin owningPlugin,
            final @NonNull Function<@NonNull CommandTree<@NonNull CommandSender>,
                    @NonNull CommandExecutionCoordinator<@NonNull CommandSender>> commandExecutionCoordinator
    ) throws Exception {
        return new BukkitCommandManager<>(
                owningPlugin,
                commandExecutionCoordinator,
                UnaryOperator.identity(),
                UnaryOperator.identity()
        );
    }

    /**
     * Create a new task recipe. This can be used to create chains of synchronous/asynchronous method calls
     *
     * @return Task recipe
     */
    public @NonNull TaskRecipe taskRecipe() {
        return this.taskFactory.recipe();
    }

    /**
     * Get the plugin that owns the manager
     *
     * @return Owning plugin
     */
    public @NonNull Plugin getOwningPlugin() {
        return this.owningPlugin;
    }

    /**
     * Create default command meta data
     *
     * @return Meta data
     */
    @Override
    public @NonNull CommandMeta createDefaultCommandMeta() {
        return CommandMeta.simple().with(CommandMeta.DESCRIPTION, "").build();
    }

    /**
     * Get the command sender mapper
     *
     * @return Command sender mapper
     */
    public final @NonNull Function<@NonNull CommandSender, @NonNull C> getCommandSenderMapper() {
        return this.commandSenderMapper;
    }

    @Override
    public final boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        if (permission.isEmpty()) {
            return true;
        }
        return this.backwardsCommandSenderMapper.apply(sender).hasPermission(permission);
    }

    protected final boolean getSplitAliases() {
        return this.splitAliases;
    }

    protected final void setSplitAliases(final boolean value) {
        this.requireState(RegistrationState.BEFORE_REGISTRATION);
        this.splitAliases = value;
    }

    /**
     * Check whether Brigadier can be used on the server instance
     *
     * @throws BrigadierFailureException An exception is thrown if Brigadier isn't available. The exception
     *                                   will contain the reason for this.
     */
    protected final void checkBrigadierCompatibility() throws BrigadierFailureException {
        if (!this.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            throw new BrigadierFailureException(
                    BrigadierFailureReason.VERSION_TOO_LOW,
                    new IllegalArgumentException(
                            "Brigadier does not appear to be present on the currently running server. This is usually due to "
                                    + "running too old a version of Minecraft (Brigadier is implemented in 1.13 and newer).")
            );
        }
    }

    /**
     * Check for the platform capabilities
     *
     * @return A set containing all capabilities of the instance
     */
    public final @NonNull Set<@NonNull CloudBukkitCapabilities> queryCapabilities() {
        return CloudBukkitCapabilities.CAPABLE;
    }

    /**
     * Attempts to enable Brigadier command registration through Commodore.
     *
     * <p>Callers should check for {@link CloudBukkitCapabilities#COMMODORE_BRIGADIER} first
     * to avoid exceptions.</p>
     *
     * @throws BrigadierFailureException If Brigadier isn't
     *                                   supported by the platform
     */
    public void registerBrigadier() throws BrigadierFailureException {
        this.requireState(RegistrationState.BEFORE_REGISTRATION);
        this.checkBrigadierCompatibility();
        if (!this.hasCapability(CloudBukkitCapabilities.COMMODORE_BRIGADIER)) {
            throw new BrigadierFailureException(BrigadierFailureReason.VERSION_TOO_HIGH);
        }
        try {
            final CloudCommodoreManager<C> cloudCommodoreManager = new CloudCommodoreManager<>(this);
            cloudCommodoreManager.initialize(this);
            this.commandRegistrationHandler(cloudCommodoreManager);
            this.setSplitAliases(true);
        } catch (final Throwable e) {
            throw new BrigadierFailureException(BrigadierFailureReason.COMMODORE_NOT_PRESENT, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.2.0
     */
    @Override
    public @Nullable CloudBrigadierManager<C, ?> brigadierManager() {
        if (this.commandRegistrationHandler() instanceof CloudCommodoreManager) {
            return ((CloudCommodoreManager<C>) this.commandRegistrationHandler()).brigadierManager();
        }
        return null;
    }

    /**
     * Strip the plugin namespace from a plugin namespaced command. This
     * will also strip the leading '/' if it's present
     *
     * @param command Command
     * @return Stripped command
     */
    public final @NonNull String stripNamespace(final @NonNull String command) {
        @NonNull String input;

        /* Remove leading '/' */
        if (command.charAt(0) == '/') {
            input = command.substring(1);
        } else {
            input = command;
        }

        /* Remove leading plugin namespace */
        final String namespace = String.format("%s:", this.getOwningPlugin().getName().toLowerCase());
        if (input.startsWith(namespace)) {
            input = input.substring(namespace.length());
        }

        return input;
    }

    /**
     * Get the backwards command sender plugin
     *
     * @return The backwards command sender mapper
     */
    public final @NonNull Function<@NonNull C, @NonNull CommandSender> getBackwardsCommandSenderMapper() {
        return this.backwardsCommandSenderMapper;
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

    final void lockIfBrigadierCapable() {
        if (this.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            this.lockRegistration();
        }
    }

    /**
     * Reasons to explain why Brigadier failed to initialize
     */
    public enum BrigadierFailureReason {
        COMMODORE_NOT_PRESENT,
        VERSION_TOO_LOW,
        VERSION_TOO_HIGH,
        PAPER_BRIGADIER_INITIALIZATION_FAILURE
    }


    public static final class BrigadierFailureException extends IllegalStateException {

        private static final long serialVersionUID = 7816660840063155703L;
        private final BrigadierFailureReason reason;

        /**
         * Initialize a new Brigadier failure exception
         *
         * @param reason Reason
         */
        public BrigadierFailureException(final @NonNull BrigadierFailureReason reason) {
            this.reason = reason;
        }

        /**
         * Initialize a new Brigadier failure exception
         *
         * @param reason Reason
         * @param cause  Cause
         */
        public BrigadierFailureException(final @NonNull BrigadierFailureReason reason, final @NonNull Throwable cause) {
            super(cause);
            this.reason = reason;
        }

        /**
         * Get the reason for the exception
         *
         * @return Reason
         */
        public @NonNull BrigadierFailureReason getReason() {
            return this.reason;
        }

        @Override
        public String getMessage() {
            return String.format(
                    "Could not initialize Brigadier mappings. Reason: %s (%s)",
                    this.reason.name().toLowerCase().replace("_", " "),
                    this.getCause() == null ? "" : this.getCause().getMessage()
            );
        }
    }
}
