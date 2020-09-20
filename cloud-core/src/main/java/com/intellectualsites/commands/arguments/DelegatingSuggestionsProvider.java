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
package com.intellectualsites.commands.arguments;

import com.intellectualsites.commands.arguments.parser.ArgumentParser;
import com.intellectualsites.commands.context.CommandContext;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiFunction;

final class DelegatingSuggestionsProvider<C> implements BiFunction<CommandContext<C>, String, List<String>> {

    private final String argumentName;
    private final ArgumentParser<C, ?> parser;

    DelegatingSuggestionsProvider(@Nonnull final String argumentName, @Nonnull final ArgumentParser<C, ?> parser) {
        this.argumentName = argumentName;
        this.parser = parser;
    }

    @Override
    public List<String> apply(final CommandContext<C> context, final String s) {
        return this.parser.suggestions(context, s);
    }

    @Override
    public String toString() {
        return String.format("DelegatingSuggestionsProvider{name='%s',parser='%s'}", this.argumentName,
                             this.parser.getClass().getCanonicalName());
    }

}
