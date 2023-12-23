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
package cloud.commandframework.examples.bukkit.annotations.feature;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.Confirmation;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.annotations.AnnotationFeature;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import static net.kyori.adventure.text.Component.text;

/**
 * Example of a command that requires confirmation before executing.
 */
public final class ConfirmationExample implements AnnotationFeature {

    private BukkitAudiences bukkitAudiences;

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull AnnotationParser<CommandSender> annotationParser
    ) {
        this.bukkitAudiences = examplePlugin.bukkitAudiences();
        this.registerConfirmationManager(annotationParser.manager());
        annotationParser.parse(this);
    }

    private void registerConfirmationManager(final @NonNull CommandManager<CommandSender> commandManager) {
        // Create the confirmation this.manager. This allows us to require certain commands to be
        // confirmed before they can be executed
        final CommandConfirmationManager<CommandSender> confirmationManager = new CommandConfirmationManager<>(
                /* Timeout */ 30L,
                /* Timeout unit */ TimeUnit.SECONDS,
                /* Action when confirmation is required */ context -> context.commandContext().sender().sendMessage(
                ChatColor.RED + "Confirmation required. Confirm using /annotations confirm."),
                /* Action when no confirmation is pending */ sender -> sender.sendMessage(
                ChatColor.RED + "You don't have any pending commands.")
        );
        // Register the confirmation processor. This will enable confirmations for commands that require it
        confirmationManager.registerConfirmationProcessor(commandManager);

        // While it is possible to create an annotated command that handles the confirmations, it is not the recommended
        // way of doing it. It is easier to create a command using a builder, and let the confirmation manager handle
        // the logic.
        commandManager.command(
                commandManager.commandBuilder("annotations")
                        .literal("confirm")
                        .handler(confirmationManager.createConfirmationExecutionHandler())
        );
    }

    @Confirmation
    @CommandMethod("annotations clear")
    @CommandDescription("Clear your inventory")
    public void commandClear(final @NonNull Player player) {
        player.getInventory().clear();
        this.bukkitAudiences.player(player)
                .sendMessage(text("Your inventory has been cleared", NamedTextColor.GOLD));
    }
}
