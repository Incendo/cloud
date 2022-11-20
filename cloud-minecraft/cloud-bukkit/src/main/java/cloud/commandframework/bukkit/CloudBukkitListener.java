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

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.checkerframework.checker.nullness.qual.NonNull;


final class CloudBukkitListener<C> implements Listener {

    private final BukkitCommandManager<C> bukkitCommandManager;

    CloudBukkitListener(final @NonNull BukkitCommandManager<C> bukkitCommandManager) {
        this.bukkitCommandManager = bukkitCommandManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPlayerLogin(final @NonNull PlayerLoginEvent event) {
        /* If the server is brigadier-capable, any registration after players
           have joined (and been sent a command tree) is unsafe.
           Bukkit's PlayerJoinEvent is called just after the command tree is sent,
           so we have to perform this state change at PlayerLoginEvent to lock before that happens. */
        this.bukkitCommandManager.lockIfBrigadierCapable();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPluginDisable(final @NonNull PluginDisableEvent event) {
        if (event.getPlugin().equals(this.bukkitCommandManager.getOwningPlugin())) {
            this.bukkitCommandManager.rootCommands().forEach(this.bukkitCommandManager::deleteRootCommand);
        }
    }
}
