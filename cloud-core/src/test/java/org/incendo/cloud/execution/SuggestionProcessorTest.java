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
package org.incendo.cloud.execution;

import java.util.List;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.TestCommandSender;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;
import static org.incendo.cloud.util.TestUtils.createManager;

class SuggestionProcessorTest {

    private CommandManager<TestCommandSender> commandManager;

    @BeforeEach
    void setup() {
        this.commandManager = createManager();
    }

    @Test
    void testModifying() {
        // Arrange
        this.commandManager.suggestionProcessor((ctx, suggestions) -> suggestions.map(s -> s.withSuggestion(
                String.format("test-%s", s.suggestion()))
        ));
        this.commandManager.command(
                this.commandManager.commandBuilder("test").required(
                        "arg",
                        stringParser(),
                        SuggestionProvider.suggestingStrings("suggestion")
                )
        );

        // Act
        final List<? extends Suggestion> suggestions = this.commandManager.suggestionFactory().suggestImmediately(
                new TestCommandSender(),
                "test "
        ).list();

        // Assert
        assertThat(suggestions).containsExactly(Suggestion.suggestion("test-suggestion"));
    }

    @Test
    void testFiltering() {
        // Arrange
        this.commandManager.suggestionProcessor((ctx, suggestions) -> suggestions.limit(0));
        this.commandManager.command(
                this.commandManager.commandBuilder("test").required(
                        "arg",
                        stringParser(),
                        SuggestionProvider.suggestingStrings("suggestion")
                )
        );

        // Act
        final List<? extends Suggestion> suggestions = this.commandManager.suggestionFactory().suggestImmediately(
                new TestCommandSender(),
                "test "
        ).list();

        // Assert
        assertThat(suggestions).isEmpty();
    }
}
