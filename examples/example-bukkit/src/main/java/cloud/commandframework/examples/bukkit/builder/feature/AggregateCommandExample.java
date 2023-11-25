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
package cloud.commandframework.examples.bukkit.builder.feature;

import cloud.commandframework.arguments.aggregate.AggregateCommandParser;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.builder.BuilderFeature;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import static cloud.commandframework.arguments.standard.IntegerParser.integerParser;
import static cloud.commandframework.bukkit.parsers.WorldParser.worldParser;

/**
 * Example showcasing the aggregate command parser system.
 */
public final class AggregateCommandExample implements BuilderFeature {

    @Override
    public void registerFeature(
            @NonNull final ExamplePlugin examplePlugin,
            @NonNull final BukkitCommandManager<CommandSender> manager
    ) {
        final AggregateCommandParser<CommandSender, Location> locationParser = AggregateCommandParser.<CommandSender>builder()
                .withComponent("world", worldParser())
                .withComponent("x", integerParser())
                .withComponent("y", integerParser())
                .withComponent("z", integerParser())
                .withMapper(Location.class, (commandContext, aggregateCommandContext) -> {
                    final World world = aggregateCommandContext.get("world");
                    final int x = aggregateCommandContext.get("x");
                    final int y = aggregateCommandContext.get("y");
                    final int z = aggregateCommandContext.get("z");
                    return CompletableFuture.completedFuture(new Location(world, x, y, z));
                })
                .build();
        manager.command(
                manager.commandBuilder("builder")
                        .literal("aggregate")
                        .required("location", locationParser)
                        .senderType(Player.class)
                        .handler(commandContext -> commandContext.getSender().teleport(commandContext.<Location>get("location")))
        );
    }
}
