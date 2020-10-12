//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
import cloud.commandframework.CommandTree;
import cloud.commandframework.Description;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.Confirmation;
import cloud.commandframework.annotations.Flag;
import cloud.commandframework.annotations.Regex;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArrayArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.BukkitCommandMetaBuilder;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.bukkit.arguments.selector.SingleEntitySelector;
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
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import cloud.commandframework.types.tuples.Triplet;
import com.google.common.collect.ImmutableList;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Example plugin class
 */
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
            this.getLogger().severe("Failed to initialize the command manager");
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
        if (manager.queryCapability(CloudBukkitCapabilities.BRIGADIER)) {
            manager.registerBrigadier();
        }
        //
        // Register asynchronous completions
        //
        if (manager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            ((PaperCommandManager<CommandSender>) this.manager).registerAsynchronousCompletions();
        }
        //
        // Create the confirmation manager. This allows us to require certain commands to be
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
        this.confirmationManager.registerConfirmationProcessor(manager);
        //
        // Create the annotation parser. This allows you to define commands using methods annotated with
        // @CommandMethod
        //
        final Function<ParserParameters, CommandMeta> commandMetaFunction = p ->
                BukkitCommandMetaBuilder.builder()
                        // This will allow you to decorate commands with descriptions
                        .withDescription(p.get(StandardParameters.DESCRIPTION, "No description"))
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
                .withDecorator(
                        component -> Component.text()
                                .append(Component.text("[", NamedTextColor.DARK_GRAY))
                                .append(Component.text("Example", NamedTextColor.GOLD))
                                .append(Component.text("] ", NamedTextColor.DARK_GRAY))
                                .append(component).build()
                ).apply(manager, bukkitAudiences::sender);
        //
        // Create the commands
        //
        this.constructCommands();
    }

    private void constructCommands() {
        //
        // Parse all @CommandMethod-annotated methods
        //
        this.annotationParser.parse(this);
        //
        // Base command builder
        //
        final Command.Builder<CommandSender> builder = this.manager.commandBuilder("example");
        //
        // Add a confirmation command
        //
        this.manager.command(builder.literal("confirm")
                .meta("description", "Confirm a pending command")
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
                .argument(worldArgument, Description.of("World name"))
                .argumentTriplet(
                        "coords",
                        TypeToken.get(Vector.class),
                        Triplet.of("x", "y", "z"),
                        Triplet.of(Integer.class, Integer.class, Integer.class),
                        (sender, triplet) -> new Vector(triplet.getFirst(), triplet.getSecond(),
                                triplet.getThird()
                        ),
                        Description.of("Coordinates")
                )
                .handler(context -> manager.taskRecipe().begin(context)
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
                                Description.of("Entity to teleport")
                        )
                        .literal("here")
                        .handler(
                                context -> manager.taskRecipe().begin(context)
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
                        ));
        manager.command(builder.literal("tasktest")
                .handler(context -> manager.taskRecipe()
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
        manager.command(manager.commandBuilder("give")
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
        manager.command(builder.literal("summon")
                .senderType(Player.class)
                .argument(EnumArgument.of(EntityType.class, "type"))
                .handler(c -> manager.taskRecipe().begin(c).synchronous(ctx -> {
                    final Location loc = ((Player) ctx.getSender()).getLocation();
                    loc.getWorld().spawnEntity(loc, ctx.get("type"));
                }).execute()));
        manager.command(builder.literal("enchant")
                .senderType(Player.class)
                .argument(EnchantmentArgument.of("enchant"))
                .argument(IntegerArgument.of("level"))
                .handler(c -> manager.taskRecipe().begin(c).synchronous(ctx -> {
                    final Player player = ((Player) ctx.getSender());
                    player.getInventory().getItemInHand().addEnchantment(ctx.get("enchant"), ctx.get("level"));
                }).execute()));

        //
        // An Argument Parser for TextColor that accepts NamedTextColor names or RGB colors in the format 'RRGGBB'
        //
        final ArgumentParser<CommandSender, TextColor> textColorArgumentParser = (context, inputQueue) -> {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new IllegalArgumentException("No input provided"));
            }
            if (NamedTextColor.NAMES.keys().contains(input.toLowerCase())) {
                inputQueue.remove();
                return ArgumentParseResult.success(NamedTextColor.NAMES.value(input.toLowerCase()));
            }
            final TextColor hex = TextColor.fromHexString("#" + input);
            if (hex != null) {
                inputQueue.remove();
                return ArgumentParseResult.success(hex);
            }
            return ArgumentParseResult.failure(new IllegalArgumentException(
                    "No such color. Try a NamedTextColor or Hex in the format 'RRGGBB'"));
        };

        //
        // A Suggestions Provider which returns the list of NamedTextColors
        //
        final BiFunction<CommandContext<CommandSender>, String, List<String>> textColorSuggestionsProvider =
                (context, input) -> ImmutableList.copyOf(NamedTextColor.NAMES.keys());

        //
        // A command to change the color scheme for the help command
        //
        manager.command(builder
                .meta("description", "Sets the color scheme for '/example help'")
                .literal("helpcolors")
                .argument(
                        manager.argumentBuilder(TextColor.class, "primary")
                                .withParser(textColorArgumentParser)
                                .withSuggestionsProvider(textColorSuggestionsProvider),
                        Description.of("The primary color for the color scheme")
                )
                .argument(
                        manager.argumentBuilder(TextColor.class, "highlight")
                                .withParser(textColorArgumentParser)
                                .withSuggestionsProvider(textColorSuggestionsProvider),
                        Description.of("The primary color used to highlight commands and queries")
                )
                .argument(
                        manager.argumentBuilder(TextColor.class, "alternate_highlight")
                                .withParser(textColorArgumentParser)
                                .withSuggestionsProvider(textColorSuggestionsProvider),
                        Description.of("The secondary color used to highlight commands and queries")
                )
                .argument(
                        manager.argumentBuilder(TextColor.class, "text")
                                .withParser(textColorArgumentParser)
                                .withSuggestionsProvider(textColorSuggestionsProvider),
                        Description.of("The color used for description text")
                )
                .argument(
                        manager.argumentBuilder(TextColor.class, "accent")
                                .withParser(textColorArgumentParser)
                                .withSuggestionsProvider(textColorSuggestionsProvider),
                        Description.of("The color used for accents and symbols")
                )
                .handler(c -> minecraftHelp.setHelpColors(MinecraftHelp.HelpColors.of(
                        c.get("primary"),
                        c.get("highlight"),
                        c.get("alternate_highlight"),
                        c.get("text"),
                        c.get("accent")
                )))
        );
        //
        // Create a Bukkit-like command
        //
        manager.command(
                manager.commandBuilder(
                        "arraycommand",
                        Description.of("Bukkit-esque cmmand")
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
                        Description.of("Arguments")
                ).handler(context -> {
                    final String[] args = context.getOrDefault("args", new String[0]);
                    context.getSender().sendMessage("You wrote: " + StringUtils.join(args, " "));
                })
        );

        /* Register a custom regex caption */
        final Caption moneyCaption = Caption.of("regex.money");
        if (manager.getCaptionRegistry() instanceof SimpleCaptionRegistry) {
            ((SimpleCaptionRegistry<CommandSender>) manager.getCaptionRegistry()).registerMessageFactory(
                    moneyCaption,
                    (sender, key) -> "'{input}' is not very cash money of you"
            );
        }
    }

    @CommandMethod("example help [query]")
    @CommandDescription("Help menu")
    private void commandHelp(
            final @NonNull CommandSender sender,
            final @Argument("query") @Greedy String query
    ) {
        this.minecraftHelp.queryCommands(query == null ? "" : query, sender);
    }

    @Confirmation
    @CommandMethod("example clear")
    @CommandDescription("Clear your inventory")
    @CommandPermission("example.clear")
    private void commandClear(final @NonNull Player player) {
        player.getInventory().clear();
        this.bukkitAudiences.player(player)
                .sendMessage(Component.text("Your inventory has been cleared", NamedTextColor.GOLD));
    }

    @CommandMethod("example give <material> <amount>")
    @CommandDescription("Give yourself an item")
    private void commandGive(
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
    private void commandPay(
            final @NonNull CommandSender sender,
            final @Argument("money") @Regex(value = "(?=.*?\\d)^\\$?(([1-9]\\d{0,2}(,\\d{3})*)|\\d+)?(\\.\\d{1,2})?$",
                    failureCaption = "regex.money") String money
    ) {
        bukkitAudiences.sender(sender).sendMessage(
                Component.text().append(Component.text("You have been given ", NamedTextColor.AQUA))
                        .append(Component.text(money, NamedTextColor.GOLD))
        );
    }

}
