//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.Confirmation;
import cloud.commandframework.annotations.Flag;
import cloud.commandframework.annotations.Regex;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArrayArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.bukkit.arguments.selector.SingleEntitySelector;
import cloud.commandframework.bukkit.data.ProtoItemStack;
import cloud.commandframework.bukkit.parsers.EnchantmentArgument;
import cloud.commandframework.bukkit.parsers.MaterialArgument;
import cloud.commandframework.bukkit.parsers.WorldArgument;
import cloud.commandframework.bukkit.parsers.selector.SingleEntitySelectorArgument;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.SimpleCaptionRegistry;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.keys.SimpleCloudKey;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.minecraft.extras.RichDescription;
import cloud.commandframework.minecraft.extras.TextColorArgument;
import cloud.commandframework.paper.PaperCommandManager;
import cloud.commandframework.paper.argument.KeyedWorldArgument;
import cloud.commandframework.permission.PredicatePermission;
import cloud.commandframework.tasks.TaskConsumer;
import cloud.commandframework.types.tuples.Pair;
import cloud.commandframework.types.tuples.Triplet;
import io.leangen.geantyref.TypeToken;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static net.kyori.adventure.text.Component.text;

/**
 * Example plugin class
 */
@SuppressWarnings("unused")
public final class ExamplePlugin extends JavaPlugin {

    private BukkitCommandManager<CommandSender> manager;
    private BukkitAudiences bukkitAudiences;
    private MinecraftHelp<CommandSender> minecraftHelp;
    private CommandConfirmationManager<CommandSender> confirmationManager;
    private AnnotationParser<CommandSender> annotationParser;

