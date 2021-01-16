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
package cloud.commandframework.paper;

import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.BukkitBrigadierMapper;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.permission.CommandPermission;
import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.BiPredicate;
import java.util.regex.Pattern;

class PaperBrigadierListener<C> implements Listener {

    private final CloudBrigadierManager<C, BukkitBrigadierCommandSource> brigadierManager;
    private final PaperCommandManager<C> paperCommandManager;

    PaperBrigadierListener(final @NonNull PaperCommandManager<C> paperCommandManager) {
        this.paperCommandManager = paperCommandManager;
        this.brigadierManager = new CloudBrigadierManager<>(
                this.paperCommandManager,
                () -> new CommandContext<>(
                        this.paperCommandManager.getCommandSenderMapper()
                                .apply(Bukkit.getConsoleSender()),
                        this.paperCommandManager
                )
        );
        this.brigadierManager.brigadierSenderMapper(
                sender -> this.paperCommandManager.getCommandSenderMapper().apply(sender.getBukkitSender())
        );
        new BukkitBrigadierMapper<>(this.paperCommandManager, this.brigadierManager);
    }

    protected @NonNull CloudBrigadierManager<C, BukkitBrigadierCommandSource> brigadierManager() {
        return this.brigadierManager;
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onCommandRegister(
            final com.destroystokyo.paper.event.brigadier.
            @NonNull CommandRegisteredEvent<BukkitBrigadierCommandSource> event
    ) {
        if (!(event.getCommand() instanceof PluginIdentifiableCommand)) {
            return;
        } else if (!((PluginIdentifiableCommand) event.getCommand())
                .getPlugin().equals(this.paperCommandManager.getOwningPlugin())) {
            return;
        }

        final CommandTree<C> commandTree = this.paperCommandManager.getCommandTree();

        String label = event.getCommandLabel();
        if (label.contains(":")) {
            label = label.split(Pattern.quote(":"))[1];
        }

        final CommandTree.Node<CommandArgument<C, ?>> node = commandTree.getNamedNode(label);
        if (node == null) {
            return;
        }
        final BiPredicate<BukkitBrigadierCommandSource, CommandPermission> permissionChecker = (s, p) -> {
            final C sender = this.paperCommandManager.getCommandSenderMapper().apply(s.getBukkitSender());
            return this.paperCommandManager.hasPermission(sender, p);
        };
        event.setLiteral(this.brigadierManager.createLiteralCommandNode(
                node,
                event.getLiteral(),
                event.getBrigadierCommand(),
                event.getBrigadierCommand(),
                permissionChecker
        ));
    }

}
