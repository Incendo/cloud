//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import org.bukkit.Bukkit;

import javax.annotation.Nonnull;

@SuppressWarnings("ALL")
class CloudCommodoreManager<C> extends BukkitPluginRegistrationHandler<C> {

    private final BukkitCommandManager<C> commandManager;
    private final CloudBrigadierManager brigadierManager;
    private final Commodore commodore;

    CloudCommodoreManager(@Nonnull final BukkitCommandManager<C> commandManager)
            throws BukkitCommandManager.BrigadierFailureException {
        if (!CommodoreProvider.isSupported()) {
            throw new BukkitCommandManager.BrigadierFailureException(BukkitCommandManager
                                                                             .BrigadierFailureReason.COMMODORE_NOT_PRESENT);
        }
        this.commandManager = commandManager;
        this.commodore = CommodoreProvider.getCommodore(commandManager.getOwningPlugin());
        this.brigadierManager = new CloudBrigadierManager<>(commandManager, () ->
                new CommandContext<>(commandManager.getCommandSenderMapper().apply(Bukkit.getConsoleSender())));
    }

    @Override
    protected void registerExternal(@Nonnull final String label,
                                    @Nonnull final Command<?> command,
                                    @Nonnull final BukkitCommand<C> bukkitCommand) {
        final com.mojang.brigadier.Command<?> cmd = o -> 1;
        final LiteralCommandNode<?> literalCommandNode = this.brigadierManager
                .createLiteralCommandNode(label, command, (o, p) -> true, cmd);
        this.commodore.register(bukkitCommand, literalCommandNode, p ->
                this.commandManager.hasPermission(commandManager.getCommandSenderMapper().apply(p),
                                                  command.getCommandPermission()));
    }
}
