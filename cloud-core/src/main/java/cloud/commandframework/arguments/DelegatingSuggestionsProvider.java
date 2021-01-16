//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
package cloud.commandframework.arguments;

import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.function.BiFunction;

final class DelegatingSuggestionsProvider<C> implements BiFunction<@NonNull CommandContext<C>,
        @NonNull String, @NonNull List<String>> {

    private final String argumentName;
    private final ArgumentParser<C, ?> parser;

    DelegatingSuggestionsProvider(final @NonNull String argumentName, final @NonNull ArgumentParser<C, ?> parser) {
        this.argumentName = argumentName;
        this.parser = parser;
    }

    @Override
    public @NonNull List<@NonNull String> apply(final @NonNull CommandContext<C> context, final @NonNull String s) {
        return this.parser.suggestions(context, s);
    }

    @Override
    public String toString() {
        return String.format("DelegatingSuggestionsProvider{name='%s',parser='%s'}", this.argumentName,
                this.parser.getClass().getCanonicalName()
        );
    }

}
