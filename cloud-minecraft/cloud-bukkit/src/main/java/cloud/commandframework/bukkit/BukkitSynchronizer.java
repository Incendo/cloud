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
package cloud.commandframework.bukkit;

import cloud.commandframework.tasks.TaskConsumer;
import cloud.commandframework.tasks.TaskFunction;
import cloud.commandframework.tasks.TaskSynchronizer;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 * {@link TaskSynchronizer} using Bukkit's {@link org.bukkit.scheduler.BukkitScheduler}
 */
public final class BukkitSynchronizer implements TaskSynchronizer {

    private final Plugin plugin;

    /**
     * Create a new instance of the Bukkit synchronizer
     *
     * @param plugin Owning plugin
     */
    public BukkitSynchronizer(final @NonNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public <I> CompletableFuture<Void> runSynchronous(final @NonNull I input, final @NonNull TaskConsumer<I> consumer) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            consumer.accept(input);
            future.complete(null);
        });
        return future;
    }

    @Override
    public <I, O> CompletableFuture<O> runSynchronous(final @NonNull I input, final @NonNull TaskFunction<I, O> function) {
        final CompletableFuture<O> future = new CompletableFuture<>();
        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> future.complete(function.apply(input)));
        return future;
    }

    @Override
    public <I> CompletableFuture<Void> runAsynchronous(final @NonNull I input, final @NonNull TaskConsumer<I> consumer) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            consumer.accept(input);
            future.complete(null);
        });
        return future;
    }

    @Override
    public <I, O> CompletableFuture<O> runAsynchronous(final @NonNull I input, final @NonNull TaskFunction<I, O> function) {
        final CompletableFuture<O> future = new CompletableFuture<>();
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> future.complete(function.apply(input)));
        return future;
    }

}
