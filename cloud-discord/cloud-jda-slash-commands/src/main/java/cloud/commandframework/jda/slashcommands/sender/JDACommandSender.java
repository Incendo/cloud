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
package cloud.commandframework.jda.slashcommands.sender;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;


/**
 *
 */
public interface JDACommandSender {

    /**
     * Create a JDA Command Sender from a {@link MessageReceivedEvent}
     *
     * @param event Message Received Event
     * @return Constructed JDA Command Sender
     */
    @SuppressWarnings("ClassReferencesSubclass")
    static @NonNull JDAMessageCommandSender of(final @NonNull MessageReceivedEvent event) {
        if (event.isWebhookMessage()) {
            return new JDAMessageCommandSender(event, event.getAuthor(), event.getChannel());
        } else if (event.isFromType(ChannelType.PRIVATE)) {
            return new JDAPrivateMessageSender(event, event.getAuthor(), event.getPrivateChannel());
        }

        return new JDAGuildMessageSender(event, Objects.requireNonNull(event.getMember()), event.getTextChannel());
    }

    /**
     * Create a JDA Command Sender from a {@link SlashCommandEvent}
     *
     * @param event Slash Command Event
     * @return Constructed JDA Command Sender
     */
    @SuppressWarnings("ClassReferencesSubclass")
    static @NonNull JDASlashCommandSender of(final @NonNull SlashCommandEvent event) {
        if (event.getChannelType() == ChannelType.PRIVATE) {
            return new JDAPrivateSlashSender(event, event.getUser(), event.getPrivateChannel());
        }

        return new JDAGuildSlashSender(event, Objects.requireNonNull(event.getMember()), event.getTextChannel());
    }

    /**
     * Get the user the command sender represents
     *
     * @return User that sent the message
     * @since 1.1.0
     */
    @NonNull User getUser();

    /**
     * Get the channel the user sent the message in
     *
     * @return Channel that the message was sent in
     */
    @NonNull MessageChannel getChannel();

}
