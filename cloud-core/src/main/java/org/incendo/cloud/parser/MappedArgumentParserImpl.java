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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.suggestion.SuggestionProvider;

import static java.util.Objects.requireNonNull;

@API(status = API.Status.INTERNAL)
public final class MappedArgumentParserImpl<C, I, O> implements MappedArgumentParser<C, I, O>,
        ArgumentParser.FutureArgumentParser<C, O> {

    private final ArgumentParser<C, I> base;
    private final Mapper<C, I, O> mapper;

    MappedArgumentParserImpl(
            final ArgumentParser<C, I> base,
            final Mapper<C, I, O> mapper
    ) {
        this.base = base;
        this.mapper = mapper;
    }

    @Override
    public @NonNull ArgumentParser<C, I> baseParser() {
        return this.base;
    }

    @Override
    public @NonNull CompletableFuture<@NonNull ArgumentParseResult<O>> parseFuture(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        return this.base.parseFuture(commandContext, commandInput)
                .thenCompose(result -> this.mapper.map(commandContext, result));
    }

    @Override
    public @NonNull SuggestionProvider<C> suggestionProvider() {
        return this.base.suggestionProvider();
    }

    @Override
    public <O1> ArgumentParser.@NonNull FutureArgumentParser<C, O1> flatMap(final Mapper<C, O, O1> mapper) {
        requireNonNull(mapper, "mapper");
        return new MappedArgumentParserImpl<>(
                this.base,
                (ctx, orig) -> this.mapper.map(ctx, orig)
                        .thenCompose(mapped -> mapper.map(ctx, mapped))
        );
    }

    @Override
    public int hashCode() {
        return 31 + this.base.hashCode()
                + 7 * this.mapper.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        if (!(other instanceof MappedArgumentParserImpl<?, ?, ?>)) {
            return false;
        }

        final MappedArgumentParserImpl<?, ?, ?> that = (MappedArgumentParserImpl<?, ?, ?>) other;
        return this.base.equals(that.base)
                && this.mapper.equals(that.mapper);
    }

    @Override
    public String toString() {
        return "MappedArgumentParserImpl{"
                + "base=" + this.base + ','
                + "mapper=" + this.mapper + '}';
    }
}
