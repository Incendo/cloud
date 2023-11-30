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
package cloud.commandframework.arguments.standard;

import cloud.commandframework.CommandManager;
import cloud.commandframework.TestCommandSender;
import cloud.commandframework.arguments.suggestion.Suggestion;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.arguments.standard.ArgumentTestHelper.suggestionList;
import static cloud.commandframework.arguments.standard.DurationParser.durationParser;
import static cloud.commandframework.util.TestUtils.createManager;

class DurationParserSuggestionsTest {

    private static CommandManager<TestCommandSender> manager;

    @BeforeAll
    static void setupManager() {
        manager = createManager();
        manager.command(manager.commandBuilder("duration")
                .required("duration", durationParser()));
    }

    @Test
    void testDurationSuggestions() {
        final String input = "duration ";
        final List<? extends Suggestion> suggestions = manager.suggestionFactory().suggestImmediately(
                new TestCommandSender(),
                input
        );
        Assertions.assertEquals(suggestionList("1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions);

        final String input2 = "duration 1";
        final List<? extends Suggestion> suggestions2 = manager.suggestionFactory().suggestImmediately(
                new TestCommandSender(),
                input2
        );
        Assertions.assertEquals(suggestionList("1d", "1h", "1m", "1s"), suggestions2);

        final String input3 = "duration 1d";
        final List<? extends Suggestion> suggestions3 = manager.suggestionFactory().suggestImmediately(
                new TestCommandSender(),
                input3
        );
        Assertions.assertEquals(Collections.emptyList(), suggestions3);

        final String input4 = "duration 1d2";
        final List<? extends Suggestion> suggestions4 = manager.suggestionFactory().suggestImmediately(
                new TestCommandSender(),
                input4
        );
        Assertions.assertTrue(suggestions4.containsAll(suggestionList("1d2h", "1d2m", "1d2s")));
        Assertions.assertFalse(suggestions4.contains(Suggestion.simple("1d2d")));

        final String input9 = "duration 1d22";
        final List<? extends Suggestion> suggestions9 = manager.suggestionFactory().suggestImmediately(
                new TestCommandSender(),
                input9
        );
        Assertions.assertTrue(suggestions9.containsAll(suggestionList("1d22h", "1d22m", "1d22s")));
        Assertions.assertFalse(suggestions9.contains(Suggestion.simple("1d22d")));

        final String input5 = "duration d";
        final List<? extends Suggestion> suggestions5 = manager.suggestionFactory().suggestImmediately(
                new TestCommandSender(),
                input5
        );
        Assertions.assertEquals(Collections.emptyList(), suggestions5);

        final String input6 = "duration 1d2d";
        final List<? extends Suggestion> suggestions6 = manager.suggestionFactory().suggestImmediately(
                new TestCommandSender(),
                input6
        );
        Assertions.assertEquals(Collections.emptyList(), suggestions6);

        final String input7 = "duration 1d2h3m4s";
        final List<? extends Suggestion> suggestions7 = manager.suggestionFactory().suggestImmediately(
                new TestCommandSender(),
                input7
        );
        Assertions.assertEquals(Collections.emptyList(), suggestions7);

        final String input8 = "duration dd";
        final List<? extends Suggestion> suggestions8 = manager.suggestionFactory().suggestImmediately(
                new TestCommandSender(),
                input8
        );
        Assertions.assertEquals(Collections.emptyList(), suggestions8);
    }
}
