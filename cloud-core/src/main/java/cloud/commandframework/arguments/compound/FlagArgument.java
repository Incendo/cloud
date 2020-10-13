//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
package cloud.commandframework.arguments.compound;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.ParserException;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Container for flag parsing logic. This should not be be used directly.
 * Internally, a flag argument is a special case of a {@link CompoundArgument}.
 *
 * @param <C> Command sender type
 */
public final class FlagArgument<C> extends CommandArgument<C, Object> {

    /**
     * Dummy object that indicates that flags were parsed successfully
     */
    public static final Object FLAG_PARSE_RESULT_OBJECT = new Object();
    /**
     * Meta data for the last argument that was suggested
     */
    public static final String FLAG_META = "__last_flag__";

    private static final String FLAG_ARGUMENT_NAME = "flags";

    private final Collection<@NonNull CommandFlag<?>> flags;

    /**
     * Construct a new flag argument
     *
     * @param flags Flags
     */
    public FlagArgument(final Collection<CommandFlag<?>> flags) {
        super(
                false,
                FLAG_ARGUMENT_NAME,
                new FlagArgumentParser<>(flags.toArray(new CommandFlag<?>[0])),
                Object.class
        );
        this.flags = flags;
    }

    /**
     * Get the flags registered in the argument
     *
     * @return Unmodifiable view of flags
     */
    public @NonNull Collection<@NonNull CommandFlag<?>> getFlags() {
        return Collections.unmodifiableCollection(this.flags);
    }


    public static final class FlagArgumentParser<C> implements ArgumentParser<C, Object> {

        private final CommandFlag<?>[] flags;

        private FlagArgumentParser(final @NonNull CommandFlag<?>[] flags) {
            this.flags = flags;
        }

