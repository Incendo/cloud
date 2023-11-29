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
package cloud.commandframework.arguments.parser;

import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.util.Objects.requireNonNull;

/**
 * Parser that parses strings into values of a specific type
 *
 * @param <C> Command sender type
 * @param <T> Value type
 */
@FunctionalInterface
@API(status = API.Status.STABLE)
public interface ArgumentParser<C, T> extends SuggestionProvider<C> {

    /**
     * Default amount of arguments that the parser expects to consume
     */
    int DEFAULT_ARGUMENT_COUNT = 1;

    /**
     * Parse command input into a command result.
     * <p>
     * This method may be called when a command chain is being parsed for execution
     * (using {@link cloud.commandframework.CommandManager#executeCommand(Object, String)})
     * or when a command is being parsed to provide context for suggestions
     * (using {@link cloud.commandframework.CommandManager#suggest(Object, String)}). It is
     * possible to use {@link CommandContext#isSuggestions()}} to see what the purpose of the
     * parsing is. Particular care should be taken when parsing for suggestions, as the parsing
     * method is then likely to be called once for every character written by the command sender.
     * <p>
     * This method should never throw any exceptions under normal circumstances. Instead, if the
     * parsing for some reason cannot be done successfully {@link ArgumentParseResult#failure(Throwable)}
     * should be returned. This then wraps any exception that should be forwarded to the command sender.
     * <p>
     * The parser is assumed to be completely stateless and should not store any information about
     * the command sender or the command context. Instead, information should be stored in the
     * {@link CommandContext}.
     *
     * @param commandContext Command context
     * @param commandInput   Command Input
     * @return Parsed command result
     */
    @NonNull ArgumentParseResult<@NonNull T> parse(
            @NonNull CommandContext<@NonNull C> commandContext,
            @NonNull CommandInput commandInput
    );

    /**
     * Returns a future that completes with the result of parsing the given {@code commandInput}.
     * <p>
     * This method may be called when a command chain is being parsed for execution
     * (using {@link cloud.commandframework.CommandManager#executeCommand(Object, String)})
     * or when a command is being parsed to provide context for suggestions
     * (using {@link cloud.commandframework.CommandManager#suggest(Object, String)}). It is
     * possible to use {@link CommandContext#isSuggestions()}} to see what the purpose of the
     * parsing is. Particular care should be taken when parsing for suggestions, as the parsing
     * method is then likely to be called once for every character written by the command sender.
     * <p>
     * This method should never throw any exceptions under normal circumstances. Instead, if the
     * parsing for some reason cannot be done successfully {@link ArgumentParseResult#failure(Throwable)}
     * should be returned. This then wraps any exception that should be forwarded to the command sender.
     * <p>
     * The parser is assumed to be completely stateless and should not store any information about
     * the command sender or the command context. Instead, information should be stored in the
     * {@link CommandContext}.
     *
     * @param commandContext Command context
     * @param commandInput   Command Input
     * @return future that completes with the result.
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    default @NonNull CompletableFuture<@NonNull T> parseFuture(
            @NonNull CommandContext<@NonNull C> commandContext,
            @NonNull CommandInput commandInput
    ) {
        return this.parse(commandContext, commandInput).asFuture();
    }

    /**
     * Returns a list of suggested arguments that would be correctly parsed by this parser
     * <p>
     * This method is likely to be called for every character provided by the sender and
     * so it may be necessary to cache results locally to prevent unnecessary computations
     *
     * @param commandContext Command context
     * @param input          Input string
     * @return List of suggestions
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    default @NonNull List<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull String input
    ) {
        return Collections.emptyList();
    }

    /**
     * Returns a list of suggested arguments that would be correctly parsed by this parser
     * <p>
     * This method is likely to be called for every character provided by the sender and
     * so it may be necessary to cache results locally to prevent unnecessary computations
     *
     * @param commandContext Command context
     * @param input          Input string
     * @return List of suggestions
     * @since 2.0.0
     */
    @Override
    @SuppressWarnings("FunctionalInterfaceMethodChanged")
    @API(status = API.Status.STABLE, since = "2.0.0")
    default @NonNull List<@NonNull Suggestion> suggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull String input
    ) {
        return this.stringSuggestions(commandContext, input).stream().map(Suggestion::simple).collect(Collectors.toList());
    }

    /**
     * Create a derived argument parser preserving all properties of this parser, but converting the output type.
     *
     * @param mapper the mapper to apply
     * @param <O>    the result type
     * @return a derived parser.
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "1.5.0")
    default <O> @NonNull ArgumentParser<C, O> map(final MappedArgumentParser.Mapper<C, T, O> mapper) {
        return new MappedArgumentParser<>(this, requireNonNull(mapper, "mapper"));
    }

    /**
     * Check whether this argument parser is context free. A context free
     * parser will not use the provided command context, and so supports impromptu parsing
     *
     * @return {@code true} if the parser is context free, else {@code false}
     */
    default boolean isContextFree() {
        return false;
    }

    /**
     * Get the amount of arguments that this parsers seeks to
     * consume
     *
     * @return The number of arguments tha the parser expects
     * @since 1.1.0
     */
    @API(status = API.Status.STABLE, since = "1.1.0")
    default int getRequestedArgumentCount() {
        return DEFAULT_ARGUMENT_COUNT;
    }


    /**
     * Utility interface extending {@link ArgumentParser} to make it easier to implement
     * {@link #parseFuture(CommandContext, CommandInput)}.
     *
     * @param <C> the command sender type
     * @param <T> the type produced by the parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    interface FutureArgumentParser<C, T> extends ArgumentParser<C, T> {

        @Override
        default @NonNull ArgumentParseResult<@NonNull T> parse(
                @NonNull CommandContext<@NonNull C> commandContext,
                @NonNull CommandInput commandInput
        ) {
            try {
                return ArgumentParseResult.mapFuture(this.parseFuture(commandContext, commandInput)).join();
            } catch (final CompletionException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                throw exception;
            }
        }

        @Override
        @NonNull
        CompletableFuture<@NonNull T> parseFuture(
                @NonNull CommandContext<@NonNull C> commandContext,
                @NonNull CommandInput commandInput
        );

        @Override
        default @NonNull List<@NonNull Suggestion> suggestions(
                @NonNull CommandContext<C> context,
                @NonNull String input
        ) {
            try {
                return this.suggestionsFuture(context, input).join();
            } catch (final CompletionException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                throw exception;
            }
        }

        @Override
        @NonNull
        default CompletableFuture<@NonNull List<@NonNull Suggestion>> suggestionsFuture(
                @NonNull CommandContext<C> context,
                @NonNull String input
        ) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }
}
