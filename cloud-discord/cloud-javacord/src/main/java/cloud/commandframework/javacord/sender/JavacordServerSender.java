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
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

public class JavacordServerSender extends JavacordCommandSender {

    /**
     * Commandsender used for commands executed on a {@link Server}
     *
     * @param event The event which triggered the command
     */
    public JavacordServerSender(final @NonNull MessageCreateEvent event) {
        super(event);
    }

    /**
     * Gets the server channel the command was executed in
     *
     * @return The server channel
     */
    @NonNull
    public ServerChannel getServerChannel() {
        return getEvent().getServerTextChannel()
                .orElseThrow(() -> new UnsupportedOperationException(
                        "ServerTextChannel not present even though message was sent in a server channel"));
    }

    /**
     * Gets the server the command was executed in
     *
     * @return The server
     */
    @NonNull
    public Server getServer() {
        return getEvent().getServer()
                .orElseThrow(() -> new UnsupportedOperationException(
                        "Server not present even though message was sent on a server"));
    }

}
