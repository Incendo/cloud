//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public final class SingleEntitySelector extends MultipleEntitySelector {

    /**
     * Construct a new selector
     *
     * @param selector The input string used to create this selector
     * @param entities The List of Bukkit {@link Entity entities} to construct the {@link EntitySelector} from
     */
    public SingleEntitySelector(
            final @NonNull String selector,
            final @NonNull List<@NonNull Entity> entities
    ) {
        super(selector, entities);
        if (entities.size() > 1) {
            throw new IllegalArgumentException("More than 1 entity selected in single entity selector.");
        }
    }

    /**
     * Get the selected entity or null if no entity matched
     *
     * @return Gets the single Bukkit Entity parsed by the selector
     */
    public @Nullable Entity getEntity() {
        if (this.getEntities().isEmpty()) {
            return null;
        }
        return this.getEntities().get(0);
    }

}
