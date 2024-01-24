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
package org.incendo.cloud;

import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Mapper than can transform command senders from a base type to another type and back.
 *
 * @param <B> base command sender type
 * @param <M> mapped command sender type
 */
@API(status = API.Status.STABLE)
public interface SenderMapper<B, M> {

    /**
     * Maps a command sender to another type.
     *
     * @param base base command sender
     * @return mapped command sender
     */
    @NonNull M map(@NonNull B base);

    /**
     * Reverses a mapped command sender to it's base type.
     *
     * @param mapped mapped command sender
     * @return base command sender
     */
    @NonNull B reverse(@NonNull M mapped);

    /**
     * Creates a new sender mapper from the provided functions.
     *
     * @param map     mapping function
     * @param reverse reverse mapping function
     * @param <B>     base command sender type
     * @param <M>     mapped command sender type
     * @return new sender mapper
     */
    static <B, M> @NonNull SenderMapper<B, M> create(
            final @NonNull Function<@NonNull B, @NonNull M> map,
            final @NonNull Function<@NonNull M, @NonNull B> reverse
    ) {
        return new SenderMapperImpl<>(map, reverse);
    }

    /**
     * Returns the identity mapper, a mapper that does not transform the sender.
     *
     * @param <S> base and mapped command sender type
     * @return identity mapper
     */
    @SuppressWarnings("unchecked")
    static <S> @NonNull SenderMapper<S, S> identity() {
        return (SenderMapper<S, S>) SenderMapperImpl.IDENTITY;
    }
}
