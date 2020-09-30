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
package cloud.commandframework;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.Confirmation;
import cloud.commandframework.annotations.specifier.Completions;
import cloud.commandframework.annotations.specifier.Range;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.arguments.standard.FloatArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.BukkitCommandMetaBuilder;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.bukkit.parsers.WorldArgument;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import cloud.commandframework.types.tuples.Triplet;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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

            final BukkitAudiences bukkitAudiences = BukkitAudiences.create(this);
            final MinecraftHelp<CommandSender> minecraftHelp = new MinecraftHelp<>("/cloud help",
                                                                                   bukkitAudiences::sender,
                                                                                   mgr);

            try {
                mgr.registerBrigadier();
            } catch (final Exception e) {
                getLogger().warning("Failed to initialize Brigadier support: " + e.getMessage());
            }

            try {
                ((PaperCommandManager<CommandSender>) mgr).registerAsynchronousCompletions();
            } catch (final Throwable e) {
                getLogger().warning("Failed to register asynchronous command completions: " + e.getMessage());
            }

            final CommandConfirmationManager<CommandSender> confirmationManager = new CommandConfirmationManager<>(
                    30,
                    TimeUnit.SECONDS,
                    c -> c.getCommandContext().getSender().sendMessage(ChatColor.RED + "Oh no. Confirm using /cloud confirm!"),
                    c -> c.sendMessage(ChatColor.RED + "You don't have any pending commands!")
            );
            confirmationManager.registerConfirmationProcessor(mgr);

            final AnnotationParser<CommandSender> annotationParser
                    = new AnnotationParser<>(mgr, CommandSender.class, p ->
                    BukkitCommandMetaBuilder.builder().withDescription(p.get(StandardParameters.DESCRIPTION,
                                                                             "No description")).build());
            annotationParser.parse(this);

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
                           .handler(c -> c.getSender().sendMessage("Hey yo dum, provide a UUID idiot. Thx!"))
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
                           .build())
               .command(mgr.commandBuilder("annotationass")
                           .handler(c -> c.getSender()
                                          .sendMessage(ChatColor.YELLOW + "Du e en ananas!"))
                           .build())
               .command(mgr.commandBuilder("cloud")
                           .literal("confirm")
                           .handler(confirmationManager.createConfirmationExecutionHandler()).build())
               .command(mgr.commandBuilder("cloud")
                           .literal("help")
                           .withPermission("cloud.help")
                           .argument(StringArgument.<CommandSender>newBuilder("query").greedy()
                                                                                      .asOptionalWithDefault("")
                                                                                      .build(), Description.of("Help Query"))
                           .handler(c -> minecraftHelp.queryCommands(c.<String>get("query").orElse(""),
                                                                     c.getSender())).build());
            this.registerTeleportCommand(mgr);
            mgr.registerExceptionHandler(InvalidSyntaxException.class, (c, e) -> e.printStackTrace());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void registerTeleportCommand(@NonNull final BukkitCommandManager<CommandSender> manager) {
        manager.command(mgr.commandBuilder("teleport")
                           .meta("description", "Takes in a location and teleports the player there")
                           .withSenderType(Player.class)
                           .argument(WorldArgument.required("world"), Description.of("World name"))
                           .argumentTriplet("coords",
                                            TypeToken.get(Vector.class),
                                            Triplet.of("x", "y", "z"),
                                            Triplet.of(Double.class, Double.class, Double.class),
                                            triplet -> new Vector(triplet.getFirst(), triplet.getSecond(), triplet.getThird()),
                                            Description.of("Coordinates"))
                           .handler(context -> {
                               context.getSender().sendMessage(ChatColor.GOLD + "Teleporting!");
                               Bukkit.getScheduler().runTask(this, () -> {
                                   final World world = context.getRequired("world");
                                   final Vector vector = context.getRequired("coords");
                                   ((Player) context.getSender()).teleport(vector.toLocation(world));
                               });
                           })
                           .build());
    }

    @CommandDescription("Test cloud command using @CommandMethod")
    @CommandMethod(value = "annotation|a <input> [number]", permission = "some.permission.node")
    private void annotatedCommand(@NonNull final Player player,
                                  @Argument(value = "input", description = "Some string") @Completions("one,two,duck")
                                  @NonNull final String input,
                                  @Argument(value = "number", defaultValue = "5", description = "A number")
                                  @Range(min = "10", max = "100") final int number) {
        player.sendMessage(ChatColor.GOLD + "Your input was: " + ChatColor.AQUA + input + ChatColor.GREEN + " (" + number + ")");
    }

    @Confirmation
    @CommandMethod(value = "cloud", permission = "cloud.debug")
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

    private @NonNull SimpleCommandMeta metaWithDescription(@NonNull final String description) {
        return BukkitCommandMetaBuilder.builder().withDescription(description).build();
    }

}
