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
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;


@SuppressWarnings("unused")
public final class DurationArgument<C> extends CommandArgument<C, Duration> {

    /**
     * Matches durations in the format of: <code>2d15h7m12s</code>
     */
    private static final Pattern DURATION_PATTERN = Pattern.compile("(([1-9][0-9]+|[1-9])[dhms])");

    private DurationArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                new DurationArgument.DurationParser<>(),
                defaultValue,
                Duration.class,
                suggestionsProvider,
                defaultDescription
        );
    }

    /**
     * Create a new builder
     *
     * @param name Name of the component
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> @NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, Duration> of(final @NonNull String name) {
        return DurationArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, Duration> optional(final @NonNull String name) {
        return DurationArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new required command component with a default value
     *
     * @param name            Component name
     * @param defaultDuration Default duration
     * @param <C>             Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, Duration> optional(
            final @NonNull String name,
            final @NonNull String defaultDuration
    ) {
        return DurationArgument.<C>newBuilder(name).asOptionalWithDefault(defaultDuration).build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, Duration> {

        private Builder(final @NonNull String name) {
            super(Duration.class, name);
        }

        /**
         * Builder a new boolean component
         *
         * @return Constructed component
         */
        @Override
        public @NonNull DurationArgument<C> build() {
            return new DurationArgument<>(this.isRequired(), this.getName(), this.getDefaultValue(),
                    this.getSuggestionsProvider(), this.getDefaultDescription()
            );
        }

    }


    public static final class DurationParser<C> implements ArgumentParser<C, Duration> {

        @Override
        public @NonNull ArgumentParseResult<Duration> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        DurationArgument.DurationParseException.class,
                        commandContext
                ));
            }
            inputQueue.remove();

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
                        return ArgumentParseResult.failure(new DurationArgument.DurationParseException(input, commandContext));
                }
            }

            if (duration.isZero()) {
                return ArgumentParseResult.failure(new DurationArgument.DurationParseException(input, commandContext));
            }

            return ArgumentParseResult.success(duration);
        }

        @Override
        @SuppressWarnings("MixedMutabilityReturnType")
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            char[] chars = input.toLowerCase(Locale.ROOT).toCharArray();

            Stream<String> nums = IntStream.range(1, 10).boxed()
                    .sorted()
                    .map(String::valueOf);

            if (chars.length == 0) {
                return nums.collect(Collectors.toList());
            }

            List<String> units = Arrays.asList("d", "h", "m", "s");

            char last = chars[chars.length - 1];

            // 1d_, 5d4m_, etc
            if (Character.isLetter(last)) {
                return Collections.emptyList();
            }

            // 1d5_, 5d4m2_, etc
            return units.stream()
                    .filter(unit -> !input.contains(unit))
                    .map(unit -> input + unit)
                    .collect(Collectors.toList());
        }

    }

    /**
     * Duration parse exception
     */
    public static final class DurationParseException extends ParserException {

        private static final long serialVersionUID = 7632293268451349508L;
        private final String input;

        /**
         * Construct a new Duration parse exception
         *
         * @param input   String input
         * @param context Command context
         */
        public DurationParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    DurationArgument.DurationParser.class,
                    context,
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_DURATION,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Get the supplied input
         *
         * @return String value
         */
        public @NonNull String getInput() {
            return this.input;
        }

    }

}
