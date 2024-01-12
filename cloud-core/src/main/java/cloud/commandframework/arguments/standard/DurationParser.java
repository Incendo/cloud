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
package cloud.commandframework.arguments.standard;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.ParserException;
import java.time.Duration;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Parser for {@link Duration}.
 *
 * @param <C> sender type
 * @since 1.7.0
 */
@API(status = API.Status.STABLE, since = "1.7.0")
public final class DurationParser<C> implements ArgumentParser<C, Duration>, BlockingSuggestionProvider.Strings<C> {

    /**
     * Creates a new duration parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Duration> durationParser() {
        return ParserDescriptor.of(new DurationParser<>(), Duration.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #durationParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Duration> durationComponent() {
        return CommandComponent.<C, Duration>builder().parser(durationParser());
    }

    /**
     * Matches durations in the format of: <code>2d15h7m12s</code>
     */
    private static final Pattern DURATION_PATTERN = Pattern.compile("(([1-9][0-9]+|[1-9])[dhms])");

    @Override
    public @NonNull ArgumentParseResult<Duration> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.readString();

        final Matcher matcher = DURATION_PATTERN.matcher(input);

        Duration duration = Duration.ofNanos(0);

        while (matcher.find()) {
            String group = matcher.group();
            String timeUnit = String.valueOf(group.charAt(group.length() - 1));
            int timeValue = Integer.parseInt(group.substring(0, group.length() - 1));
            switch (timeUnit) {
                case "d":
                    duration = duration.plusDays(timeValue);
                    break;
                case "h":
                    duration = duration.plusHours(timeValue);
                    break;
                case "m":
                    duration = duration.plusMinutes(timeValue);
                    break;
                case "s":
                    duration = duration.plusSeconds(timeValue);
                    break;
                default:
                    return ArgumentParseResult.failure(new DurationParseException(input, commandContext));
            }
        }

        if (duration.isZero()) {
            return ArgumentParseResult.failure(new DurationParseException(input, commandContext));
        }

        return ArgumentParseResult.success(duration);
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        if (input.isEmpty(true)) {
            return IntStream.range(1, 10).boxed()
                    .sorted()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
        }

        // 1d_, 5d4m_, etc
        if (Character.isLetter(input.lastRemainingCharacter())) {
            return Collections.emptyList();
        }

        // 1d5_, 5d4m2_, etc
        final String string = input.readString();
        return Stream.of("d", "h", "m", "s")
                .filter(unit -> !string.contains(unit))
                .map(unit -> string + unit)
                .collect(Collectors.toList());
    }


    /**
     * Failure exception for {@link DurationParser}.
     *
     * @since 1.7.0
     */
    @API(status = API.Status.STABLE, since = "1.7.0")
    public static final class DurationParseException extends ParserException {

        private final String input;

        /**
         * Construct a new {@link DurationParseException}.
         *
         * @param input   input string
         * @param context command context
         * @since 1.7.0
         */
        public DurationParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    DurationParser.class,
                    context,
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_DURATION,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Returns the supplied input string.
         *
         * @return input string
         * @since 1.7.0
         */
        public @NonNull String input() {
            return this.input;
        }
    }
}
