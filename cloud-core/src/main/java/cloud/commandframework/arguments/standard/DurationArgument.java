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
import cloud.commandframework.Completion;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import java.time.Duration;
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
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Parses {@link Duration} from a <code>1d2h3m4s</code> format.
 *
 * @param <C> sender type
 * @since 1.7.0
 */
@API(status = API.Status.STABLE, since = "1.7.0")
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
                    @NonNull List<@NonNull Completion>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                new Parser<>(),
                defaultValue,
                suggestionsProvider,
                Duration.class,
                defaultDescription
        );
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return new builder
     * @since 1.7.0
     */
    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required {@link DurationArgument}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return built argument
     * @since 1.7.0
     */
    public static <C> @NonNull DurationArgument<C> of(final @NonNull String name) {
        return DurationArgument.<C>builder(name).asRequired().build();
    }

    /**
     * Create a new optional {@link DurationArgument}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return built argument
     * @since 1.7.0
     */
    public static <C> @NonNull DurationArgument<C> optional(final @NonNull String name) {
        return DurationArgument.<C>builder(name).asOptional().build();
    }

    /**
     * Create a new optional {@link DurationArgument} with the specified default value.
     *
     * @param name            argument name
     * @param defaultDuration default duration
     * @param <C>             sender type
     * @return built argument
     * @since 1.7.0
     */
    public static <C> @NonNull DurationArgument<C> optional(
            final @NonNull String name,
            final @NonNull String defaultDuration
    ) {
        return DurationArgument.<C>builder(name).asOptionalWithDefault(defaultDuration).build();
    }

    /**
     * Create a new optional {@link DurationArgument} with the specified default value.
     *
     * @param name            argument name
     * @param defaultDuration default duration
     * @param <C>             sender type
     * @return built argument
     * @since 1.7.0
     */
    public static <C> @NonNull DurationArgument<C> optional(
            final @NonNull String name,
            final @NonNull Duration defaultDuration
    ) {
        return DurationArgument.<C>builder(name).asOptionalWithDefault(defaultDuration).build();
    }


    /**
     * Builder for {@link DurationArgument}.
     *
     * @param <C> sender type
     * @since 1.7.0
     */
    @API(status = API.Status.STABLE, since = "1.7.0")
    public static final class Builder<C> extends TypedBuilder<C, Duration, Builder<C>> {

        private Builder(final @NonNull String name) {
            super(Duration.class, name);
        }

        /**
         * Sets the command argument to be optional, with the specified default value.
         *
         * @param defaultValue default value
         * @return this builder
         * @see CommandArgument.Builder#asOptionalWithDefault(String)
         * @since 1.7.0
         */
        public @NonNull Builder<C> asOptionalWithDefault(final @NonNull Duration defaultValue) {
            return this.asOptionalWithDefault(defaultValue.getSeconds() + "s");
        }

        /**
         * Create a new {@link DurationArgument} from this builder.
         *
         * @return built argument
         * @since 1.7.0
         */
        @Override
        public @NonNull DurationArgument<C> build() {
            return new DurationArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getCompletionsProvider(),
                    this.getDefaultDescription()
            );
        }
    }


    /**
     * Parser for {@link Duration}.
     *
     * @param <C> sender type
     * @since 1.7.0
     */
    @API(status = API.Status.STABLE, since = "1.7.0")
    public static final class Parser<C> implements ArgumentParser<C, Duration> {

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

            inputQueue.remove();
            return ArgumentParseResult.success(duration);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            char[] chars = input.toLowerCase(Locale.ROOT).toCharArray();

            if (chars.length == 0) {
                return IntStream.range(1, 10).boxed()
                        .sorted()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
            }

            char last = chars[chars.length - 1];

            // 1d_, 5d4m_, etc
            if (Character.isLetter(last)) {
                return Collections.emptyList();
            }

            // 1d5_, 5d4m2_, etc
            return Stream.of("d", "h", "m", "s")
                    .filter(unit -> !input.contains(unit))
                    .map(unit -> input + unit)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Failure exception for {@link Parser}.
     *
     * @since 1.7.0
     */
    @API(status = API.Status.STABLE, since = "1.7.0")
    public static final class DurationParseException extends ParserException {

        private static final long serialVersionUID = 7632293268451349508L;
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
                    Parser.class,
                    context,
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_DURATION,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Get the supplied input string.
         *
         * @return input string
         * @since 1.7.0
         */
        public @NonNull String getInput() {
            return this.input;
        }
    }
}
