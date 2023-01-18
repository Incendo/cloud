package cloud.commandframework.annotations.suggestions;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * List of command completions
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@API(status = API.Status.STABLE)
public @interface ConstantCompletions {
    /**
     * The completions which will be returned as suggestions, useful when the suggestions don't change
     * @return the completions
     * @since 1.9.0
     */
    @NonNull SingleCompletion[] value() default {};
}
