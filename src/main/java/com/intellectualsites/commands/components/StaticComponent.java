//
// MIT License
//
// Copyright (c) 2020 IntellectualSites
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
package com.intellectualsites.commands.components;

import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.parser.ComponentParseResult;
import com.intellectualsites.commands.parser.ComponentParser;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;

public final class StaticComponent<C extends CommandSender> extends CommandComponent<C, String> {

    private StaticComponent(final boolean required, @Nonnull final String name, @Nonnull final String... aliases) {
        super(required, name, new StaticComponentParser<>(name, aliases));
    }

    @Nonnull
    public static <C extends CommandSender> StaticComponent<C> required(@Nonnull final String name,
                                                                        @Nonnull final String... aliases) {
        return new StaticComponent<>(true, name, aliases);
    }

    @Nonnull
    public static <C extends CommandSender> StaticComponent<C> optional(@Nonnull final String name,
                                                                        @Nonnull final String... aliases) {
        return new StaticComponent<>(false, name, aliases);
    }


    private static final class StaticComponentParser<C extends CommandSender> implements ComponentParser<C, String> {

        private final String name;
        private final Set<String> acceptedStrings = new HashSet<>();

        private StaticComponentParser(@Nonnull final String name, @Nonnull final String... aliases) {
            this.acceptedStrings.add(this.name = name);
            this.acceptedStrings.addAll(Arrays.asList(aliases));
        }

        @Nonnull
        @Override
        public ComponentParseResult<String> parse(@Nonnull final CommandContext<C> commandContext,
                                                  @Nonnull final Queue<String> inputQueue) {
            final String string = inputQueue.peek();
            if (string == null) {
                return ComponentParseResult.failure(this.name);
            }
            for (final String acceptedString : this.acceptedStrings) {
                if (string.equalsIgnoreCase(acceptedString)) {
                    // Remove the head of the queue
                    inputQueue.remove();
                    return ComponentParseResult.success(this.name);
                }
            }
            return ComponentParseResult.failure(this.name);
        }

        @Nonnull
        @Override
        public List<String> suggestions(@Nonnull final CommandContext<C> commandContext, @Nonnull final String input) {
            if (this.name.toLowerCase(Locale.ENGLISH).startsWith(input)) {
                return Collections.singletonList(this.name);
            }
            return Collections.emptyList();
        }

    }

}
