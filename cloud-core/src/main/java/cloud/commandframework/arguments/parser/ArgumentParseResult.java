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
package cloud.commandframework.arguments.parser;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

/**
 * Result of the parsing done by a {@link ArgumentParser}
 *
 * @param <T> Parser return type
 */
public abstract class ArgumentParseResult<T> {

    private ArgumentParseResult() {
    }

    /**
     * Indicate that the parsing failed
     *
     * @param failure Failure reason
     * @param <T>     Parser return type
     * @return Failed parse result
     */
    public static <T> @NonNull ArgumentParseResult<T> failure(final @NonNull Throwable failure) {
        return new ParseFailure<>(failure);
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
     * Get the parsed value, if it exists
     *
     * @return Optional containing the parsed value
     */
    public abstract @NonNull Optional<T> getParsedValue();

    /**
     * Get the failure reason, if it exists
     *
     * @return Optional containing the failure reason
     */
    public abstract @NonNull Optional<Throwable> getFailure();


    private static final class ParseSuccess<T> extends ArgumentParseResult<T> {

        /**
         * Parsed value
         */
        private final T value;

        private ParseSuccess(final @NonNull T value) {
            this.value = value;
        }

        @Override
        public @NonNull Optional<T> getParsedValue() {
            return Optional.of(this.value);
        }

        @Override
        public @NonNull Optional<Throwable> getFailure() {
            return Optional.empty();
        }

    }


    private static final class ParseFailure<T> extends ArgumentParseResult<T> {

        /**
         * Parse failure
         */
        private final Throwable failure;

        private ParseFailure(final @NonNull Throwable failure) {
            this.failure = failure;
        }

        @Override
        public @NonNull Optional<T> getParsedValue() {
            return Optional.empty();
        }

        @Override
        public @NonNull Optional<Throwable> getFailure() {
            return Optional.of(this.failure);
        }

    }

}
