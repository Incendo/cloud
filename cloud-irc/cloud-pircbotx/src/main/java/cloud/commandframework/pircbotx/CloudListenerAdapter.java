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
package cloud.commandframework.pircbotx;

import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

final class CloudListenerAdapter<C> extends ListenerAdapter {

    private static final String MESSAGE_INTERNAL_ERROR = "An internal error occurred while attempting to perform this command.";
    private static final String MESSAGE_INVALID_SYNTAX = "Invalid Command Syntax. Correct command syntax is: ";
    private static final String MESSAGE_NO_PERMS = "I'm sorry, but you do not have permission to perform this command. "
            + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command";

    private final PircBotXCommandManager<C> manager;

    CloudListenerAdapter(final @NonNull PircBotXCommandManager<C> manager) {
        this.manager = manager;
    }

    @Override
    public void onGenericMessage(final @NonNull GenericMessageEvent event) {
        final String message = event.getMessage();
        if (!message.startsWith(this.manager.getCommandPrefix())) {
            return;
        }
        final C sender = this.manager.getUserMapper().apply(event.getUser());
        manager.executeCommand(sender, message.substring(this.manager.getCommandPrefix().length()))
                .whenComplete((commandResult, throwable) -> {
                    if (throwable == null) {
                        return;
                    }
                    final Throwable finalThrowable = throwable;

                    if (throwable instanceof InvalidSyntaxException) {
                        this.manager.handleException(sender,
                                InvalidSyntaxException.class,
                                (InvalidSyntaxException) throwable, (c, e) -> event.respondWith(
                                        MESSAGE_INVALID_SYNTAX + this.manager.getCommandPrefix()
                                                + ((InvalidSyntaxException) throwable).getCorrectSyntax()
                                )
                        );
                    } else if (throwable instanceof InvalidCommandSenderException) {
                        this.manager.handleException(sender,
                                InvalidCommandSenderException.class,
                                (InvalidCommandSenderException) throwable, (c, e) ->
                                        event.respondWith(throwable.getMessage())
                        );
                    } else if (throwable instanceof NoPermissionException) {
                        this.manager.handleException(sender,
                                NoPermissionException.class,
                                (NoPermissionException) throwable, (c, e) ->
                                        event.respondWith(MESSAGE_NO_PERMS)
                        );
                    } else if (throwable instanceof NoSuchCommandException) {
                        this.manager.handleException(sender,
                                NoSuchCommandException.class,
                                (NoSuchCommandException) throwable, (c, e) ->
                                        event.respondWith(MESSAGE_UNKNOWN_COMMAND)
                        );
                    } else if (throwable instanceof ArgumentParseException) {
                        this.manager.handleException(sender, ArgumentParseException.class,
                                (ArgumentParseException) throwable, (c, e) -> event.respondWith(
                                        "Invalid Command Argument: " + throwable.getCause().getMessage()
                                )
                        );
                    } else if (throwable instanceof CommandExecutionException) {
                        this.manager.handleException(sender, CommandExecutionException.class,
                                (CommandExecutionException) throwable, (c, e) -> {
                                    event.respondWith(MESSAGE_INTERNAL_ERROR);
                                    finalThrowable.getCause().printStackTrace();
                                }
                        );
                    } else {
                        event.respondWith(throwable.getMessage());
                    }
                });
    }

}
