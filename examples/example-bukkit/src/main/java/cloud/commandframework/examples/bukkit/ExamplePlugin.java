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

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.bukkit.arguments.selector.SingleEntitySelector;
import cloud.commandframework.bukkit.data.ProtoItemStack;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.examples.bukkit.annotations.AnnotationParserExample;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.keys.SimpleCloudKey;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.RichDescription;
import cloud.commandframework.paper.PaperCommandManager;
import cloud.commandframework.types.tuples.Pair;
import cloud.commandframework.types.tuples.Triplet;
import io.leangen.geantyref.TypeToken;
import java.util.Collections;
import java.util.function.Function;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.StringUtils;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;

import static cloud.commandframework.arguments.standard.EnumParser.enumParser;
import static cloud.commandframework.arguments.standard.IntegerParser.integerParser;
import static cloud.commandframework.arguments.standard.StringArrayParser.stringArrayParser;
import static cloud.commandframework.bukkit.parsers.EnchantmentParser.enchantmentParser;
import static cloud.commandframework.bukkit.parsers.MaterialParser.materialParser;
import static cloud.commandframework.bukkit.parsers.NamespacedKeyParser.namespacedKeyParser;
import static cloud.commandframework.bukkit.parsers.WorldParser.worldParser;
import static cloud.commandframework.bukkit.parsers.selector.SingleEntitySelectorParser.singleEntitySelectorParser;
import static cloud.commandframework.minecraft.extras.TextColorParser.textColorParser;
import static cloud.commandframework.paper.parser.KeyedWorldParser.keyedWorldParser;
import static net.kyori.adventure.text.Component.text;

/**
 * Example plugin class
 */
@SuppressWarnings("unused")
public final class ExamplePlugin extends JavaPlugin {

    private BukkitCommandManager<CommandSender> manager;
    private CommandConfirmationManager<CommandSender> confirmationManager;
    private BukkitAudiences bukkitAudiences;

