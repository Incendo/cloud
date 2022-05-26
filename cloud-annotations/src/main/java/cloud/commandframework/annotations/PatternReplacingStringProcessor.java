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
package cloud.commandframework.annotations;

import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * {@link StringProcessor} that replaces matches of a given {@link Pattern}.
 *
 * @since 1.7.0
 */
public class PatternReplacingStringProcessor implements StringProcessor {

    private final Pattern pattern;
    private final Function<MatchResult, String> replacementProvider;

    /**
     * Creates a new property replacing string processor.
     *
     * @param pattern             the pattern to search for
     * @param replacementProvider function generating the replacement strings
     */
    public PatternReplacingStringProcessor(
            final @NonNull Pattern pattern,
            final @NonNull Function<@NonNull MatchResult, @Nullable String> replacementProvider
    ) {
        this.pattern = pattern;
        this.replacementProvider = replacementProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull String processString(
            @NonNull final String input
    ) {
        final Matcher matcher = this.pattern.matcher(input);

        // Kind of copied from the JDK 9+ implementation of "Matcher#replaceFirst"
        final StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            final String replacement = this.replacementProvider.apply(matcher);
            matcher.appendReplacement(stringBuffer, replacement == null ? "$0" : replacement);
        }
        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }
}
