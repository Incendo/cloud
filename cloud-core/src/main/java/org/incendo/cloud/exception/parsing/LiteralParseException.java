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
package org.incendo.cloud.exception.parsing;

import org.apiguardian.api.API;

/**
 * Exception thrown when a {@link org.incendo.cloud.parser.standard.LiteralParser} fails to parse an input.
 */
@API(status = API.Status.STABLE)
public class LiteralParseException extends RuntimeException {

    /**
     * Creates a new {@link LiteralParseException} for the provided mismatched {@code input} string.
     *
     * @param input the input that was not successfully parsed
     */
    public LiteralParseException(final String input) {
        super(input, null, true, false);
    }

    /**
     * Noop override - disables stacktraces for performance
     *
     * @return {@code this}
     */
    @Override
    public final synchronized Throwable fillInStackTrace() {
        return this;
    }
}
