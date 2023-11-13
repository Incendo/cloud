//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import io.leangen.geantyref.TypeToken;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This is a command argument type that essentially mimics {@link StringArgument#greedy(String)},
 * but then splits the input string into a string array. The input string will be split at
 * every blank space.
 *
 * @param <C> Command sender type
 */
@API(status = API.Status.STABLE)
public final class StringArrayArgument<C> extends CommandArgument<C, String[]> {

    private StringArrayArgument(
            final @NonNull String name,
            final @Nullable SuggestionProvider<C> suggestionProvider,
            final @NonNull ArgumentDescription defaultDescription,
            final boolean flagYielding
    ) {
        super(
                name,
                new StringArrayParser<>(flagYielding),
                TypeToken.get(String[].class),
                suggestionProvider,
                defaultDescription
        );
    }

    /**
     * Create a new required string array argument
     *
     * @param name               Argument name
     * @param suggestionProvider Suggestion provider
     * @param <C>                Command sender type
     * @return Created argument
     */
    public static <C> @NonNull StringArrayArgument<C> of(
            final @NonNull String name,
            final @NonNull SuggestionProvider<C> suggestionProvider
    ) {
        return new StringArrayArgument<>(
                name,
                suggestionProvider,
                ArgumentDescription.empty(),
                false /* flagYielding */
        );
    }

    /**
     * Create a new required string array argument
     *
     * @param name               Argument name
     * @param flagYielding       Whether the parser should stop parsing when encountering a potential flag
     * @param suggestionProvider Suggestion provider
     * @param <C>                Command sender type
     * @return Created argument
     * @since 1.7.0
     */
    @API(status = API.Status.STABLE, since = "1.7.0")
    public static <C> @NonNull StringArrayArgument<C> of(
            final @NonNull String name,
            final boolean flagYielding,
            final @NonNull SuggestionProvider<C> suggestionProvider
    ) {
        return new StringArrayArgument<>(
                name,
                suggestionProvider,
                ArgumentDescription.empty(),
                flagYielding
        );
    }


    /**
     * Parser that parses input into a string array
     *
     * @param <C> Command sender type
     */
    @API(status = API.Status.STABLE)
    public static final class StringArrayParser<C> implements ArgumentParser<C, String[]> {

        private static final Pattern FLAG_PATTERN = Pattern.compile("(-[A-Za-z_\\-0-9])|(--[A-Za-z_\\-0-9]*)");

        private final boolean flagYielding;

        /**
         * Construct a new string array parser.
         */
        public StringArrayParser() {
            this.flagYielding = false;
        }

        /**
         * Construct a new string array parser.
         *
         * @param flagYielding Whether the parser should stop parsing when encountering a potential flag
         * @since 1.7.0
         */
        @API(status = API.Status.STABLE, since = "1.7.0")
        public StringArrayParser(final boolean flagYielding) {
            this.flagYielding = flagYielding;
        }

        @Override
        public @NonNull ArgumentParseResult<String @NonNull []> parse(
                final @NonNull CommandContext<@NonNull C> commandContext,
                final @NonNull CommandInput commandInput
        ) {
            final int size = commandInput.tokenize().size();

            if (this.flagYielding) {
                final List<String> result = new LinkedList<>();

                for (int i = 0; i < size; i++) {
                    final String string = commandInput.peekString();
                    if (string.isEmpty() || FLAG_PATTERN.matcher(string).matches()) {
                        break;
                    }
                    result.add(commandInput.readStringSkipWhitespace());
                }

                return ArgumentParseResult.success(result.toArray(new String[0]));
            } else {
                final String[] result = new String[size];
                for (int i = 0; i < result.length; i++) {
                    result[i] = commandInput.readStringSkipWhitespace();
                }
                return ArgumentParseResult.success(result);
            }
        }
    }
}
