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
package com.intellectualsites.commands.arguments.parser;

import com.google.common.reflect.TypeToken;

import javax.annotation.Nonnull;

/**
 * Common parser parameters used when resolving types in the {@link ParserRegistry}
 */
public final class StandardParameters {

    private StandardParameters() {
    }

    /**
     * Minimum value accepted by a numerical parser
     */
    public static final ParserParameter<Number> RANGE_MIN = create("min", TypeToken.of(Number.class));

    /**
     * Maximum value accepted by a numerical parser
     */
    public static final ParserParameter<Number> RANGE_MAX = create("max", TypeToken.of(Number.class));

    private static <T> ParserParameter<T> create(@Nonnull final String key, @Nonnull final TypeToken<T> expectedType) {
        return new ParserParameter<>(key, expectedType);
    }

}
