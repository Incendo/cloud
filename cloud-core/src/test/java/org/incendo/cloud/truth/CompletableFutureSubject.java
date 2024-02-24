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
package org.incendo.cloud.truth;

import com.google.common.truth.Fact;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.OptionalSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.ThrowableSubject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.truth.Truth.assertAbout;

/**
 * Subject for assertions about completable futures. Futures are join()ed to get
 * results or exceptions, and {@link CompletionException}'s cause is unwrapped to
 * {@link #failure()}. Any other exceptions thrown by join() are unexpected failures.
 * This class also does not properly handle futures where null is a successful result.
 *
 * @param <T> future result type
 */
public final class CompletableFutureSubject<T> extends Subject {

    public static <T> @NonNull Factory<CompletableFutureSubject<T>, CompletableFuture<? extends @NonNull T>> completableFuture() {
        return CompletableFutureSubject::new;
    }

    public static <T> @NonNull CompletableFutureSubject<T> assertThat(
            final @Nullable CompletableFuture<? extends @NonNull T> actual
    ) {
        return assertAbout(CompletableFutureSubject.<T>completableFuture()).that(actual);
    }

    private final CompletableFuture<? extends @NonNull T> actual;

    private CompletableFutureSubject(
            final @NonNull FailureMetadata metadata,
            final @Nullable CompletableFuture<? extends @NonNull T> actual
    ) {
        super(metadata, actual);
        this.actual = actual;
    }

    public @NonNull OptionalSubject result() {
        if (this.actual == null) {
            this.failWithActual(Fact.simpleFact("expected future to not be null"));
        }
        try {
            final T join = this.actual.join();
            if (join == null) {
                this.failWithActual(Fact.simpleFact("expected result to not be null"));
            }
            return this.check("join() result or null on failure").about(OptionalSubject.optionals()).that(Optional.of(join));
        } catch (final CompletionException e) {
            return this.check("join() result or null on failure").about(OptionalSubject.optionals()).that(Optional.empty());
        }
    }

    public @NonNull OptionalSubject failure() {
        if (this.actual == null) {
            this.failWithActual(Fact.simpleFact("expected future to not be null"));
        }
        try {
            this.actual.join();
            return this.check("cause of CompletionException thrown by join()").about(OptionalSubject.optionals()).that(Optional.empty());
        } catch (final CompletionException e) {
            return this.check("cause of CompletionException thrown by join()").about(OptionalSubject.optionals()).that(Optional.of(e.getCause()));
        }
    }

    public void hasResult() {
        this.result().isPresent();
        this.failure().isEmpty();
    }

    public void hasResult(final @NonNull T value) {
        this.result().hasValue(value);
        this.failure().isEmpty();
    }

    public void hasFailure(final @NonNull Throwable throwable) {
        this.failure().hasValue(throwable);
        this.result().isEmpty();
    }

    public @NonNull ThrowableSubject hasFailureThat() {
        this.result().isEmpty();
        this.failure().isPresent();
        try {
            this.actual.join();
            this.failWithActual(Fact.simpleFact("expected future to throw on join()"));
            throw new IllegalStateException();
        } catch (final CompletionException e) {
            return this.check("cause of CompletionException thrown by join()").that(e.getCause());
        }
    }
}
