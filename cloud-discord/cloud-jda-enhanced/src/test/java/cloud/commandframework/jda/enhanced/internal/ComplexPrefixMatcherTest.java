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

package cloud.commandframework.jda.enhanced.internal;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class ComplexPrefixMatcherTest {

    @Test
    void testBotMentionPrefix() {
        final ComplexPrefixMatcher<ComplexPrefixMatcherTest> prefixMatcher = new ComplexPrefixMatcher<>(
                Collections.emptyList(),
                Collections.emptyList(),
                true,
                "123"
        );

        assertEquals("abcd", prefixMatcher.apply(null, "<@123>abcd"));
        assertEquals("abcd", prefixMatcher.apply(null, "<@!123>abcd"));
        assertEquals("abcd", prefixMatcher.apply(null, "<@123> abcd"));
        assertEquals("abcd", prefixMatcher.apply(null, "<@!123> abcd"));
    }

    @Test
    void testSinglePrefixMatches() {
        final ComplexPrefixMatcher<ComplexPrefixMatcherTest> prefixMatcher = new ComplexPrefixMatcher<>(
                Collections.emptyList(),
                Arrays.asList((c) -> "!", (c) -> "$", (c) -> "word "),
                false,
                null
        );

        assertEquals("abcd", prefixMatcher.apply(null, "!abcd"));
        assertEquals("abcd", prefixMatcher.apply(null, "$abcd"));
        assertEquals("abcd", prefixMatcher.apply(null, "word abcd"));
    }

    @Test
    void testMultiPrefixMatches() {
        final ComplexPrefixMatcher<ComplexPrefixMatcherTest> prefixMatcher = new ComplexPrefixMatcher<>(
                Arrays.asList((c) -> Arrays.asList("!", "$", "word "), (c) -> Arrays.asList("ya ", "123_! ")),
                Collections.emptyList(),
                false,
                null
        );

        assertEquals("abcd", prefixMatcher.apply(null, "!abcd"));
        assertEquals("abcd", prefixMatcher.apply(null, "$abcd"));
        assertEquals("abcd", prefixMatcher.apply(null, "word abcd"));
        assertEquals("abcd", prefixMatcher.apply(null, "ya abcd"));
        assertEquals("abcd", prefixMatcher.apply(null, "123_! abcd"));
    }

}
