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
package cloud.commandframework.minecraft.extras;

import cloud.commandframework.Completion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A suggestion that contains adventure component as description
 * @since 1.9.0
 */
public interface RichCompletion extends Completion {

    /**
     * Create a new RichCompletion object with the given suggestion and tooltip.
     *
     * @param suggestion The suggestion itself
     * @param tooltip The tooltip to show when the user hovers over the suggestion.
     * @return A RichCompletion object.
     */
    static @NonNull RichCompletion of(@NonNull String suggestion, @NonNull ComponentLike tooltip) {
        return new RichCompletionImpl(suggestion, tooltip.asComponent());
    }

    /**
     * Returns the tooltip component for this completion.
     *
     * @return the tooltip.
     */
    @NonNull Component tooltip();

    /**
     * Creates a new completion with given tooltip
     *
     * @param tooltip The new tooltip
     * @return A new RichCompletion instance.
     */
    @NonNull RichCompletion withTooltip(@NonNull Component tooltip);
}
