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

import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.meta.CommandMeta;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;

final class BukkitCommand<C> extends org.bukkit.command.Command implements PluginIdentifiableCommand {

    private static final String MESSAGE_INTERNAL_ERROR = ChatColor.RED
            + "An internal error occurred while attempting to perform this command.";
    private static final String MESSAGE_NO_PERMS = ChatColor.RED
            + "I'm sorry, but you do not have permission to perform this command. "
            + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command. Type \"/help\" for help.";

    private final CommandArgument<C, ?> command;
    private final BukkitCommandManager<C> manager;
    private final Command<C> cloudCommand;

    BukkitCommand(
            final @NonNull String label,
            final @NonNull List<@NonNull String> aliases,
            final @NonNull Command<C> cloudCommand,
            final @NonNull CommandArgument<C, ?> command,
            final @NonNull BukkitCommandManager<C> manager
    ) {
        super(
                label,
                cloudCommand.getCommandMeta().getOrDefault(CommandMeta.DESCRIPTION, ""),
                "",
                aliases
        );
        this.command = command;
        this.manager = manager;
        this.cloudCommand = cloudCommand;
        if (this.command.getOwningCommand() != null) {
            this.setPermission(this.command.getOwningCommand().getCommandPermission().toString());
        }
    }

    @Override
    public boolean execute(
            final @NonNull CommandSender commandSender,
            final @NonNull String s,
            final @NonNull String @NonNull [] strings
    ) {
        /* Join input */
        final StringBuilder builder = new StringBuilder(this.command.getName());
        for (final String string : strings) {
            builder.append(" ").append(string);
        }
        final C sender = this.manager.getCommandSenderMapper().apply(commandSender);
        this.manager.executeCommand(
                sender,
                builder.toString()
        )
                .whenComplete((commandResult, throwable) -> {
                    if (throwable != null) {
                        if (throwable instanceof CompletionException) {
                            throwable = throwable.getCause();
                        }
                        final Throwable finalThrowable = throwable;
                        if (throwable instanceof InvalidSyntaxException) {
                            this.manager.handleException(sender,
                                    InvalidSyntaxException.class,
                                    (InvalidSyntaxException) throwable, (c, e) ->
                                            commandSender.sendMessage(
                                                    ChatColor.RED + "Invalid Command Syntax. "
                                                            + "Correct command syntax is: "
                                                            + ChatColor.GRAY + "/"
                                                            + ((InvalidSyntaxException) finalThrowable)
                                                            .getCorrectSyntax())
                            );
                        } else if (throwable instanceof InvalidCommandSenderException) {
                            this.manager.handleException(sender,
                                    InvalidCommandSenderException.class,
                                    (InvalidCommandSenderException) throwable, (c, e) ->
                                            commandSender.sendMessage(
                                                    ChatColor.RED + finalThrowable.getMessage())
                            );
                        } else if (throwable instanceof NoPermissionException) {
                            this.manager.handleException(sender,
                                    NoPermissionException.class,
                                    (NoPermissionException) throwable, (c, e) ->
                                            commandSender.sendMessage(MESSAGE_NO_PERMS)
                            );
                        } else if (throwable instanceof NoSuchCommandException) {
                            this.manager.handleException(sender,
                                    NoSuchCommandException.class,
                                    (NoSuchCommandException) throwable, (c, e) ->
                                            commandSender.sendMessage(MESSAGE_UNKNOWN_COMMAND)
                            );
                        } else if (throwable instanceof ArgumentParseException) {
                            this.manager.handleException(sender,
                                    ArgumentParseException.class,
                                    (ArgumentParseException) throwable, (c, e) ->
                                            commandSender.sendMessage(
                                                    ChatColor.RED + "Invalid Command Argument: "
                                                            + ChatColor.GRAY + finalThrowable.getCause()
                                                            .getMessage())
                            );
                        } else if (throwable instanceof CommandExecutionException) {
                            this.manager.handleException(sender,
                                    CommandExecutionException.class,
                                    (CommandExecutionException) throwable, (c, e) -> {
                                        commandSender.sendMessage(MESSAGE_INTERNAL_ERROR);
                                        this.manager.getOwningPlugin().getLogger().log(
                                                Level.SEVERE,
                                                "Exception executing command handler",
                                                finalThrowable.getCause()
                                        );
                                    }
                            );
                        } else {
                            commandSender.sendMessage(MESSAGE_INTERNAL_ERROR);
                            this.manager.getOwningPlugin().getLogger().log(
                                    Level.SEVERE,
                                    "An unhandled exception was thrown during command execution",
                                    throwable
                            );
                        }
                    }
                });
        return true;
    }

    @Override
    public String getDescription() {
        return this.cloudCommand.getCommandMeta().getOrDefault(CommandMeta.DESCRIPTION, "");
    }

    @Override
    public Plugin getPlugin() {
        return this.manager.getOwningPlugin();
    }

    @Override
    public String getPermission() {
        return this.cloudCommand.getCommandPermission().toString();
    }

    @Override
    public String getUsage() {
        return this.manager.getCommandSyntaxFormatter().apply(this.cloudCommand.getArguments(), null);
    }

}
