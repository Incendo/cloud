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
package cloud.commandframework.examples.jda;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class GuildUser extends CustomUser {

    private final Member member;
    private final TextChannel channel;

    /**
     * Construct a Guild user
     *
     * @param event   The message received event
     * @param member  Guild member that sent the message
     * @param channel Text channel that the message was sent in
     */
    public GuildUser(
            final @Nullable MessageReceivedEvent event,
            final @NonNull Member member,
            final @NonNull TextChannel channel
    ) {
        super(event, member.getUser(), channel);
        this.member = member;
        this.channel = channel;
    }

    /**
     * Get the member that sent the message
     *
     * @return Sending member
     */
    public @NonNull Member getMember() {
        return this.member;
    }

    /**
     * Get the text channel the message was sent in
     *
     * @return Message channel
     */
    public @NonNull TextChannel getTextChannel() {
        return this.channel;
    }
}
