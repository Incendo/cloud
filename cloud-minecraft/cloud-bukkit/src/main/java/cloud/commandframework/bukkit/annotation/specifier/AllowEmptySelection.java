package cloud.commandframework.bukkit.annotation.specifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apiguardian.api.API;

/**
 * Annotation used to specify if an empty result is allowed for
 * {@link cloud.commandframework.bukkit.parsers.selector.MultiplePlayerSelectorArgument} and
 * {@link cloud.commandframework.bukkit.parsers.selector.MultipleEntitySelectorArgument}.
 *
 * @since 1.8.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@API(status = API.Status.STABLE, since = "1.8.0")
public @interface AllowEmptySelection {

    /**
     * Whether to allow empty results.
     *
     * @return value
     */
    boolean value() default true;

}
