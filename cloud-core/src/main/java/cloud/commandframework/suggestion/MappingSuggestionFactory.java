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
package cloud.commandframework.suggestion;

import cloud.commandframework.context.CommandContext;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

final class MappingSuggestionFactory<C, S extends Suggestion> implements SuggestionFactory<C, S> {

    private final SuggestionFactory<C, ?> other;
    private final SuggestionMapper<S> suggestionMapper;

    MappingSuggestionFactory(
            final @NonNull SuggestionFactory<C, ?> other,
            final @NonNull SuggestionMapper<S> suggestionMapper
    ) {
        this.other = other;
        this.suggestionMapper = suggestionMapper;
    }

    @Override
    public @NonNull CompletableFuture<@NonNull Suggestions<C, S>> suggest(
            final @NonNull CommandContext<C> context,
            final @NonNull String input
    ) {
        return this.map(this.other.suggest(context, input));
    }

    @Override
    public @NonNull CompletableFuture<@NonNull Suggestions<C, S>> suggest(final @NonNull C sender, final @NonNull String input) {
        return this.map(this.other.suggest(sender, input));
    }

    @Override
    public @NonNull <S2 extends Suggestion> SuggestionFactory<C, S2> mapped(final @NonNull SuggestionMapper<S2> mapper) {
        final SuggestionMapper<S> mapper0 = this.suggestionMapper;
        return new MappingSuggestionFactory<>(this.other, s -> mapper.map(mapper0.map(s)));
    }

    private <S1 extends Suggestion> @NonNull CompletableFuture<@NonNull Suggestions<C, S>> map(
            final @NonNull CompletableFuture<Suggestions<C, S1>> future
    ) {
        return future.thenApply(suggestions -> Suggestions.create(
                suggestions.commandContext(),
                suggestions.list().stream().map(this.suggestionMapper::map).collect(Collectors.toList()),
                suggestions.commandInput()
        ));
    }
}
