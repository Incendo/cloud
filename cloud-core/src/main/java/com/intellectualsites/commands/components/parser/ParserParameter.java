//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
package com.intellectualsites.commands.components.parser;

import com.google.common.reflect.TypeToken;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Parser parameter used when retrieving parsers from the {@link ParserRegistry}
 *
 * @param <T> Type required by the parameter
 */
public class ParserParameter<T> {

    private final String key;
    private final TypeToken<T> expectedType;

    /**
     * Create a new parser parameter
     *
     * @param key          Parameter key
     * @param expectedType Type that is expected to be mapped to this parameter
     */
    public ParserParameter(@Nonnull final String key, @Nonnull final TypeToken<T> expectedType) {
        this.key = key;
        this.expectedType = expectedType;
    }

    /**
     * Get the parameter key
     *
     * @return Parameter key
     */
    @Nonnull public String getKey() {
        return this.key;
    }

    /**
     * Ge the type that is expected to be mapped to this parameter
     *
     * @return Expected type
     */
    @Nonnull public TypeToken<T> getExpectedType() {
        return this.expectedType;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ParserParameter<?> that = (ParserParameter<?>) o;
        return Objects.equals(key, that.key)
                && Objects.equals(expectedType, that.expectedType);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(key, expectedType);
    }

}
