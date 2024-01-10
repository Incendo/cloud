//
// MIT License
//
// Copyright (c) 2024 Incendo
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

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.DefaultValue;
import cloud.commandframework.arguments.aggregate.AggregateCommandParser;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.builder.BuilderFeature;
import cloud.commandframework.types.tuples.Pair;
import io.leangen.geantyref.TypeToken;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.NonNull;

import static cloud.commandframework.arguments.standard.BooleanParser.booleanParser;
import static cloud.commandframework.arguments.standard.IntegerParser.integerParser;
import static cloud.commandframework.arguments.standard.StringParser.stringParser;
import static cloud.commandframework.bukkit.parsers.WorldParser.worldParser;

/**
 * Example showcasing the aggregate command parser system.
 */
public final class AggregateCommandExample implements BuilderFeature {

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        this.registerLocationExample(manager);
        this.registerRenameExample(manager);
    }

    private void registerLocationExample(final CommandManager<CommandSender> manager) {
        final AggregateCommandParser<CommandSender, Location> locationParser = AggregateCommandParser.<CommandSender>builder()
                .withComponent("world", worldParser())
                .withComponent("x", integerParser())
                .withComponent("y", integerParser())
                .withComponent("z", integerParser())
                .withDirectMapper(Location.class, (commandContext, aggregateCommandContext) -> {
                    final World world = aggregateCommandContext.get("world");
                    final int x = aggregateCommandContext.get("x");
                    final int y = aggregateCommandContext.get("y");
                    final int z = aggregateCommandContext.get("z");
                    return ArgumentParseResult.success(new Location(world, x, y, z));
                })
                .build();
        manager.command(
                manager.commandBuilder("builder")
                        .literal("aggregate")
                        .literal("teleport")
                        .required("location", locationParser)
                        .optional("announce", booleanParser(), DefaultValue.constant(true))
                        .senderType(Player.class)
                        .handler(commandContext -> {
                            commandContext.sender().teleport(commandContext.<Location>get("location"));
                            if (commandContext.get("announce")) {
                                commandContext.sender().sendMessage("You've been teleported :)");
                            }
                        })
        );
    }

    private void registerRenameExample(final CommandManager<CommandSender> manager) {
        final AggregateCommandParser<CommandSender, Pair<Integer, String>> parser =
                AggregateCommandParser.<CommandSender>builder()
                        .withComponent("slot", integerParser(), this.slotSuggestions())
                        .withComponent("name", stringParser(), this.nameSuggestions())
                        .withMapper(new TypeToken<Pair<Integer, String>>(){}, (commandContext, context) -> {
                            final int slot = context.get("slot");
                            final String name = context.get("name");
                            return CompletableFuture.completedFuture(ArgumentParseResult.success(Pair.of(slot, name)));
                        })
                        .build();
        manager.command(
                manager.commandBuilder("builder")
                        .literal("aggregate")
                        .literal("rename")
                        .required("name", parser)
                        .senderType(Player.class)
                        .handler(commandContext -> {
                            final Pair<Integer, String> name = commandContext.get("name");
                            final ItemStack stack = commandContext.sender().getInventory().getItem(name.getFirst());
                            if (stack == null) {
                                return;
                            }
                            final ItemMeta meta = stack.getItemMeta();
                            meta.setDisplayName(name.getSecond());
                            stack.setItemMeta(meta);
                        })
        );
    }

    private @NonNull BlockingSuggestionProvider<CommandSender> slotSuggestions() {
        return (context, input) -> IntStream.rangeClosed(1, 9)
                .mapToObj(Integer::toString)
                .map(Suggestion::simple)
                .collect(Collectors.toList());
    }

    private @NonNull BlockingSuggestionProvider<CommandSender> nameSuggestions() {
        return (context, input) -> Stream.of(context.<Integer>get("slot"), context.sender().getName())
                .map(Object::toString)
                .map(Suggestion::simple)
                .collect(Collectors.toList());
    }
}
