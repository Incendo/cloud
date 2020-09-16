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

import com.intellectualsites.commands.execution.CommandExecutionCoordinator;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Paper command manager that extends {@link BukkitCommandManager}
 *
 * @param <C> Command sender type
 */
public class PaperCommandManager<C extends com.intellectualsites.commands.sender.CommandSender>
        extends BukkitCommandManager<C> {

    /**
     * Construct a new Paper command manager
     *
     * @param owningPlugin                Plugin that is constructing the manager
     * @param commandExecutionCoordinator Coordinator provider
     * @param commandSenderMapper         Function that maps {@link CommandSender} to the command sender type
     * @throws Exception If the construction of the manager fails
     */
    public PaperCommandManager(@Nonnull final Plugin owningPlugin,
                               @Nonnull final Function<CommandTree<C, BukkitCommandMeta>,
                          CommandExecutionCoordinator<C, BukkitCommandMeta>> commandExecutionCoordinator,
                               @Nonnull final Function<CommandSender, C> commandSenderMapper) throws
            Exception {
        super(owningPlugin, commandExecutionCoordinator, commandSenderMapper);
    }

    /**
     * Attempt to register the Brigadier mapper, and return it.
     *
     * @return {@link PaperBrigadierListener} instance, if it could be created. If it cannot
     *         be created {@code null} is returned
     */
    @Nullable
    public PaperBrigadierListener<C> registerBrigadier() {
        try {
            final PaperBrigadierListener<C> brigadierListener = new PaperBrigadierListener<>(this);
            Bukkit.getPluginManager().registerEvents(brigadierListener,
                                                     this.getOwningPlugin());
            return brigadierListener;
        } catch (final Throwable e) {
            this.getOwningPlugin().getLogger().severe("Failed to register Brigadier listener");
            e.printStackTrace();
        }
        return null;
    }

}
