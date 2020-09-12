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
package com.intellectualsites.commands.execution;

import com.intellectualsites.commands.execution.preprocessor.CommandPreprocessingContext;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

/**
 * Command suggestions processor that checks the input queue head and filters based on that
 *
 * @param <C> Command sender type
 */
public final class FilteringCommandSuggestionProcessor<C extends CommandSender> implements CommandSuggestionProcessor<C> {

    @Nonnull
    @Override
    public List<String> apply(@Nonnull final CommandPreprocessingContext<C> context, @Nonnull final List<String> strings) {
        final String input;
        if (context.getInputQueue().isEmpty()) {
            input = "";
        } else {
            input = context.getInputQueue().peek();
        }
        final List<String> suggestions = new LinkedList<>();
        for (final String suggestion : strings) {
            if (suggestion.startsWith(input)) {
                suggestions.add(suggestion);
            }
        }
        return suggestions;
    }

}
