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
package org.incendo.cloud.util;

import java.util.Locale;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.context.CommandInput;

/**
 * String utilities
 */
@API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Count the occurrences of a character in a string
     *
     * @param haystack The string to search in
     * @param needle   The character to count for
     * @return Number of occurrences
     */
    public static int countCharOccurrences(final @NonNull String haystack, final char needle) {
        int occurrences = 0;
        for (int i = 0; i < haystack.length(); i++) {
            if (haystack.charAt(i) == needle) {
                occurrences++;
            }
        }
        return occurrences;
    }

    /**
     * Replace all matches in a string.
     *
     * @param string   string to process
     * @param pattern  replacement pattern
     * @param replacer replacement function
     * @return processed string
     */
    public static @NonNull String replaceAll(
            final @NonNull String string,
            final @NonNull Pattern pattern,
            final @NonNull Function<@NonNull MatchResult, @NonNull String> replacer
    ) {
        final Matcher matcher = pattern.matcher(string);
        matcher.reset();
        boolean result = matcher.find();
        if (result) {
            final StringBuffer sb = new StringBuffer();
            do {
                final String replacement = replacer.apply(matcher);
                matcher.appendReplacement(sb, replacement);
                result = matcher.find();
            } while (result);
            matcher.appendTail(sb);
            return sb.toString();
        }
        return string;
    }

    /**
     * Trims before the last space from suggestion, if it matches input tokens.
     *
     * @param suggestion suggestion text
     * @param input      current input
     * @return filtered suggestion text
     */
    public static @Nullable String trimBeforeLastSpace(final String suggestion, final String input) {
        final int lastSpace = input.lastIndexOf(' ');
        // No spaces in input, don't do anything
        if (lastSpace == -1) {
            return suggestion;
        }

        if (suggestion.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT).substring(0, lastSpace))) {
            return suggestion.substring(lastSpace + 1);
        }

        return null;
    }

    /**
     * Trims before the last space from suggestion, if it matches input tokens.
     *
     * @param suggestion   suggestion
     * @param commandInput command input
     * @return filtered suggestion text
     */
    public static @Nullable String trimBeforeLastSpace(final String suggestion, final CommandInput commandInput) {
        // get the input similarly to FilteringCommandSuggestionProcessor
        final String input;
        if (commandInput.isEmpty(true /* ignoreWhitespace */)) {
            input = "";
        } else {
            input = commandInput.copy().skipWhitespace().remainingInput();
        }
        return trimBeforeLastSpace(suggestion, input);
    }
}
