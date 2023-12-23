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
package cloud.commandframework.truth;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import com.google.common.truth.Fact;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.OptionalSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.ThrowableSubject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.truth.Truth.assertAbout;

/**
 * Truth subject used to simplify testing {@link ArgumentParseResult argument parse results}.
 *
 * @param <T> type of the parsed values
 */
public final class ArgumentParseResultSubject<T> extends Subject {

    public static <T> @NonNull Factory<ArgumentParseResultSubject<T>, ArgumentParseResult<T>> argumentParseResults() {
        return ArgumentParseResultSubject::new;
    }

    public static <T> @NonNull ArgumentParseResultSubject<T> assertThat(final @Nullable ArgumentParseResult<T> actual) {
        return assertAbout(ArgumentParseResultSubject.<T>argumentParseResults()).that(actual);
    }

    private final ArgumentParseResult<T> actual;

    private ArgumentParseResultSubject(final @NonNull FailureMetadata metadata, final @Nullable ArgumentParseResult<T> actual) {
        super(metadata, actual);
        this.actual = actual;
    }

    public @NonNull OptionalSubject parsedValue() {
        if (this.actual == null) {
            this.failWithActual(Fact.simpleFact("expected to not be null"));
        }
        return this.check("getParsedValue()").about(OptionalSubject.optionals()).that(this.actual.getParsedValue());
    }

    public @NonNull OptionalSubject failure() {
        if (this.actual == null) {
            this.failWithActual(Fact.simpleFact("expected to not be null"));
        }
        return this.check("getFailure()").about(OptionalSubject.optionals()).that(this.actual.getFailure());
    }

    public void hasParsedValue(final @NonNull T value) {
        this.parsedValue().hasValue(value);
        this.failure().isEmpty();
    }

    public void hasFailure(final @NonNull Throwable throwable) {
        this.failure().hasValue(throwable);
        this.parsedValue().isEmpty();
    }

    public @NonNull ThrowableSubject hasFailureThat() {
        this.failure().isPresent();
        return this.check("getFailure().get()").that(this.actual.getFailure().get());
    }
}
