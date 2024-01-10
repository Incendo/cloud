//
// MIT License
//
// Copyright (c) 2024 Incendo
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

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.Flag;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.annotations.AnnotationFeature;
import java.util.Locale;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Example that showcases command flags with values.
 */
public final class FlagExample implements AnnotationFeature {

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull AnnotationParser<CommandSender> annotationParser
    ) {
        annotationParser.parse(this);
    }

    @CommandMethod("annotations give <material> <amount>")
    @CommandDescription("Give yourself an item")
    public void commandGive(
            final @NonNull Player player,
            final @Argument("material") @NonNull Material material,
            final @Argument("amount") int number,
            final @Flag("color") @Nullable ChatColor nameColor,
            final @Flag("enchant") @Nullable Enchantment enchant
    ) {
        final ItemStack itemStack = new ItemStack(material, number);
        String itemName = String.format(
                "%s's %s",
                player.getName(),
                material.name()
                        .toLowerCase(Locale.ROOT)
                        .replace('_', ' ')
        );
        if (nameColor != null) {
            itemName = nameColor + itemName;
        }
        final ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(itemName);
            itemStack.setItemMeta(meta);
        }
        if (enchant != null) {
            itemStack.addUnsafeEnchantment(enchant, 10);
        }
        player.getInventory().addItem(itemStack);
        player.sendMessage(ChatColor.GREEN + String.format("You have been given %d x %s", number, material));
    }
}
