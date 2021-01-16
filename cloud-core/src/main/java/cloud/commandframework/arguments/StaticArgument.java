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
package cloud.commandframework.arguments;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * {@link CommandArgument} type that recognizes fixed strings. This type does not parse variables.
 *
 * @param <C> Command sender type
 */
public final class StaticArgument<C> extends CommandArgument<C, String> {

    private StaticArgument(final boolean required, final @NonNull String name, final @NonNull String... aliases) {
        super(required, name, new StaticArgumentParser<>(name, aliases), String.class);
    }

    /**
     * Create a new static argument instance for a required command argument
     *
     * @param name    Argument name
     * @param aliases Argument aliases
     * @param <C>     Command sender type
     * @return Constructed argument
     */
    public static <C> @NonNull StaticArgument<C> of(
            final @NonNull String name,
            final @NonNull String... aliases
    ) {
        return new StaticArgument<>(true, name, aliases);
    }

    /**
     * Register a new alias
     *
     * @param alias New alias
     */
    public void registerAlias(final @NonNull String alias) {
        ((StaticArgumentParser<C>) this.getParser()).insertAlias(alias);
    }

    /**
     * Get an immutable view of the aliases
     *
     * @return Immutable view of the argument aliases
     */
    public @NonNull Set<@NonNull String> getAliases() {
        return Collections.unmodifiableSet(((StaticArgumentParser<C>) this.getParser()).getAcceptedStrings());
    }

    /**
     * Get an immutable list of all aliases that are not the main literal
     *
     * @return Immutable view of the optional argument aliases
     */
    public @NonNull List<@NonNull String> getAlternativeAliases() {
        return Collections.unmodifiableList(new ArrayList<>(((StaticArgumentParser<C>) this.getParser()).alternativeAliases));
    }


    private static final class StaticArgumentParser<C> implements ArgumentParser<C, String> {

        private final Set<String> allAcceptedAliases = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        private final Set<String> alternativeAliases = new HashSet<>();

        private final String name;

        private StaticArgumentParser(final @NonNull String name, final @NonNull String... aliases) {
            this.name = name;
            this.allAcceptedAliases.add(this.name);
            this.allAcceptedAliases.addAll(Arrays.asList(aliases));
            this.alternativeAliases.addAll(Arrays.asList(aliases));
        }

        @Override
        public @NonNull ArgumentParseResult<String> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String string = inputQueue.peek();
            if (string == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        StaticArgumentParser.class,
                        commandContext
                ));
            }
            if (this.allAcceptedAliases.contains(string)) {
                inputQueue.remove();
                return ArgumentParseResult.success(this.name);
            }
            return ArgumentParseResult.failure(new IllegalArgumentException(string));
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return Collections.singletonList(this.name);
        }

        /**
         * Get the accepted strings
         *
         * @return Accepted strings
         */
        public @NonNull Set<@NonNull String> getAcceptedStrings() {
            return this.allAcceptedAliases;
        }

        /**
         * Insert a new alias
         *
         * @param alias New alias
         */
        public void insertAlias(final @NonNull String alias) {
            this.allAcceptedAliases.add(alias);
            this.alternativeAliases.add(alias);
        }

    }

}
