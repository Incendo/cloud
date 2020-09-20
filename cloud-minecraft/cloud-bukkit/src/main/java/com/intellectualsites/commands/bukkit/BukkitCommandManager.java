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
package com.intellectualsites.commands.bukkit;

import com.google.common.reflect.TypeToken;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.CommandTree;
import com.intellectualsites.commands.bukkit.parsers.MaterialArgument;
import com.intellectualsites.commands.bukkit.parsers.WorldArgument;
import com.intellectualsites.commands.execution.CommandExecutionCoordinator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command manager for the Bukkit platform, using {@link BukkitCommandSender} as the
 * command sender type
 *
 * @param <C> Command sender type
 */
public class BukkitCommandManager<C> extends CommandManager<C> {

    private static final int VERSION_RADIX             = 10;
    private static final int BRIGADIER_MINIMAL_VERSION = 13;
    private static final int PAPER_BRIGADIER_VERSION   = 15;

    private final Plugin owningPlugin;
    private final int minecraftVersion;
    private final boolean paper;

    private final Function<CommandSender, C> commandSenderMapper;
    private final Function<C, CommandSender> backwardsCommandSenderMapper;

    private boolean splitAliases = false;

    /**
     * Construct a new Bukkit command manager
     *
     * @param owningPlugin                 Plugin that is constructing the manager
     * @param commandExecutionCoordinator  Coordinator provider
     * @param commandSenderMapper          Function that maps {@link CommandSender} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link CommandSender}
     * @throws Exception If the construction of the manager fails
     */
    public BukkitCommandManager(@Nonnull final Plugin owningPlugin,
                                @Nonnull final Function<CommandTree<C>,
                                        CommandExecutionCoordinator<C>> commandExecutionCoordinator,
                                @Nonnull final Function<CommandSender, C> commandSenderMapper,
                                @Nonnull final Function<C, CommandSender> backwardsCommandSenderMapper)
            throws Exception {
        super(commandExecutionCoordinator, new BukkitPluginRegistrationHandler<>());
        ((BukkitPluginRegistrationHandler<C>) this.getCommandRegistrationHandler()).initialize(this);
        this.owningPlugin = owningPlugin;
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;

        /* Register Bukkit parsers */
        this.getParserRegistry().registerParserSupplier(TypeToken.of(World.class), params -> new WorldArgument.WorldParser<>());
        this.getParserRegistry().registerParserSupplier(TypeToken.of(Material.class),
                                                        params -> new MaterialArgument.MaterialParser<>());

        /* Try to determine the Minecraft version */
        int version = -1;
        try {
            final Matcher matcher = Pattern.compile("\\(MC: (\\d)\\.(\\d+)\\.?(\\d+?)?\\)")
                                            .matcher(Bukkit.getVersion());
            if (matcher.find()) {
                version = Integer.parseInt(matcher.toMatchResult().group(2),
                                           VERSION_RADIX);
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
        }
        this.paper = paper;
    }

    /**
     * Get the plugin that owns the manager
     *
     * @return Owning plugin
     */
    @Nonnull
    public Plugin getOwningPlugin() {
        return this.owningPlugin;
    }

    /**
     * Create default command meta data
     *
     * @return Meta data
     */
    @Nonnull
    @Override
    public BukkitCommandMeta createDefaultCommandMeta() {
        return BukkitCommandMetaBuilder.builder().withDescription("").build();
    }

    /**
     * Get the command sender mapper
     *
     * @return Command sender mapper
     */
    @Nonnull
    public final Function<CommandSender, C> getCommandSenderMapper() {
        return this.commandSenderMapper;
    }

    @Override
    public final boolean hasPermission(@Nonnull final C sender, @Nonnull final String permission) {
        if (permission.isEmpty()) {
            return true;
        }
        return this.backwardsCommandSenderMapper.apply(sender).hasPermission(permission);
    }

    protected final void setSplitAliases(final boolean value) {
        this.splitAliases = value;
    }

    protected final boolean getSplitAliases() {
        return this.splitAliases;
    }

    protected final void checkBrigadierCompatibility() throws BrigadierFailureException {
        if (!this.queryCapability(CloudBukkitCapabilities.BRIGADIER)) {
            throw new BrigadierFailureException(BrigadierFailureReason.VERSION_TOO_LOW,
                                                new IllegalArgumentException("Version: " + this.minecraftVersion));
        }
    }

    /**
     * Query for a specific capability
     *
     * @param capability Capability
     * @return {@code true} if the manager has the given capability, else {@code false}
     */
    public final boolean queryCapability(@Nonnull final CloudBukkitCapabilities capability) {
        return this.queryCapabilities().contains(capability);
    }

    /**
     * Check for the platform capabilities
     *
     * @return A set containing all capabilities of the instance
     */
    public final Set<CloudBukkitCapabilities> queryCapabilities() {
        if (this.paper) {
            if (this.minecraftVersion >= BRIGADIER_MINIMAL_VERSION) {
                if (this.minecraftVersion >= PAPER_BRIGADIER_VERSION) {
                    return EnumSet.of(CloudBukkitCapabilities.NATIVE_BRIGADIER,
                                      CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION,
                                      CloudBukkitCapabilities.BRIGADIER);
                } else {
                    return EnumSet.of(CloudBukkitCapabilities.COMMODORE_BRIGADIER,
                                      CloudBukkitCapabilities.BRIGADIER);
                }
            } else {
                return EnumSet.of(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION);
            }
        } else {
            if (this.minecraftVersion >= BRIGADIER_MINIMAL_VERSION) {
                return EnumSet.of(CloudBukkitCapabilities.COMMODORE_BRIGADIER,
                                  CloudBukkitCapabilities.BRIGADIER);
            }
        }
        return EnumSet.noneOf(CloudBukkitCapabilities.class);
    }

    /**
     * Attempt to register the Brigadier mapper, and return it.
     *
     * @throws BrigadierFailureException If Brigadier isn't
     *         supported by the platform
     */
    public void registerBrigadier() throws BrigadierFailureException {
        this.checkBrigadierCompatibility();
        try {
            final CloudCommodoreManager<C> cloudCommodoreManager = new CloudCommodoreManager<>(this);
            cloudCommodoreManager.initialize(this);
            this.setCommandRegistrationHandler(cloudCommodoreManager);
        } catch (final Throwable e) {
            throw new BrigadierFailureException(BrigadierFailureReason.COMMODORE_NOT_PRESENT, e);
        }
    }


    /**
     * Reasons to explain why Brigadier failed to initialize
     */
    public enum BrigadierFailureReason {
        COMMODORE_NOT_PRESENT, VERSION_TOO_LOW, PAPER_BRIGADIER_INITIALIZATION_FAILURE
    }


    public static final class BrigadierFailureException extends IllegalStateException {

        private final BrigadierFailureReason reason;

        /**
         * Initialize a new Brigadier failure exception
         *
         * @param reason Reason
         */
        public BrigadierFailureException(@Nonnull final BrigadierFailureReason reason) {
            this.reason = reason;
        }

        /**
         * Initialize a new Brigadier failure exception
         *
         * @param reason Reason
         * @param cause  Cause
         */
        public BrigadierFailureException(@Nonnull final BrigadierFailureReason reason, @Nonnull final Throwable cause) {
            super(cause);
            this.reason = reason;
        }

        /**
         * Get the reason for the exception
         *
         * @return Reason
         */
        @Nonnull
        public BrigadierFailureReason getReason() {
            return this.reason;
        }

        @Override
        public String getMessage() {
            return String.format("Could not initialize Brigadier mappings. Reason: %s (%s)",
                                 this.reason.name().toLowerCase().replace("_", " "),
                                 this.getCause() == null ? "" : this.getCause().getMessage());
        }

    }

}
