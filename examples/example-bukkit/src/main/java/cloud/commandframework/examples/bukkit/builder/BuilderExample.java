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
package cloud.commandframework.examples.bukkit.builder;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.builder.feature.CommandBeanExample;
import cloud.commandframework.examples.bukkit.builder.feature.CompoundArgumentExample;
import cloud.commandframework.examples.bukkit.builder.feature.ConfirmationExample;
import cloud.commandframework.examples.bukkit.builder.feature.EnumExample;
import cloud.commandframework.examples.bukkit.builder.feature.FlagExample;
import cloud.commandframework.examples.bukkit.builder.feature.HelpExample;
import cloud.commandframework.examples.bukkit.builder.feature.PermissionExample;
import cloud.commandframework.examples.bukkit.builder.feature.StringArrayExample;
import cloud.commandframework.examples.bukkit.builder.feature.TaskRecipeExample;
import cloud.commandframework.examples.bukkit.builder.feature.minecraft.ItemStackExample;
import cloud.commandframework.examples.bukkit.builder.feature.minecraft.SelectorExample;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.RichDescription;
import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import static cloud.commandframework.arguments.standard.EnumParser.enumParser;
import static cloud.commandframework.arguments.standard.IntegerParser.integerParser;
import static cloud.commandframework.bukkit.parsers.EnchantmentParser.enchantmentParser;
import static cloud.commandframework.bukkit.parsers.MaterialParser.materialParser;
import static cloud.commandframework.bukkit.parsers.NamespacedKeyParser.namespacedKeyParser;
import static cloud.commandframework.bukkit.parsers.WorldParser.worldParser;
import static cloud.commandframework.minecraft.extras.TextColorParser.textColorParser;
import static cloud.commandframework.paper.parser.KeyedWorldParser.keyedWorldParser;
import static net.kyori.adventure.text.Component.text;

/**
 * Main entrypoint for all builder-related examples. It registers a bunch of {@link #FEATURES features}
 * that showcase specific cloud concepts.
 */
public final class BuilderExample {

    private static final List<BuilderFeature> FEATURES = Arrays.asList(
            new CommandBeanExample(),
            new CompoundArgumentExample(),
            new ConfirmationExample(),
            new EnumExample(),
            new FlagExample(),
            new HelpExample(),
            new PermissionExample(),
            new StringArrayExample(),
            new TaskRecipeExample(),
            // Minecraft-specific features
            new ItemStackExample(),
            new SelectorExample()
    );

    private final ExamplePlugin examplePlugin;
    private final BukkitCommandManager<CommandSender> manager;

    public BuilderExample(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        this.examplePlugin = examplePlugin;
        this.manager = manager;

        // Set up the example modules.
        this.setupExamples();
    }

    private void setupExamples() {
        FEATURES.forEach(feature -> feature.registerFeature(this.examplePlugin, this.manager));
    }

    @SuppressWarnings("unused")
    private void constructCommands() {
        //
        // Base command builder
        //
        final Command.Builder<CommandSender> builder = this.manager.commandBuilder("example");
        //
        // Create a teleportation command
        //
        this.manager.command(builder.literal("teleport")
                        .meta(CommandMeta.DESCRIPTION, "Teleport to a world")
                        .senderType(Player.class)
                        .required("world", worldParser(), ArgumentDescription.of("World to teleport to"))
                        .handler(context -> this.manager.taskRecipe().begin(context).synchronous(ctx -> {
                            final Player player = ctx.getSender();
                            player.teleport(ctx.<World>get("world").getSpawnLocation());
                            player.sendMessage(ChatColor.GREEN + "You have been teleported!");
                        }).execute()));

        this.manager.command(this.manager.commandBuilder("give")
                .senderType(Player.class)
                .required("material", materialParser())
                .required("amount", integerParser())
                .handler(c -> {
                    final Material material = c.get("material");
                    final int amount = c.get("amount");
                    final ItemStack itemStack = new ItemStack(material, amount);
                    c.getSender().getInventory().addItem(itemStack);
                    c.getSender().sendMessage("You've been given stuff, bro.");
                }));

        this.manager.command(builder.literal("summon")
                .senderType(Player.class)
                .required("type", enumParser(EntityType.class))
                .handler(c -> this.manager.taskRecipe().begin(c).synchronous(ctx -> {
                    final Location loc = ctx.getSender().getLocation();
                    loc.getWorld().spawnEntity(loc, ctx.get("type"));
                }).execute()));

        this.manager.command(builder.literal("enchant")
                .senderType(Player.class)
                .required("enchant", enchantmentParser())
                .required("level", integerParser())
                .handler(c -> this.manager.taskRecipe().begin(c).synchronous(ctx -> {
                    final Player player = ctx.getSender();
                    player.getInventory().getItemInMainHand().addEnchantment(ctx.get("enchant"), ctx.get("level"));
                }).execute()));

        //
        // A command to change the color scheme for the help command
        //
        this.manager.command(builder
                        .meta(CommandMeta.DESCRIPTION, "Sets the color scheme for '/example help'")
                        .literal("helpcolors")
                        .required(
                                "primary", textColorParser(),
                                RichDescription.of(text("The primary color for the color scheme", Style.style(TextDecoration.ITALIC)))
                        )
                        .required(
                                "highlight", textColorParser(),
                                RichDescription.of(text("The primary color used to highlight commands and queries"))
                        )
                        .required(
                                "alternate_highlight",
                                textColorParser(),
                                RichDescription.of(text("The secondary color used to highlight commands and queries"))
                        ).required("text", textColorParser(), RichDescription.of(text("The color used for description text")))
                        .required("accent", textColorParser(), RichDescription.of(text("The color used for accents and symbols")))
                /*.handler(c -> this.minecraftHelp.setHelpColors(MinecraftHelp.HelpColors.of(
                        c.get("primary"),
                        c.get("highlight"),
                        c.get("alternate_highlight"),
                        c.get("text"),
                        c.get("accent")
                )))*/
        );

        // Commands using MC 1.13+ argument types
        if (this.manager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            new Mc113(this.manager).registerCommands();
        }

        this.registerNamespacedKeyUsingCommand();
        this.manager.command(builder.literal("keyed_world")
                .required("world", keyedWorldParser())
                .senderType(Player.class)
                .handler(ctx -> {
                    final World world = ctx.get("world");
                    final Player sender = ctx.getSender();
                    Bukkit.getScheduler().runTask(this.examplePlugin, () -> sender.teleport(world.getSpawnLocation()));
                }));
    }

    private void registerNamespacedKeyUsingCommand() {
        boolean nsk = true;
        try {
            Class.forName("org.bukkit.NamespacedKey");
        } catch (final ClassNotFoundException e) {
            nsk = false;
        }

        if (!nsk) {
            return;
        }

        this.manager.command(this.manager.commandBuilder("example")
                .literal("namespacedkey")
                .required("key", namespacedKeyParser())
                .handler(ctx -> {
                    final NamespacedKey namespacedKey = ctx.get("key");
                    final String key = namespacedKey.getNamespace() + ":" + namespacedKey.getKey();
                    ctx.getSender().sendMessage("The key you typed is '" + key + "'.");
                }));
    }
}
