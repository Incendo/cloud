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
package cloud.commandframework.arguments.suggestion;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

class SimpleTooltipSuggestion<T> extends SimpleSuggestion implements TooltipSuggestion<T> {

    private final T tooltip;

    SimpleTooltipSuggestion(final @NonNull String suggestion, final @NonNull T tooltip) {
        super(suggestion);
        this.tooltip = tooltip;
    }

    @Override
    public @NonNull T tooltip() {
        return this.tooltip;
    }

    @Override
    public @NonNull TooltipSuggestion<T> withSuggestion(@NonNull final String suggestion) {
        return new SimpleTooltipSuggestion<>(suggestion, this.tooltip());
    }

    @Override
    public @NonNull <U> TooltipSuggestion<U> withTooltip(@NonNull final U tooltip) {
        return new SimpleTooltipSuggestion<>(this.suggestion(), tooltip);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final SimpleTooltipSuggestion<?> that = (SimpleTooltipSuggestion<?>) o;
        return Objects.equals(this.suggestion(), that.suggestion()) && Objects.equals(this.tooltip, that.tooltip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.suggestion(), this.tooltip);
    }

    @Override
    public @NonNull String toString() {
        return String.format("%s (%s)", this.suggestion(), this.tooltip());
    }
}
