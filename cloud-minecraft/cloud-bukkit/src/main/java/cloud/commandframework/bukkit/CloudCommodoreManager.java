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

import cloud.commandframework.Command;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.permission.CommandPermission;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;

@SuppressWarnings({"unchecked", "rawtypes"})
class CloudCommodoreManager<C> extends BukkitPluginRegistrationHandler<C> {

    private final BukkitCommandManager<C> commandManager;
    private final CloudBrigadierManager brigadierManager;
    private final Commodore commodore;

    CloudCommodoreManager(final @NonNull BukkitCommandManager<C> commandManager)
            throws BukkitCommandManager.BrigadierFailureException {
        if (!CommodoreProvider.isSupported()) {
            throw new BukkitCommandManager.BrigadierFailureException(BukkitCommandManager
                    .BrigadierFailureReason.COMMODORE_NOT_PRESENT);
        }
        this.commandManager = commandManager;
        this.commodore = CommodoreProvider.getCommodore(commandManager.getOwningPlugin());
        this.brigadierManager = new CloudBrigadierManager<>(commandManager, () ->
                new CommandContext<>(
                        commandManager.getCommandSenderMapper().apply(Bukkit.getConsoleSender()),
                        commandManager
                ));
        this.brigadierManager.brigadierSenderMapper(
                sender -> this.commandManager.getCommandSenderMapper().apply(
                        commodore.getBukkitSender(sender)
                )
        );
        new BukkitBrigadierMapper<>(this.commandManager, this.brigadierManager);
    }

    @Override
    protected void registerExternal(
            final @NonNull String label,
            final @NonNull Command<?> command,
            final @NonNull BukkitCommand<C> bukkitCommand
    ) {
        this.registerWithCommodore(label, command);
    }

    protected @NonNull CloudBrigadierManager brigadierManager() {
        return this.brigadierManager;
    }

    private void registerWithCommodore(
            final @NonNull String label,
            final @NonNull Command<?> command
    ) {
        final com.mojang.brigadier.Command<?> cmd = o -> 1;
        final LiteralCommandNode<?> literalCommandNode = this.brigadierManager
                .createLiteralCommandNode(label, command, (o, p) -> {
                    final CommandSender sender = this.commodore.getBukkitSender(o);
                    return this.commandManager.hasPermission(
                            this.commandManager.getCommandSenderMapper().apply(sender),
                            (CommandPermission) p
                    );
                }, false, cmd);
        final CommandNode existingNode = this.commodore.getDispatcher().findNode(Collections.singletonList(label));
        if (existingNode != null) {
            this.mergeChildren(existingNode, literalCommandNode);
        } else {
            this.commodore.register(literalCommandNode);
        }
    }

    private void mergeChildren(@Nullable final CommandNode<?> existingNode, @Nullable final CommandNode<?> node) {
        for (final CommandNode child : node.getChildren()) {
            final CommandNode<?> existingChild = existingNode.getChild(child.getName());
            if (existingChild == null) {
                existingNode.addChild(child);
            } else {
                this.mergeChildren(existingChild, child);
            }
        }
    }

}
