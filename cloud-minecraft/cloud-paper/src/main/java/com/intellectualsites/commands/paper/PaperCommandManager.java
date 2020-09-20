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
package com.intellectualsites.commands.paper;

import com.intellectualsites.commands.CommandTree;
import com.intellectualsites.commands.bukkit.BukkitCommandManager;
import com.intellectualsites.commands.bukkit.CloudBukkitCapabilities;
import com.intellectualsites.commands.execution.CommandExecutionCoordinator;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Paper command manager that extends {@link BukkitCommandManager}
 *
 * @param <C> Command sender type
 */
public class PaperCommandManager<C> extends BukkitCommandManager<C> {

    /**
     * Construct a new Paper command manager
     *
     * @param owningPlugin                 Plugin that is constructing the manager
     * @param commandExecutionCoordinator  Coordinator provider
     * @param commandSenderMapper          Function that maps {@link CommandSender} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link CommandSender}
     * @throws Exception If the construction of the manager fails
     */
    public PaperCommandManager(@Nonnull final Plugin owningPlugin,
                               @Nonnull final Function<CommandTree<C>,
                                       CommandExecutionCoordinator<C>> commandExecutionCoordinator,
                               @Nonnull final Function<CommandSender, C> commandSenderMapper,
                               @Nonnull final Function<C, CommandSender> backwardsCommandSenderMapper) throws
            Exception {
        super(owningPlugin, commandExecutionCoordinator, commandSenderMapper, backwardsCommandSenderMapper);

    }

    /**
     * Register Brigadier mappings using the native paper events
     *
     * @throws BrigadierFailureException Exception thrown if the mappings cannot be registered
     */
    @Override
    public void registerBrigadier() throws BrigadierFailureException {
        this.checkBrigadierCompatibility();
        if (!this.queryCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            super.registerBrigadier();
        } else {
            try {
                final PaperBrigadierListener<C> brigadierListener = new PaperBrigadierListener<>(this);
                Bukkit.getPluginManager().registerEvents(brigadierListener,
                                                         this.getOwningPlugin());
                this.setSplitAliases(true);
            } catch (final Throwable e) {
                throw new BrigadierFailureException(BrigadierFailureReason.PAPER_BRIGADIER_INITIALIZATION_FAILURE, e);
            }
        }
    }

}
