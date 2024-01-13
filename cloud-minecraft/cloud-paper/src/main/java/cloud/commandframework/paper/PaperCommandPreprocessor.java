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
package cloud.commandframework.paper;

import cloud.commandframework.bukkit.BukkitCommandContextKeys;
import cloud.commandframework.bukkit.internal.CraftBukkitReflection;
import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import cloud.commandframework.execution.preprocessor.CommandPreprocessor;
import java.util.concurrent.Executor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
final class PaperCommandPreprocessor<C> implements CommandPreprocessor<C> {

    private static final boolean FOLIA =
            CraftBukkitReflection.classExists("io.papermc.paper.threadedregions.RegionizedServer");

    private final PaperCommandManager<C> manager;

    PaperCommandPreprocessor(final PaperCommandManager<C> manager) {
        this.manager = manager;
    }

    @Override
    public void accept(final CommandPreprocessingContext<C> ctx) {
        // cloud-bukkit's preprocessor will store the main thread executor if we don't store anything.
        if (FOLIA) {
            ctx.commandContext().store(
                    BukkitCommandContextKeys.SENDER_SCHEDULER_EXECUTOR,
                    this.foliaExecutorFor(ctx.commandContext().sender())
            );
        }
    }

    private Executor foliaExecutorFor(final C sender) {
        final CommandSender commandSender = this.manager.senderMapper().reverse(sender);
        final Plugin plugin = this.manager.owningPlugin();
        if (commandSender instanceof Entity) {
            return task -> {
                ((Entity) commandSender).getScheduler().run(
                        plugin,
                        handle -> task.run(),
                        null
                );
            };
        } else if (commandSender instanceof BlockCommandSender) {
            final BlockCommandSender blockSender = (BlockCommandSender) commandSender;
            return task -> {
                blockSender.getServer().getRegionScheduler().run(
                        plugin,
                        blockSender.getBlock().getLocation(),
                        handle -> task.run()
                );
            };
        }
        return task -> {
            plugin.getServer().getGlobalRegionScheduler().run(
                    plugin,
                    handle -> task.run()
            );
        };
    }
}
