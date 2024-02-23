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
package org.incendo.cloud.parser.flag;

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
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

@API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class CommandFlagParser<C> implements ArgumentParser.FutureArgumentParser<C, Object>, SuggestionProvider<C> {

    /**
     * Dummy object that indicates that flags were parsed successfully
     */
    public static final Object FLAG_PARSE_RESULT_OBJECT = new Object();
    /**
     * Metadata for the last argument that was suggested
     */
    public static final CloudKey<String> FLAG_META_KEY = CloudKey.of("__last_flag__", TypeToken.get(String.class));
    /**
     * Metadata for the set of parsed flags, used to detect duplicates.
     */
    public static final CloudKey<Set<CommandFlag<?>>> PARSED_FLAGS = CloudKey.of("__parsed_flags__",
            new TypeToken<Set<CommandFlag<?>>>(){});

    private static final Pattern FLAG_PRIMARY_PATTERN = Pattern.compile(" --(?<name>([A-Za-z]+))");
    private static final Pattern FLAG_ALIAS_PATTERN = Pattern.compile(" -(?<name>([A-Za-z]+))");

    private final Collection<@NonNull CommandFlag<?>> flags;

    /**
     * Creates a new command flag parser.
     *
     * @param flags the flags
     */
    public CommandFlagParser(final @NonNull Collection<@NonNull CommandFlag<?>> flags) {
        this.flags = flags;
    }

    /**
     * Returns the recognized flags.
     *
     * @return unmodifiable view of flags
     */
    @API(status = API.Status.STABLE)
    public @NonNull Collection<@NonNull CommandFlag<?>> flags() {
        return Collections.unmodifiableCollection(this.flags);
    }

    @Override
    public @NonNull CompletableFuture<@NonNull ArgumentParseResult<Object>> parseFuture(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        return new FlagParser().parse(commandContext, commandInput);
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
    @API(status = API.Status.STABLE)
    public @NonNull Optional<String> parseCurrentFlag(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        /* If empty, nothing to do */
        if (commandInput.isEmpty()) {
            return Optional.empty();
        }

        /* Before parsing, retrieve the last known input of the queue */
        final String lastInputValue = commandInput.lastRemainingToken();

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
    public @NonNull CompletableFuture<Iterable<@NonNull Suggestion>> suggestionsFuture(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput input
    ) {
        /* Check if we have a last flag stored */
        final String lastArg = Objects.requireNonNull(commandContext.getOrDefault(FLAG_META_KEY, ""));
        if (!lastArg.startsWith("-")) {
            final String readInput = input.readInput();
            /* Collection containing all used flags */
            final List<CommandFlag<?>> usedFlags = new LinkedList<>();
            /* Find all "primary" flags, using --flag */
            final Matcher primaryMatcher = FLAG_PRIMARY_PATTERN.matcher(readInput);
            while (primaryMatcher.find()) {
                final String name = primaryMatcher.group("name");
                for (final CommandFlag<?> flag : this.flags) {
                    if (flag.name().equalsIgnoreCase(name)) {
                        usedFlags.add(flag);
                        break;
                    }
                }
            }
            /* Find all alias flags */
            final Matcher aliasMatcher = FLAG_ALIAS_PATTERN.matcher(readInput);
            while (aliasMatcher.find()) {
                final String name = aliasMatcher.group("name");
                for (final CommandFlag<?> flag : this.flags) {
                    for (final String alias : flag.aliases()) {
                        /* Aliases are single-char strings */
                        if (name.contains(alias)) {
                            usedFlags.add(flag);
                            break;
                        }
                    }
                }
            }
            final String nextToken = input.peekString();
            final String currentFlag;
            if (nextToken.length() > 1) {
                currentFlag = nextToken.substring(1);
            } else {
                currentFlag = "";
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

                suggestions.add(Suggestion.suggestion(String.format("--%s", flag.name())));
            }
            /* Recommend aliases */
            final boolean suggestCombined = nextToken.length() > 1 && nextToken.startsWith("-") && !nextToken.startsWith("--");
            for (final CommandFlag<?> flag : this.flags) {
                if (usedFlags.contains(flag) && flag.mode() != CommandFlag.FlagMode.REPEATABLE) {
                    continue;
                }
                if (!commandContext.hasPermission(flag.permission())) {
                    continue;
                }

                for (final String alias : flag.aliases()) {
                    if (alias.equalsIgnoreCase(currentFlag)) {
                        continue;
                    }
                    if (suggestCombined && flag.commandComponent() == null) {
                        suggestions.add(Suggestion.suggestion(String.format("%s%s", input.peekString(), alias)));
                    } else {
                        suggestions.add(Suggestion.suggestion(String.format("-%s", alias)));
                    }
                }
            }
            /* If we are suggesting the combined flag, then also suggest the current input */
            if (suggestCombined) {
                suggestions.add(Suggestion.suggestion(input.peekString()));
            }
            return CompletableFuture.completedFuture(suggestions);
        } else {
            CommandFlag<?> currentFlag = null;
            if (lastArg.startsWith("--")) { // --long
                final String flagName = lastArg.substring(2);
                for (final CommandFlag<?> flag : this.flags) {
                    if (flagName.equalsIgnoreCase(flag.name())) {
                        currentFlag = flag;
                        break;
                    }
                }
            } else { // -x
                final String flagName = lastArg.substring(1);
                outer:
                for (final CommandFlag<?> flag : this.flags) {
                    for (final String alias : flag.aliases()) {
                        if (alias.equalsIgnoreCase(flagName)) {
                            currentFlag = flag;
                            break outer;
                        }
                    }
                }
            }
            if (currentFlag != null
                    && commandContext.hasPermission(currentFlag.permission())
                    && currentFlag.commandComponent() != null) {
                final SuggestionProvider suggestionProvider = currentFlag.commandComponent().suggestionProvider();
                return suggestionProvider.suggestionsFuture(commandContext, input);
            }
        }
        commandContext.store(FLAG_META_KEY, "");
        return this.suggestionsFuture(commandContext, input);
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
         * Returns the caption used for this failure reason.
         *
         * @return the caption
         */
        public @NonNull Caption caption() {
            return this.caption;
        }
    }


    /**
     * Flag parse exception
     */
    @API(status = API.Status.STABLE)
    public static final class FlagParseException extends ParserException {

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
                final CommandFlagParser.@NonNull FailureReason failureReason,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    CommandFlagParser.class,
                    context,
                    failureReason.caption(),
                    CaptionVariable.of("input", input),
                    CaptionVariable.of("flag", input)
            );
            this.input = input;
            this.failureReason = failureReason;
        }

        /**
         * Returns the supplied input.
         *
         * @return input
         */
        public String input() {
            return this.input;
        }

        /**
         * Returns the reason why the flag parsing failed.
         *
         * @return the failure reason
         */
        @API(status = API.Status.STABLE)
        public CommandFlagParser.@NonNull FailureReason failureReason() {
            return this.failureReason;
        }
    }


    /**
     * Helper class to parse the command input queue into flags
     * and flag values. On failure the intermediate results
     * can be obtained, which are used for providing suggestions.
     */
    private final class FlagParser {

        private String lastParsedFlag;

        @SuppressWarnings({"unchecked", "rawtypes"})
        private @NonNull CompletableFuture<@NonNull ArgumentParseResult<Object>> parse(
                final @NonNull CommandContext<@NonNull C> commandContext,
                final @NonNull CommandInput commandInput
        ) {
            CompletableFuture<ArgumentParseResult<Object>> result = CompletableFuture.completedFuture(null);
            final Set<CommandFlag<?>> parsedFlags = commandContext.computeIfAbsent(PARSED_FLAGS, k -> new HashSet());

            final int remainingTokens = commandInput.remainingTokens();
            for (int i = 0; i <= remainingTokens; i++) {
                result = result.thenCompose(parseResult -> {
                    // The previous flag might have left us with trailing whitespace. We remove it so that we
                    // do not have to account for it throughout the parsing process.
                    commandInput.skipWhitespace();

                    if (parseResult != null || commandInput.isEmpty()) {
                        return CompletableFuture.completedFuture(parseResult);
                    }

                    final String string = commandInput.peekString();

                    if (!string.startsWith("-")) {
                        // If we're not starting a new flag then we're outside the scope of this parser. We exit.
                        return CompletableFuture.completedFuture(ArgumentParseResult.success(FLAG_PARSE_RESULT_OBJECT));
                    }

                    // We're definitely not supplying anything to the flag.
                    this.lastParsedFlag = null;

                    // We figure out which flag we're dealing with.
                    if (string.startsWith("--")) {
                        commandInput.moveCursor(2);
                    } else {
                        commandInput.moveCursor(1);
                    }

                    final String flagName = commandInput.readStringSkipWhitespace();
                    CommandFlag<?> flag = null;

                    if (string.startsWith("--")) {
                        for (final CommandFlag<?> flagCandidate : CommandFlagParser.this.flags) {
                            if (flagName.equalsIgnoreCase(flagCandidate.name())) {
                                flag = flagCandidate;
                                break;
                            }
                        }
                    } else if (flagName.length() == 1) {
                        outer:
                        for (final CommandFlag<?> flagCandidate : CommandFlagParser.this.flags) {
                            for (final String alias : flagCandidate.aliases()) {
                                if (alias.equalsIgnoreCase(flagName)) {
                                    flag = flagCandidate;
                                    break outer;
                                }
                            }
                        }
                    } else {
                        boolean flagFound = false;
                        for (int j = 0; j < flagName.length(); j++) {
                            final String parsedFlag = Character.toString(flagName.charAt(j)).toLowerCase(Locale.ENGLISH);
                            for (final CommandFlag<?> candidateFlag : CommandFlagParser.this.flags) {
                                // Argument flags cannot use the shorthand form in this way.
                                if (candidateFlag.commandComponent() != null) {
                                    continue;
                                }

                                if (!candidateFlag.aliases().contains(parsedFlag)) {
                                    continue;
                                }

                                if (parsedFlags.contains(candidateFlag) && candidateFlag.mode() != CommandFlag.FlagMode.REPEATABLE) {
                                    return this.fail(
                                            new FlagParseException(
                                                    string,
                                                    FailureReason.DUPLICATE_FLAG,
                                                    commandContext
                                            )
                                    );
                                } else if (!commandContext.hasPermission(candidateFlag.permission())) {
                                    return this.fail(
                                            new FlagParseException(
                                                    string,
                                                    FailureReason.NO_PERMISSION,
                                                    commandContext
                                            )
                                    );
                                }

                                commandContext.flags().addPresenceFlag(candidateFlag);
                                parsedFlags.add(candidateFlag);
                                flagFound = true;
                            }
                        }

                        if (!flagFound) {
                            return this.fail(
                                    new FlagParseException(
                                            string,
                                            FailureReason.NO_FLAG_STARTED,
                                            commandContext
                                    )
                            );
                        }

                        // This type of flag can never have a value. We move on to the next flag.
                        return CompletableFuture.completedFuture(null);
                    }

                    if (flag == null) {
                        return this.fail(
                                new FlagParseException(
                                        string,
                                        FailureReason.UNKNOWN_FLAG,
                                        commandContext
                                )
                        );
                    } else if (parsedFlags.contains(flag) && flag.mode() != CommandFlag.FlagMode.REPEATABLE) {
                        return this.fail(
                                new FlagParseException(
                                        string,
                                        FailureReason.DUPLICATE_FLAG,
                                        commandContext
                                )
                        );
                    } else if (!commandContext.hasPermission(flag.permission())) {
                        return this.fail(
                                new FlagParseException(
                                        string,
                                        FailureReason.NO_PERMISSION,
                                        commandContext
                                )
                        );
                    }

                    // The flag has no argument, so we're done.
                    if (flag.commandComponent() == null) {
                        commandContext.flags().addPresenceFlag(flag);
                        parsedFlags.add(flag);
                        return CompletableFuture.completedFuture(null);
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
                        return this.fail(
                                new FlagParseException(
                                        flag.name(),
                                        FailureReason.MISSING_ARGUMENT,
                                        commandContext
                                )
                        );
                    }

                    // Indicate that we parsed the flag and that we're trying to populate the value for it.
                    this.lastParsedFlag = string;

                    // We then attempt to parse the flag.
                    final CommandFlag parsingFlag = flag;
                    return ((CommandComponent<C>) flag.commandComponent())
                            .parser()
                            .parseFuture(
                                    commandContext,
                                    commandInput
                            ).thenApply(parsedValue -> {
                                // Forward parsing errors.
                                if (parsedValue.failure().isPresent()) {
                                    return (ArgumentParseResult<Object>) parsedValue;
                                }

                                // We store the parsed flag in the context. We do ugly erasure here because generics :)
                                commandContext.flags().addValueFlag(parsingFlag, (Object) parsedValue.parsedValue().get());
                                // At this point we know the flag parsed successfully.
                                parsedFlags.add(parsingFlag);

                                // We're no longer parsing a flag.
                                this.lastParsedFlag = null;

                                return null;
                            });
                });
            }

            // We've consumed everything!
            return result.thenApply(r -> r == null ? ArgumentParseResult.success(FLAG_PARSE_RESULT_OBJECT) : r);
        }

        private @Nullable String lastParsedFlag() {
            return this.lastParsedFlag;
        }

        private @NonNull CompletableFuture<ArgumentParseResult<Object>> fail(final @NonNull Throwable exception) {
            return CompletableFuture.completedFuture(ArgumentParseResult.failure(exception));
        }
    }
}
