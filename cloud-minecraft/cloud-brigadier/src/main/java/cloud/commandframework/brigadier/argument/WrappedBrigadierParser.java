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
package cloud.commandframework.brigadier.argument;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
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
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.util.Objects.requireNonNull;

/**
 * A wrapped argument parser that can expose Brigadier argument types to the Cloud world.
 *
 * @param <C> the sender type
 * @param <T> the value type of the argument
 * @since 1.5.0
 */
public final class WrappedBrigadierParser<C, T> implements ArgumentParser<C, T> {

    public static final String COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER = "_cloud_brigadier_native_sender";

    private final Supplier<ArgumentType<T>> nativeType;
    private final int expectedArgumentCount;

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
        requireNonNull(nativeType, "brigadierType");
        this.nativeType = nativeType;
        this.expectedArgumentCount = expectedArgumentCount;
    }

    /**
     * Get the backing Brigadier {@link ArgumentType} for this parser.
     *
     * @return the argument type
     * @since 1.5.0
     */
    public ArgumentType<T> getNativeArgument() {
        return this.nativeType.get();
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull T> parse(
            @NonNull final CommandContext<@NonNull C> commandContext,
            @NonNull final Queue<@NonNull String> inputQueue
    ) {
        // Convert to a brig reader
        final StringReader reader;

        if (inputQueue instanceof StringReader) {
            reader = (StringReader) inputQueue;
        } else if (inputQueue instanceof StringReaderAsQueue) {
            reader = ((StringReaderAsQueue) inputQueue).getOriginal();
        } else {
            reader = new QueueAsStringReader(inputQueue);
        }

        // Then try to parse
        try {
            return ArgumentParseResult.success(this.nativeType.get().parse(reader));
        } catch (final CommandSyntaxException ex) {
            return ArgumentParseResult.failure(ex);
        } finally {
            if (reader instanceof QueueAsStringReader) {
                ((QueueAsStringReader) reader).updateQueue();
            }
        }
    }

    @Override
    public @NonNull List<@NonNull String> suggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull String input
    ) {
        /*
         * Strictly, this is incorrect.
         * However, it seems that all Mojang really does with the context passed here
         * is use it to query data on the native sender. Hopefully this hack holds up.
         */
        final com.mojang.brigadier.context.CommandContext<Object> reverseMappedContext = new com.mojang.brigadier.context.CommandContext<>(
                commandContext.getOrDefault(COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER, commandContext.getSender()),
                commandContext.getRawInputJoined(),
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
    public boolean isContextFree() {
        return true;
    }

    @Override
    public int getRequestedArgumentCount() {
        return this.expectedArgumentCount;
    }

}
