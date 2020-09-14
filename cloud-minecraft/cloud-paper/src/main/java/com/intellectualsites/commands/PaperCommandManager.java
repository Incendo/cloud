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
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Paper command manager that extends {@link BukkitCommandManager}
 */
public class PaperCommandManager extends BukkitCommandManager {

    /**
     * Construct a new Paper command manager
     *
     * @param owningPlugin                Plugin that is constructing the manager
     * @param commandExecutionCoordinator Coordinator provider
     * @throws Exception If the construction of the manager fails
     */
    public PaperCommandManager(@Nonnull final Plugin owningPlugin,
                               @Nonnull final Function<CommandTree<BukkitCommandSender, BukkitCommandMeta>,
                          CommandExecutionCoordinator<BukkitCommandSender, BukkitCommandMeta>> commandExecutionCoordinator) throws
            Exception {
        super(owningPlugin, commandExecutionCoordinator);
    }

    /**
     * Register the brigadier listener
     */
    public void registerBrigadier() {
        try {
            Bukkit.getPluginManager().registerEvents(new PaperBrigadierListener(this),
                                                     this.getOwningPlugin());
        } catch (final Exception e) {
            this.getOwningPlugin().getLogger().severe("Failed to register Brigadier listener");
            e.printStackTrace();
        }
    }

}
