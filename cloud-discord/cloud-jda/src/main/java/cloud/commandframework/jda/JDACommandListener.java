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
package cloud.commandframework.jda;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * JDA Command Listener
 *
 * @param <C> Command sender type
 */
@SuppressWarnings("deprecation")
public class JDACommandListener<C> extends ListenerAdapter {

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
        final Message message = event.getMessage();
        final C sender = this.commandManager.senderMapper().map(event);

        if (this.commandManager.getBotId() == event.getAuthor().getIdLong()) {
            return;
        }

        final String prefix = this.commandManager.getPrefixMapper().apply(sender);
        final String content = message.getContentRaw();
        if (!content.startsWith(prefix)) {
            return;
        }

        this.commandManager.commandExecutor().executeCommand(sender, content.substring(prefix.length()));
    }
}
