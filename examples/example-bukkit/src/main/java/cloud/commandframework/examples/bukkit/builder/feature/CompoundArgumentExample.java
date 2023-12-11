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

import cloud.commandframework.Description;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.builder.BuilderFeature;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.types.tuples.Triplet;
import io.leangen.geantyref.TypeToken;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;

import static cloud.commandframework.bukkit.parsers.WorldParser.worldParser;

public final class CompoundArgumentExample implements BuilderFeature {

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        final CloudKey<World> worldKey = CloudKey.of("world", World.class);
        final CloudKey<Vector> coordsKey = CloudKey.of("coords", Vector.class);

        manager.command(manager.commandBuilder("builder")
                .literal("teleport")
                .literal("me")
                // Require a player sender
                .senderType(Player.class)
                .required(worldKey, worldParser(), Description.of("World name"))
                .requiredArgumentTriplet(
                        coordsKey,
                        TypeToken.get(Vector.class),
                        Triplet.of("x", "y", "z"),
                        Triplet.of(Integer.class, Integer.class, Integer.class),
                        (sender, triplet) -> new Vector(triplet.getFirst(), triplet.getSecond(),
                                triplet.getThird()
                        ),
                        Description.of("Coordinates")
                )
                .handler(context -> {
                    final Player player = context.sender();
                    final World world = context.get(worldKey);
                    final Vector coords = context.get(coordsKey);
                    final Location location = coords.toLocation(world);
                    player.teleport(location);
                }));
    }
}
