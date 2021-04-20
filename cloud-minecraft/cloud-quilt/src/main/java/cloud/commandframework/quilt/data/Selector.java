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

package cloud.commandframework.quilt.data;

import net.minecraft.command.EntitySelector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * A selector string to query multiple entity-like values
 *
 * @param <V> Value type
 * @since 1.5.0
 */
public interface Selector<V> {

    /**
     * Get the raw string associated with the selector.
     *
     * @return the input
     * @since 1.5.0
     */
    @NonNull String getInput();

    /**
     * If this value came from a parsed selector, this will provide the details of that selector.
     *
     * @return the selector
     * @since 1.5.0
     */
    @Nullable EntitySelector getSelector();

    /**
     * Resolve the value of this selector.
     *
     * <p>A successfully parsed selector must match one or more values</p>
     *
     * @return all matched entities
     * @since 1.5.0
     */
    @NonNull Collection<V> get();

    /**
     * A specialized selector that can only return one value.
     *
     * @param <V> the value type
     * @since 1.5.0
     */
    interface Single<V> extends Selector<V> {

        @Override
        default @NonNull Collection<V> get() {
            return Collections.singletonList(this.getSingle());
        }

        /**
         * Get the single value from this selector.
         *
         * @return the value
         * @since 1.5.0
         */
        @NonNull V getSingle();

    }

}
