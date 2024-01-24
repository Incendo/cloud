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
package cloud.commandframework.parser.standard;

import cloud.commandframework.component.CommandComponent;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.parser.ArgumentParseResult;
import cloud.commandframework.parser.ArgumentParser;
import cloud.commandframework.parser.ParserDescriptor;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Parser that parses input into a string array
 *
 * @param <C> Command sender type
 */
@API(status = API.Status.STABLE)
public final class StringArrayParser<C> implements ArgumentParser<C, String[]> {

    private static final Pattern FLAG_PATTERN = Pattern.compile("(-[A-Za-z_\\-0-9])|(--[A-Za-z_\\-0-9]*)");

    /**
     * Creates a new character parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, String[]> stringArrayParser() {
        return ParserDescriptor.of(new StringArrayParser<>(), String[].class);
    }

    /**
     * Creates a new character parser that stops parsing when it encounters a command flag.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, String[]> flagYieldingStringArrayParser() {
        return ParserDescriptor.of(new StringArrayParser<>(true /* flagYielding */), String[].class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #stringArrayParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, String[]> characterComponent() {
        return CommandComponent.<C, String[]>builder().parser(stringArrayParser());
    }

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
        final int size = commandInput.remainingTokens();

        if (this.flagYielding) {
            final List<String> result = new LinkedList<>();

            for (int i = 0; i < size; i++) {
                final String string = commandInput.peekString();
                if (string.isEmpty() || FLAG_PATTERN.matcher(string).matches()) {
                    break;
                }
                result.add(commandInput.readString());
            }

            return ArgumentParseResult.success(result.toArray(new String[0]));
        } else {
            final String[] result = new String[size];
            for (int i = 0; i < result.length; i++) {
                result[i] = commandInput.readString();
            }
            return ArgumentParseResult.success(result);
        }
    }
}
