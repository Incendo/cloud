//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
package cloud.commandframework.bukkit.arguments.selector;

import org.bukkit.entity.Entity;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * A class to represent the result of parsing a Minecraft Entity/Target Selector argument
 */
public abstract class EntitySelector {

    private final String selector;
    private final List<Entity> entities;

    /**
     * Construct a new entity selector
     *
     * @param selector The input string used to create this selector
     * @param entities The List of Bukkit {@link Entity entities} to construct the {@link EntitySelector} from
     */
    protected EntitySelector(
            final @NonNull String selector,
            final @NonNull List<@NonNull Entity> entities
    ) {
        this.selector = selector;
        this.entities = entities;
    }

    /**
     * Get the resulting entities
     *
     * @return Immutable view of the list list of entities resulting from parsing the entity selector
     */
    public @NonNull List<@NonNull Entity> getEntities() {
        return Collections.unmodifiableList(this.entities);
    }

    /**
     * Get the input String for this selector
     *
     * @return The input String for this selector
     */
    public @NonNull String getSelector() {
        return this.selector;
    }

    /**
     * Check whether the selector selected at least one entity
     *
     * @return {@code true} if at least one entity was selected, else {@code false}
     */
    public boolean hasAny() {
        return !this.entities.isEmpty();
    }

}
