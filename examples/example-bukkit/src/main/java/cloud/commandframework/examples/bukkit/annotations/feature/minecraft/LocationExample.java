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
package cloud.commandframework.examples.bukkit.annotations.feature.minecraft;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.parsers.location.Location2D;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.annotations.AnnotationFeature;
import cloud.commandframework.tasks.TaskConsumer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Example of the Bukkit location parsers. This also showcases the task recipes that can be used
 * to synchronize tasks to the main Bukkit thread.
 */
public final class LocationExample implements AnnotationFeature {

    private BukkitCommandManager<CommandSender> manager;

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull AnnotationParser<CommandSender> annotationParser
    ) {
        this.manager = (BukkitCommandManager<CommandSender>) annotationParser.manager();
        annotationParser.parse(this);
    }

    @CommandMethod("annotations teleport location <location>")
    public void teleportComplex(
            final @NonNull Player sender,
            final @NonNull @Argument("location") Location location
    ) {
        // We cannot teleport players asynchronously, but we use an asynchronous command execution coordinator.
        // The task recipe allows us to chain together tasks and run some of them on the main thread.
        this.manager.taskRecipe()
                .begin(location)
                .synchronous((@NonNull TaskConsumer<Location>) sender::teleport)
                .execute(() -> sender.sendMessage("You have been teleported!"));
    }

    @CommandMethod("annotations teleport chunk <chunk>")
    public void teleportComplex(
            final @NonNull Player sender,
            final @NonNull @Argument("chunk") Location2D chunkCoordinates
    ) {
        final Chunk chunk = sender.getWorld().getChunkAt(chunkCoordinates.getBlockX(), chunkCoordinates.getBlockY());
        // We cannot teleport players asynchronously, but we use an asynchronous command execution coordinator.
        // The task recipe allows us to chain together tasks and run some of them on the main thread.
        this.manager.taskRecipe()
                .begin(chunk.getBlock(0, sender.getLocation().getBlockY(), 0).getLocation())
                .synchronous((@NonNull TaskConsumer<Location>) sender::teleport)
                .execute(() -> sender.sendMessage("You have been teleported!"));
    }
}
