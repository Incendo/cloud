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
package cloud.commandframework.arguments;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * {@link CommandArgument} type that recognizes fixed strings. This type does not parse variables.
 *
 * @param <C> Command sender type
 */
@API(status = API.Status.STABLE)
public final class StaticArgument<C> extends CommandArgument<C, String> {

    private StaticArgument(final @NonNull String name, final @NonNull String... aliases) {
        super(name, new StaticArgumentParser<>(name, aliases), String.class);
    }

    private StaticArgument(final @NonNull String name, final @NonNull Collection<@NonNull String> aliases) {
        super(name, new StaticArgumentParser<>(name, aliases), String.class);
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
        return new StaticArgument<>(name, aliases);
    }

    @Override
    public @NonNull CommandArgument<C, String> copy() {
        return new StaticArgument<>(this.getName(), this.getParser().alternativeAliases());
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull StaticArgumentParser<C> getParser() {
        return (StaticArgumentParser<C>) super.getParser();
    }

    public static final class StaticArgumentParser<C> implements ArgumentParser<C, String> {

        private final Set<String> allAcceptedAliases = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        private final Set<String> alternativeAliases = new HashSet<>();

        private final String name;

        private StaticArgumentParser(final @NonNull String name, final @NonNull String... aliases) {
            this.name = name;
            this.allAcceptedAliases.add(this.name);
            this.allAcceptedAliases.addAll(Arrays.asList(aliases));
            this.alternativeAliases.addAll(Arrays.asList(aliases));
        }

        private StaticArgumentParser(final @NonNull String name, final @NonNull Collection<String> aliases) {
            this.name = name;
            this.allAcceptedAliases.add(this.name);
            this.allAcceptedAliases.addAll(aliases);
            this.alternativeAliases.addAll(aliases);
        }

        @Override
        public @NonNull ArgumentParseResult<String> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull CommandInput commandInput
        ) {
            final String string = commandInput.peekString();
            if (string.isEmpty()) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        StaticArgumentParser.class,
                        commandContext
                ));
            }
            if (this.allAcceptedAliases.contains(string)) {
                commandInput.readString();
                return ArgumentParseResult.success(this.name);
            }
            return ArgumentParseResult.failure(new IllegalArgumentException(string));
        }

        @Override
        public @NonNull List<@NonNull String> stringSuggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return Collections.singletonList(this.name);
        }

        /**
         * Returns the aliases, if relevant.
         * <p>
         * Only literal components may have aliases. If this is a non-literal
         * component then an empty collection is returned.
         *
         * @return unmodifiable view of the aliases
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public @NonNull Collection<@NonNull String> aliases() {
            return Collections.unmodifiableCollection(this.allAcceptedAliases);
        }

        /**
         * Returns the aliases excluding the name, if relevant.
         * <p>
         * Only literal components may have aliases. If this is a non-literal
         * component then an empty collection is returned.
         *
         * @return unmodifiable view of the aliases
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public @NonNull Collection<@NonNull String> alternativeAliases() {
            return Collections.unmodifiableCollection(this.alternativeAliases);
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
