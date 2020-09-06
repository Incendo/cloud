//
// MIT License
//
// Copyright (c) 2020 IntellectualSites
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
package com.intellectualsites.commands.parser;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class ComponentParseResult<T> {

    private ComponentParseResult() {
    }

    @Nonnull
    public static <T> ComponentParseResult<T> failure(@Nonnull final String failure) {
        return new ParseFailure<>(failure);
    }

    @Nonnull
    public static <T> ComponentParseResult<T> success(@Nonnull final T value) {
        return new ParseSuccess<>(value);
    }

    @Nonnull
    public abstract Optional<T> getParsedValue();

    @Nonnull
    public abstract Optional<String> getFailure();


    private static final class ParseSuccess<T> extends ComponentParseResult<T> {

        private final T value;

        private ParseSuccess(@Nonnull final T value) {
            this.value = value;
        }

        @Nonnull
        @Override
        public Optional<T> getParsedValue() {
            return Optional.of(this.value);
        }

        @Nonnull
        @Override
        public Optional<String> getFailure() {
            return Optional.empty();
        }

    }


    private static final class ParseFailure<T> extends ComponentParseResult<T> {

        private final String failure;

        private ParseFailure(@Nonnull final String failure) {
            this.failure = failure;
        }

        @Nonnull
        @Override
        public Optional<T> getParsedValue() {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<String> getFailure() {
            return Optional.of(this.failure);
        }
    }

}
