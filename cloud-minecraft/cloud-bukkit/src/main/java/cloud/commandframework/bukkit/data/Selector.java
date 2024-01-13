//
// MIT License
//
// Copyright (c) 2024 Incendo
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
package cloud.commandframework.bukkit.data;

import java.util.Collection;
import java.util.Collections;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A selector string to query multiple entity-like values.
 *
 * @param <V> value type
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public interface Selector<V> {

    /**
     * Get the raw string associated with the selector.
     *
     * @return the input
     */
    @NonNull String inputString();

    /**
     * Get the value of this selector.
     *
     * @return all matched entities
     */
    @NonNull Collection<V> values();

    /**
     * A specialized {@link Selector} that can only return one value.
     *
     * @param <V> value type
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    interface Single<V> extends Selector<V> {

        @Override
        default @NonNull Collection<V> values() {
            return Collections.singletonList(this.single());
        }

        /**
         * Get the single value from this selector.
         *
         * <p>A successfully parsed {@link Single} must match a value.</p>
         *
         * @return the value
         */
        @NonNull V single();
    }
}
