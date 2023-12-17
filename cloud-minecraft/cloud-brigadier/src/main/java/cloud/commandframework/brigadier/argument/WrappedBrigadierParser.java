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
package cloud.commandframework.brigadier.argument;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * A wrapped argument parser that can expose Brigadier argument types to the Cloud world.
 *
 * @param <C> the sender type
 * @param <T> the value type of the argument
 * @since 1.5.0
 */
public class WrappedBrigadierParser<C, T> implements ArgumentParser<C, T>, BlockingSuggestionProvider.Strings<C> {

    public static final String COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER = "_cloud_brigadier_native_sender";

    private final Supplier<ArgumentType<T>> nativeType;
    private final int expectedArgumentCount;
    private final @Nullable ParseFunction<T> parse;

    /**
     * Create an argument parser based on a brigadier command.
     *
     * @param nativeType the native command type
     * @since 1.5.0
     */
    public WrappedBrigadierParser(final ArgumentType<T> nativeType) {
        this(() -> nativeType, DEFAULT_ARGUMENT_COUNT);
    }

    /**
     * Create an argument parser based on a brigadier command.
     *
     * @param nativeType the native command type, computed lazily
     * @since 1.7.0
     */
    public WrappedBrigadierParser(final Supplier<ArgumentType<T>> nativeType) {
        this(nativeType, DEFAULT_ARGUMENT_COUNT);
    }

    /**
     * Create an argument parser based on a brigadier command.
     *
     * @param nativeType            the native command type
     * @param expectedArgumentCount the number of arguments the brigadier type is expected to consume
     * @since 1.5.0
     */
    public WrappedBrigadierParser(
            final ArgumentType<T> nativeType,
            final int expectedArgumentCount
    ) {
        this(() -> nativeType, expectedArgumentCount);
    }

    /**
     * Create an argument parser based on a brigadier command.
     *
     * @param nativeType            the native command type provider, calculated lazily
     * @param expectedArgumentCount the number of arguments the brigadier type is expected to consume
     * @since 1.7.0
     */
    public WrappedBrigadierParser(
            final Supplier<ArgumentType<T>> nativeType,
            final int expectedArgumentCount
    ) {
        this(nativeType, expectedArgumentCount, null);
    }

    /**
     * Create an argument parser based on a brigadier command.
     *
     * @param nativeType            the native command type provider, calculated lazily
     * @param expectedArgumentCount the number of arguments the brigadier type is expected to consume
     * @param parse                 special function to replace {@link ArgumentType#parse(StringReader)} (for CraftBukkit weirdness)
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    public WrappedBrigadierParser(
            final Supplier<ArgumentType<T>> nativeType,
            final int expectedArgumentCount,
            final @Nullable ParseFunction<T> parse
    ) {
        requireNonNull(nativeType, "brigadierType");
        this.nativeType = nativeType;
        this.expectedArgumentCount = expectedArgumentCount;
        this.parse = parse;
    }

    /**
     * Get the backing Brigadier {@link ArgumentType} for this parser.
     *
     * @return the argument type
     * @since 1.5.0
     */
    public final ArgumentType<T> getNativeArgument() {
        return this.nativeType.get();
    }

    @Override
    public final @NonNull ArgumentParseResult<@NonNull T> parse(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        // Convert to a brig reader
        final StringReader reader = CloudStringReader.of(commandInput);

        // Then try to parse
        try {
            final T result = this.parse != null
                    ? this.parse.apply(this.nativeType.get(), reader)
                    : this.nativeType.get().parse(reader);
            // Brigadier doesn't automatically do this, whereas Cloud does.
            commandInput.skipWhitespace();
            return ArgumentParseResult.success(result);
        } catch (final CommandSyntaxException ex) {
            return ArgumentParseResult.failure(ex);
        }
    }

    @Override
    public final @NonNull List<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull String input
    ) {
        /*
         * Strictly, this is incorrect.
         * However, it seems that all Mojang really does with the context passed here
         * is use it to query data on the native sender. Hopefully this hack holds up.
         */
        final com.mojang.brigadier.context.CommandContext<Object> reverseMappedContext = new com.mojang.brigadier.context.CommandContext<>(
                commandContext.getOrDefault(COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER, commandContext.sender()),
                commandContext.rawInput().input(),
                Collections.emptyMap(),
                null,
                null,
                Collections.emptyList(),
                StringRange.at(0),
                null,
                null,
                false
        );

        final CompletableFuture<Suggestions> result = this.nativeType.get().listSuggestions(
                reverseMappedContext,
                new SuggestionsBuilder(input, 0)
        );

        /* again, avert your eyes */
        final List<Suggestion> suggestions = result.join().getList();
        final List<String> out = new ArrayList<>(suggestions.size());
        for (final Suggestion suggestion : suggestions) {
            out.add(suggestion.getText());
        }
        return out;
    }

    @Override
    public final int getRequestedArgumentCount() {
        return this.expectedArgumentCount;
    }

    /**
     * Function which can call {@link ArgumentType#parse(StringReader)} or another method.
     *
     * @param <T> result type
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    @FunctionalInterface
    public interface ParseFunction<T> {

        /**
         * Apply the parse function.
         *
         * @param type   argument type
         * @param reader string reader
         * @return result
         * @throws CommandSyntaxException on failure
         */
        T apply(ArgumentType<T> type, StringReader reader) throws CommandSyntaxException;
    }
}
