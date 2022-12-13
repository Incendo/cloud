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
package cloud.commandframework.brigadier;

import cloud.commandframework.Completion;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A suggestion which gives native minecraft chat component as description, used for brigadier support
 */
public interface NativeCompletion extends Completion {

    /**
     * Gives a native chat component tooltip for suggestion
     * @return chat component
     */
    @NonNull Message tooltip();

    /**
     * Returns a new native suggestion with a new tooltip
     * @param tooltip the new tooltip message
     * @return a new suggestion instance with this tooltip
     */
    @NonNull NativeCompletion withTooltip(@NonNull Message tooltip);

    /**
     * Gives a suggestion instance with native brigadier message as description
     * @param suggestion the suggestion
     * @param description the plain text as description
     * @return a new suggestion instance
     */
    static @NonNull NativeCompletion of(@NonNull final String suggestion, @NonNull final String description) {
        return new SimpleCompletion(suggestion, new LiteralMessage(description));
    }
    /**
     * Gives a suggestion instance with native brigadier message as description
     * @param suggestion the suggestion
     * @param description the rich text as description
     * @return a new suggestion instance
     */
    static @NonNull NativeCompletion of(@NonNull final String suggestion, @NonNull final Message description) {
        return new SimpleCompletion(suggestion, description);
    }
    /**
     * Gives a suggestion instance with native brigadier message as description
     * if the description object is a brigadier message
     * @param suggestion the suggestion
     * @param description the rich text as description
     * @return a new suggestion instance
     */
    static @NonNull Completion ofUnknown(@NonNull final String suggestion, @Nullable final Object description) {
        Message message = null;
        if (description instanceof Message) {
            message = (Message) description;
        }
        return message == null ? Completion.of(suggestion) : new SimpleCompletion(suggestion, message);
    }

    @API(status = API.Status.INTERNAL)
    final class SimpleCompletion implements NativeCompletion {

        private final @NonNull String suggestion;
        private final @NonNull Message tooltip;

        private SimpleCompletion(final @NonNull String suggestion, final @NonNull Message tooltip) {
            this.suggestion = suggestion;
            this.tooltip = tooltip;
        }

        @Override
        public @NonNull String completion() {
            return this.suggestion;
        }

        @Override
        public @NonNull Completion withSuggestion(@NonNull final String suggestion) {
            return new NativeCompletion.SimpleCompletion(suggestion, this.tooltip);
        }

        @Override
        public @NonNull Message tooltip() {
            return this.tooltip;
        }

        @Override
        public @NonNull NativeCompletion withTooltip(@NonNull final Message tooltip) {
            return new NativeCompletion.SimpleCompletion(this.suggestion, tooltip);
        }
    }
}
