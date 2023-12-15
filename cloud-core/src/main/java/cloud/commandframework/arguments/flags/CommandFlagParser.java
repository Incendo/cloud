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
package cloud.commandframework.arguments.flags;

import cloud.commandframework.CommandComponent;
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

@API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*", since = "2.0.0")
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
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull Collection<@NonNull CommandFlag<?>> flags() {
        return Collections.unmodifiableCollection(this.flags);
    }

    @Override
    public @NonNull CompletableFuture<@NonNull Object> parseFuture(
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
    public @NonNull CompletableFuture<List<@NonNull Suggestion>> suggestionsFuture(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull String input
    ) {
        /* Check if we have a last flag stored */
        final String lastArg = Objects.requireNonNull(commandContext.getOrDefault(FLAG_META_KEY, ""));
        if (!lastArg.startsWith("-")) {
            final String rawInput = commandContext.rawInput().input();
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
                    if (suggestCombined && flag.commandComponent() == null) {
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
            return CompletableFuture.completedFuture(suggestions);
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
                outer:
                for (final CommandFlag<?> flag : this.flags) {
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
         * Get the caption used for this failure reason
         *
         * @return The caption
         */
        public @NonNull Caption getCaption() {
            return this.caption;
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
                final CommandFlagParser.@NonNull FailureReason failureReason,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    CommandFlagParser.class,
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
        public CommandFlagParser.@NonNull FailureReason failureReason() {
            return this.failureReason;
        }
    }


    /**
     * Helper class to parse the command input queue into flags
     * and flag values. On failure the intermediate results
     * can be obtained, which are used for providing suggestions.
     */
    private class FlagParser {

        private String lastParsedFlag;

        @SuppressWarnings({"unchecked", "rawtypes"})
        private @NonNull CompletableFuture<@NonNull Object> parse(
                final @NonNull CommandContext<@NonNull C> commandContext,
                final @NonNull CommandInput commandInput
        ) {
            CompletableFuture<Boolean> result = CompletableFuture.completedFuture(false);
            final Set<CommandFlag<?>> parsedFlags = commandContext.computeIfAbsent(PARSED_FLAGS, k -> new HashSet());

            final int remainingTokens = commandInput.remainingTokens();
            for (int i = 0; i <= remainingTokens; i++) {
                result = result.thenCompose(done -> {
                    if (done || commandInput.isEmpty()) {
                        return CompletableFuture.completedFuture(true);
                    }

                    final String string = commandInput.peekString();

                    if (!string.startsWith("-")) {
                        // If we're not starting a new flag then we're outside the scope of this parser. We exit.
                        return CompletableFuture.completedFuture(true);
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
                        for (final CommandFlag<?> flagCandidate : CommandFlagParser.this.flags) {
                            if (flagName.equalsIgnoreCase(flagCandidate.getName())) {
                                flag = flagCandidate;
                                break;
                            }
                        }
                    } else if (flagName.length() == 1) {
                        outer:
                        for (final CommandFlag<?> flagCandidate : CommandFlagParser.this.flags) {
                            for (final String alias : flagCandidate.getAliases()) {
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

                                if (!candidateFlag.getAliases().contains(parsedFlag)) {
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
                        return CompletableFuture.completedFuture(false);
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
                        return CompletableFuture.completedFuture(false);
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
                                        flag.getName(),
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
                                // We store the parsed flag in the context. We do ugly erasure here because generics :)
                                commandContext.flags().addValueFlag(parsingFlag, (Object) parsedValue);
                                // At this point we know the flag parsed successfully.
                                parsedFlags.add(parsingFlag);

                                // We're no longer parsing a flag.
                                this.lastParsedFlag = null;

                                return false;
                            });
                });
            }

            // We've consumed everything!
            return result.thenApply(v -> FLAG_PARSE_RESULT_OBJECT);
        }

        private @Nullable String lastParsedFlag() {
            return this.lastParsedFlag;
        }

        private @NonNull CompletableFuture<Boolean> fail(final @NonNull Throwable exception) {
            final CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(exception);
            return future;
        }
    }
}
