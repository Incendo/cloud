//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
package com.intellectualsites.commands;

import com.intellectualsites.commands.arguments.parser.ArgumentParseResult;
import com.intellectualsites.commands.arguments.standard.BooleanArgument;
import com.intellectualsites.commands.arguments.standard.DoubleArgument;
import com.intellectualsites.commands.arguments.standard.EnumArgument;
import com.intellectualsites.commands.arguments.standard.FloatArgument;
import com.intellectualsites.commands.arguments.standard.IntegerArgument;
import com.intellectualsites.commands.arguments.standard.StringArgument;
import com.intellectualsites.commands.execution.CommandExecutionCoordinator;
import com.intellectualsites.commands.parsers.WorldArgument;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class BukkitTest extends JavaPlugin {

    private static final int PERC_MIN = 0;
    private static final int PERC_MAX = 100;

    @Override
    public void onEnable() {
        try {
            final PaperCommandManager<BukkitCommandSender> mgr = new PaperCommandManager<>(
                    this,
                   CommandExecutionCoordinator
                           .simpleCoordinator(),
                   BukkitCommandSender::of,
                   BukkitCommandSender::getInternalSender
            );
            mgr.registerBrigadier();
            mgr.command(mgr.commandBuilder("gamemode",
                                           Collections.singleton("gajmöde"),
                                           BukkitCommandMetaBuilder.builder()
                                                                   .withDescription("Your ugli")
                                                                   .build())
                           .argument(EnumArgument.required(GameMode.class, "gamemode"))
                           .argument(StringArgument.<BukkitCommandSender>newBuilder("player")
                                              .withSuggestionsProvider((v1, v2) -> {
                                                  final List<String> suggestions =
                                                          new ArrayList<>(
                                                                  Bukkit.getOnlinePlayers()
                                                                        .stream()
                                                                        .map(Player::getName)
                                                                        .collect(Collectors.toList()));
                                                  suggestions.add("dog");
                                                  suggestions.add("cat");
                                                  return suggestions;
                                              }).build())
                           .handler(c -> c.getSender()
                                          .asPlayer()
                                          .setGameMode(c.<GameMode>get("gamemode")
                                                               .orElse(GameMode.SURVIVAL)))
                           .build())
               .command(mgr.commandBuilder("kenny")
                           .literal("sux")
                           .argument(IntegerArgument
                                              .<BukkitCommandSender>newBuilder("perc")
                                              .withMin(PERC_MIN).withMax(PERC_MAX).build())
                           .handler(context -> {
                               context.getSender().asPlayer().sendMessage(String.format(
                                       "Kenny sux %d%%",
                                       context.<Integer>get("perc").orElse(PERC_MIN)
                               ));
                           })
                           .build())
               .command(mgr.commandBuilder("test")
                           .literal("one")
                           .handler(c -> c.getSender().sendMessage("One!"))
                           .build())
               .command(mgr.commandBuilder("test")
                           .literal("two")
                           .handler(c -> c.getSender().sendMessage("Two!"))
                           .build())
               .command(mgr.commandBuilder("uuidtest")
                           .argument(UUID.class, "uuid", builder -> builder
                                   .asRequired()
                                   .withParser((c, i) -> {
                                       final String string = i.peek();
                                       try {
                                           final UUID uuid = UUID.fromString(string);
                                           i.remove();
                                           return ArgumentParseResult.success(uuid);
                                       } catch (final Exception e) {
                                           return ArgumentParseResult.failure(e);
                                       }
                                   }).build())
                           .handler(c -> c.getSender()
                                          .sendMessage(String.format("UUID: %s\n", c.<UUID>get("uuid").orElse(null))))
                           .build())
               .command(mgr.commandBuilder("give")
                           .argument(EnumArgument.required(Material.class, "material"))
                           .argument(IntegerArgument.required("amount"))
                           .handler(c -> {
                               final Material material = c.getRequired("material");
                               final int amount = c.getRequired("amount");
                               final ItemStack itemStack = new ItemStack(material, amount);
                               c.getSender().asPlayer().getInventory().addItem(itemStack);
                               c.getSender().sendMessage("You've been given stuff, bro.");
                           })
                           .build())
               .command(mgr.commandBuilder("worldtp", BukkitCommandMetaBuilder.builder()
                                                                              .withDescription("Teleport to a world")
                                                                              .build())
                           .argument(WorldArgument.required("world"))
                           .handler(c -> {
                               final World world = c.getRequired("world");
                               c.getSender().asPlayer().teleport(world.getSpawnLocation());
                               c.getSender().sendMessage("Teleported.");
                           })
                           .build())
               .command(mgr.commandBuilder("brigadier")
                           .argument(FloatArgument.required("float"))
                           .argument(DoubleArgument.required("double"))
                           .argument(IntegerArgument.required("int"))
                           .argument(BooleanArgument.required("bool"))
                           .argument(StringArgument.required("string"))
                           .handler(c -> c.getSender().sendMessage("Executed the command"))
                           .build());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
