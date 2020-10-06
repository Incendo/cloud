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

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Wrapper for {@link MessageReceivedEvent}
 */
public class JDACommandSender {

    private final MessageReceivedEvent event;

    /**
     * Construct a JDA Command Sender using an event
     *
     * @param event Message Received Event
     */
    public JDACommandSender(final @NonNull MessageReceivedEvent event) {
        this.event = event;
    }

    /**
     * Create a JDA Command Sender from a {@link MessageReceivedEvent}
     *
     * @param event Message Received Event
     * @return Constructed JDA Command Sender
     */
    public static JDACommandSender of(final @NonNull MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            return new JDAPrivateSender(event);
        }

        return new JDAGuildSender(event);
    }

    /**
     * Get the {@link MessageReceivedEvent}
     *
     * @return Message Received Event
     */
    public @NonNull MessageReceivedEvent getEvent() {
        return event;
    }

}
