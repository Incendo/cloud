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

import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.builder.BuilderFeature;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.NonNull;

import static cloud.commandframework.arguments.standard.EnumParser.enumParser;
import static cloud.commandframework.arguments.standard.IntegerParser.integerParser;
import static cloud.commandframework.bukkit.parsers.EnchantmentParser.enchantmentParser;
import static cloud.commandframework.bukkit.parsers.MaterialParser.materialParser;

/**
 * Example that showcases command flags with values.
 */
public final class FlagExample implements BuilderFeature {

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        manager.command(
                manager.commandBuilder("builder")
                        .literal("give")
                        .required("material", materialParser())
                        .required("amount", integerParser(0 /* minValue */))
                        .flag(manager.flagBuilder("color").withComponent(enumParser(ChatColor.class)))
                        .flag(manager.flagBuilder("enchant").withComponent(enchantmentParser()))
                        .senderType(Player.class)
                        .handler(context -> {
                            final ItemStack itemStack = new ItemStack(
                                    context.get("material"),
                                    context.get("amount")
                            );
                            String itemName = String.format(
                                    "%s's %s",
                                    context.sender().getName(),
                                    context.<Material>get("material").name()
                                            .toLowerCase(Locale.ROOT)
                                            .replace('_', ' ')
                            );
                            if (context.flags().contains("color")) {
                                final ChatColor color = context.flags().get("color");
                                itemName = color + itemName;
                            }
                            final ItemMeta meta = itemStack.getItemMeta();
                            if (meta != null) {
                                meta.setDisplayName(itemName);
                                itemStack.setItemMeta(meta);
                            }
                            if (context.flags().contains("enchant")) {
                                final Enchantment enchantment = context.flags().get("enchant");
                                itemStack.addUnsafeEnchantment(Objects.requireNonNull(enchantment), 10);
                            }
                            context.sender().getInventory().addItem(itemStack);
                            context.sender().sendMessage(
                                    ChatColor.GREEN + String.format(
                                            "You have been given %d x %s",
                                            context.<Integer>get("amount"),
                                            context.<Material>get("material")
                                    )
                            );
                        })
        );
    }
}
