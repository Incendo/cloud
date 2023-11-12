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
package cloud.commandframework.paper;

import cloud.commandframework.arguments.suggestion.TooltipConverter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE, since = "2.0.0")
public final class PaperTooltipConverters {

    private final List<@NonNull TooltipConverter<?, Component>> tooltipConverters = new ArrayList<>();

    PaperTooltipConverters() {
        this.registerTooltipConverter(new ComponentToComponentConverter());
    }

    /**
     * Registers a {@code converter} that converts from {@link T} to a Brigadier {@link Component} for use in suggestion tooltips.
     *
     * @param converter the converter
     * @param <T> the input type
     */
    public <T> void registerTooltipConverter(final @NonNull TooltipConverter<T, Component> converter) {
        this.tooltipConverters.add(converter);
    }

    @API(status = API.Status.INTERNAL, since = "2.0.0")
    @NonNull List<@NonNull TooltipConverter<?, Component>> tooltipConverters() {
        return Collections.unmodifiableList(this.tooltipConverters);
    }

    private static final class ComponentToComponentConverter extends TooltipConverter<Component, Component> {

        private ComponentToComponentConverter() {
            super(Component.class);
        }

        @Override
        public @NonNull Component convert(final @NonNull Component tooltip) {
            return tooltip;
        }
    }
}
