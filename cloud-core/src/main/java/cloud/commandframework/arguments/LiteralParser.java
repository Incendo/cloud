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
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class LiteralParser<C> implements ArgumentParser<C, String>, BlockingSuggestionProvider.Strings<C> {

    /**
     * Creates a new literal parser that accepts the given {@code name} and {@code aliases}.
     *
     * @param name    the literal name
     * @param aliases the aliases
     * @param <C>     the command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, String> literal(
            final @NonNull String name,
            final @NonNull String @NonNull... aliases
    ) {
        return ParserDescriptor.of(new LiteralParser<>(name, aliases), String.class);
    }

    private final Set<String> allAcceptedAliases = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private final Set<String> alternativeAliases = new HashSet<>();

    private final String name;

    private LiteralParser(final @NonNull String name, final @NonNull String... aliases) {
        this.name = name;
        this.allAcceptedAliases.add(this.name);
        this.allAcceptedAliases.addAll(Arrays.asList(aliases));
        this.alternativeAliases.addAll(Arrays.asList(aliases));
    }

    @Override
    public @NonNull ArgumentParseResult<String> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String string = commandInput.peekString();
        if (string.isEmpty()) {
            return ArgumentParseResult.failure(new NoInputProvidedException(
                    LiteralParser.class,
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
    public @NonNull Iterable<@NonNull String> stringSuggestions(
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
