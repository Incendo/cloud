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
package com.intellectualsites.commands.bukkit;

import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.arguments.StaticArgument;
import com.intellectualsites.commands.internal.CommandRegistrationHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.help.GenericCommandHelpTopic;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BukkitPluginRegistrationHandler<C> implements CommandRegistrationHandler {

    private final Map<CommandArgument<?, ?>, org.bukkit.command.Command> registeredCommands = new HashMap<>();

    private Map<String, org.bukkit.command.Command> bukkitCommands;
    private BukkitCommandManager<C> bukkitCommandManager;
    private CommandMap commandMap;

    BukkitPluginRegistrationHandler() {
    }

    void initialize(@Nonnull final BukkitCommandManager<C> bukkitCommandManager) throws Exception {
        final Method getCommandMap = Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap");
        getCommandMap.setAccessible(true);
        this.commandMap = (CommandMap) getCommandMap.invoke(Bukkit.getServer());
        final Field knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
        knownCommands.setAccessible(true);
        @SuppressWarnings("ALL") final Map<String, org.bukkit.command.Command> bukkitCommands =
                (Map<String, org.bukkit.command.Command>) knownCommands.get(commandMap);
        this.bukkitCommands = bukkitCommands;
        this.bukkitCommandManager = bukkitCommandManager;
        Bukkit.getHelpMap().registerHelpTopicFactory(BukkitCommand.class, GenericCommandHelpTopic::new);
    }

    @Override
    public boolean registerCommand(@Nonnull final Command<?> command) {
        /* We only care about the root command argument */
        final CommandArgument<?, ?> commandArgument = command.getArguments().get(0);
        if (this.registeredCommands.containsKey(commandArgument)) {
            return false;
        }
        final String label;
        if (bukkitCommands.containsKey(commandArgument.getName())) {
            label = String.format("%s:%s", this.bukkitCommandManager.getOwningPlugin().getName(), commandArgument.getName());
        } else {
            label = commandArgument.getName();
        }

        @SuppressWarnings("unchecked") final List<String> aliases = ((StaticArgument<C>) commandArgument).getAlternativeAliases();

        @SuppressWarnings("unchecked") final BukkitCommand<C> bukkitCommand = new BukkitCommand<>(
                commandArgument.getName(),
                (this.bukkitCommandManager.getSplitAliases() ? Collections.<String>emptyList() : aliases),
                (Command<C>) command,
                (CommandArgument<C, ?>) commandArgument,
                this.bukkitCommandManager);
        this.registeredCommands.put(commandArgument, bukkitCommand);
        this.commandMap.register(commandArgument.getName(), this.bukkitCommandManager.getOwningPlugin().getName().toLowerCase(),
                                 bukkitCommand);
        this.registerExternal(commandArgument.getName(), command, bukkitCommand);

        if (this.bukkitCommandManager.getSplitAliases()) {
            for (final String alias : aliases) {
                if (!this.bukkitCommands.containsKey(alias)) {
                    @SuppressWarnings("unchecked") final BukkitCommand<C> aliasCommand = new BukkitCommand<>(
                            alias,
                            Collections.emptyList(),
                            (Command<C>) command,
                            (CommandArgument<C, ?>) commandArgument,
                            this.bukkitCommandManager);
                    this.commandMap.register(alias, this.bukkitCommandManager.getOwningPlugin()
                                                                             .getName().toLowerCase(),
                                             bukkitCommand);
                    this.registerExternal(alias, command, aliasCommand);
                }
            }
        }

        return true;
    }

    protected void registerExternal(@Nonnull final String label,
                                    @Nonnull final Command<?> command,
                                    @Nonnull final BukkitCommand<C> bukkitCommand) {
    }

}
