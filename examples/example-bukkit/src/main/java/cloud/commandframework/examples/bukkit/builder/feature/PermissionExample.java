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

import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.builder.BuilderFeature;
import cloud.commandframework.execution.CommandExecutionHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Example of a command with a permission attached to it.
 */
public final class PermissionExample implements BuilderFeature, CommandExecutionHandler<CommandSender> {

    private ExamplePlugin examplePlugin;

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull BukkitCommandManager<CommandSender> manager
    ) {
        this.examplePlugin = examplePlugin;
        manager.command(
                manager.commandBuilder("builder")
                        .literal("disableplugin")
                        .permission("example.annotation.disableplugin")
                        .handler(this)
        );
    }

    @Override
    public void execute(final @NonNull CommandContext<CommandSender> commandContext) {
        Bukkit.getServer().getPluginManager().disablePlugin(this.examplePlugin);
    }
}
