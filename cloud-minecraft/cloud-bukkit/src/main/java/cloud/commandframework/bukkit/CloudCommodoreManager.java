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

import cloud.commandframework.Command;
import cloud.commandframework.SenderMapper;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.brigadier.suggestion.TooltipSuggestion;
import cloud.commandframework.bukkit.internal.BukkitBackwardsBrigadierSenderMapper;
import cloud.commandframework.context.CommandContext;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

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
        this.brigadierManager = new CloudBrigadierManager<>(
                commandManager,
                () -> new CommandContext<>(
                        commandManager.senderMapper().map(Bukkit.getConsoleSender()),
                        commandManager
                ),
                commandManager.suggestionFactory().mapped(TooltipSuggestion::tooltipSuggestion),
                SenderMapper.create(
                        sender -> {
                            final CommandSender bukkitSender = getBukkitSender(sender);
                            return this.commandManager.senderMapper().map(bukkitSender);
                        },
                        new BukkitBackwardsBrigadierSenderMapper<>(this.commandManager)
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
        this.registerWithCommodore(label, (Command<C>) command);
    }

    @Override
    protected void unregisterExternal(final @NonNull String label) {
        this.unregisterWithCommodore(label);
    }

    protected @NonNull CloudBrigadierManager<C, Object> brigadierManager() {
        return this.brigadierManager;
    }

    private void registerWithCommodore(
            final @NonNull String label,
            final @NonNull Command<C> command
    ) {
        final LiteralCommandNode<?> literalCommandNode = this.brigadierManager.literalBrigadierNodeFactory()
                .createNode(label, command, (commandSourceStack, commandPermission) -> {
                    // We need to check that the command still exists...
                    if (this.commandManager.commandTree().getNamedNode(label) == null) {
                        return false;
                    }

                    final CommandSender bukkitSender = getBukkitSender(commandSourceStack);
                    return this.commandManager.hasPermission(
                            this.commandManager.senderMapper().map(bukkitSender),
                            commandPermission
                    );
                }, o -> 1);
        final CommandNode existingNode = this.getDispatcher().findNode(Collections.singletonList(label));
        if (existingNode != null) {
            this.mergeChildren(existingNode, literalCommandNode);
        } else {
            this.commodore.register(literalCommandNode);
        }
    }

    private void unregisterWithCommodore(
            final @NonNull String label
    ) {
        final CommandDispatcher<?> dispatcher = this.getDispatcher();
        final CommandNode node = dispatcher.findNode(Collections.singletonList(label));
        if (node == null) {
            return;
        }

        try {
            final Class<?> commodoreImpl = this.commodore.getClass();

            Method removeChild;
            try {
                removeChild = commodoreImpl.getDeclaredMethod("removeChild", RootCommandNode.class, String.class);
            } catch (final NoSuchMethodException ex) {
                removeChild = commodoreImpl.getSuperclass().getDeclaredMethod("removeChild", RootCommandNode.class, String.class);
            }
            removeChild.setAccessible(true);

            removeChild.invoke(
                    null /* static method */,
                    dispatcher.getRoot(),
                    node.getName()
            );

            final Field registeredNodesField = commodoreImpl.getDeclaredField("registeredNodes");
            registeredNodesField.setAccessible(true);

            final List<?> registeredNodes = (List<?>) registeredNodesField.get(this.commodore);
            registeredNodes.remove(node);
        } catch (final Exception e) {
            throw new RuntimeException(String.format("Failed to unregister command '%s' with commodore", label), e);
        }
    }

    private void mergeChildren(final CommandNode<?> existingNode, final CommandNode<?> node) {
        for (final CommandNode child : node.getChildren()) {
            final CommandNode<?> existingChild = existingNode.getChild(child.getName());
            if (existingChild == null) {
                existingNode.addChild(child);
            } else {
                this.mergeChildren(existingChild, child);
            }
        }
    }

    private CommandDispatcher<?> getDispatcher() {
        try {
            final Method getDispatcherMethod = this.commodore.getClass().getDeclaredMethod("getDispatcher");
            getDispatcherMethod.setAccessible(true);
            return (CommandDispatcher<?>) getDispatcherMethod.invoke(this.commodore);
        } catch (final ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static CommandSender getBukkitSender(final @NonNull Object commandSourceStack) {
        Objects.requireNonNull(commandSourceStack, "commandSourceStack");
        try {
            final Method getBukkitSenderMethod = commandSourceStack.getClass().getDeclaredMethod("getBukkitSender");
            getBukkitSenderMethod.setAccessible(true);
            return (CommandSender) getBukkitSenderMethod.invoke(commandSourceStack);
        } catch (final ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
