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
package org.incendo.cloud.caption;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public interface CaptionFormatter<C, T> {

    /**
     * Returns a caption formatter that replaces the results from the given {@code pattern} with
     * the values from the caption variables.
     * <p>
     * The 1st capturing group will be used to determine the name of the variable to use for
     * the replacement.
     *
     * @param <C>     the command sender type
     * @param pattern the pattern
     * @return the formatter
     */
    static <C> @NonNull CaptionFormatter<C, String> patternReplacing(final @NonNull Pattern pattern) {
        return new PatternReplacingCaptionFormatter<>(pattern);
    }

    /**
     * Returns a caption formatter that replaces placeholders in the form of {@code <placeholder>}
     * with the caption variables.
     *
     * @param <C> the command sender type
     * @return the formatter
     */
    static <C> @NonNull CaptionFormatter<C, String> placeholderReplacing() {
        return new PatternReplacingCaptionFormatter<>(Pattern.compile("<(\\S+)>"));
    }

    /**
     * Formats the {@code caption}.
     *
     * @param captionKey the caption key
     * @param recipient  the recipient of the message
     * @param caption    the value of the caption
     * @param variables  the caption variables
     * @return the transformed message
     */
    @NonNull T formatCaption(
            @NonNull Caption captionKey,
            @NonNull C recipient,
            @NonNull String caption,
            @NonNull CaptionVariable @NonNull... variables
    );


    final class PatternReplacingCaptionFormatter<C> implements CaptionFormatter<C, String> {

        private final Pattern pattern;

        private PatternReplacingCaptionFormatter(final @NonNull Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public @NonNull String formatCaption(
                final @NonNull Caption captionKey,
                final @NonNull C recipient,
                final @NonNull String caption,
                final @NonNull CaptionVariable @NonNull... variables
        ) {
            final Map<String, String> replacements = new HashMap<>();
            for (final CaptionVariable variable : variables) {
                replacements.put(variable.key(), variable.value());
            }

            final Matcher matcher = this.pattern.matcher(caption);
            final StringBuffer stringBuffer = new StringBuffer();
            while (matcher.find()) {
                final String replacement = replacements.get(matcher.group(1));
                matcher.appendReplacement(stringBuffer, replacement == null ? "$0" : replacement);
            }
            matcher.appendTail(stringBuffer);

            return stringBuffer.toString();
        }
    }
}
