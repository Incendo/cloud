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
package cloud.commandframework.paper;

import cloud.commandframework.bukkit.BukkitPluginRegistrationHandler;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

class AsyncCommandSuggestionsListener<C> implements Listener {

    private static final long CACHE_EXPIRATION_TIME = 30L;

    private final PaperCommandManager<C> paperCommandManager;

    AsyncCommandSuggestionsListener(final @NonNull PaperCommandManager<C> paperCommandManager) {
        this.paperCommandManager = paperCommandManager;
    }

    @EventHandler
    void onTabCompletion(final @NonNull AsyncTabCompleteEvent event) throws Exception {
        if (event.getBuffer().isEmpty() || !event.getBuffer().startsWith("/")) {
            return;
        }
        final String[] arguments = event.getBuffer().substring(1).split(" ");
        if (this.paperCommandManager.getCommandTree().getNamedNode(arguments[0]) == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        final BukkitPluginRegistrationHandler<C> bukkitPluginRegistrationHandler =
                (BukkitPluginRegistrationHandler<C>) this.paperCommandManager.getCommandRegistrationHandler();
        if (!bukkitPluginRegistrationHandler.isRecognized(arguments[0])) {
            return;
        }
        final CommandSender sender = event.getSender();
        final C cloudSender = this.paperCommandManager.getCommandSenderMapper().apply(sender);
        final List<String> suggestions = new ArrayList<>(this.paperCommandManager.suggest(cloudSender,
                                                                                          event.getBuffer().substring(1)));
        event.setCompletions(suggestions);
        event.setHandled(true);
    }

}
