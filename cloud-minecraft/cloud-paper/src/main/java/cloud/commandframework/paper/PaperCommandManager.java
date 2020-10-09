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
package cloud.commandframework.paper;

import cloud.commandframework.CommandTree;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

/**
 * Paper command manager that extends {@link BukkitCommandManager}
 *
 * @param <C> Command sender type
 */
public class PaperCommandManager<C> extends BukkitCommandManager<C> {

    /**
     * Construct a new Paper command manager
     *
     * @param owningPlugin                 Plugin that is constructing the manager
     * @param commandExecutionCoordinator  Coordinator provider
     * @param commandSenderMapper          Function that maps {@link CommandSender} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link CommandSender}
     * @throws Exception If the construction of the manager fails
     */
    public PaperCommandManager(
            final @NonNull Plugin owningPlugin,
            final @NonNull Function<CommandTree<C>,
                    CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @NonNull Function<CommandSender, C> commandSenderMapper,
            final @NonNull Function<C, CommandSender> backwardsCommandSenderMapper
    ) throws Exception {
        super(owningPlugin, commandExecutionCoordinator, commandSenderMapper, backwardsCommandSenderMapper);
    }

    /**
     * Register Brigadier mappings using the native paper events
     *
     * @throws BrigadierFailureException Exception thrown if the mappings cannot be registered
     */
    @Override
    public void registerBrigadier() throws BrigadierFailureException {
        this.checkBrigadierCompatibility();
        if (!this.queryCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            super.registerBrigadier();
        } else {
            try {
                final PaperBrigadierListener<C> brigadierListener = new PaperBrigadierListener<>(this);
                Bukkit.getPluginManager().registerEvents(
                        brigadierListener,
                        this.getOwningPlugin()
                );
                this.setSplitAliases(true);
            } catch (final Throwable e) {
                throw new BrigadierFailureException(BrigadierFailureReason.PAPER_BRIGADIER_INITIALIZATION_FAILURE, e);
            }
        }
    }

    /**
     * Register asynchronous completions. This requires all argument parsers to be thread safe, and it
     * is up to the caller to guarantee that such is the case
     *
     * @throws IllegalStateException when the server does not support asynchronous completions.
     */
    public void registerAsynchronousCompletions() throws IllegalStateException {
        if (!this.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            throw new IllegalStateException("Failed to register asynchronous command completion listener.");
        }
        Bukkit.getServer().getPluginManager().registerEvents(new AsyncCommandSuggestionsListener<>(this), this.getOwningPlugin());
    }

}