        @Override
        public @NonNull ArgumentParseResult<@NonNull Object> parse(
                final @NonNull CommandContext<@NonNull C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            /*
            This argument must necessarily be the last so we can just consume all remaining input. This argument type
            is similar to a greedy string in that sense. But, we need to keep all flag logic contained to the parser
             */
            final Set<CommandFlag<?>> parsedFlags = new HashSet<>();
            CommandFlag<?> currentFlag = null;

            for (final @NonNull String string : inputQueue) {
                if (string.startsWith("-") && currentFlag == null) {
                    if (string.startsWith("--")) {
                        final String flagName = string.substring(2);
                        for (final CommandFlag<?> flag : this.flags) {
                            if (flagName.equalsIgnoreCase(flag.getName())) {
                                currentFlag = flag;
                                break;
                            }
                        }
                    } else {
                        final String flagName = string.substring(1);
                        for (final CommandFlag<?> flag : this.flags) {
                            for (final String alias : flag.getAliases()) {
                                if (alias.equalsIgnoreCase(flagName)) {
                                    currentFlag = flag;
                                    break;
                                }
                            }
                        }
                    }
                    if (currentFlag == null) {
                        return ArgumentParseResult.failure(new FlagParseException(
                                string,
                                FailureReason.UNKNOWN_FLAG,
                                commandContext
                        ));
                    } else if (parsedFlags.contains(currentFlag)) {
                        return ArgumentParseResult.failure(new FlagParseException(
                                string,
                                FailureReason.DUPLICATE_FLAG,
                                commandContext
                        ));
                    }
                    parsedFlags.add(currentFlag);
                    if (currentFlag.getCommandArgument() == null) {
                        /* It's a presence flag */
                        commandContext.flags().addPresenceFlag(currentFlag);
                        /* We don't want to parse a value for this flag */
                        currentFlag = null;
                    }
                } else {
                    if (currentFlag == null) {
                        return ArgumentParseResult.failure(new FlagParseException(
                                string,
                                FailureReason.NO_FLAG_STARTED,
                                commandContext
                        ));
                    } else {
                        final ArgumentParseResult<?> result =
                                ((CommandArgument) currentFlag.getCommandArgument())
                                        .getParser()
                                        .parse(
                                                commandContext,
                                                new LinkedList<>(Collections.singletonList(string))
                                        );
                        if (result.getFailure().isPresent()) {
                            return ArgumentParseResult.failure(result.getFailure().get());
                        } else {
                            final CommandFlag erasedFlag = currentFlag;
                            final Object value = result.getParsedValue().get();
                            commandContext.flags().addValueFlag(erasedFlag, value);
                            currentFlag = null;
                        }
                    }
                }
            }
            if (currentFlag != null) {
                return ArgumentParseResult.failure(new FlagParseException(
                        currentFlag.getName(),
                        FailureReason.MISSING_ARGUMENT,
                        commandContext
                ));
            }
            /* We've consumed everything */
            inputQueue.clear();
            return ArgumentParseResult.success(FLAG_PARSE_RESULT_OBJECT);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            /* Check if we have a last flag stored */
            final String lastArg = commandContext.getOrDefault(FLAG_META, "");
            if (lastArg.isEmpty() || !lastArg.startsWith("-")) {
                /* We don't care about the last value and so we expect a flag */
                final List<String> strings = new LinkedList<>();
                for (final CommandFlag<?> flag : this.flags) {
                    strings.add(String.format("--%s", flag.getName()));
                    for (final String alias : flag.getAliases()) {
                        strings.add(String.format("-%s", alias));
                    }
                }
                return strings;
            } else {
                CommandFlag<?> currentFlag = null;
                if (lastArg.startsWith("--")) {
                    final String flagName = lastArg.substring(2);
                    for (final CommandFlag<?> flag : this.flags) {
                        if (flagName.equalsIgnoreCase(flag.getName())) {
                            currentFlag = flag;
                            break;
                        }
                    }
                } else if (lastArg.startsWith("-")) {
                    final String flagName = lastArg.substring(1);
                    for (final CommandFlag<?> flag : this.flags) {
                        for (final String alias : flag.getAliases()) {
                            if (alias.equalsIgnoreCase(flagName)) {
                                currentFlag = flag;
                                break;
                            }
                        }
                    }
                }
                if (currentFlag != null && currentFlag.getCommandArgument() != null) {
                    // noinspection all
                    return (List<String>) ((BiFunction) currentFlag.getCommandArgument().getSuggestionsProvider())
                            .apply(commandContext, input);
                }
            }
            commandContext.store(FLAG_META, "");
            return suggestions(commandContext, input);
        }

    }

    /**
     * Flag parse exception
     */
    public static final class FlagParseException extends ParserException {

        private final String input;

        /**
         * Construct a new flag parse exception
         *
         * @param input         Input
         * @param failureReason The reason of failure
         * @param context       Command context
         */
        public FlagParseException(
                final @NonNull String input,
                final @NonNull FailureReason failureReason,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    FlagArgument.FlagArgumentParser.class,
                    context,
                    failureReason.getCaption(),
                    CaptionVariable.of("input", input),
                    CaptionVariable.of("flag", input)
            );
            this.input = input;
        }

        /**
         * Get the supplied input
         *
         * @return String value
         */
        public String getInput() {
            return input;
        }

    }

    /**
     * Reasons for which flag parsing may fail
     */
    public enum FailureReason {

        UNKNOWN_FLAG(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_UNKNOWN_FLAG),
        DUPLICATE_FLAG(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_DUPLICATE_FLAG),
        NO_FLAG_STARTED(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_NO_FLAG_STARTED),
        MISSING_ARGUMENT(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_MISSING_ARGUMENT);

        private final Caption caption;

        FailureReason(final @NonNull Caption caption) {
            this.caption = caption;
        }

        /**
         * Get the caption used for this failure reason
         *
         * @return The caption
         */
        public @NonNull Caption getCaption() {
            return this.caption;
        }

    }

}
