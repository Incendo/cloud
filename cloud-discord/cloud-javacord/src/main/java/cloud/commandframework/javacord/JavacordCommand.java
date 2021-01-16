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
package cloud.commandframework.javacord;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.javacord.sender.JavacordCommandSender;
import cloud.commandframework.javacord.sender.JavacordPrivateSender;
import cloud.commandframework.javacord.sender.JavacordServerSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.concurrent.CompletionException;

public class JavacordCommand<C> implements MessageCreateListener {

    private static final String MESSAGE_INTERNAL_ERROR = "An internal error occurred while attempting to perform this command.";
    private static final String MESSAGE_NO_PERMS = "I'm sorry, but you do not have the permission to do this :/";

    private final JavacordCommandManager<C> manager;
    private final CommandArgument<C, ?> command;

    JavacordCommand(
            final @NonNull CommandArgument<C, ?> command,
            final @NonNull JavacordCommandManager<C> manager
    ) {
        this.command = command;
        this.manager = manager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void onMessageCreate(final @NonNull MessageCreateEvent event) {
        MessageAuthor messageAuthor = event.getMessageAuthor();

        if (messageAuthor.isWebhook() || !messageAuthor.isRegularUser()) {
            return;
        }

        JavacordCommandSender commandSender;
        if (event.getMessage().isServerMessage()) {
            commandSender = new JavacordServerSender(event);
        } else if (event.getMessage().isPrivateMessage()) {
            commandSender = new JavacordPrivateSender(event);
        } else {
            commandSender = new JavacordCommandSender(event);
        }

        C sender = manager.getCommandSenderMapper().apply(commandSender);

        String messageContent = event.getMessageContent();
        String commandPrefix = manager.getCommandPrefix(sender);
        if (!messageContent.startsWith(commandPrefix)) {
            return;
        }
        messageContent = messageContent.replaceFirst(commandPrefix, "");

        final String finalContent = messageContent;
        if (((StaticArgument<C>) command).getAliases()
                .stream()
                .map(String::toLowerCase)
                .noneMatch(commandAlias -> finalContent.toLowerCase().startsWith(commandAlias))) {
            return;
        }

        manager.executeCommand(sender, finalContent)
                .whenComplete((commandResult, throwable) -> {
                    if (throwable == null) {
                        return;
                    }
                    final Throwable finalThrowable = throwable;

                    if (throwable instanceof CompletionException) {
                        throwable = throwable.getCause();
                    }

                    if (throwable instanceof NoSuchCommandException) {
                        //Ignore, should never happen
                        return;
                    }

                    if (throwable instanceof InvalidSyntaxException) {
                        manager.handleException(
                                sender,
                                InvalidSyntaxException.class,
                                (InvalidSyntaxException) throwable,
                                (c, e) -> commandSender.sendErrorMessage(
                                        "Invalid Command Syntax. Correct command syntax is: `"
                                                + e.getCorrectSyntax()
                                                + "`")
                        );

                        return;
                    }

                    if (throwable instanceof InvalidCommandSenderException) {
                        manager.handleException(
                                sender,
                                InvalidCommandSenderException.class,
                                (InvalidCommandSenderException) throwable,
                                (c, e) -> commandSender.sendErrorMessage(e.getMessage())
                        );

                        return;
                    }

                    if (throwable instanceof NoPermissionException) {
                        manager.handleException(
                                sender,
                                NoPermissionException.class,
                                (NoPermissionException) throwable,
                                (c, e) -> commandSender.sendErrorMessage(MESSAGE_NO_PERMS)
                        );

                        return;
                    }

                    if (throwable instanceof ArgumentParseException) {
                        manager.handleException(
                                sender,
                                ArgumentParseException.class,
                                (ArgumentParseException) throwable,
                                (c, e) -> commandSender.sendErrorMessage(
                                        "Invalid Command Argument: `" + e.getCause().getMessage() + "`")
                        );

                        return;
                    }

                    if (throwable instanceof CommandExecutionException) {
                        manager.handleException(
                                sender,
                                CommandExecutionException.class,
                                (CommandExecutionException) throwable,
                                (c, e) -> {
                                    commandSender.sendErrorMessage(MESSAGE_INTERNAL_ERROR);
                                    finalThrowable.getCause().printStackTrace();
                                }
                        );

                        return;
                    }

                    commandSender.sendErrorMessage(throwable.getMessage());
                    throwable.printStackTrace();
                });
    }

}