    @Override
    public void onEnable() {
        try {
            //
            // (1) The execution coordinator determines how commands are executed. The simple execution coordinator will
            //     run the command on the thread that is calling it. In the case of Bukkit, this is the primary server thread.
            //     It is possible to execute (and parse!) commands asynchronously by using the
            //     AsynchronousCommandExecutionCoordinator.
            // (2) This functions maps the Bukkit command sender to your custom sender type. If you're not using a custom
            //     type, then Function.identity() maps CommandSender to itself.
            // (3) The same concept as (2), but mapping from your custom type to a Bukkit command sender.
            //
            this.manager = new PaperCommandManager<>(
                    /* Owning plugin */ this,
                    /* (1) */ CommandExecutionCoordinator.simpleCoordinator(),
                    /* (2) */ Function.identity(),
                    /* (3) */ Function.identity()
            );
        } catch (final Exception e) {
            this.getLogger().severe("Failed to initialize the command this.manager");
            /* Disable the plugin */
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        //
        // Use contains to filter suggestions instead of default startsWith
        //
        this.manager.commandSuggestionProcessor(new FilteringCommandSuggestionProcessor<>(
                FilteringCommandSuggestionProcessor.Filter.<CommandSender>contains(true).andTrimBeforeLastSpace()
        ));
        //
        // Register Brigadier mappings. The capability tells us whether Brigadier is natively available
        // on the current server. If it is, we can safely register the Brigadier integration.
        //
        if (this.manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            this.manager.registerBrigadier();
        }
        //
        // Register asynchronous completions. The capability tells us whether asynchronous completions
        // are available on the server that is running the plugin. The asynchronous completion method
        // is only available in cloud-paper, not cloud-bukkit.
        //
        if (this.manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            ((PaperCommandManager<CommandSender>) this.manager).registerAsynchronousCompletions();
        }
        //
        // Create the Bukkit audiences that maps command senders to adventure audience. This is not needed
        // if you're using paper-api instead of Bukkit.
        //
        this.bukkitAudiences = BukkitAudiences.create(this);
        //
        // Override the default exception handlers and use the exception handlers from cloud-minecraft-extras instead.
        //
        new MinecraftExceptionHandler<CommandSender>()
                .withInvalidSyntaxHandler()
                .withInvalidSenderHandler()
                .withNoPermissionHandler()
                .withArgumentParsingHandler()
                .withCommandExecutionHandler()
                .withDecorator(
                        component -> text()
                                .append(text("[", NamedTextColor.DARK_GRAY))
                                .append(text("Example", NamedTextColor.GOLD))
                                .append(text("] ", NamedTextColor.DARK_GRAY))
                                .append(component).build()
                ).apply(this.manager, this.bukkitAudiences::sender);
        //
        // Create the annotation examples.
        //
        new AnnotationParserExample(this, this.manager);


        //
        // Create the commands
        //
        // this.constructCommands();
    }

    private void constructCommands() {
        //
        // Base command builder
        //
        final Command.Builder<CommandSender> builder = this.manager.commandBuilder("example");
        //
        // Add a confirmation command
        //
        this.manager.command(builder.literal("confirm")
                .meta(CommandMeta.DESCRIPTION, "Confirm a pending command")
                .handler(this.confirmationManager.createConfirmationExecutionHandler()));
        //
        // Create a world argument
        //
        final CloudKey<World> worldKey = SimpleCloudKey.of("world", World.class);
        //
        // Create a teleportation command
        //
        this.manager.command(builder.literal("teleport")
                        .literal("me")
                        // Require a player sender
                        .senderType(Player.class)
                        .required(worldKey, worldParser(), ArgumentDescription.of("World name"))
                        .requiredArgumentTriplet(
                                "coords",
                                TypeToken.get(Vector.class),
                                Triplet.of("x", "y", "z"),
                                Triplet.of(Integer.class, Integer.class, Integer.class),
                                (sender, triplet) -> new Vector(triplet.getFirst(), triplet.getSecond(),
                                        triplet.getThird()
                                ),
                                ArgumentDescription.of("Coordinates")
                        )
                        .handler(context -> this.manager.taskRecipe().begin(context)
                                .synchronous(commandContext -> {
                                    final Player player = commandContext.getSender();
                                    final World world = commandContext.get(worldKey);
                                    final Vector coords = commandContext.get("coords");
                                    final Location location = coords.toLocation(world);
                                    player.teleport(location);
                                }).execute()))
                .command(builder.literal("teleport")
                        .literal("entity")
                        .senderType(Player.class)
                        .required("entity", singleEntitySelectorParser(), ArgumentDescription.of("Entity to teleport"))
                        .literal("here")
                        .handler(
                                context -> this.manager.taskRecipe().begin(context)
                                        .synchronous(commandContext -> {
                                            final Player player = commandContext.getSender();
                                            final SingleEntitySelector singleEntitySelector = commandContext
                                                    .get("entity");
                                            if (singleEntitySelector.hasAny()) {
                                                singleEntitySelector.getEntity().teleport(player);
                                                player.sendMessage(ChatColor.GREEN + "The entity was teleported to you!");
                                            } else {
                                                player.sendMessage(ChatColor.RED + "No entity matched your query.");
                                            }
                                        }).execute()
                        ))
                .command(builder.literal("teleport")
                        .meta(CommandMeta.DESCRIPTION, "Teleport to a world")
                        .senderType(Player.class)
                        .required("world", worldParser(), ArgumentDescription.of("World to teleport to"))
                        .handler(context -> this.manager.taskRecipe().begin(context).synchronous(ctx -> {
                            final Player player = ctx.getSender();
                            player.teleport(ctx.<World>get("world").getSpawnLocation());
                            player.sendMessage(ChatColor.GREEN + "You have been teleported!");
                        }).execute()));

        this.manager.command(builder.literal("tasktest")
                .handler(context -> this.manager.taskRecipe()
                        .begin(context)
                        .asynchronous(c -> {
                            c.getSender().sendMessage("ASYNC: " + !Bukkit.isPrimaryThread());
                            return c;
                        })
                        .synchronous(c -> {
                            c.getSender().sendMessage("SYNC: " + Bukkit.isPrimaryThread());
                        })
                        .execute(() -> context.getSender().sendMessage("DONE!"))
                ));

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

        //
        // Create a Bukkit-like command
        //
        this.manager.command(
                this.manager.commandBuilder(
                        "arraycommand",
                        ArgumentDescription.of("Bukkit-esque cmmand")
                ).optional(
                        "args",
                        stringArrayParser(),
                        ArgumentDescription.of("Arguments"),
                        (context, lastString) -> {
                            final CommandInput allArgs = context.rawInput();
                            if (allArgs.remainingTokens() > 1 && allArgs.readString().equals("curry")) {
                                return Collections.singletonList(Suggestion.simple("hot"));
                            }
                            return Collections.emptyList();
                        }
                ).handler(context -> {
                    final String[] args = context.getOrDefault("args", new String[0]);
                    context.getSender().sendMessage("You wrote: " + StringUtils.join(args, " "));
                })
        );

        // compound itemstack arg
        this.manager.command(this.manager.commandBuilder("gib")
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
                }));

        this.manager.command(builder.literal("keyed_world")
                .required("world", keyedWorldParser())
                .senderType(Player.class)
                .handler(ctx -> {
                    final World world = ctx.get("world");
                    final Player sender = ctx.getSender();
                    this.getServer().getScheduler().runTask(this, () -> sender.teleport(world.getSpawnLocation()));
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

    /**
     * Returns the {@link BukkitAudiences} instance.
     *
     * @return audiences
     */
    public @NonNull BukkitAudiences bukkitAudiences() {
        return this.bukkitAudiences;
    }
}
