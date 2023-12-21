//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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
package cloud.commandframework.pircbotx;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

final class CloudListenerAdapter<C> extends ListenerAdapter {

    private final PircBotXCommandManager<C> manager;

    CloudListenerAdapter(final @NonNull PircBotXCommandManager<C> manager) {
        this.manager = manager;
    }

    @Override
    public void onGenericMessage(final @NonNull GenericMessageEvent event) {
        final String message = event.getMessage();
        if (!message.startsWith(this.manager.getCommandPrefix())) {
            return;
        }
        final C sender = this.manager.getUserMapper().apply(event.getUser());
        this.manager.commandExecutor().executeCommand(
                sender,
                message.substring(this.manager.getCommandPrefix().length()),
                context -> context.store(PircBotXCommandManager.PIRCBOTX_MESSAGE_EVENT_KEY, event)
        );
    }
}
