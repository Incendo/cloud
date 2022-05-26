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
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * {@link PropertyReplacementProvider} that replaces all sub-strings with the format
 * {@code ${some.property}} with a function-generated string.
 *
 * @since 1.7.0
 */
public class PropertyReplacingStringProcessor extends PatternReplacingStringProcessor {

    public static final Pattern PROPERTY_REGEX = Pattern.compile("\\$\\{(\\S+)}");

    /**
     * Creates a new property replacing string processor.
     *
     * @param replacementProvider function generating the replacement strings
     */
    public PropertyReplacingStringProcessor(
            final @NonNull Function<@NonNull String, @Nullable String> replacementProvider
    ) {
        super(PROPERTY_REGEX, new PropertyReplacementProvider(replacementProvider));
    }


    private static final class PropertyReplacementProvider implements Function<@NonNull MatchResult, @Nullable String> {

        private final Function<String, String> replacementProvider;

        private PropertyReplacementProvider(
                final @NonNull Function<String, String> replacementProvider
        ) {
            this.replacementProvider = replacementProvider;
        }

        @Override
        public @Nullable String apply(final @NonNull MatchResult matchResult) {
            return this.replacementProvider.apply(matchResult.group(1));
        }
    }
}
