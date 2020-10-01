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
import cloud.commandframework.context.CommandContext;
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
public class FlagArgument<C> extends CommandArgument<C, Object> {

    /**
     * Dummy object that indicates that flags were parsed successfully
     */
    public static final Object FLAG_PARSE_RESULT_OBJECT = new Object();
    /**
     * Meta data for the last argument that was suggested
     */
    public static final String FLAG_META = "__last_flag__";

    private static final String FLAG_ARGUMENT_NAME = "flags";

    /**
     * Construct a new flag argument
     *
     * @param flags Flags
     */
    public FlagArgument(final Collection<CommandFlag<?>> flags) {
        super(false,
              FLAG_ARGUMENT_NAME,
              new FlagArgumentParser<>(flags.toArray(new CommandFlag<?>[0])),
              Object.class);
    }

    public static final class FlagArgumentParser<C> implements ArgumentParser<C, Object> {

        private final CommandFlag<?>[] flags;

        private FlagArgumentParser(@NonNull final CommandFlag<?>[] flags) {
            this.flags = flags;
        }

        @Override
        public @NonNull ArgumentParseResult<@NonNull Object> parse(@NonNull final CommandContext<@NonNull C> commandContext,
                                                                   @NonNull final Queue<@NonNull String> inputQueue) {
            /*
            This argument must necessarily be the last so we can just consume all remaining input. This argument type
            is similar to a greedy string in that sense. But, we need to keep all flag logic contained to the parser
             */
            final Set<CommandFlag<?>> parsedFlags = new HashSet<>();
            CommandFlag<?> currentFlag = null;

            for (@NonNull final String string : inputQueue) {
                if (string.startsWith("-")) {
                    if (currentFlag != null && currentFlag.getCommandArgument() != null) {
                        return ArgumentParseResult.failure(
                                new IllegalArgumentException(String.format("Missing argument for '%s'", currentFlag.getName())));
                    }
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
                        return ArgumentParseResult.failure(
                                new IllegalArgumentException(String.format("Unknown flag '%s'", string)));
                    } else if (parsedFlags.contains(currentFlag)) {
                        return ArgumentParseResult.failure(
                                new IllegalArgumentException(String.format("Duplicate flag '%s'", string)));
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
                        return ArgumentParseResult.failure(
                                new IllegalArgumentException(String.format("No flag started. Don't"
                                                                                   + " know what to do with '%s'", string)));
                    } else {
                        final ArgumentParseResult<?> result =
                                ((CommandArgument) currentFlag.getCommandArgument())
                                        .getParser()
                                        .parse(commandContext,
                                               new LinkedList<>(Collections.singletonList(string)));
                        if (result.getFailure().isPresent()) {
                            return ArgumentParseResult.failure(result.getFailure().get());
                        } else {
                            final CommandFlag erasedFlag = currentFlag;
                            final Object value = result.getParsedValue().get();
                            commandContext.flags().addValueFlag(erasedFlag, value);
                        }
                    }
                }
            }
            /* We've consumed everything */
            inputQueue.clear();
            return ArgumentParseResult.success(FLAG_PARSE_RESULT_OBJECT);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(final @NonNull CommandContext<C> commandContext,
                                                          final @NonNull String input) {
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

}
