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

import cloud.commandframework.Suggestion;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A suggestion which gives native minecraft chat component as description, used for brigadier support
 */
public interface NativeSuggestion extends Suggestion {

    /**
     * Gives a native chat component
     * @return chat component
     */
    @API(status = API.Status.INTERNAL)
    @NonNull Message richDescription();

    /**
     * Gives a suggestion instance with native brigadier message as description
     * @param suggestion the suggestion
     * @param description the plain text as description
     * @return a new suggestion instance
     */
    static @NonNull NativeSuggestion of(@NonNull final String suggestion, @NonNull final String description) {
        return new SimpleSuggestion(suggestion, new LiteralMessage(description));
    }
    /**
     * Gives a suggestion instance with native brigadier message as description
     * @param suggestion the suggestion
     * @param description the rich text as description
     * @return a new suggestion instance
     */
    static @NonNull NativeSuggestion of(@NonNull final String suggestion, @NonNull final Message description) {
        return new SimpleSuggestion(suggestion, description);
    }
    /**
     * Gives a suggestion instance with native brigadier message as description
     * if the description object is a brigadier message
     * @param suggestion the suggestion
     * @param description the rich text as description
     * @return a new suggestion instance
     */
    static @NonNull Suggestion ofUnknown(@NonNull final String suggestion, @Nullable final Object description) {
        Message message = null;
        if (description instanceof Message) {
            message = (Message) description;
        }
        return message == null ? Suggestion.of(suggestion) : new SimpleSuggestion(suggestion, message);
    }

    @API(status = API.Status.INTERNAL)
    final class SimpleSuggestion implements NativeSuggestion {

        private final @NonNull String suggestion;
        private final @NonNull Message richDescription;

        private SimpleSuggestion(final @NonNull String suggestion, final @NonNull Message description) {
            this.suggestion = suggestion;
            this.richDescription = description;
        }

        @Override
        public @Nullable String description() {
            return this.richDescription().getString();
        }

        @Override
        public @NonNull String suggestion() {
            return this.suggestion;
        }

        @Override
        public @NonNull Suggestion withSuggestion(@NonNull final String suggestion) {
            return new NativeSuggestion.SimpleSuggestion(suggestion, this.richDescription);
        }

        @Override
        public @NonNull Message richDescription() {
            return this.richDescription;
        }
    }
}
