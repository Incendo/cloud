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

    static @NonNull NativeSuggestion of(@NonNull final String suggestion, @NonNull final String description) {
        return new SimpleSuggestion(suggestion, new LiteralMessage(description));
    }
    static @NonNull NativeSuggestion of(@NonNull final String suggestion, @NonNull final Message description) {
        return new SimpleSuggestion(suggestion, description);
    }
    static @NonNull Suggestion ofUnknown(@NonNull final String suggestion, @Nullable final Object description) {
        Message message = null;
        if (description instanceof Message) {
            message = (Message) description;
        }
        return message == null ? Suggestion.of(suggestion) : new SimpleSuggestion(suggestion, message);
    }

    @API(status = API.Status.INTERNAL)
    class SimpleSuggestion implements NativeSuggestion {

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
            return new NativeSuggestion.SimpleSuggestion(suggestion, richDescription);
        }

        @Override
        public @NonNull Message richDescription() {
            return richDescription;
        }
    }
}
