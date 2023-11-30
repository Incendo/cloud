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
package cloud.commandframework.paper.suggestions.tooltips;

import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionMapper;
import cloud.commandframework.brigadier.TooltipSuggestion;
import io.papermc.paper.brigadier.PaperBrigadier;
import net.kyori.adventure.text.Component;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * {@link SuggestionMapper} that maps {@link ComponentTooltipSuggestion} to {@link TooltipSuggestion}.
 * <p>
 * This will <b>not</b> work if you have relocated adventure.
 *
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public final class ComponentTooltipSuggestionMapper implements SuggestionMapper<TooltipSuggestion> {

    @Override
    public @NonNull TooltipSuggestion map(@NonNull final Suggestion suggestion) {
        if (suggestion instanceof TooltipSuggestion) {
            return (TooltipSuggestion) suggestion;
        } else if (!(suggestion instanceof ComponentTooltipSuggestion)) {
            return TooltipSuggestion.tooltipSuggestion(suggestion);
        }
        final ComponentTooltipSuggestion componentTooltipSuggestion = (ComponentTooltipSuggestion) suggestion;
        final Component tooltip = componentTooltipSuggestion.tooltip();
        if (tooltip == null) {
            return TooltipSuggestion.tooltipSuggestion(componentTooltipSuggestion);
        }
        return TooltipSuggestion.tooltipSuggestion(
                componentTooltipSuggestion.suggestion(),
                PaperBrigadier.message(tooltip)
        );
    }
}
