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
package cloud.commandframework.javacord.sender;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.concurrent.CompletableFuture;

public class JavacordCommandSender {

    private final MessageCreateEvent event;

    /**
     * Commandsender used for all javacord commands executed.
     *
     * @param event The event which triggered the command
     */
    public JavacordCommandSender(final @NonNull MessageCreateEvent event) {
        this.event = event;
    }

    /**
     * Gets the author of the {@link Message message} which triggered the event
     *
     * @return The author of the message
     */
    @NonNull
    public MessageAuthor getAuthor() {
        return this.event.getMessageAuthor();
    }

    /**
     * Gets the message of the event.
     *
     * @return The message of the event.
     */
    @NonNull
    public Message getMessage() {
        return this.event.getMessage();
    }

    /**
     * Gets the textchannel the {@link Message message} was sent in
     *
     * @return The textchannel of the event
     */
    @NonNull
    public TextChannel getTextChannel() {
        return this.event.getChannel();
    }

    /**
     * Gets the event which triggered the command
     *
     * @return The event of the command
     */
    @NonNull
    public MessageCreateEvent getEvent() {
        return this.event;
    }

    /**
     * Sends a message to the executor of the command
     *
     * @param message message which should be sent
     * @return The sent message
     */
    @NonNull
    public CompletableFuture<Message> sendMessage(final @Nullable String message) {
        return this.event.getChannel().sendMessage(message);
    }

    /**
     * Sends an error message to the executor of the command
     *
     * @param message message which should be sent
     * @return The sent message
     */
    @NonNull
    public CompletableFuture<Message> sendErrorMessage(final @Nullable String message) {
        return sendMessage(":x: " + message);
    }

    /**
     * Sends a success message to the executor of the command
     *
     * @param message message which should be sent
     * @return The sent message
     */
    @NonNull
    public CompletableFuture<Message> sendSuccessMessage(final @Nullable String message) {
        return sendMessage(":white_check_mark: " + message);
    }

}
