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
package cloud.commandframework.examples.bukkit;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.data.BlockPredicate;
import cloud.commandframework.bukkit.data.ItemStackPredicate;
import cloud.commandframework.bukkit.data.ProtoItemStack;
import cloud.commandframework.context.CommandContext;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static cloud.commandframework.arguments.standard.IntegerParser.integerParser;
import static cloud.commandframework.bukkit.parsers.BlockPredicateParser.blockPredicateParser;
import static cloud.commandframework.bukkit.parsers.ItemStackParser.itemStackParser;
import static cloud.commandframework.bukkit.parsers.ItemStackPredicateParser.itemStackPredicateParser;
import static cloud.commandframework.bukkit.parsers.MaterialParser.materialParser;

@DefaultQualifier(NonNull.class)
final class Mc113 {

    private final BukkitCommandManager<CommandSender> manager;

    Mc113(final BukkitCommandManager<CommandSender> manager) {
        this.manager = manager;
    }

    void registerCommands() {
        final Command.Builder<CommandSender> builder = this.manager.commandBuilder("example")
                .literal("mc113");

        this.manager.command(builder.literal("replace")
                .senderType(Player.class)
                .required("predicate", blockPredicateParser())
                .literal("with")
                .required("block", materialParser()) // todo: use BlockDataArgument
                .required("radius", integerParser(1 /* minValue */))
                .handler(this::executeReplace));

        this.manager.command(builder.literal("test_item")
                .required("item", itemStackParser())
                .literal("is")
                .required("predicate", itemStackPredicateParser())
                .handler(Mc113::executeTestItem));
    }

    private void executeReplace(final CommandContext<Player> ctx) {
        final BlockData block = ctx.<Material>get("block").createBlockData();
        final BlockPredicate predicate = ctx.get("predicate");
        final int radius = ctx.get("radius");

        final Player player = ctx.getSender();
        final Location loc = player.getLocation();

        this.manager.taskRecipe().begin(ctx).synchronous(context -> {
            for (double x = loc.getX() - radius; x < loc.getX() + radius; x++) {
                for (double y = loc.getY() - radius; y < loc.getY() + radius; y++) {
                    for (double z = loc.getZ() - radius; z < loc.getZ() + radius; z++) {
                        final Block blockAt = player.getWorld().getBlockAt((int) x, (int) y, (int) z);
                        if (predicate.test(blockAt)) {
                            blockAt.setBlockData(block);
                        }
                    }
                }
            }
        }).execute();
    }

    private static void executeTestItem(final CommandContext<CommandSender> ctx) {
        final ProtoItemStack protoItemStack = ctx.get("item");
        final ItemStackPredicate predicate = ctx.get("predicate");
        ctx.getSender().sendMessage("result: " + predicate.test(
                protoItemStack.createItemStack(1, true)));
    }
}
