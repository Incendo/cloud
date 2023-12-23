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

import cloud.commandframework.arguments.suggestion.SuggestionFactory;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.arguments.suggestion.SuggestionProviderHolder;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
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
public interface ArgumentParser<C, T> extends SuggestionProviderHolder<C> {

    /**
     * Default amount of arguments that the parser expects to consume
     */
    int DEFAULT_ARGUMENT_COUNT = 1;

    /**
     * Parse command input into a command result.
     * <p>
     * This method may be called when a command chain is being parsed for execution
     * (using {@link cloud.commandframework.CommandExecutor#executeCommand(Object, String)})
     * or when a command is being parsed to provide context for suggestions
     * (using {@link SuggestionFactory#suggest(Object, String)}).
     * It is possible to use {@link CommandContext#isSuggestions()}} to see what the purpose of the
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
     * (using {@link cloud.commandframework.CommandExecutor#executeCommand(Object, String)})
     * or when a command is being parsed to provide context for suggestions
     * (using {@link SuggestionFactory#suggest(Object, String)}).
     * It is possible to use {@link CommandContext#isSuggestions()}} to see what the purpose of the
     * parsing is. Particular care should be taken when parsing for suggestions, as the parsing
     * method is then likely to be called once for every character written by the command sender.
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
    default @NonNull CompletableFuture<@NonNull ArgumentParseResult<T>> parseFuture(
            @NonNull CommandContext<@NonNull C> commandContext,
            @NonNull CommandInput commandInput
    ) {
        return CompletableFuture.completedFuture(this.parse(commandContext, commandInput));
    }

    /**
     * Create a derived argument parser preserving all properties of this parser, but converting the output type.
     *
     * @param mapper the mapper to apply
     * @param <O>    the result type
     * @return a derived parser.
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    default <O> ArgumentParser.@NonNull FutureArgumentParser<C, O> flatMap(final MappedArgumentParser.Mapper<C, T, O> mapper) {
        return new MappedArgumentParser<>(this, requireNonNull(mapper, "mapper"));
    }

    /**
     * Create a parser that passes through failures and flat maps
     * successfully parsed values with {@code mapper}.
     *
     * @param mapper success mapper
     * @param <O>    mapped parser value type
     * @return mapped parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    default <O> @NonNull ArgumentParser<C, O> flatMapSuccess(
            final @NonNull BiFunction<CommandContext<C>, T, CompletableFuture<ArgumentParseResult<O>>> mapper
    ) {
        requireNonNull(mapper, "mapper");
        return this.flatMap((ctx, orig) -> {
            if (orig.getFailure().isPresent()) {
                return ArgumentParseResult.failureFuture(orig.getFailure().get());
            }
            return mapper.apply(ctx, orig.getParsedValue().get());
        });
    }

    /**
     * Create a parser that passes through failures and maps
     * successfully parsed values with {@code mapper}.
     *
     * @param mapper success mapper
     * @param <O>    mapped parser value type
     * @return mapped parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    default <O> @NonNull ArgumentParser<C, O> mapSuccess(
            final @NonNull BiFunction<CommandContext<C>, T, CompletableFuture<O>> mapper
    ) {
        requireNonNull(mapper, "mapper");
        return this.flatMapSuccess((ctx, orig) -> mapper.apply(ctx, orig).thenApply(ArgumentParseResult::success));
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
     * Returns the suggestion provider.
     * <p>
     * By default, this will return the parser, if the parser is also a {@link SuggestionProvider}.
     * Otherwise, {@link SuggestionProvider#noSuggestions()} will be returned.
     *
     * @return the suggestion provider
     */
    @SuppressWarnings("unchecked")
    @Override
    default @NonNull SuggestionProvider<C> suggestionProvider() {
        if (this instanceof SuggestionProvider) {
            return (SuggestionProvider<C>) this;
        }
        return SuggestionProvider.noSuggestions();
    }


    /**
     * Utility interface extending {@link ArgumentParser} to make it easier to implement
     * {@link #parseFuture(CommandContext, CommandInput)}.
     *
     * @param <C> the command sender type
     * @param <T> the type produced by the parser
     * @since 2.0.0
     */
    @FunctionalInterface
    @API(status = API.Status.STABLE, since = "2.0.0")
    interface FutureArgumentParser<C, T> extends ArgumentParser<C, T> {

        @Override
        default @NonNull ArgumentParseResult<@NonNull T> parse(
                @NonNull CommandContext<@NonNull C> commandContext,
                @NonNull CommandInput commandInput
        ) {
            throw new UnsupportedOperationException(
                    "parse should not be called on a FutureArgumentParser. Call parseFuture instead.");
        }

        @Override
        @NonNull CompletableFuture<@NonNull ArgumentParseResult<T>> parseFuture(
                @NonNull CommandContext<@NonNull C> commandContext,
                @NonNull CommandInput commandInput
        );
    }
}
