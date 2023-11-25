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
package cloud.commandframework.examples.bukkit.builder.feature.minecraft;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.data.ProtoItemStack;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.builder.BuilderFeature;
import cloud.commandframework.types.tuples.Pair;
import io.leangen.geantyref.TypeToken;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Example showcasing the use of the native item stack parser.
 */
public final class ItemStackExample implements BuilderFeature {

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        manager.command(
                manager.commandBuilder("builder")
                        .literal("gib")
                        .senderType(Player.class)
                        .requiredArgumentPair(
                                "itemstack",
                                TypeToken.get(ItemStack.class),
                                Pair.of("item", "amount"),
                                Pair.of(ProtoItemStack.class, Integer.class),
                                (sender, pair) -> {
                                    final ProtoItemStack proto = pair.getFirst();
                                    final int amount = pair.getSecond();
                                    return proto.createItemStack(amount, true);
                                },
                                ArgumentDescription.of("The ItemStack to give")
                        )
                        .handler(ctx -> {
                            final ItemStack stack = ctx.get("itemstack");
                            ctx.getSender().getInventory().addItem(stack);
                        })
        );
    }
}
