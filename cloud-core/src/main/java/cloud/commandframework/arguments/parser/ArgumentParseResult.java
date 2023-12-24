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

import cloud.commandframework.exceptions.handling.ExceptionController;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

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
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
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
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <T> @NonNull CompletableFuture<@NonNull ArgumentParseResult<T>> successFuture(final @NonNull T value) {
        return success(value).asFuture();
    }

    /**
     * Get the parsed value, if it exists
     *
     * @return Optional containing the parsed value
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public abstract @NonNull Optional<T> parsedValue();

    /**
     * Get the failure reason, if it exists
     *
     * @return Optional containing the failure reason
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public abstract @NonNull Optional<Throwable> failure();

    /**
     * Maps the result to a completable future.
     *
     * @return the future
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public final @NonNull CompletableFuture<ArgumentParseResult<T>> asFuture() {
        return CompletableFuture.completedFuture(this);
    }


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
    }
}
