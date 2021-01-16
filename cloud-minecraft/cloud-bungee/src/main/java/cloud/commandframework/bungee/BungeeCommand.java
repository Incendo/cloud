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
package cloud.commandframework.bungee;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.CompletionException;
import java.util.logging.Level;

public final class BungeeCommand<C> extends Command implements TabExecutor {

    private static final String MESSAGE_INTERNAL_ERROR = "An internal error occurred while attempting to perform this command.";
    private static final String MESSAGE_NO_PERMS =
            "I'm sorry, but you do not have permission to perform this command. "
                    + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command. Type \"/help\" for help.";

    private final BungeeCommandManager<C> manager;
    private final CommandArgument<C, ?> command;

    @SuppressWarnings("unchecked")
    BungeeCommand(
            final cloud.commandframework.@NonNull Command<C> cloudCommand,
            final @NonNull CommandArgument<C, ?> command,
            final @NonNull BungeeCommandManager<C> manager
    ) {
        super(
                command.getName(),
                cloudCommand.getCommandPermission().toString(),
                ((StaticArgument<C>) command).getAlternativeAliases().toArray(new String[0])
        );
        this.command = command;
        this.manager = manager;
    }

    @Override
    public void execute(final CommandSender commandSender, final String[] strings) {
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
                                                    new ComponentBuilder(
                                                            "Invalid Command Syntax. Correct command syntax is: ")
                                                            .color(ChatColor.RED)
                                                            .append("/")
                                                            .color(ChatColor.GRAY)
                                                            .append(((InvalidSyntaxException) finalThrowable)
                                                                    .getCorrectSyntax())
                                                            .color(ChatColor.GRAY)
                                                            .create()
                                            )
                            );
                        } else if (throwable instanceof InvalidCommandSenderException) {
                            final Throwable finalThrowable1 = throwable;
                            this.manager.handleException(sender,
                                    InvalidCommandSenderException.class,
                                    (InvalidCommandSenderException) throwable, (c, e) ->
                                            commandSender.sendMessage(new ComponentBuilder(
                                                    finalThrowable1.getMessage())
                                                    .color(ChatColor.RED)
                                                    .create())
                            );
                        } else if (throwable instanceof NoPermissionException) {
                            this.manager.handleException(sender,
                                    NoPermissionException.class,
                                    (NoPermissionException) throwable, (c, e) ->
                                            commandSender.sendMessage(new ComponentBuilder(
                                                    MESSAGE_NO_PERMS)
                                                    .color(ChatColor.WHITE)
                                                    .create())
                            );
                        } else if (throwable instanceof NoSuchCommandException) {
                            this.manager.handleException(sender,
                                    NoSuchCommandException.class,
                                    (NoSuchCommandException) throwable, (c, e) ->
                                            commandSender.sendMessage(new ComponentBuilder(
                                                    MESSAGE_UNKNOWN_COMMAND)
                                                    .color(ChatColor.WHITE)
                                                    .create())
                            );
                        } else if (throwable instanceof ArgumentParseException) {
                            this.manager.handleException(sender,
                                    ArgumentParseException.class,
                                    (ArgumentParseException) throwable, (c, e) ->
                                            commandSender.sendMessage(new ComponentBuilder(
                                                    "Invalid Command Argument: ")
                                                    .color(ChatColor.GRAY)
                                                    .append(finalThrowable
                                                            .getCause()
                                                            .getMessage())
                                                    .create())
                            );
                        } else if (throwable instanceof CommandExecutionException) {
                            this.manager.handleException(sender,
                                    CommandExecutionException.class,
                                    (CommandExecutionException) throwable, (c, e) -> {
                                        commandSender.sendMessage(new ComponentBuilder(MESSAGE_INTERNAL_ERROR)
                                                .color(ChatColor.RED)
                                                .create());
                                        this.manager.getOwningPlugin().getLogger().log(
                                                Level.SEVERE,
                                                "Exception executing command handler",
                                                finalThrowable.getCause()
                                        );
                                    }
                            );
                        } else {
                            commandSender.sendMessage(new ComponentBuilder(MESSAGE_INTERNAL_ERROR).color(ChatColor.RED).create());
                            this.manager.getOwningPlugin().getLogger().log(
                                    Level.SEVERE,
                                    "An unhandled exception was thrown during command execution",
                                    throwable
                            );
                        }
                    }
                });
    }

    @Override
    public Iterable<String> onTabComplete(
            final CommandSender sender,
            final String[] args
    ) {
        final StringBuilder builder = new StringBuilder(this.command.getName());
        for (final String string : args) {
            builder.append(" ").append(string);
        }
        return this.manager.suggest(
                this.manager.getCommandSenderMapper().apply(sender),
                builder.toString()
        );
    }

}
