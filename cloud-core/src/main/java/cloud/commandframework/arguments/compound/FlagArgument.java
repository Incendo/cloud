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
package cloud.commandframework.arguments.compound;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.keys.SimpleCloudKey;
import io.leangen.geantyref.TypeToken;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Container for flag parsing logic. This should not be be used directly.
 * Internally, a flag argument is a special case of a {@link CompoundArgument}.
 *
 * @param <C> Command sender type
 */
@API(status = API.Status.STABLE)
public final class FlagArgument<C> extends CommandArgument<C, Object> {

    private static final Pattern FLAG_PRIMARY_PATTERN = Pattern.compile(" --(?<name>([A-Za-z]+))");
    private static final Pattern FLAG_ALIAS_PATTERN = Pattern.compile(" -(?<name>([A-Za-z]+))");

    /**
     * Dummy object that indicates that flags were parsed successfully
     */
    public static final Object FLAG_PARSE_RESULT_OBJECT = new Object();
    /**
     * Meta data for the last argument that was suggested
     *
     * @deprecated Use {@link #FLAG_META_KEY} instead
     */
    @Deprecated
    public static final String FLAG_META = "__last_flag__";
    /**
     * Meta data for the last argument that was suggested
     */
    public static final CloudKey<String> FLAG_META_KEY = SimpleCloudKey.of("__last_flag__", TypeToken.get(String.class));

    /**
     * Meta data for the set of parsed flags, used to detect duplicates.
     * @since 1.8.0
     */
    @API(status = API.Status.EXPERIMENTAL, since = "1.8.0")
    public static final CloudKey<Set<CommandFlag<?>>> PARSED_FLAGS = SimpleCloudKey.of("__parsed_flags__",
            new TypeToken<Set<CommandFlag<?>>>(){});

    private static final String FLAG_ARGUMENT_NAME = "flags";

    private final Collection<@NonNull CommandFlag<?>> flags;

