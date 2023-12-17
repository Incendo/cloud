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

import cloud.commandframework.arguments.suggestion.SuggestionFactory;
import cloud.commandframework.brigadier.suggestion.TooltipSuggestion;
import cloud.commandframework.paper.PaperCommandManager;
import cloud.commandframework.paper.suggestions.tooltips.CompletionMapper;
import cloud.commandframework.paper.suggestions.tooltips.CompletionMapperFactory;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.event.EventHandler;
import org.checkerframework.checker.nullness.qual.NonNull;

class BrigadierAsyncCommandSuggestionListener<C> extends AsyncCommandSuggestionListener<C> {

    private final CompletionMapperFactory completionMapperFactory = CompletionMapperFactory.detectingRelocation();
    private final SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory;

    BrigadierAsyncCommandSuggestionListener(final @NonNull PaperCommandManager<C> paperCommandManager) {
        super(paperCommandManager);
        this.suggestionFactory = paperCommandManager.suggestionFactory().mapped(TooltipSuggestion::tooltipSuggestion);
    }

    @EventHandler
    @Override
    void onTabCompletion(final @NonNull AsyncTabCompleteEvent event) {
        super.onTabCompletion(event);
    }

    @Override
    protected List<? extends TooltipSuggestion> querySuggestions(final @NonNull C commandSender, final @NonNull String input) {
        return this.suggestionFactory.suggestImmediately(commandSender, input);
    }

    @Override
    protected void setSuggestions(
            final @NonNull AsyncTabCompleteEvent event,
            final @NonNull C commandSender,
            final @NonNull String input
    ) {
        final CompletionMapper completionMapper = this.completionMapperFactory.createMapper();
        final List<? extends TooltipSuggestion> suggestions = this.querySuggestions(commandSender, input);
        event.completions(suggestions.stream().map(completionMapper::map).collect(Collectors.toList()));
    }
}
