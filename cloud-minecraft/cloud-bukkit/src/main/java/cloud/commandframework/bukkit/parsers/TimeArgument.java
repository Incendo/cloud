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
package cloud.commandframework.bukkit.parsers;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.bukkit.BukkitCaptionKeys;
import cloud.commandframework.bukkit.arguments.Time;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * cloud argument type that parser time
 *
 * @param <C> Command sender type
 */
public class TimeArgument<C> extends CommandArgument<C, Time> {

    protected TimeArgument(
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
                new TimeArgument.TimeParser<>(),
                defaultValue,
                Time.class,
                suggestionsProvider,
                defaultDescription
        );
    }


    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> TimeArgument.@NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new TimeArgument.Builder<>(name);
    }

    /**
     * Create a new required argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Time> of(final @NonNull String name) {
        return TimeArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Time> optional(final @NonNull String name) {
        return TimeArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new optional argument with a default value
     *
     * @param name Argument name
     * @param time Default value
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Time> optional(
            final @NonNull String name,
            final @NonNull Integer time
    ) {
        return TimeArgument.<C>newBuilder(name).asOptionalWithDefault(Integer.toString(time)).build();
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, Time> {

        private Builder(final @NonNull String name) {
            super(Time.class, name);
        }

        @Override
        public @NonNull CommandArgument<C, Time> build() {
            return new TimeArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

    }

    public static final class TimeParser<C> implements ArgumentParser<C, Time> {

        @Override
        public @NonNull ArgumentParseResult<@NonNull Time> parse(
                @NonNull final CommandContext<@NonNull C> commandContext,
                @NonNull final Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        TimeParser.class,
                        commandContext
                ));
            }

            int multiplier = 1;
            String modInput = input;
            if (input.endsWith("d")) {
                multiplier = 24000;
                modInput = input.replace("d", "");
            }
            if (input.endsWith("s")) {
                multiplier = 20;
                modInput = input.replace("s", "");
            }
            if (input.endsWith("t")) {
                multiplier = 1;
                modInput = input.replace("t", "");
            }

            try {
                float value = Float.parseFloat(modInput);
                int ret = Math.round(value * multiplier);
                if (ret < 0) {
                    return ArgumentParseResult.failure(new TimeParseException(
                            input,
                            commandContext
                    ));
                }
                return ArgumentParseResult.success(new Time(ret));
            } catch (NumberFormatException ex) {
                return ArgumentParseResult.failure(new TimeParseException(
                        input,
                        commandContext
                ));
            }
        }

        private static final String[] SUGGESTION_SUFFIXES = new String[]{"d", "s", "t"};

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            List<String> ret = new ArrayList<>();
            for (String suffix : SUGGESTION_SUFFIXES) {
                if (input.endsWith(suffix)) {
                    ret.add(suffix);
                    break;
                }
                ret.add(suffix);
            }
            return ret;
        }

    }

    public static final class TimeParseException extends ParserException {

        private static final long serialVersionUID = 1415174766296065151L;
        private final String input;

        /**
         * Construct a new TimeParseException
         *
         * @param input   Input
         * @param context Command context
         */
        public TimeParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    TimeArgument.TimeParser.class,
                    context,
                    BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_TIME,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Get the input
         *
         * @return Input
         */
        public @NonNull String getInput() {
            return this.input;
        }

    }

}
