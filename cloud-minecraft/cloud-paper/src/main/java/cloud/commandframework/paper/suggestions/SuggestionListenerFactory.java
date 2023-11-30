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

import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.audience.Audience;
import org.apiguardian.api.API;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.INTERNAL, since = "2.0.0")
public interface SuggestionListenerFactory<C> {

    /**
     * Returns a suggestion listener factory.
     *
     * @param <C>            the command sender type
     * @param commandManager the command manager
     * @return the suggestion listener factory
     */
    static <C> @NonNull SuggestionListenerFactory<C> create(final @NonNull PaperCommandManager<C> commandManager) {
        return new SuggestionListenerFactoryImpl<>(commandManager);
    }

    /**
     * Returns the appropriate suggestion listener for the current environment.
     *
     * @return the suggestion listener
     */
    @NonNull SuggestionListener<C> createListener();


    final class SuggestionListenerFactoryImpl<C> implements SuggestionListenerFactory<C> {

        private final PaperCommandManager<C> commandManager;

        private SuggestionListenerFactoryImpl(final @NonNull PaperCommandManager<C> commandManager) {
            this.commandManager = commandManager;
        }

        @Override
        public @NonNull SuggestionListener<C> createListener() {
            if (Audience.class.isAssignableFrom(Player.class)) {
                return new BrigadierAsyncCommandSuggestionListener<>(this.commandManager);
            }
            return new AsyncCommandSuggestionListener<>(this.commandManager);
        }
    }
}
