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
package cloud.commandframework.paper.suggestions;

import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.bukkit.BukkitPluginRegistrationHandler;
import cloud.commandframework.paper.PaperCommandManager;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.event.EventHandler;
import org.checkerframework.checker.nullness.qual.NonNull;

class AsyncCommandSuggestionListener<C> implements SuggestionListener<C> {

    private final PaperCommandManager<C> paperCommandManager;

    AsyncCommandSuggestionListener(final @NonNull PaperCommandManager<C> paperCommandManager) {
        this.paperCommandManager = paperCommandManager;
    }

    @EventHandler
    void onTabCompletion(final @NonNull AsyncTabCompleteEvent event) {
        // Strip leading slash
        final String strippedBuffer = event.getBuffer().startsWith("/")
                ? event.getBuffer().substring(1)
                : event.getBuffer();
        if (strippedBuffer.trim().isEmpty()) {
            return;
        }

        final BukkitPluginRegistrationHandler<C> bukkitPluginRegistrationHandler =
                (BukkitPluginRegistrationHandler<C>) this.paperCommandManager.commandRegistrationHandler();

        /* Turn 'plugin:command arg1 arg2 ...' into 'plugin:command' */
        final String commandLabel = strippedBuffer.split(" ")[0];
        if (!bukkitPluginRegistrationHandler.isRecognized(commandLabel)) {
            return;
        }

        this.setSuggestions(
                event,
                this.paperCommandManager.getCommandSenderMapper().apply(event.getSender()),
                this.paperCommandManager.stripNamespace(event.getBuffer())
        );

        event.setHandled(true);
    }

    protected List<? extends Suggestion> querySuggestions(final @NonNull C commandSender, final @NonNull String input) {
        return this.paperCommandManager.suggestionFactory().suggestImmediately(commandSender, input);
    }

    protected void setSuggestions(
            final @NonNull AsyncTabCompleteEvent event,
            final @NonNull C commandSender,
            final @NonNull String input
    ) {
        final List<? extends Suggestion> suggestions = this.querySuggestions(commandSender, input);
        event.setCompletions(suggestions.stream().map(Suggestion::suggestion).collect(Collectors.toList()));
    }
}
