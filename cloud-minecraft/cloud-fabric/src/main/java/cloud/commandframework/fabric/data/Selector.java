package cloud.commandframework.fabric.data;

import net.minecraft.command.EntitySelector;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * A selector string to query multiple entity-like values
 *
 * @param <V> Value type
 */
public interface Selector<V> {

    /**
     * Get the raw string associated with the selector.
     *
     * @return the input
     */
    String getInput();

    /**
     * If this value came from a parsed selector, this will provide the details of that selector.
     *
     * @return the selector
     */
    @Nullable EntitySelector getSelector();

    /**
     * Resolve the value of this selector.
     *
     * <p>A successfully parsed selector must match one or more values</p>
     *
     * @return all matched entities
     */
    Collection<V> get();

    /**
     * A specialized selector that can only return one value.
     *
     * @param <V> the value type
     */
    interface Single<V> extends Selector<V> {

        @Override
        default Collection<V> get() {
            return Collections.singletonList(this.getSingle());
        }

        V getSingle();
    }

}
