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

package cloud.commandframework.jda.slashcommands.internal;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ComplexPrefixMatcher<C> implements BiFunction<C, String, String> {

    private final List<Function<C, List<String>>> prefixMappers;
    private final List<Function<C, String>> singlePrefixMappers;
    private final boolean enableBotMentionPrefix;
    private final String botIdString;

    public ComplexPrefixMatcher(
            final List<Function<C, List<String>>> prefixMappers,
            final List<Function<C, String>> singlePrefixMappers,
            final boolean enableBotMentionPrefix,
            final String botIdString
    ) {
        this.prefixMappers = prefixMappers;
        this.singlePrefixMappers = singlePrefixMappers;
        this.enableBotMentionPrefix = enableBotMentionPrefix;
        this.botIdString = botIdString;
    }

    @Override
    public String apply(final C c, final String rawContent) {
        for (final Function<C, String> prefixMapper : singlePrefixMappers) {
            final String prefix = prefixMapper.apply(c);

            if (rawContent.startsWith(prefix)) {
                return rawContent.substring(prefix.length());
            }
        }

        for (final Function<C, List<String>> prefixMapper : prefixMappers) {
            final List<String> prefixes = prefixMapper.apply(c);

            for (final String prefix : prefixes) {
                if (rawContent.startsWith(prefix)) {
                    return rawContent.substring(prefix.length());
                }
            }
        }

        if (enableBotMentionPrefix && rawContent.startsWith("<@")) { // last, match against bot mention
            final int angleClose = rawContent.indexOf('>');
            if (angleClose != -1) {
                final int prefixSize;

                // 3 because "<@" is 2, then if the next character is !, it can also be a mention
                if (rawContent.charAt(2) == '!') {
                    prefixSize = 3;
                } else {
                    prefixSize = 2;
                }

                if (!rawContent.substring(prefixSize, angleClose).equals(this.botIdString)) {
                    return null;
                }

                if (rawContent.charAt(angleClose + 1) == ' ') {
                    return rawContent.substring(angleClose + 2);
                } else {
                    return rawContent.substring(angleClose + 1);
                }
            }
        }

        return null;
    }

}
