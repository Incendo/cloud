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
package org.incendo.cloud.parser;

import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;

/**
 * An argument parser which wraps another argument parser, converting the output type.
 *
 * @param <C> command sender type
 * @param <I> base output type
 * @param <O> mapped output type
 */
@API(status = API.Status.STABLE)
public interface MappedArgumentParser<C, I, O> extends ArgumentParser<C, O> {

    /**
     * Get the parser this one is derived from.
     *
     * @return the base parser
     */
    @NonNull ArgumentParser<C, I> baseParser();

    @FunctionalInterface
    interface Mapper<C, I, O> {

        /**
         * Maps the input to a future that completes with the output.
         *
         * @param context the context
         * @param input   the input
         * @return future that completes with the output
         */
        @NonNull CompletableFuture<ArgumentParseResult<O>> map(
                @NonNull CommandContext<C> context,
                @NonNull ArgumentParseResult<I> input
        );
    }
}
