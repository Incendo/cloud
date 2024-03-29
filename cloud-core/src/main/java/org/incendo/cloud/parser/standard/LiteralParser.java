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
package org.incendo.cloud.parser.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

public final class LiteralParser<C> implements ArgumentParser<C, String>, BlockingSuggestionProvider.Strings<C> {

    /**
     * Creates a new literal parser that accepts the given {@code name} and {@code aliases}.
     *
     * @param name    the literal name
     * @param aliases the aliases
     * @param <C>     the command sender type
     * @return the created parser
     */
    @API(status = API.Status.STABLE)
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
        validateNames(name, aliases);
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
        if (this.allAcceptedAliases.contains(string)) {
            commandInput.readString();
            return ArgumentParseResult.success(this.name);
        }
        return ArgumentParseResult.failure(new IllegalArgumentException(string));
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(final @NonNull CommandContext<C> commandContext,
                                                                final @NonNull CommandInput input) {
        return Collections.singletonList(this.name);
    }

    /**
     * Returns the aliases, if relevant.
     * <p>
     * Only literal components may have aliases. If this is a non-literal
     * component then an empty collection is returned.
     *
     * @return unmodifiable view of the aliases
     */
    @API(status = API.Status.STABLE)
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
     */
    @API(status = API.Status.STABLE)
    public @NonNull Collection<@NonNull String> alternativeAliases() {
        return Collections.unmodifiableCollection(this.alternativeAliases);
    }

    /**
     * Insert a new alias
     *
     * @param alias New alias
     */
    public void insertAlias(final @NonNull String alias) {
        validateNames("valid", new String[]{alias});
        this.allAcceptedAliases.add(alias);
        this.alternativeAliases.add(alias);
    }

    private static void validateNames(final String name, final @NonNull String[] aliases) {
        @Nullable List<String> errors = null;
        errors = validateName(name, false, errors);
        for (final String alias : aliases) {
            errors = validateName(alias, true, errors);
        }
        if (errors != null && !errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }
    }

    @SuppressWarnings("checkstyle:FinalParameters")
    private static @Nullable List<String> validateName(
            final @NonNull String name,
            final boolean alias,
            @Nullable List<String> errors
    ) {
        final int found = name.codePoints().filter(Character::isWhitespace).findFirst().orElse(Integer.MIN_VALUE);
        if (found != Integer.MIN_VALUE) {
            if (errors == null) {
                errors = new ArrayList<>();
            }
            errors.add(String.format("%s '%s' is invalid: contains whitespace", alias ? "Alias" : "Name", name));
        }
        return errors;
    }
}
