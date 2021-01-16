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
package cloud.commandframework.examples.jda;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

public abstract class CustomUser {

    private final MessageReceivedEvent event;
    private final User user;
    private final MessageChannel channel;

    /**
     * Construct a user
     *
     * @param event   The message received event
     * @param user    Sending user
     * @param channel Channel that the message was sent in
     */
    protected CustomUser(
            final @Nullable MessageReceivedEvent event,
            final @NonNull User user,
            final @NonNull MessageChannel channel
    ) {
        this.event = event;
        this.user = user;
        this.channel = channel;
    }

    /**
     * Get the message received event
     *
     * @return Optional of the message received event
     */
    public final @NonNull Optional<MessageReceivedEvent> getEvent() {
        return Optional.ofNullable(this.event);
    }

    /**
     * Get the user that sent the message
     *
     * @return Sending user
     */
    public final @NonNull User getUser() {
        return user;
    }

    /**
     * Get the channel the message was sent in
     *
     * @return Message channel
     */
    public final @NonNull MessageChannel getChannel() {
        return channel;
    }

}
