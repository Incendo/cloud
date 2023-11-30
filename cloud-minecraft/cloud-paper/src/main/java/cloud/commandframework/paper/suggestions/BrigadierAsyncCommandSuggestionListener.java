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

import cloud.commandframework.brigadier.TooltipSuggestion;
import cloud.commandframework.paper.PaperCommandManager;
import cloud.commandframework.paper.suggestions.tooltips.CompletionMapper;
import cloud.commandframework.paper.suggestions.tooltips.CompletionMapperFactory;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import java.util.List;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

class BrigadierAsyncCommandSuggestionListener<C> extends AsyncCommandSuggestionListener<C> {

    private final CompletionMapperFactory completionMapperFactory = CompletionMapperFactory.detectingRelocation();

    BrigadierAsyncCommandSuggestionListener(final @NonNull PaperCommandManager<C> paperCommandManager) {
        super(paperCommandManager);
    }

    @Override
    protected void setSuggestions(
            final @NonNull AsyncTabCompleteEvent event,
            final @NonNull List<@NonNull TooltipSuggestion> suggestions
    ) {
        final CompletionMapper completionMapper = this.completionMapperFactory.createMapper();
        event.completions(suggestions.stream().map(completionMapper::map).collect(Collectors.toList()));
    }
}
