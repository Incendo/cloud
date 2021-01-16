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
package cloud.commandframework.arguments.standard;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

/**
 * This is a command argument type that essentially mimics {@link StringArgument#greedy(String)},
 * but then splits the input string into a string array. The input string will be split at
 * every blank space.
 *
 * @param <C> Command sender type
 */
public final class StringArrayArgument<C> extends CommandArgument<C, String[]> {

    private StringArrayArgument(
            final boolean required,
            final @NonNull String name,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                new StringArrayParser<>(),
                "",
                TypeToken.get(String[].class),
                suggestionsProvider,
                defaultDescription
        );
    }

    /**
     * Create a new required string array argument
     *
     * @param name                Argument name
     * @param suggestionsProvider Suggestions provider
     * @param <C>                 Command sender type
     * @return Created argument
     */
    public static <C> @NonNull StringArrayArgument<C> of(
            final @NonNull String name,
            final @NonNull BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider
    ) {
        return new StringArrayArgument<>(
                true,
                name,
                suggestionsProvider,
                ArgumentDescription.empty()
        );
    }

    /**
     * Create a new optional string array argument
     *
     * @param name                Argument name
     * @param suggestionsProvider Suggestions provider
     * @param <C>                 Command sender type
     * @return Created argument
     */
    public static <C> @NonNull StringArrayArgument<C> optional(
            final @NonNull String name,
            final @NonNull BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider
    ) {
        return new StringArrayArgument<>(
                false,
                name,
                suggestionsProvider,
                ArgumentDescription.empty()
        );
    }


    /**
     * Parser that parses input into a string array
     *
     * @param <C> Command sender type
     */
    public static final class StringArrayParser<C> implements ArgumentParser<C, String[]> {

        @Override
        public @NonNull ArgumentParseResult<String @NonNull []> parse(
                final @NonNull CommandContext<@NonNull C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String[] result = new String[inputQueue.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = inputQueue.remove();
            }
            return ArgumentParseResult.success(result);
        }

    }

}
