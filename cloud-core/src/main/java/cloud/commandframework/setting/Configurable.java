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
package cloud.commandframework.setting;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

/**
 * Something with configurable {@link Setting settings}.
 *
 * @param <S> setting type
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public interface Configurable<S extends Setting> {

    /**
     * Returns a new {@link Configurable} instance for the given {@link E enum type}.
     *
     * @param <E>       enum type
     * @param enumClass enum class
     * @return the configurable
     */
    static <E extends Enum<E> & Setting> @NonNull Configurable<E> enumConfigurable(final @NonNull Class<E> enumClass) {
        return new EnumConfigurable<>(enumClass);
    }

    /**
     * Updates the {@code value} of the given {@code setting}.
     *
     * @param setting setting to update
     * @param value   new value
     * @return {@code this}
     */
    @This @NonNull Configurable<S> set(@NonNull S setting, boolean value);

    /**
     * Returns the value of the given {@code setting}
     *
     * @param setting setting to retrieve the value for
     * @return the value
     */
    boolean get(@NonNull S setting);
}
