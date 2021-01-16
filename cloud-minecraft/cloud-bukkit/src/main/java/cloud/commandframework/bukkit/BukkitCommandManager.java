//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.brigadier.BrigadierManagerHolder;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.arguments.selector.MultipleEntitySelector;
import cloud.commandframework.bukkit.arguments.selector.MultiplePlayerSelector;
import cloud.commandframework.bukkit.arguments.selector.SingleEntitySelector;
import cloud.commandframework.bukkit.arguments.selector.SinglePlayerSelector;
import cloud.commandframework.bukkit.parsers.EnchantmentArgument;
import cloud.commandframework.bukkit.parsers.MaterialArgument;
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.bukkit.parsers.WorldArgument;
import cloud.commandframework.bukkit.parsers.location.Location2D;
import cloud.commandframework.bukkit.parsers.location.LocationArgument;
import cloud.commandframework.bukkit.parsers.location.Location2DArgument;
import cloud.commandframework.bukkit.parsers.selector.MultipleEntitySelectorArgument;
import cloud.commandframework.bukkit.parsers.selector.MultiplePlayerSelectorArgument;
import cloud.commandframework.bukkit.parsers.selector.SingleEntitySelectorArgument;
import cloud.commandframework.bukkit.parsers.selector.SinglePlayerSelectorArgument;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.tasks.TaskFactory;
import cloud.commandframework.tasks.TaskRecipe;
import io.leangen.geantyref.TypeToken;
import org.bukkit.Bukkit;
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

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command manager for the Bukkit platform
 *
 * @param <C> Command sender type
 */
@SuppressWarnings("unchecked")
public class BukkitCommandManager<C> extends CommandManager<C> implements BrigadierManagerHolder<C> {

    private static final int VERSION_RADIX = 10;
    private static final int BRIGADIER_MINIMUM_VERSION = 13;
    private static final int PAPER_BRIGADIER_VERSION = 15;
    private static final int ASYNC_TAB_MINIMUM_VERSION = 12;

