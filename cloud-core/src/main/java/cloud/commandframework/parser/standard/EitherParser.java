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
package cloud.commandframework.parser.standard;

import cloud.commandframework.caption.CaptionVariable;
import cloud.commandframework.caption.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exception.parsing.ParserException;
import cloud.commandframework.parser.ArgumentParseResult;
import cloud.commandframework.parser.ArgumentParser;
import cloud.commandframework.parser.ParserDescriptor;
import cloud.commandframework.suggestion.Suggestion;
import cloud.commandframework.suggestion.SuggestionProvider;
import cloud.commandframework.type.Either;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A parser which attempts to use the {@link #primary()} parser and falls back on the {@link #fallback()} parser if that fails.
 *
 * @param <C> command sender type
 * @param <U> primary value type
 * @param <V> fallback value type
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public final class EitherParser<C, U, V> implements ArgumentParser.FutureArgumentParser<C, Either<U, V>>, SuggestionProvider<C> {

    /**
     * Creates a new parser which attempts to use the {@code primary} parser and falls back on
     * the {@code fallback} parser if that fails.
     *
     * @param <C>      command sender type
     * @param <U>      primary value type
     * @param <V>      fallback value type
     * @param primary  primary parser
     * @param fallback fallback parser which gets invoked if the primary parser fails to parse the input
     * @return the descriptor of the parser
     */
    @SuppressWarnings("unchecked")
    public static <C, U, V> ParserDescriptor<C, Either<U, V>> eitherParser(
            final @NonNull ParserDescriptor<C, U> primary,
            final @NonNull ParserDescriptor<C, V> fallback
    ) {
        return ParserDescriptor.of(
                new EitherParser<>(primary, fallback),
                (TypeToken<Either<U, V>>) TypeToken.get(
                        TypeFactory.parameterizedClass(
                                Either.class,
                                primary.valueType().getType(),
                                fallback.valueType().getType()
                        )
                )
        );
    }

    private final ParserDescriptor<C, U> primary;
    private final ParserDescriptor<C, V> fallback;

    /**
     * Creates a new either parser.
     *
     * @param primary  primary parser
     * @param fallback fallback parser which gets invoked if the primary parser fails to parse the input
     */
    public EitherParser(final @NonNull ParserDescriptor<C, U> primary, final @NonNull ParserDescriptor<C, V> fallback) {
        this.primary = Objects.requireNonNull(primary, "primary");
        this.fallback = Objects.requireNonNull(fallback, "fallback");
    }

    /**
     * Returns the primary parser.
     *
     * @return the primary parser
     */
    public @NonNull ParserDescriptor<C, U> primary() {
        return this.primary;
    }

    /**
     * Returns the fallback parser.
     *
     * @return the fallback parser
     */
    public @NonNull ParserDescriptor<C, V> fallback() {
        return this.fallback;
    }

    @Override
    public @NonNull CompletableFuture<@NonNull ArgumentParseResult<Either<U, V>>> parseFuture(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.peekString();
        final int originalCursor = commandInput.cursor();

        return this.primary.parser().parseFuture(commandContext, commandInput).thenCompose(primaryResult -> {
            if (primaryResult.parsedValue().isPresent()) {
                return ArgumentParseResult.successFuture(Either.ofPrimary(primaryResult.parsedValue().get()));
            }

            // We need to restore the input if the first parser fails, so that the fallback parser gets access
            // to the unspoiled input.
            commandInput.cursor(originalCursor);

            return this.fallback.parser()
                    .parseFuture(commandContext, commandInput)
                    .thenApply(fallbackResult -> {
                        if (fallbackResult.parsedValue().isPresent()) {
                            return ArgumentParseResult.success(Either.ofFallback(fallbackResult.parsedValue().get()));
                        }
                        return ArgumentParseResult.failure(new EitherParseException(
                                primaryResult.failure().get(),
                                fallbackResult.failure().get(),
                                this.primary.valueType(),
                                this.fallback.valueType(),
                                commandContext,
                                input
                        ));
                    });
        });
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public @NonNull CompletableFuture<@NonNull Iterable<@NonNull Suggestion>> suggestionsFuture(
            final @NonNull CommandContext<C> context,
            final @NonNull CommandInput input
    ) {
        if (!(this.primary.parser() instanceof SuggestionProvider)) {
            if (!(this.fallback.parser() instanceof SuggestionProvider)) {
                return CompletableFuture.completedFuture(Collections.emptyList());
            }
            return ((SuggestionProvider<C>) this.fallback.parser()).suggestionsFuture(context, input);
        }
        if (!(this.fallback.parser() instanceof SuggestionProvider)) {
            return ((SuggestionProvider<C>) this.primary.parser()).suggestionsFuture(context, input);
        }
        final CompletableFuture<Iterable<Suggestion>>[] suggestionFutures = new CompletableFuture[] {
                ((SuggestionProvider<C>) this.primary.parser()).suggestionsFuture(context, input.copy()),
                ((SuggestionProvider<C>) this.fallback.parser()).suggestionsFuture(context, input)
        };
        return CompletableFuture.allOf(suggestionFutures).thenApply(ignored ->
                Stream.concat(
                    StreamSupport.stream(suggestionFutures[0].getNow(Collections.emptyList()).spliterator(), false),
                    StreamSupport.stream(suggestionFutures[1].getNow(Collections.emptyList()).spliterator(), false)
                ).collect(Collectors.toList())
        );
    }


    /**
     * Exception thrown when both the primary and fallback parsers fail to parse the input.
     */
    public static final class EitherParseException extends ParserException {

        private final Throwable primaryFailure;
        private final Throwable fallbackFailure;
        private final TypeToken<?> primaryType;
        private final TypeToken<?> fallbackType;

        private EitherParseException(
                final @NonNull Throwable primaryFailure,
                final @NonNull Throwable fallbackFailure,
                final @NonNull TypeToken<?> primaryType,
                final @NonNull TypeToken<?> fallbackType,
                final @NonNull CommandContext<?> context,
                final @NonNull String input
        ) {
            super(
                    fallbackFailure,
                    EitherParser.class,
                    context,
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_EITHER,
                    CaptionVariable.of("input", input),
                    CaptionVariable.of("primary", GenericTypeReflector.erase(primaryType.getType()).getSimpleName()),
                    CaptionVariable.of("fallback", GenericTypeReflector.erase(fallbackType.getType()).getSimpleName())
            );
            this.primaryFailure = primaryFailure;
            this.fallbackFailure = fallbackFailure;
            this.primaryType = primaryType;
            this.fallbackType = fallbackType;
        }

        /**
         * Returns the throwable thrown by the primary parser.
         *
         * @return primary failure
         */
        public @NonNull Throwable primaryFailure() {
            return this.primaryFailure;
        }

        /**
         * Returns the throwable thrown by the fallback parser.
         *
         * @return fallback failure
         */
        public @NonNull Throwable fallbackFailure() {
            return this.fallbackFailure;
        }

        /**
         * Returns the type produced by the primary parser.
         *
         * @return primary type
         */
        public @NonNull TypeToken<?> primaryType() {
            return this.primaryType;
        }

        /**
         * Returns the type produced by the fallback parser.
         *
         * @return fallback type
         */
        public @NonNull TypeToken<?> fallbackType() {
            return this.fallbackType;
        }
    }
}
