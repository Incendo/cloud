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

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Parses command syntax into syntax fragments
 */
final class SyntaxParser implements Function<@NonNull String, @NonNull List<@NonNull SyntaxFragment>> {

    private static final Predicate<String> PATTERN_ARGUMENT_LITERAL = Pattern.compile("([A-Za-z0-9\\-_]+)(|([A-Za-z0-9\\-_]+))*")
            .asPredicate();
    private static final Predicate<String> PATTERN_ARGUMENT_REQUIRED = Pattern.compile("<([A-Za-z0-9\\-_]+)>")
            .asPredicate();
    private static final Predicate<String> PATTERN_ARGUMENT_OPTIONAL = Pattern.compile("\\[([A-Za-z0-9\\-_]+)]")
            .asPredicate();

    @Override
    public @NonNull List<@NonNull SyntaxFragment> apply(final @NonNull String syntax) {
        final StringTokenizer stringTokenizer = new StringTokenizer(syntax, " ");
        final List<SyntaxFragment> syntaxFragments = new ArrayList<>();
        while (stringTokenizer.hasMoreTokens()) {
            final String token = stringTokenizer.nextToken();
            String major;
            List<String> minor = new ArrayList<>();
            ArgumentMode mode;
            if (PATTERN_ARGUMENT_REQUIRED.test(token)) {
                major = token.substring(1, token.length() - 1);
                mode = ArgumentMode.REQUIRED;
            } else if (PATTERN_ARGUMENT_OPTIONAL.test(token)) {
                major = token.substring(1, token.length() - 1);
                mode = ArgumentMode.OPTIONAL;
            } else if (PATTERN_ARGUMENT_LITERAL.test(token)) {
                final String[] literals = token.split("\\|");
                /* Actually use the other literals as well */
                major = literals[0];
                minor.addAll(Arrays.asList(literals).subList(1, literals.length));
                mode = ArgumentMode.LITERAL;
            } else {
                throw new IllegalArgumentException(String.format("Unrecognizable syntax token '%s'", syntax));
            }
            syntaxFragments.add(new SyntaxFragment(major, minor, mode));
        }
        return syntaxFragments;
    }

}
