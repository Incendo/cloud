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
package com.intellectualsites.commands.bungee;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.CommandTree;
import com.intellectualsites.commands.execution.CommandExecutionCoordinator;
import com.intellectualsites.commands.meta.SimpleCommandMeta;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class BungeeCommandManager<C> extends CommandManager<C, SimpleCommandMeta> {

    private final Plugin owningPlugin;
    private final Function<CommandSender, C> commandSenderMapper;
    private final Function<C, CommandSender> backwardsCommandSenderMapper;

    /**
     * Construct a new Bukkit command manager
     *
     * @param owningPlugin                 Plugin that is constructing the manager
     * @param commandExecutionCoordinator  Coordinator provider
     * @param commandSenderMapper          Function that maps {@link CommandSender} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link CommandSender}
     * @throws Exception If the construction of the manager fails
     */
    public BungeeCommandManager(@Nonnull final Plugin owningPlugin,
                                @Nonnull final Function<CommandTree<C, SimpleCommandMeta>,
                                        CommandExecutionCoordinator<C, SimpleCommandMeta>> commandExecutionCoordinator,
                                @Nonnull final Function<CommandSender, C> commandSenderMapper,
                                @Nonnull final Function<C, CommandSender> backwardsCommandSenderMapper)
            throws Exception {
        super(commandExecutionCoordinator, new BungeePluginRegistrationHandler<>());
        ((BungeePluginRegistrationHandler<C>) this.getCommandRegistrationHandler()).initialize(this);
        this.owningPlugin = owningPlugin;
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;
    }

    @Override
    public final boolean hasPermission(@Nonnull final C sender,
                                 @Nonnull final String permission) {
        return this.backwardsCommandSenderMapper.apply(sender).hasPermission(permission);
    }

    @Nonnull
    @Override
    public final SimpleCommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

    @Nonnull
    final Function<CommandSender, C> getCommandSenderMapper() {
        return this.commandSenderMapper;
    }

    /**
     * Get the owning plugin
     *
     * @return Owning plugin
     */
    @Nonnull
    public Plugin getOwningPlugin() {
        return this.owningPlugin;
    }

}