    @Override
    public void onEnable() {
        //
        // This is a function that will provide a command execution coordinator that parses and executes commands
        // asynchronously
        //
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
                AsynchronousCommandExecutionCoordinator.<CommandSender>newBuilder().build();
        //
        // However, in many cases it is fine for to run everything synchronously:
        //
        // final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
        //        CommandExecutionCoordinator.simpleCoordinator();
        //
        // This function maps the command sender type of our choice to the bukkit command sender.
        // However, in this example we use the Bukkit command sender, and so we just need to map it
        // to itself
        //
        final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
        try {
            this.manager = new PaperCommandManager<>(
                    /* Owning plugin */ this,
                    /* Coordinator function */ executionCoordinatorFunction,
                    /* Command Sender -> C */ mapperFunction,
                    /* C -> Command Sender */ mapperFunction
            );
        } catch (final Exception e) {
            this.getLogger().severe("Failed to initialize the command this.manager");
            /* Disable the plugin */
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        //
        // Create a BukkitAudiences instance (adventure) in order to use the minecraft-extras
        // help system
        //
        this.bukkitAudiences = BukkitAudiences.create(this);
        //
        // Create the Minecraft help menu system
        //
        this.minecraftHelp = new MinecraftHelp<>(
                /* Help Prefix */ "/example help",
                /* Audience mapper */ this.bukkitAudiences::sender,
                /* Manager */ this.manager
        );
        //
        // Register Brigadier mappings
        //
        if (this.manager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            this.manager.registerBrigadier();
        }
        //
        // Register asynchronous completions
        //
        if (this.manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            ((PaperCommandManager<CommandSender>) this.manager).registerAsynchronousCompletions();
        }
        //
        // Create the confirmation this.manager. This allows us to require certain commands to be
        // confirmed before they can be executed
        //
        this.confirmationManager = new CommandConfirmationManager<>(
                /* Timeout */ 30L,
                /* Timeout unit */ TimeUnit.SECONDS,
                /* Action when confirmation is required */ context -> context.getCommandContext().getSender().sendMessage(
                ChatColor.RED + "Confirmation required. Confirm using /example confirm."),
                /* Action when no confirmation is pending */ sender -> sender.sendMessage(
                ChatColor.RED + "You don't have any pending commands.")
        );
        //
        // Register the confirmation processor. This will enable confirmations for commands that require it
        //
        this.confirmationManager.registerConfirmationProcessor(this.manager);
        //
        // Create the annotation parser. This allows you to define commands using methods annotated with
        // @CommandMethod
        //
        final Function<ParserParameters, CommandMeta> commandMetaFunction = p ->
                CommandMeta.simple()
                        // This will allow you to decorate commands with descriptions
                        .with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description"))
                        .build();
        this.annotationParser = new AnnotationParser<>(
                /* Manager */ this.manager,
                /* Command sender type */ CommandSender.class,
                /* Mapper for command meta instances */ commandMetaFunction
        );
        //
        // Override the default exception handlers
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
        // Create the commands
        //
        this.constructCommands();
    }

    private void constructCommands() {
        // Add a custom permission checker
        this.annotationParser.registerBuilderModifier(
                GameModeRequirement.class,
                (requirement, builder) -> builder.permission(
                        PredicatePermission.of(SimpleCloudKey.of("gamemode"), sender ->
                                !(sender instanceof Player) || ((Player) sender).getGameMode() == requirement.value()
                        )
                )
        );
        //
        // Parse all @CommandMethod-annotated methods
        //
        this.annotationParser.parse(this);
        // Parse all @CommandContainer-annotated classes
        try {
            this.annotationParser.parseContainers();
        } catch (final Exception e) {
            e.printStackTrace();
        }
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
        final CommandArgument<CommandSender, World> worldArgument = WorldArgument.of("world");
        //
        // Create a teleportation command
        //
        this.manager.command(builder.literal("teleport")
                        .literal("me")
                        // Require a player sender
                        .senderType(Player.class)
                        .argument(worldArgument, ArgumentDescription.of("World name"))
                        .argumentTriplet(
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
                                    final Player player = (Player) commandContext.getSender();
                                    final World world = commandContext.get(worldArgument);
                                    final Vector coords = commandContext.get("coords");
                                    final Location location = coords.toLocation(world);
                                    player.teleport(location);
                                }).execute()))
                .command(builder.literal("teleport")
                        .literal("entity")
                        .senderType(Player.class)
                        .argument(
                                SingleEntitySelectorArgument.of("entity"),
                                ArgumentDescription.of("Entity to teleport")
                        )
                        .literal("here")
                        .handler(
                                context -> this.manager.taskRecipe().begin(context)
                                        .synchronous(commandContext -> {
                                            final Player player = (Player) commandContext.getSender();
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
                        .argument(WorldArgument.of("world"), ArgumentDescription.of("World to teleport to"))
                        .handler(context -> this.manager.taskRecipe().begin(context).synchronous(ctx -> {
                            final Player player = (Player) ctx.getSender();
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
                .argument(MaterialArgument.of("material"))
                .argument(IntegerArgument.of("amount"))
                .handler(c -> {
                    final Material material = c.get("material");
                    final int amount = c.get("amount");
                    final ItemStack itemStack = new ItemStack(material, amount);
                    ((Player) c.getSender()).getInventory().addItem(itemStack);
                    c.getSender().sendMessage("You've been given stuff, bro.");
                }));
        this.manager.command(builder.literal("summon")
                .senderType(Player.class)
                .argument(EnumArgument.of(EntityType.class, "type"))
                .handler(c -> this.manager.taskRecipe().begin(c).synchronous(ctx -> {
                    final Location loc = ((Player) ctx.getSender()).getLocation();
                    loc.getWorld().spawnEntity(loc, ctx.get("type"));
                }).execute()));
        this.manager.command(builder.literal("enchant")
                .senderType(Player.class)
                .argument(EnchantmentArgument.of("enchant"))
                .argument(IntegerArgument.of("level"))
                .handler(c -> this.manager.taskRecipe().begin(c).synchronous(ctx -> {
                    final Player player = ((Player) ctx.getSender());
                    player.getInventory().getItemInMainHand().addEnchantment(ctx.get("enchant"), ctx.get("level"));
                }).execute()));

        //
        // A command to change the color scheme for the help command
        //
        this.manager.command(builder
                .meta(CommandMeta.DESCRIPTION, "Sets the color scheme for '/example help'")
                .literal("helpcolors")
                .argument(
                        TextColorArgument.of("primary"),
                        RichDescription.of(text("The primary color for the color scheme", Style.style(TextDecoration.ITALIC)))
                )
                .argument(
                        TextColorArgument.of("highlight"),
                        RichDescription.of(text("The primary color used to highlight commands and queries"))
                )
                .argument(
                        TextColorArgument.of("alternate_highlight"),
                        RichDescription.of(text("The secondary color used to highlight commands and queries"))
                )
                .argument(
                        TextColorArgument.of("text"),
                        RichDescription.of(text("The color used for description text"))
                )
                .argument(
                        TextColorArgument.of("accent"),
                        RichDescription.of(text("The color used for accents and symbols"))
                )
                .handler(c -> this.minecraftHelp.setHelpColors(MinecraftHelp.HelpColors.of(
                        c.get("primary"),
                        c.get("highlight"),
                        c.get("alternate_highlight"),
                        c.get("text"),
                        c.get("accent")
                )))
        );

        // Commands using MC 1.13+ argument types
        if (this.manager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            new Mc113(this.manager).registerCommands();
        }

        //
        // Create a Bukkit-like command
        //
        this.manager.command(
                this.manager.commandBuilder(
                        "arraycommand",
                        ArgumentDescription.of("Bukkit-esque cmmand")
                ).argument(
                        StringArrayArgument.optional(
                                "args",
                                (context, lastString) -> {
                                    final List<String> allArgs = context.getRawInput();
                                    if (allArgs.size() > 1 && allArgs.get(1).equals("curry")) {
                                        return Collections.singletonList("hot");
                                    }
                                    return Collections.emptyList();
                                }
                        ),
                        ArgumentDescription.of("Arguments")
                ).handler(context -> {
                    final String[] args = context.getOrDefault("args", new String[0]);
                    context.getSender().sendMessage("You wrote: " + StringUtils.join(args, " "));
                })
        );

        /* Register a custom regex caption */
        final Caption moneyCaption = Caption.of("regex.money");
        if (this.manager.getCaptionRegistry() instanceof SimpleCaptionRegistry) {
            ((SimpleCaptionRegistry<CommandSender>) this.manager.getCaptionRegistry()).registerMessageFactory(
                    moneyCaption,
                    (sender, key) -> "'{input}' is not very cash money of you"
            );
        }

        // compound itemstack arg
        this.manager.command(this.manager.commandBuilder("gib")
                .senderType(Player.class)
                .argumentPair(
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
                    ((Player) ctx.getSender()).getInventory().addItem(stack);
                }));

        this.manager.command(builder.literal("keyed_world")
                .argument(KeyedWorldArgument.of("world"))
                .senderType(Player.class)
                .handler(ctx -> {
                    final World world = ctx.get("world");
                    final Player sender = (Player) ctx.getSender();
                    this.getServer().getScheduler().runTask(this, () -> sender.teleport(world.getSpawnLocation()));
                }));
    }

    @CommandMethod("example|e|ex help [query]")
    @CommandDescription("Help menu")
    public void commandHelp(
            final @NonNull CommandSender sender,
            final @Argument("query") @Greedy String query
    ) {
        this.minecraftHelp.queryCommands(query == null ? "" : query, sender);
    }

    @Confirmation
    @CommandMethod("example clear")
    @CommandDescription("Clear your inventory")
    @CommandPermission("example.clear")
    public void commandClear(final @NonNull Player player) {
        player.getInventory().clear();
        this.bukkitAudiences.player(player)
                .sendMessage(Identity.nil(), text("Your inventory has been cleared", NamedTextColor.GOLD));
    }

    @CommandMethod("example give <material> <amount>")
    @CommandDescription("Give yourself an item")
    public void commandGive(
            final @NonNull Player player,
            final @NonNull @Argument("material") Material material,
            final @Argument("amount") int number,
            final @Nullable @Flag("color") ChatColor nameColor,
            final @Nullable @Flag("enchant") Enchantment enchant
    ) {
        final ItemStack itemStack = new ItemStack(material, number);
        String itemName = String.format(
                "%s's %s",
                player.getName(),
                material.name()
                        .toLowerCase()
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

    @CommandMethod("example pay <money>")
    @CommandDescription("Command to test the preprocessing system")
    public void commandPay(
            final @NonNull CommandSender sender,
            final @Argument("money") @Regex(value = "(?=.*?\\d)^\\$?(([1-9]\\d{0,2}(,\\d{3})*)|\\d+)?(\\.\\d{1,2})?$",
                    failureCaption = "regex.money") String money
    ) {
        this.bukkitAudiences.sender(sender).sendMessage(
                Identity.nil(),
                text().append(text("You have been given ", NamedTextColor.AQUA))
                        .append(text(money, NamedTextColor.GOLD))
        );
    }

    @CommandMethod("example teleport complex <location>")
    public void teleportComplex(
            final @NonNull Player sender,
            final @NonNull @Argument("location") Location location
    ) {
        this.manager.taskRecipe().begin(location).synchronous((@NonNull TaskConsumer<Location>) sender::teleport)
                .execute(() -> sender.sendMessage("You have been teleported!"));
    }

    @CommandMethod("removeall")
    public void removeAll(
            final @NonNull CommandSender sender
    ) {
        this.manager.rootCommands().forEach(this.manager::deleteRootCommand);
        sender.sendMessage("All root commands have been deleted :)");
    }

    @CommandMethod("removesingle <command>")
    public void removeSingle(
            final @NonNull CommandSender sender,
            final @Argument(value = "command", suggestions = "commands") String command
    ) {
        this.manager.deleteRootCommand(command);
        sender.sendMessage("Deleted the root command :)");
    }

    @Suggestions("commands")
    public List<String> commands(
            final @NonNull CommandContext<CommandSender> context,
            final @NonNull String input
    ) {
        return new ArrayList<>(this.manager.rootCommands());
    }

    @CommandMethod("disableme")
    public void disableMe() {
        this.getServer().getPluginManager().disablePlugin(this);
    }


    /**
     * Command must have the given game mode
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface GameModeRequirement {

        /**
         * The required game mode
         *
         * @return Required game mode
         */
        GameMode value();

    }

}
