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
package cloud.commandframework.execution;

import cloud.commandframework.CommandManager;
import cloud.commandframework.TestCommandSender;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static cloud.commandframework.parser.standard.ArgumentTestHelper.suggestionList;
import static cloud.commandframework.parser.standard.StringParser.greedyStringParser;
import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FilteringCommandSuggestionProcessorTest {

    private CommandManager<TestCommandSender> commandManager;

    @BeforeEach
    void setup() {
        this.commandManager = createManager();
    }

    @ParameterizedTest
    @MethodSource
    void testPartialTokenMatches(final String input, final List<Suggestion> expected) {
        // Arrange
        this.commandManager.commandSuggestionProcessor(
                new FilteringCommandSuggestionProcessor<>(
                        FilteringCommandSuggestionProcessor.Filter.partialTokenMatches(true)
                )
        );
        this.commandManager.command(this.commandManager.commandBuilder("test1").required(
                "arg",
                greedyStringParser(),
                SuggestionProvider.suggestingStrings(
                        "a b c d e",
                        "x y z a"
                )
        ));

        // Act
        final List<? extends Suggestion> suggestions = this.commandManager.suggestionFactory()
                .suggestImmediately(new TestCommandSender(), input)
                .list();

        // Assert
        assertThat(suggestions).containsExactlyElementsIn(expected);
    }

    static Stream<Arguments> testPartialTokenMatches() {
        return Stream.of(
                arguments("test1 ", suggestionList("a b c d e", "x y z a")),
                arguments("test1 a", suggestionList("a b c d e", "x y z a")),
                arguments("test1 a e", suggestionList("a b c d e")),
                arguments("test1 a e a", suggestionList()),
                arguments("test1 z", suggestionList("x y z a"))
        );
    }
}