    /**
     * Construct a new flag argument
     *
     * @param flags Flags
     */
    public FlagArgument(final Collection<CommandFlag<?>> flags) {
        super(
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


    @API(status = API.Status.STABLE)
    public static final class FlagArgumentParser<C> implements ArgumentParser<C, Object> {

        private final CommandFlag<?>[] flags;

        private FlagArgumentParser(final @NonNull CommandFlag<?>[] flags) {
            this.flags = flags;
        }

        @Override
        public @NonNull ArgumentParseResult<@NonNull Object> parse(
                final @NonNull CommandContext<@NonNull C> commandContext,
                final @NonNull CommandInput commandInput
        ) {
            final FlagParser parser = new FlagParser();
            return parser.parse(commandContext, commandInput);
        }

        /**
         * Parses command input to figure out what flag is currently being
         * typed at the end of the input queue. If no flag value is being
         * inputted, returns {@link Optional#empty()}.<br>
         * <br>
         * Will consume all but the last element from the input queue.
         *
         * @param commandContext Command context
         * @param commandInput   The input arguments
         * @return current flag being typed, or {@code empty()} if none is
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public @NonNull Optional<String> parseCurrentFlag(
                final @NonNull CommandContext<@NonNull C> commandContext,
                final @NonNull CommandInput commandInput
        ) {
            /* If empty, nothing to do */
            if (commandInput.isEmpty()) {
                return Optional.empty();
            }

            /* Before parsing, retrieve the last known input of the queue */
            final String lastInputValue = commandInput.tokenize().getLast();

            /* Parse, but ignore the result of parsing */
            final FlagParser parser = new FlagParser();
            parser.parse(commandContext, commandInput);

            /*
             * If the parser parsed the entire queue, restore the last typed
             * input obtained earlier.
             */
            if (commandInput.isEmpty()) {
                final int count = lastInputValue.length();
                commandInput.moveCursor(-count);
            }

            return Optional.ofNullable(parser.lastParsedFlag());
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public @NonNull List<@NonNull Suggestion> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            /* Check if we have a last flag stored */
            final String lastArg = Objects.requireNonNull(commandContext.getOrDefault(FLAG_META_KEY, ""));
            if (!lastArg.startsWith("-")) {
                final String rawInput = commandContext.getRawInputJoined();
                /* Collection containing all used flags */
                final List<CommandFlag<?>> usedFlags = new LinkedList<>();
                /* Find all "primary" flags, using --flag */
                final Matcher primaryMatcher = FLAG_PRIMARY_PATTERN.matcher(rawInput);
                while (primaryMatcher.find()) {
                    final String name = primaryMatcher.group("name");
                    for (final CommandFlag<?> flag : this.flags) {
                        if (flag.getName().equalsIgnoreCase(name)) {
                            usedFlags.add(flag);
                            break;
                        }
                    }
                }
                /* Find all alias flags */
                final Matcher aliasMatcher = FLAG_ALIAS_PATTERN.matcher(rawInput);
                while (aliasMatcher.find()) {
                    final String name = aliasMatcher.group("name");
                    for (final CommandFlag<?> flag : this.flags) {
                        for (final String alias : flag.getAliases()) {
                            /* Aliases are single-char strings */
                            if (name.contains(alias)) {
                                usedFlags.add(flag);
                                break;
                            }
                        }
                    }
                }
                /* Suggestions */
                final List<Suggestion> suggestions = new LinkedList<>();
                /* Recommend "primary" flags */
                for (final CommandFlag<?> flag : this.flags) {
                    if (usedFlags.contains(flag) && flag.mode() != CommandFlag.FlagMode.REPEATABLE) {
                        continue;
                    }
                    if (!commandContext.hasPermission(flag.permission())) {
                        continue;
                    }

                    suggestions.add(Suggestion.simple(String.format("--%s", flag.getName())));
                }
                /* Recommend aliases */
                final boolean suggestCombined = input.length() > 1 && input.charAt(0) == '-' && input.charAt(1) != '-';
                for (final CommandFlag<?> flag : this.flags) {
                    if (usedFlags.contains(flag) && flag.mode() != CommandFlag.FlagMode.REPEATABLE) {
                        continue;
                    }
                    if (!commandContext.hasPermission(flag.permission())) {
                        continue;
                    }

                    for (final String alias : flag.getAliases()) {
                        if (suggestCombined && flag.getCommandArgument() == null) {
                            suggestions.add(Suggestion.simple(String.format("%s%s", input, alias)));
                        } else {
                            suggestions.add(Suggestion.simple(String.format("-%s", alias)));
                        }
                    }
                }
                /* If we are suggesting the combined flag, then also suggest the current input */
                if (suggestCombined) {
                    suggestions.add(Suggestion.simple(input));
                }
                return suggestions;
            } else {
                CommandFlag<?> currentFlag = null;
                if (lastArg.startsWith("--")) { // --long
                    final String flagName = lastArg.substring(2);
                    for (final CommandFlag<?> flag : this.flags) {
                        if (flagName.equalsIgnoreCase(flag.getName())) {
                            currentFlag = flag;
                            break;
                        }
                    }
                } else { // -x
                    final String flagName = lastArg.substring(1);
                    outer: for (final CommandFlag<?> flag : this.flags) {
                        for (final String alias : flag.getAliases()) {
                            if (alias.equalsIgnoreCase(flagName)) {
                                currentFlag = flag;
                                break outer;
                            }
                        }
                    }
                }
                if (currentFlag != null
                        && commandContext.hasPermission(currentFlag.permission())
                        && currentFlag.getCommandArgument() != null) {
                    return (List<Suggestion>) ((SuggestionProvider) currentFlag.getCommandArgument().suggestionProvider())
                            .suggestions(commandContext, input);
                }
            }
            commandContext.store(FLAG_META_KEY, "");
            return this.suggestions(commandContext, input);
        }


        /**
         * Helper class to parse the command input queue into flags
         * and flag values. On failure the intermediate results
         * can be obtained, which are used for providing suggestions.
         */
        private class FlagParser {

            private String lastParsedFlag;

            @SuppressWarnings({"unchecked", "rawtypes"})
            public @NonNull ArgumentParseResult<@NonNull Object> parse(
                    final @NonNull CommandContext<@NonNull C> commandContext,
                    final @NonNull CommandInput commandInput
            ) {
                final Set<CommandFlag<?>> parsedFlags = commandContext.computeIfAbsent(PARSED_FLAGS, k -> new HashSet());

                while (!commandInput.isEmpty()) {
                    final String string = commandInput.peekString();

                    // If we're not starting a new flag then we're outside the scope of this parser. We exit.
                    if (!string.startsWith("-")) {
                        return ArgumentParseResult.success(FLAG_PARSE_RESULT_OBJECT);
                    }

                    // We're definitely not supplying anything to the flag.
                    this.lastParsedFlag = null;

                    // We figure out which flag we're dealing with.
                    if (string.startsWith("--")) {
                        commandInput.moveCursor(2);
                    } else {
                        commandInput.moveCursor(1);
                    }

                    final String flagName = commandInput.readString();
                    CommandFlag<?> flag = null;

                    if (string.startsWith("--")) {
                        for (final CommandFlag<?> flagCandidate : FlagArgumentParser.this.flags) {
                            if (flagName.equalsIgnoreCase(flagCandidate.getName())) {
                                flag = flagCandidate;
                                break;
                            }
                        }
                    } else if (flagName.length() == 1) {
                        outer: for (final CommandFlag<?> flagCandidate: FlagArgumentParser.this.flags) {
                            for (final String alias : flagCandidate.getAliases()) {
                                if (alias.equalsIgnoreCase(flagName)) {
                                    flag = flagCandidate;
                                    break outer;
                                }
                            }
                        }
                    } else {
                        boolean flagFound = false;
                        for (int i = 0; i < flagName.length(); i++) {
                            final String parsedFlag = Character.toString(flagName.charAt(i)).toLowerCase(Locale.ENGLISH);
                            for (final CommandFlag<?> candidateFlag : FlagArgumentParser.this.flags) {
                                // Argument flags cannot use the shorthand form in this way.
                                if (candidateFlag.getCommandArgument() != null) {
                                    continue;
                                }

                                if (!candidateFlag.getAliases().contains(parsedFlag)) {
                                    continue;
                                }

                                if (parsedFlags.contains(candidateFlag) && candidateFlag.mode() != CommandFlag.FlagMode.REPEATABLE) {
                                    return ArgumentParseResult.failure(new FlagParseException(
                                            string,
                                            FailureReason.DUPLICATE_FLAG,
                                            commandContext
                                    ));
                                } else if (!commandContext.hasPermission(candidateFlag.permission())) {
                                    return ArgumentParseResult.failure(new FlagParseException(
                                            string,
                                            FailureReason.NO_PERMISSION,
                                            commandContext
                                    ));
                                }

                                commandContext.flags().addPresenceFlag(candidateFlag);
                                parsedFlags.add(candidateFlag);
                                flagFound = true;
                            }
                        }

                        if (!flagFound) {
                            return ArgumentParseResult.failure(new FlagParseException(
                                    string,
                                    FailureReason.NO_FLAG_STARTED,
                                    commandContext
                            ));
                        }

                        // This type of flag can never have a value. We move on to the next flag.
                        continue;
                    }

                    if (flag == null) {
                        return ArgumentParseResult.failure(new FlagParseException(
                                string,
                                FailureReason.UNKNOWN_FLAG,
                                commandContext
                        ));
                    } else if (parsedFlags.contains(flag) && flag.mode() != CommandFlag.FlagMode.REPEATABLE) {
                        return ArgumentParseResult.failure(new FlagParseException(
                                string,
                                FailureReason.DUPLICATE_FLAG,
                                commandContext
                        ));
                    } else if (!commandContext.hasPermission(flag.permission())) {
                        return ArgumentParseResult.failure(new FlagParseException(
                                string,
                                FailureReason.NO_PERMISSION,
                                commandContext
                        ));
                    }

                    // The flag has no argument, so we're done.
                    if (flag.getCommandArgument() == null) {
                        commandContext.flags().addPresenceFlag(flag);
                        parsedFlags.add(flag);
                        continue;
                    }

                    // If the command input ends with a space then we set lastParsedFlag before checking if the command
                    // input is empty. This is because this means that the sender has actually started
                    // completing the value.
                    if (commandInput.hasRemainingInput() && commandInput.peek() == ' ') {
                        // Indicate that we parsed the flag and that we're trying to populate the value for it.
                        this.lastParsedFlag = string;
                    }

                    // If there is no input (a space cannot be parsed into anything) then
                    // we cannot complete this flag.
                    if (commandInput.isEmpty(true /* ignoreWhitespace */)) {
                        return ArgumentParseResult.failure(new FlagParseException(
                                flag.getName(),
                                FailureReason.MISSING_ARGUMENT,
                                commandContext
                        ));
                    }

                    // Indicate that we parsed the flag and that we're trying to populate the value for it.
                    this.lastParsedFlag = string;

                    // We then attempt to parse the flag.
                    final ArgumentParseResult<?> result =
                            ((CommandArgument) flag.getCommandArgument())
                                    .getParser()
                                    .parse(
                                            commandContext,
                                            commandInput
                                    );

                    if (result.getFailure().isPresent()) {
                        return ArgumentParseResult.failure(result.getFailure().get());
                    } else if (!result.getParsedValue().isPresent()) {
                        throw new IllegalStateException("Neither result or value were present. Panicking.");
                    }

                    // We store the parsed flag in the context. We do ugly erasure here because generics :)
                    commandContext.flags().addValueFlag(((CommandFlag) flag), (Object) result.getParsedValue().get());
                    // At this point we know the flag parsed successfully.
                    parsedFlags.add(flag);

                    // We're no longer parsing a flag.
                    this.lastParsedFlag = null;
                }

                // We've consumed everything!
                return ArgumentParseResult.success(FLAG_PARSE_RESULT_OBJECT);
            }

            private @Nullable String lastParsedFlag() {
                return this.lastParsedFlag;
            }
        }
    }


    /**
     * Flag parse exception
     */
    @API(status = API.Status.STABLE)
    public static final class FlagParseException extends ParserException {

        private static final long serialVersionUID = -7725389394142868549L;
        private final String input;
        private final FailureReason failureReason;

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
            this.failureReason = failureReason;
        }

        /**
         * Get the supplied input
         *
         * @return String value
         */
        public String getInput() {
            return this.input;
        }

        /**
         * Returns the reason why the flag parsing failed.
         *
         * @return the failure reason
         * @since 1.8.0
         */
        @API(status = API.Status.STABLE, since = "1.8.0")
        public @NonNull FailureReason failureReason() {
            return this.failureReason;
        }
    }


    /**
     * Reasons for which flag parsing may fail
     */
    @API(status = API.Status.STABLE)
    public enum FailureReason {

        UNKNOWN_FLAG(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_UNKNOWN_FLAG),
        DUPLICATE_FLAG(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_DUPLICATE_FLAG),
        NO_FLAG_STARTED(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_NO_FLAG_STARTED),
        MISSING_ARGUMENT(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_MISSING_ARGUMENT),
        NO_PERMISSION(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_NO_PERMISSION);

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
