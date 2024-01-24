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
package cloud.commandframework.parser.aggregate;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.parser.ArgumentParseResult;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Mapper that maps the result of invoking the inner parsers of a {@link AggregateParser}.
 *
 * @param <C> the command sender type
 * @param <O> the output type
 */
@API(status = API.Status.STABLE)
public interface AggregateResultMapper<C, O> {

    /**
     * Maps the given {@code context} into the output of type {@link O}.
     *
     * @param commandContext the command context
     * @param context        the context to map
     * @return future that completes with the result
     */
    @NonNull CompletableFuture<ArgumentParseResult<O>> map(
            @NonNull CommandContext<C> commandContext,
            @NonNull AggregateParsingContext<C> context
    );


    @API(status = API.Status.STABLE)
    interface DirectAggregateResultMapper<C, O> extends AggregateResultMapper<C, O> {

        /**
         * Maps the given {@code context} into the output of type {@link O}.
         *
         * @param commandContext the command context
         * @param context        the context to map
         * @return the result
         */
        @NonNull ArgumentParseResult<O> mapImmediately(
                @NonNull CommandContext<C> commandContext,
                @NonNull AggregateParsingContext<C> context
        );

        @Override
        default @NonNull CompletableFuture<ArgumentParseResult<O>> map(
                @NonNull CommandContext<C> commandContext,
                @NonNull AggregateParsingContext<C> context
        ) {
            final CompletableFuture<ArgumentParseResult<O>> result = new CompletableFuture<>();
            try {
                result.complete(this.mapImmediately(commandContext, context));
            } catch (final Exception e) {
                result.completeExceptionally(e);
            }
            return result;
        }
    }
}
