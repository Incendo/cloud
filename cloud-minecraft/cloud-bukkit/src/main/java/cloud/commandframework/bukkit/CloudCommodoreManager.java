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
import cloud.commandframework.bukkit.internal.BukkitBackwardsBrigadierSenderMapper;
import cloud.commandframework.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings({"unchecked", "rawtypes"})
class CloudCommodoreManager<C> extends BukkitPluginRegistrationHandler<C> {

    private final BukkitCommandManager<C> commandManager;
    private final CloudBrigadierManager<C, Object> brigadierManager;
    private final Commodore commodore;

    CloudCommodoreManager(final @NonNull BukkitCommandManager<C> commandManager)
            throws BukkitCommandManager.BrigadierFailureException {
        if (!CommodoreProvider.isSupported()) {
            throw new BukkitCommandManager.BrigadierFailureException(BukkitCommandManager
                    .BrigadierFailureReason.COMMODORE_NOT_PRESENT);
        }
        this.commandManager = commandManager;
        this.commodore = CommodoreProvider.getCommodore(commandManager.getOwningPlugin());
        this.brigadierManager = new CloudBrigadierManager<>(commandManager, () -> new CommandContext<>(
                commandManager.getCommandSenderMapper().apply(Bukkit.getConsoleSender()),
                commandManager
        ));

        this.brigadierManager.brigadierSenderMapper(sender ->
                this.commandManager.getCommandSenderMapper().apply(this.commodore.getBukkitSender(sender)));

        new BukkitBrigadierMapper<>(this.commandManager, this.brigadierManager);

        this.brigadierManager.backwardsBrigadierSenderMapper(new BukkitBackwardsBrigadierSenderMapper<>(this.commandManager));
    }

    @Override
    protected void registerExternal(
            final @NonNull String label,
            final @NonNull Command<?> command,
            final @NonNull BukkitCommand<C> bukkitCommand
    ) {
        this.registerWithCommodore(label, (Command<C>) command);
    }

    @Override
    protected void unregisterExternal(final @NonNull String label) {
        this.unregisterWithCommodore(label);
    }

    protected @NonNull CloudBrigadierManager brigadierManager() {
        return this.brigadierManager;
    }

    private void registerWithCommodore(
            final @NonNull String label,
            final @NonNull Command<C> command
    ) {
        final LiteralCommandNode<?> literalCommandNode = this.brigadierManager
                .createLiteralCommandNode(label, command, (o, p) -> {
                    // We need to check that the command still exists...
                    if (this.commandManager.getCommandTree().getNamedNode(label) == null) {
                        return false;
                    }

                    final CommandSender sender = this.commodore.getBukkitSender(o);
                    return this.commandManager.hasPermission(this.commandManager.getCommandSenderMapper().apply(sender), p);
                }, false, o -> 1);
        final CommandNode existingNode = this.commodore.getDispatcher().findNode(Collections.singletonList(label));
        if (existingNode != null) {
            this.mergeChildren(existingNode, literalCommandNode);
        } else {
            this.commodore.register(literalCommandNode);
        }
    }

    private void unregisterWithCommodore(
            final @NonNull String label
    ) {
        final CommandNode node = this.commodore.getDispatcher().findNode(Collections.singletonList(label));
        if (node == null) {
            return;
        }

        try {
            final Class<? extends Commodore> commodoreImpl = (Class<? extends Commodore>) Class.forName("me.lucko.commodore.CommodoreImpl");

            final Method removeChild = commodoreImpl.getDeclaredMethod("removeChild", RootCommandNode.class, String.class);
            removeChild.setAccessible(true);

            removeChild.invoke(
                    null /* static method */,
                    this.commodore.getDispatcher().getRoot(),
                    node.getName()
            );

            final Field registeredNodes = commodoreImpl.getDeclaredField("registeredNodes");
            registeredNodes.setAccessible(true);

            ((List<LiteralCommandNode<?>>) registeredNodes.get(this.commodore)).remove(node);
        } catch (final Exception e) {
            throw new RuntimeException(String.format("Failed to unregister command '%s' with commodore", label), e);
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
