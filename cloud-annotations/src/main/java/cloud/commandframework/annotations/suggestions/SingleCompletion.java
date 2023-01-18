package cloud.commandframework.annotations.suggestions;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A single completion
 * @since 1.9.0
 */
public @interface SingleCompletion {

    /**
     * The suggestion that this completion represent
     * @return the suggestion
     */
    @NonNull String value();

    /**
     * The description, if it's an empty string, the completion won't have description
     * @return the description
     */
    @NonNull String description() default "";
}
