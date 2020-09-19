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
package com.intellectualsites.commands.velocity;

import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.arguments.StaticArgument;
import com.intellectualsites.commands.brigadier.CloudBrigadierManager;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.internal.CommandRegistrationHandler;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class VelocityPluginRegistrationHandler<C> implements CommandRegistrationHandler {

    private final Map<CommandArgument<?, ?>, BrigadierCommand> registeredCommands = new HashMap<>();
    private CloudBrigadierManager<C, CommandSource> brigadierManager;
    private VelocityCommandManager<C> velocityCommandManager;

    void initialize(@Nonnull final VelocityCommandManager<C> velocityCommandManager) {
        this.velocityCommandManager = velocityCommandManager;
        this.brigadierManager = new CloudBrigadierManager<>(velocityCommandManager,
            () -> new CommandContext<>(
                    velocityCommandManager.getCommandSenderMapper()
                                          .apply(velocityCommandManager.getProxyServer()
                                                                       .getConsoleCommandSource()))
        );
    }

    @Override
    public boolean registerCommand(@Nonnull final Command<?> command) {
        final CommandArgument<?, ?> argument = command.getArguments().get(0);
        if (this.registeredCommands.containsKey(argument)) {
            return false;
        }
        final List<String> aliases = ((StaticArgument<C>) argument).getAlternativeAliases();
        final BrigadierCommand brigadierCommand = new BrigadierCommand(
                this.brigadierManager.createLiteralCommandNode((Command<C>) command,
                                                               (c, p) -> this.velocityCommandManager.hasPermission(
                                                                       this.velocityCommandManager.getCommandSenderMapper()
                                                                                                  .apply(c), p),
                                                               commandContext -> {
                                                                   final CommandSource source = commandContext.getSource();
                                                                   final String input = commandContext.getInput();
                                                                   this.velocityCommandManager.executeCommand(
                                                                           this.velocityCommandManager.getCommandSenderMapper()
                                                                                                      .apply(source), input);
                                                                   return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                                               })
        );
        final CommandMeta commandMeta = this.velocityCommandManager.getProxyServer().getCommandManager()
                                                                   .metaBuilder(brigadierCommand)
                                                                   .aliases(aliases.toArray(new String[0])).build();
        this.velocityCommandManager.getProxyServer().getCommandManager().register(commandMeta, brigadierCommand);
        this.registeredCommands.put(argument, brigadierCommand);
        return true;
    }

}
