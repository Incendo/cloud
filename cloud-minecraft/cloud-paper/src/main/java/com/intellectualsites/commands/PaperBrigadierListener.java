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

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import com.intellectualsites.commands.brigadier.CloudBrigadierManager;
import com.intellectualsites.commands.components.CommandComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;

class PaperBrigadierListener implements Listener {

    private final CloudBrigadierManager<BukkitCommandSender, BukkitBrigadierCommandSource> brigadierManager;
    private final PaperCommandManager paperCommandManager;

    PaperBrigadierListener(@Nonnull final PaperCommandManager paperCommandManager) throws Exception {
        this.paperCommandManager = paperCommandManager;
        this.brigadierManager = new CloudBrigadierManager<>();
    }

    @EventHandler
    public void onCommandRegister(@Nonnull final CommandRegisteredEvent<BukkitBrigadierCommandSource> event) {
        final CommandTree<BukkitCommandSender, BukkitCommandMeta> commandTree = this.paperCommandManager.getCommandTree();
        final CommandTree.Node<CommandComponent<BukkitCommandSender, ?>> node = commandTree.getNamedNode(event.getCommandLabel());
        if (node == null) {
            return;
        }
        event.setLiteral(this.brigadierManager.createLiteralCommandNode(node,
                                                                        event.getLiteral(),
                                                                        event.getBrigadierCommand(),
                                                                        event.getBrigadierCommand(),
                                                                        (s, p) -> s.getBukkitSender().hasPermission(p)));
    }

}
