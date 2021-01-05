//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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

package cloud.commandframework.arguments.parser;

import cloud.commandframework.context.CommandContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

public final class MappedArgumentParser<C, I, O> implements ArgumentParser<C, O> {
    private final ArgumentParser<C, I> base;
    private final BiFunction<CommandContext<C>, I, ArgumentParseResult<O>> mapper;

    MappedArgumentParser(
            final ArgumentParser<C, I> base,
            final BiFunction<CommandContext<C>, I, ArgumentParseResult<O>> mapper
    ) {
        this.base = base;
        this.mapper = mapper;
    }

    /**
     * Get the parser this one is derived from.
     *
     * @return the base parser
     */
    public ArgumentParser<C, I> getBaseParser() {
        return this.base;
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull O> parse(
            @NonNull final CommandContext<@NonNull C> commandContext,
            @NonNull final Queue<@NonNull String> inputQueue
    ) {
        final ArgumentParseResult<@NonNull I> baseResult = this.base.parse(commandContext, inputQueue);
        return baseResult.flatMapParsedValue(value -> this.mapper.apply(commandContext, value));
    }

    @Override
    public @NonNull List<@NonNull String> suggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull String input
    ) {
        return this.base.suggestions(commandContext, input);
    }

    @Override
    public @NonNull <O1> ArgumentParser<C, O1> map(final BiFunction<CommandContext<C>, O, ArgumentParseResult<O1>> mapper) {
        return new MappedArgumentParser<>(
                this.base,
                (ctx, original) -> this.mapper.apply(ctx, original).flatMapParsedValue(value -> mapper.apply(ctx, value))
        );
    }

    @Override
    public boolean isContextFree() {
        return this.base.isContextFree();
    }

    @Override
    public int getRequestedArgumentCount() {
        return this.base.getRequestedArgumentCount();
    }

    @Override
    public int hashCode() {
        return 31 + this.base.hashCode()
                + 7 * this.mapper.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        if (!(other instanceof MappedArgumentParser<?, ?, ?>)) {
            return false;
        }

        final MappedArgumentParser<?, ?, ?> that = (MappedArgumentParser<?, ?, ?>) other;
        return this.base.equals(that.base)
                && this.mapper.equals(that.mapper);
    }

    @Override
    public String toString() {
        return "MappedArgumentParser{"
                + "base=" + this.base + ','
                + "mapper=" + this.mapper + '}';
    }

}
