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
package cloud.commandframework.jda;

import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * JDA Command Listener
 *
 * @param <C> Command sender type
 */
public class JDACommandListener<C> extends ListenerAdapter {

    private static final String MESSAGE_INVALID_SYNTAX = "Invalid Command Syntax. Correct command syntax is: ";
    private static final String MESSAGE_NO_PERMS = "I'm sorry, but you do not have permission to perform this command. "
            + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command";

    private final JDACommandManager<C> commandManager;

    /**
     * Construct a new JDA Command Listener
     *
     * @param commandManager Command Manager instance
     */
    public JDACommandListener(final @NonNull JDACommandManager<C> commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public final void onMessageReceived(final @NonNull MessageReceivedEvent event) {
        Message message = event.getMessage();
        C sender = commandManager.getCommandSenderMapper().apply(event);

        if (commandManager.getBotId() == event.getAuthor().getIdLong()) {
            return;
        }

        String prefix = commandManager.getPrefixMapper().apply(sender);
        String content = message.getContentRaw();

        if (!content.startsWith(prefix)) {
            return;
        }

        content = content.substring(prefix.length());

        commandManager.executeCommand(sender, content)
                      .whenComplete((commandResult, throwable) -> {
                          if (throwable == null) {
                              return;
                          }

                          if (throwable instanceof InvalidSyntaxException) {
                              commandManager.handleException(sender,
                                                             InvalidSyntaxException.class,
                                                             (InvalidSyntaxException) throwable, (c, e) -> {
                                          sendMessage(event,
                                                      MESSAGE_INVALID_SYNTAX + prefix + ((InvalidSyntaxException) throwable)
                                                              .getCorrectSyntax());
                                      });
                          } else if (throwable instanceof InvalidCommandSenderException) {
                              commandManager.handleException(sender,
                                                             InvalidCommandSenderException.class,
                                                             (InvalidCommandSenderException) throwable, (c, e) ->
                                                                     sendMessage(event, throwable.getMessage())
                              );
                          } else if (throwable instanceof NoPermissionException) {
                              commandManager.handleException(sender,
                                                             NoPermissionException.class,
                                                             (NoPermissionException) throwable, (c, e) ->
                                                                     sendMessage(event, MESSAGE_NO_PERMS)
                              );
                          } else if (throwable instanceof NoSuchCommandException) {
                              commandManager.handleException(sender,
                                                             NoSuchCommandException.class,
                                                             (NoSuchCommandException) throwable, (c, e) ->
                                                                     sendMessage(event, MESSAGE_UNKNOWN_COMMAND)
                              );
                          } else if (throwable instanceof ArgumentParseException) {
                              commandManager.handleException(sender, ArgumentParseException.class,
                                                             (ArgumentParseException) throwable, (c, e) -> {
                                          sendMessage(event,
                                                      "Invalid Command Argument: " + throwable.getCause()
                                                                                              .getMessage());
                                      });
                          } else {
                              sendMessage(event, throwable.getMessage());
                          }
                      });
    }

    private void sendMessage(final @NonNull MessageReceivedEvent event, final @NonNull String message) {
        event.getChannel().sendMessage(message).queue();
    }
}
