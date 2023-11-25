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
package cloud.commandframework.examples.bukkit.builder.feature;

import cloud.commandframework.CommandManager;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.builder.BuilderFeature;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import static net.kyori.adventure.text.Component.text;

/**
 * Example of a command that requires confirmation before executing.
 */
public final class ConfirmationExample implements BuilderFeature {

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        this.registerConfirmationManager(manager);

        manager.command(
                manager.commandBuilder("builder")
                        .literal("clear")
                        .senderType(Player.class)
                        .meta(CommandConfirmationManager.META_CONFIRMATION_REQUIRED, true)
                        .handler(context -> {
                            context.getSender().getInventory().clear();
                            examplePlugin.bukkitAudiences()
                                    .player(context.getSender())
                                    .sendMessage(text("Your inventory has been cleared", NamedTextColor.GOLD));
                        })
        );
    }

    private void registerConfirmationManager(final @NonNull CommandManager<CommandSender> commandManager) {
        // Create the confirmation this.manager. This allows us to require certain commands to be
        // confirmed before they can be executed
        final CommandConfirmationManager<CommandSender> confirmationManager = new CommandConfirmationManager<>(
                /* Timeout */ 30L,
                /* Timeout unit */ TimeUnit.SECONDS,
                /* Action when confirmation is required */ context -> context.getCommandContext().getSender().sendMessage(
                ChatColor.RED + "Confirmation required. Confirm using /builder confirm."),
                /* Action when no confirmation is pending */ sender -> sender.sendMessage(
                ChatColor.RED + "You don't have any pending commands.")
        );
        // Register the confirmation processor. This will enable confirmations for commands that require it
        confirmationManager.registerConfirmationProcessor(commandManager);

        // While it is possible to create an annotated command that handles the confirmations, it is not the recommended
        // way of doing it. It is easier to create a command using a builder, and let the confirmation manager handle
        // the logic.
        commandManager.command(
                commandManager.commandBuilder("builder")
                        .literal("confirm")
                        .handler(confirmationManager.createConfirmationExecutionHandler())
        );
    }
}
