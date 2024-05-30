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
package org.incendo.cloud.parser;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.exception.handling.ExceptionController;

/**
 * Result of the parsing done by a {@link ArgumentParser}
 *
 * @param <T> Parser return type
 */
@API(status = API.Status.STABLE)
public abstract class ArgumentParseResult<T> {

    private ArgumentParseResult() {
    }

    /**
     * Indicate that the parsing failed
     *
     * <p>If the {@code failure} is a {@link CompletionException} then it will be unwrapped.</p>
     *
     * @param failure Failure reason
     * @param <T>     Parser return type
     * @return Failed parse result
     */
    public static <T> @NonNull ArgumentParseResult<T> failure(final @NonNull Throwable failure) {
        return new ParseFailure<>(failure);
    }

    /**
     * Create a {@link CompletableFuture future} completed with a failed parse result.
     *
     * @param failure failure reason
     * @param <T>     parser value type
     * @return completed future with failed result
     */
    @API(status = API.Status.STABLE)
    public static <T> @NonNull CompletableFuture<@NonNull ArgumentParseResult<T>> failureFuture(final @NonNull Throwable failure) {
        return new ParseFailure<T>(failure).asFuture();
    }

    /**
     * Indicate that the parsing succeeded
     *
     * @param value Value produced by the parser
     * @param <T>   Parser return type
     * @return Succeeded parse result
     */
    public static <T> @NonNull ArgumentParseResult<T> success(final @NonNull T value) {
        return new ParseSuccess<>(value);
    }

    /**
     * Create a {@link CompletableFuture future} completed with a successful parse result.
     *
     * @param value parsed value
     * @param <T>   parser value type
     * @return completed future with successful result
     */
    @API(status = API.Status.STABLE)
    public static <T> @NonNull CompletableFuture<@NonNull ArgumentParseResult<T>> successFuture(final @NonNull T value) {
        return success(value).asFuture();
    }

    /**
     * Get the parsed value, if it exists
     *
     * @return Optional containing the parsed value
     */
    @API(status = API.Status.STABLE)
    public abstract @NonNull Optional<T> parsedValue();

    /**
     * Get the failure reason, if it exists
     *
     * @return Optional containing the failure reason
     */
    @API(status = API.Status.STABLE)
    public abstract @NonNull Optional<Throwable> failure();

    /**
     * Maps the result to a completable future.
     *
     * @return the future
     */
    @API(status = API.Status.STABLE)
    public final @NonNull CompletableFuture<ArgumentParseResult<T>> asFuture() {
        return CompletableFuture.completedFuture(this);
    }

    /**
     * Returns a future resulting from applying {@code mapper} to the parsed value,
     * or a completed future with the same failure as this result.
     *
     * @param mapper mapper
     * @param <O>    new result value type
     * @return new result future
     */
    public abstract <O> @NonNull CompletableFuture<ArgumentParseResult<O>> flatMapSuccessFuture(
            @NonNull Function<T, CompletableFuture<ArgumentParseResult<O>>> mapper
    );

    /**
     * Returns a success future resulting from applying {@code mapper} to the parsed value and
     * wrapping in {@link #success(Object)}, or a completed future with the same failure as this result.
     *
     * @param mapper mapper
     * @param <O>    new result value type
     * @return new result future
     */
    public abstract <O> @NonNull CompletableFuture<ArgumentParseResult<O>> mapSuccessFuture(
            @NonNull Function<T, CompletableFuture<O>> mapper
    );

    /**
     * Returns the result from applying {@code mapper} to the parsed value,
     * or the same failure as this result.
     *
     * @param mapper mapper
     * @param <O>    new result value type
     * @return new result
     */
    public abstract <O> @NonNull ArgumentParseResult<O> flatMapSuccess(@NonNull Function<T, ArgumentParseResult<O>> mapper);

    /**
     * Returns the result from applying {@code mapper} to the parsed value and
     * wrapping in {@link #success(Object)}, or the same failure as this result.
     *
     * @param mapper mapper
     * @param <O>    new result value type
     * @return new result
     */
    public abstract <O> @NonNull ArgumentParseResult<O> mapSuccess(@NonNull Function<T, O> mapper);

    private static final class ParseSuccess<T> extends ArgumentParseResult<T> {

        /**
         * Parsed value
         */
        private final T value;

        private ParseSuccess(final @NonNull T value) {
            this.value = value;
        }

        @Override
        public @NonNull Optional<T> parsedValue() {
            return Optional.of(this.value);
        }

        @Override
        public @NonNull Optional<Throwable> failure() {
            return Optional.empty();
        }

        @Override
        public <O> @NonNull CompletableFuture<ArgumentParseResult<O>> flatMapSuccessFuture(
                final @NonNull Function<T, CompletableFuture<ArgumentParseResult<O>>> mapper
        ) {
            return mapper.apply(this.value);
        }

        @Override
        public <O> @NonNull CompletableFuture<ArgumentParseResult<O>> mapSuccessFuture(
                final @NonNull Function<T, CompletableFuture<O>> mapper
        ) {
            return mapper.apply(this.value).thenApply(ArgumentParseResult::success);
        }

        @Override
        public <O> @NonNull ArgumentParseResult<O> flatMapSuccess(
                final @NonNull Function<T, ArgumentParseResult<O>> mapper
        ) {
            return mapper.apply(this.value);
        }

        @Override
        public <O> @NonNull ArgumentParseResult<O> mapSuccess(final @NonNull Function<T, O> mapper) {
            return ArgumentParseResult.success(mapper.apply(this.value));
        }
    }

    private static final class ParseFailure<T> extends ArgumentParseResult<T> {

        /**
         * Parse failure
         */
        private final Throwable failure;

        private ParseFailure(final @NonNull Throwable failure) {
            this.failure = ExceptionController.unwrapCompletionException(failure);
        }

        @Override
        public @NonNull Optional<T> parsedValue() {
            return Optional.empty();
        }

        @Override
        public @NonNull Optional<Throwable> failure() {
            return Optional.of(this.failure);
        }

        @Override
        public <O> @NonNull CompletableFuture<ArgumentParseResult<O>> flatMapSuccessFuture(
                final @NonNull Function<T, CompletableFuture<ArgumentParseResult<O>>> mapper
        ) {
            return CompletableFuture.completedFuture(this.self());
        }

        @Override
        public <O> @NonNull CompletableFuture<ArgumentParseResult<O>> mapSuccessFuture(
                final @NonNull Function<T, CompletableFuture<O>> mapper
        ) {
            return CompletableFuture.completedFuture(this.self());
        }

        @Override
        public <O> @NonNull ArgumentParseResult<O> flatMapSuccess(final @NonNull Function<T, ArgumentParseResult<O>> mapper) {
            return this.self();
        }

        @Override
        public <O> @NonNull ArgumentParseResult<O> mapSuccess(final @NonNull Function<T, O> mapper) {
            return this.self();
        }

        @SuppressWarnings("unchecked")
        private <O> @NonNull ArgumentParseResult<O> self() {
            return (ArgumentParseResult<O>) this;
        }
    }
}
