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

import com.intellectualsites.commands.annotations.AnnotationParser;
import com.intellectualsites.commands.annotations.Argument;
import com.intellectualsites.commands.annotations.CommandMethod;
import com.intellectualsites.commands.annotations.Description;
import com.intellectualsites.commands.annotations.specifier.Completions;
import com.intellectualsites.commands.annotations.specifier.Range;
import com.intellectualsites.commands.arguments.parser.ArgumentParseResult;
import com.intellectualsites.commands.arguments.parser.StandardParameters;
import com.intellectualsites.commands.arguments.standard.BooleanArgument;
import com.intellectualsites.commands.arguments.standard.DoubleArgument;
import com.intellectualsites.commands.arguments.standard.EnumArgument;
import com.intellectualsites.commands.arguments.standard.FloatArgument;
import com.intellectualsites.commands.arguments.standard.IntegerArgument;
import com.intellectualsites.commands.arguments.standard.StringArgument;
import com.intellectualsites.commands.bukkit.BukkitCommandManager;
import com.intellectualsites.commands.bukkit.BukkitCommandMetaBuilder;
import com.intellectualsites.commands.bukkit.CloudBukkitCapabilities;
import com.intellectualsites.commands.bukkit.parsers.WorldArgument;
import com.intellectualsites.commands.execution.AsynchronousCommandExecutionCoordinator;
import com.intellectualsites.commands.execution.CommandExecutionCoordinator;
import com.intellectualsites.commands.meta.SimpleCommandMeta;
import com.intellectualsites.commands.paper.PaperCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class BukkitTest extends JavaPlugin {

    private static final int PERC_MIN = 0;
    private static final int PERC_MAX = 100;

    private BukkitCommandManager<CommandSender> mgr;

    @Override
    public void onEnable() {
        try {
            final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
                    AsynchronousCommandExecutionCoordinator.<CommandSender>newBuilder().build();
            mgr = new PaperCommandManager<>(
                    this,
                    executionCoordinatorFunction,
                    Function.identity(),
                    Function.identity()
            );

            try {
                ((PaperCommandManager<CommandSender>) mgr).registerBrigadier();
            } catch (final Exception e) {
                getLogger().warning("Failed to initialize Brigadier support: " + e.getMessage());
            }

            try {
                ((PaperCommandManager<CommandSender>) mgr).registerAsynchronousCompletions();
            } catch (final Throwable e) {
                getLogger().warning("Failed to register asynchronous command completions: " + e.getMessage());
            }

            final AnnotationParser<CommandSender> annotationParser
                    = new AnnotationParser<>(mgr, CommandSender.class, p ->
                    BukkitCommandMetaBuilder.builder().withDescription(p.get(StandardParameters.DESCRIPTION,
                                                                             "No description")).build());
            annotationParser.parse(this);
            //noinspection all

            mgr.command(mgr.commandBuilder("gamemode", this.metaWithDescription("Your ugli"), "gajmöde")
                           .argument(EnumArgument.required(GameMode.class, "gamemode"))
                           .argument(StringArgument.<CommandSender>newBuilder("player")
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
                           .handler(c -> ((Player) c.getSender())
                                   .setGameMode(c.<GameMode>get("gamemode")
                                                        .orElse(GameMode.SURVIVAL)))
                           .build())
               .command(mgr.commandBuilder("kenny", "k")
                           .literal("sux", "s")
                           .argument(IntegerArgument
                                             .<CommandSender>newBuilder("perc")
                                             .withMin(PERC_MIN).withMax(PERC_MAX).build())
                           .handler(context -> {
                               context.getSender().sendMessage(String.format(
                                       "Kenny sux %d%%",
                                       context.<Integer>get("perc").orElse(PERC_MIN)
                               ));
                           })
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
                           .withSenderType(Player.class)
                           .argument(EnumArgument.required(Material.class, "material"))
                           .argument(IntegerArgument.required("amount"))
                           .handler(c -> {
                               final Material material = c.getRequired("material");
                               final int amount = c.getRequired("amount");
                               final ItemStack itemStack = new ItemStack(material, amount);
                               ((Player) c.getSender()).getInventory().addItem(itemStack);
                               c.getSender().sendMessage("You've been given stuff, bro.");
                           })
                           .build())
               .command(mgr.commandBuilder("worldtp", BukkitCommandMetaBuilder.builder()
                                                                              .withDescription("Teleport to a world")
                                                                              .build())
                           .argument(WorldArgument.required("world"))
                           .handler(c -> {
                               final World world = c.getRequired("world");
                               ((Player) c.getSender()).teleport(world.getSpawnLocation());
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

    @Description("Test cloud command using @CommandMethod")
    @CommandMethod(value = "annotation|a <input> [number]", permission = "some.permission.node")
    private void annotatedCommand(@Nonnull final Player player,
                                  @Argument("input") @Completions("one,two,duck") @Nonnull final String input,
                                  @Argument(value = "number", defaultValue = "5") @Range(min = "10", max = "100")
                                      final int number) {
        player.sendMessage(ChatColor.GOLD + "Your input was: " + ChatColor.AQUA + input + ChatColor.GREEN + " (" + number + ")");
    }

    @CommandMethod("cloud help")
    private void doHelp() {
        final Set<CloudBukkitCapabilities> capabilities = this.mgr.queryCapabilities();
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Capabilities");
        for (final CloudBukkitCapabilities capability : capabilities) {
            Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "- " + ChatColor.AQUA + capability);
        }
        Bukkit.broadcastMessage(ChatColor.GRAY + "Using Registration Manager: "
                                        + this.mgr.getCommandRegistrationHandler().getClass().getSimpleName());
        Bukkit.broadcastMessage(ChatColor.GRAY + "Calling Thread: " + Thread.currentThread().getName());
    }

    @Nonnull
    private SimpleCommandMeta metaWithDescription(@Nonnull final String description) {
        return BukkitCommandMetaBuilder.builder().withDescription(description).build();
    }

}
