//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
package com.intellectualsites.commands.cloudburst;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.CommandTree;
import com.intellectualsites.commands.execution.CommandExecutionCoordinator;
import com.intellectualsites.commands.meta.CommandMeta;
import com.intellectualsites.commands.meta.SimpleCommandMeta;
import org.cloudburstmc.server.command.CommandSender;
import org.cloudburstmc.server.plugin.PluginContainer;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Command manager for the Cloudburst platform
 *
 * @param <C> Command sender type
 */
public class CloudburstCommandManager<C> extends CommandManager<C> {

    private final Function<CommandSender, C> commandSenderMapper;
    private final Function<C, CommandSender> backwardsCommandSenderMapper;

    private final PluginContainer owningPlugin;

    /**
     * Construct a new Cloudburst command manager
     *
     * @param owningPlugin                 Plugin that is constructing the manager
     * @param commandExecutionCoordinator  Coordinator provider
     * @param commandSenderMapper          Function that maps {@link CommandSender} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link CommandSender}
     * @throws Exception If the construction of the manager fails
     */
    public CloudburstCommandManager(@Nonnull final PluginContainer owningPlugin,
                                @Nonnull final Function<CommandTree<C>,
                                        CommandExecutionCoordinator<C>> commandExecutionCoordinator,
                                @Nonnull final Function<CommandSender, C> commandSenderMapper,
                                @Nonnull final Function<C, CommandSender> backwardsCommandSenderMapper) throws Exception {
        super(commandExecutionCoordinator, new CloudburstPluginRegistrationHandler<>());
        ((CloudburstPluginRegistrationHandler<C>) this.getCommandRegistrationHandler()).initialize(this);
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;
        this.owningPlugin = owningPlugin;
    }

    @Override
    public final boolean hasPermission(@Nonnull final C sender,
                                       @Nonnull final String permission) {
        return this.backwardsCommandSenderMapper.apply(sender).hasPermission(permission);
    }

    @Nonnull
    @Override
    public final CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.builder().build();
    }

    @Nonnull
    final Function<CommandSender, C> getCommandSenderMapper() {
        return this.commandSenderMapper;
    }

    /**
     * Get the plugin that owns the manager
     *
     * @return Owning plugin
     */
    @Nonnull
    public final PluginContainer getOwningPlugin() {
        return this.owningPlugin;
    }

}