    private final Plugin owningPlugin;
    private final int minecraftVersion;
    private final boolean paper;

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
            final @NonNull Function<@NonNull CommandTree<C>,
                    @NonNull CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @NonNull Function<@NonNull CommandSender, @NonNull C> commandSenderMapper,
            final @NonNull Function<@NonNull C, @NonNull CommandSender> backwardsCommandSenderMapper
    )
            throws Exception {
        super(commandExecutionCoordinator, new BukkitPluginRegistrationHandler<>());
        ((BukkitPluginRegistrationHandler<C>) this.getCommandRegistrationHandler()).initialize(this);
        this.owningPlugin = owningPlugin;
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;

        final BukkitSynchronizer bukkitSynchronizer = new BukkitSynchronizer(owningPlugin);
        this.taskFactory = new TaskFactory(bukkitSynchronizer);

        /* Try to determine the Minecraft version */
        int version = -1;
        try {
            final Matcher matcher = Pattern.compile("\\(MC: (\\d)\\.(\\d+)\\.?(\\d+?)?\\)")
                    .matcher(Bukkit.getVersion());
            if (matcher.find()) {
                version = Integer.parseInt(
                        matcher.toMatchResult().group(2),
                        VERSION_RADIX
                );
            }
        } catch (final Exception e) {
            this.owningPlugin.getLogger().severe("Failed to determine Minecraft version "
                    + "for cloud Bukkit capability detection");
        }
        this.minecraftVersion = version;

        boolean paper = false;
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            paper = true;
        } catch (final Exception ignored) {
            // This is fine
        }
        this.paper = paper;

        /* Register Bukkit Preprocessor */
        this.registerCommandPreProcessor(new BukkitCommandPreprocessor<>(this));

        /* Register Bukkit Parsers */
        this.getParserRegistry().registerParserSupplier(TypeToken.get(World.class), parserParameters ->
                new WorldArgument.WorldParser<>());
        this.getParserRegistry().registerParserSupplier(TypeToken.get(Material.class), parserParameters ->
                new MaterialArgument.MaterialParser<>());
        this.getParserRegistry().registerParserSupplier(TypeToken.get(Player.class), parserParameters ->
                new PlayerArgument.PlayerParser<>());
        this.getParserRegistry().registerParserSupplier(TypeToken.get(OfflinePlayer.class), parserParameters ->
                new OfflinePlayerArgument.OfflinePlayerParser<>());
        this.getParserRegistry().registerParserSupplier(TypeToken.get(Enchantment.class), parserParameters ->
                new EnchantmentArgument.EnchantmentParser<>());
        this.getParserRegistry().registerParserSupplier(TypeToken.get(Location.class), parserParameters ->
                new LocationArgument.LocationParser<>());
        this.getParserRegistry().registerParserSupplier(TypeToken.get(Location2D.class), parserParameters ->
                new Location2DArgument.Location2DParser<>());
        /* Register Entity Selector Parsers */
        this.getParserRegistry().registerParserSupplier(TypeToken.get(SingleEntitySelector.class), parserParameters ->
                new SingleEntitySelectorArgument.SingleEntitySelectorParser<>());
        this.getParserRegistry().registerParserSupplier(TypeToken.get(SinglePlayerSelector.class), parserParameters ->
                new SinglePlayerSelectorArgument.SinglePlayerSelectorParser<>());
        this.getParserRegistry().registerParserSupplier(TypeToken.get(MultipleEntitySelector.class), parserParameters ->
                new MultipleEntitySelectorArgument.MultipleEntitySelectorParser<>());
        this.getParserRegistry().registerParserSupplier(TypeToken.get(MultiplePlayerSelector.class), parserParameters ->
                new MultiplePlayerSelectorArgument.MultiplePlayerSelectorParser<>());

        /* Register suggestion and state listener */
        this.owningPlugin.getServer().getPluginManager().registerEvents(
                new CloudBukkitListener<>(this),
                this.owningPlugin
        );

        this.setCaptionRegistry(new BukkitCaptionRegistryFactory<C>().create());
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
    @SuppressWarnings("deprecation")
    public @NonNull BukkitCommandMeta createDefaultCommandMeta() {
        return BukkitCommandMetaBuilder.builder().withDescription("").build();
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
     * Check whether or not Brigadier can be used on the server instance
     *
     * @throws BrigadierFailureException An exception is thrown if Brigadier isn't available. The exception
     *                                   will contain the reason for this.
     */
    protected final void checkBrigadierCompatibility() throws BrigadierFailureException {
        if (!this.queryCapability(CloudBukkitCapabilities.BRIGADIER)) {
            throw new BrigadierFailureException(
                    BrigadierFailureReason.VERSION_TOO_LOW,
                    new IllegalArgumentException("Version: " + this.minecraftVersion)
            );
        }
    }

    /**
     * Query for a specific capability
     *
     * @param capability Capability
     * @return {@code true} if the manager has the given capability, else {@code false}
     */
    public final boolean queryCapability(final @NonNull CloudBukkitCapabilities capability) {
        return this.queryCapabilities().contains(capability);
    }

    /**
     * Check for the platform capabilities
     *
     * @return A set containing all capabilities of the instance
     */
    public final @NonNull Set<@NonNull CloudBukkitCapabilities> queryCapabilities() {
        if (this.paper) {
            if (this.minecraftVersion >= ASYNC_TAB_MINIMUM_VERSION) {
                if (this.minecraftVersion >= PAPER_BRIGADIER_VERSION) {
                    return EnumSet.of(
                            CloudBukkitCapabilities.NATIVE_BRIGADIER,
                            CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION,
                            CloudBukkitCapabilities.BRIGADIER
                    );
                } else if (this.minecraftVersion >= BRIGADIER_MINIMUM_VERSION) {
                    return EnumSet.of(
                            CloudBukkitCapabilities.COMMODORE_BRIGADIER,
                            CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION,
                            CloudBukkitCapabilities.BRIGADIER
                    );
                } else {
                    return EnumSet.of(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION);
                }
            }
        } else {
            if (this.minecraftVersion >= BRIGADIER_MINIMUM_VERSION) {
                return EnumSet.of(
                        CloudBukkitCapabilities.COMMODORE_BRIGADIER,
                        CloudBukkitCapabilities.BRIGADIER
                );
            }
        }
        return EnumSet.noneOf(CloudBukkitCapabilities.class);
    }

    /**
     * Attempt to register the Brigadier mapper, and return it.
     *
     * @throws BrigadierFailureException If Brigadier isn't
     *                                   supported by the platform
     */
    public void registerBrigadier() throws BrigadierFailureException {
        this.requireState(RegistrationState.BEFORE_REGISTRATION);
        this.checkBrigadierCompatibility();
        try {
            final CloudCommodoreManager<C> cloudCommodoreManager = new CloudCommodoreManager<>(this);
            cloudCommodoreManager.initialize(this);
            this.setCommandRegistrationHandler(cloudCommodoreManager);
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
        if (this.getCommandRegistrationHandler() instanceof CloudCommodoreManager) {
            return ((CloudCommodoreManager<C>) this.getCommandRegistrationHandler()).brigadierManager();
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

    final void lockIfBrigadierCapable() {
        if (this.minecraftVersion >= BRIGADIER_MINIMUM_VERSION) {
            this.lockRegistration();
        }
    }

    /**
     * Reasons to explain why Brigadier failed to initialize
     */
    public enum BrigadierFailureReason {
        COMMODORE_NOT_PRESENT,
        VERSION_TOO_LOW,
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
