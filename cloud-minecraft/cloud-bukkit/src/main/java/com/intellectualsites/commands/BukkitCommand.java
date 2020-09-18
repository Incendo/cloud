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
package com.intellectualsites.commands;

import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.arguments.StaticArgument;
import com.intellectualsites.commands.exceptions.ArgumentParseException;
import com.intellectualsites.commands.exceptions.InvalidCommandSenderException;
import com.intellectualsites.commands.exceptions.InvalidSyntaxException;
import com.intellectualsites.commands.exceptions.NoPermissionException;
import com.intellectualsites.commands.exceptions.NoSuchCommandException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

final class BukkitCommand<C> extends org.bukkit.command.Command implements PluginIdentifiableCommand {

    private static final String MESSAGE_NO_PERMS = ChatColor.RED
            + "I'm sorry, but you do not have permission to perform this command. "
            + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command. Type \"/help\" for help.";

    private final CommandArgument<C, ?> command;
    private final BukkitCommandManager<C> bukkitCommandManager;
    private final com.intellectualsites.commands.Command<C, BukkitCommandMeta> cloudCommand;

    @SuppressWarnings("unchecked")
    BukkitCommand(@Nonnull final com.intellectualsites.commands.Command<C, BukkitCommandMeta> cloudCommand,
                  @Nonnull final CommandArgument<C, ?> command,
                  @Nonnull final BukkitCommandManager<C> bukkitCommandManager) {
        super(command.getName(),
              cloudCommand.getCommandMeta().getOrDefault("description", ""),
              "",
              ((StaticArgument<C>) command).getAlternativeAliases());
        this.command = command;
        this.bukkitCommandManager = bukkitCommandManager;
        this.cloudCommand = cloudCommand;
    }

    @Override
    public boolean execute(final CommandSender commandSender, final String s, final String[] strings) {
        /* Join input */
        final StringBuilder builder = new StringBuilder(this.command.getName());
        for (final String string : strings) {
            builder.append(" ").append(string);
        }
        this.bukkitCommandManager.executeCommand(this.bukkitCommandManager.getCommandSenderMapper().apply(commandSender),
                                                 builder.toString())
                                 .whenComplete(((commandResult, throwable) -> {
                                     if (throwable != null) {
                                         if (throwable instanceof InvalidSyntaxException) {
                                             commandSender.sendMessage(ChatColor.RED + "Invalid Command Syntax. "
                                                                     + "Correct command syntax is: "
                                                                     + ChatColor.GRAY + "/"
                                                                     + ((InvalidSyntaxException) throwable).getCorrectSyntax());
                                         } else if (throwable instanceof InvalidCommandSenderException) {
                                             commandSender.sendMessage(ChatColor.RED + throwable.getMessage());
                                         } else if (throwable instanceof NoPermissionException) {
                                             commandSender.sendMessage(MESSAGE_NO_PERMS);
                                         } else if (throwable instanceof NoSuchCommandException) {
                                             commandSender.sendMessage(MESSAGE_UNKNOWN_COMMAND);
                                         } else if (throwable instanceof ArgumentParseException) {
                                             commandSender.sendMessage(ChatColor.RED + "Invalid Command Argument: "
                                                                      + ChatColor.GRAY + throwable.getCause().getMessage());
                                         } else {
                                             commandSender.sendMessage(throwable.getMessage());
                                             throwable.printStackTrace();
                                         }
                                     }
                                 }));
        return true;
    }

    @Override
    public String getDescription() {
        return this.cloudCommand.getCommandMeta().getOrDefault("description", "");
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) throws
            IllegalArgumentException {
        final StringBuilder builder = new StringBuilder(this.command.getName());
        for (final String string : args) {
            builder.append(" ").append(string);
        }
        return this.bukkitCommandManager.suggest(this.bukkitCommandManager.getCommandSenderMapper().apply(sender),
                                                 builder.toString());
    }

    @Override
    public Plugin getPlugin() {
        return this.bukkitCommandManager.getOwningPlugin();
    }

    @Override
    public String getPermission() {
        return this.cloudCommand.getCommandPermission();
    }

    @Nullable
    private <E extends Throwable> E captureException(@Nonnull final Class<E> clazz, @Nullable final Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        if (clazz.equals(throwable.getClass())) {
            //noinspection unchecked
            return (E) throwable;
        }
        return captureException(clazz, throwable.getCause());
    }

    private <E extends Throwable> boolean handleException(@Nullable final E throwable,
                                                          @Nonnull final Consumer<E> consumer) {
        if (throwable == null) {
            return false;
        }
        consumer.accept(throwable);
        return true;
    }

}
