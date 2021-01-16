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
package cloud.commandframework.minecraft.extras;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.types.tuples.Pair;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

/**
 * Parser for color codes.
 * <p>
 * Accepts {@link NamedTextColor NamedTextColors}, Legacy Minecraft {@literal &} color codes, and Hex Codes (#RRGGBB or RRGGBB)
 *
 * @param <C> Command sender type
 * @since 1.1.0
 */
public final class TextColorArgument<C> extends CommandArgument<C, TextColor> {

    private static final Pattern LEGACY_PREDICATE = Pattern.compile(
            "&[0-9a-fA-F]"
    );

    private static final Pattern HEX_PREDICATE = Pattern.compile(
            "#?([a-fA-F0-9]{1,6})"
    );

    private static final Collection<Pair<Character, NamedTextColor>> COLORS = Arrays.asList(
            Pair.of('0', NamedTextColor.BLACK),
            Pair.of('1', NamedTextColor.DARK_BLUE),
            Pair.of('2', NamedTextColor.DARK_GREEN),
            Pair.of('3', NamedTextColor.DARK_GREEN),
            Pair.of('4', NamedTextColor.DARK_AQUA),
            Pair.of('5', NamedTextColor.DARK_PURPLE),
            Pair.of('6', NamedTextColor.GOLD),
            Pair.of('7', NamedTextColor.GRAY),
            Pair.of('8', NamedTextColor.DARK_GRAY),
            Pair.of('9', NamedTextColor.BLUE),
            Pair.of('a', NamedTextColor.GREEN),
            Pair.of('b', NamedTextColor.AQUA),
            Pair.of('c', NamedTextColor.RED),
            Pair.of('d', NamedTextColor.LIGHT_PURPLE),
            Pair.of('e', NamedTextColor.YELLOW),
            Pair.of('f', NamedTextColor.WHITE)
    );

    private TextColorArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue
    ) {
        super(
                required,
                name,
                new TextColorParser<>(),
                defaultValue,
                TypeToken.get(TextColor.class),
                null,
                new LinkedList<>()
        );
    }

    /**
     * Create a new required TextColor argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull TextColorArgument<C> of(final @NonNull String name) {
        return new TextColorArgument<>(
                true,
                name,
                ""
        );
    }

    /**
     * Create a new optional TextColor argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull TextColorArgument<C> optional(final @NonNull String name) {
        return new TextColorArgument<>(
                false,
                name,
                ""
        );
    }

    /**
     * Create a new optional TextColor argument
     *
     * @param name         Argument name
     * @param defaultValue Default value
     * @param <C>          Command sender type
     * @return Created argument
     */
    public static <C> @NonNull TextColorArgument<C> optionalWithDefault(
            final @NonNull String name,
            final @NonNull String defaultValue
    ) {
        return new TextColorArgument<>(
                false,
                name,
                defaultValue
        );
    }


    public static final class TextColorParser<C> implements ArgumentParser<C, TextColor> {

        @Override
        public @NonNull ArgumentParseResult<@NonNull TextColor> parse(
                final @NonNull CommandContext<@NonNull C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        TextColorParser.class,
                        commandContext
                ));
            }
            if (LEGACY_PREDICATE.matcher(input).matches()) {
                final char code = input.substring(1).toLowerCase().charAt(0);
                for (final Pair<Character, NamedTextColor> pair : COLORS) {
                    if (pair.getFirst() == code) {
                        inputQueue.remove();
                        return ArgumentParseResult.success(
                                pair.getSecond()
                        );
                    }
                }
            }
            for (final Pair<Character, NamedTextColor> pair : COLORS) {
                if (pair.getSecond().toString().equalsIgnoreCase(input)) {
                    inputQueue.remove();
                    return ArgumentParseResult.success(
                            pair.getSecond()
                    );
                }
            }
            if (HEX_PREDICATE.matcher(input).matches()) {
                inputQueue.remove();
                return ArgumentParseResult.success(
                        TextColor.color(Integer.parseInt(input.startsWith("#") ? input.substring(1) : input, 16))
                );
            }
            return ArgumentParseResult.failure(
                    new TextColorParseException(
                            commandContext,
                            input
                    )
            );
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext, final @NonNull String input
        ) {
            final List<String> suggestions = new LinkedList<>();
            if (input.isEmpty() || input.equals("#") || (HEX_PREDICATE.matcher(input).matches()
                    && input.length() < (input.startsWith("#") ? 7 : 6))) {
                for (char c = 'a'; c <= 'f'; c++) {
                    suggestions.add(String.format("%s%c", input, c));
                    suggestions.add(String.format("&%c", c));
                }
                for (char c = '0'; c <= '9'; c++) {
                    suggestions.add(String.format("%s%c", input, c));
                    suggestions.add(String.format("&%c", c));
                }
            }
            suggestions.addAll(NamedTextColor.NAMES.keys());
            return suggestions;
        }

    }


    private static final class TextColorParseException extends ParserException {

        private static final long serialVersionUID = -6236625328843879518L;

        private TextColorParseException(
                final @NonNull CommandContext<?> commandContext,
                final @NonNull String input
        ) {
            super(
                    TextColorParser.class,
                    commandContext,
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_COLOR,
                    CaptionVariable.of("input", input)
            );
        }

    }

}
