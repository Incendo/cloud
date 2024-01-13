//
// MIT License
//
// Copyright (c) 2024 Incendo
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

import cloud.commandframework.CommandComponent;
import cloud.commandframework.javacord.sender.JavacordCommandSender;
import cloud.commandframework.javacord.sender.JavacordPrivateSender;
import cloud.commandframework.javacord.sender.JavacordServerSender;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class JavacordCommand<C> implements MessageCreateListener {

    private final JavacordCommandManager<C> manager;
    private final CommandComponent<C> command;

    JavacordCommand(
            final @NonNull CommandComponent<C> command,
            final @NonNull JavacordCommandManager<C> manager
    ) {
        this.command = command;
        this.manager = manager;
    }

    @Override
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

        C sender = this.manager.commandSenderMapper().apply(commandSender);

        String messageContent = event.getMessageContent();
        String commandPrefix = this.manager.getCommandPrefix(sender);
        if (!messageContent.startsWith(commandPrefix)) {
            return;
        }
        messageContent = messageContent.substring(commandPrefix.length());

        final String finalContent = messageContent;
        if (this.command.aliases()
                .stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .noneMatch(commandAlias -> finalContent.toLowerCase(Locale.ROOT).startsWith(commandAlias))) {
            return;
        }

        this.manager.commandExecutor().executeCommand(sender, finalContent, ctx ->
                        ctx.store(JavacordCommandManager.JAVACORD_COMMAND_SENDER_KEY, commandSender));
    }
}
