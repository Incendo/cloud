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
package cloud.commandframework.jda;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Guild specific JDA Command Sender
 */
public class JDAGuildSender extends JDACommandSender {

    private final Member member;
    private final TextChannel channel;

    /**
     * Construct a JDA Guild Sender using an event
     *
     * @param event   Message received event
     * @param member  Sending member
     * @param channel Channel sent in
     */
    public JDAGuildSender(
            final @Nullable MessageReceivedEvent event,
            final @NonNull Member member,
            final @NonNull TextChannel channel
    ) {
        super(event, member.getUser(), channel);
        this.member = member;
        this.channel = channel;
    }

    /**
     * Get the member the command sender represents
     *
     * @return Member that sent the message
     * @since 1.1.0
     */
    public @NonNull Member getMember() {
        return this.member;
    }

    /**
     * Get the channel the user sent the message in
     *
     * @return Channel that the message was sent in
     * @since 1.1.0
     */
    public @NonNull TextChannel getTextChannel() {
        return this.channel;
    }

}
