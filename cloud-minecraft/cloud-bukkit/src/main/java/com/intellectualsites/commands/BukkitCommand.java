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

import com.intellectualsites.commands.components.CommandComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.List;

final class BukkitCommand extends org.bukkit.command.Command implements PluginIdentifiableCommand {

    private final CommandComponent<BukkitCommandSender, ?> command;
    private final BukkitCommandManager bukkitCommandManager;

    BukkitCommand(@Nonnull final CommandComponent<BukkitCommandSender, ?> command,
                  @Nonnull final BukkitCommandManager bukkitCommandManager) {
        super(command.getName());
        this.command = command;
        this.bukkitCommandManager = bukkitCommandManager;
    }

    @Override
    public boolean execute(final CommandSender commandSender, final String s, final String[] strings) {
        /* Join input */
        final StringBuilder builder = new StringBuilder(this.command.getName());
        for (final String string : strings) {
            builder.append(" ").append(string);
        }
        this.bukkitCommandManager.executeCommand(BukkitCommandSender.of(commandSender), builder.toString())
                                 .whenComplete(((commandResult, throwable) -> {
                                     if (throwable != null) {
                                         throwable.printStackTrace();
                                     } else {
                                         // Do something...
                                         commandSender.sendMessage("All good!");
                                     }
                                 }));
        return true;
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) throws
            IllegalArgumentException {
        final StringBuilder builder = new StringBuilder(this.command.getName());
        for (final String string : args) {
            builder.append(" ").append(string);
        }
        return this.bukkitCommandManager.suggest(BukkitCommandSender.of(sender), builder.toString());
    }

    @Override
    public Plugin getPlugin() {
        return this.bukkitCommandManager.getOwningPlugin();
    }

}
