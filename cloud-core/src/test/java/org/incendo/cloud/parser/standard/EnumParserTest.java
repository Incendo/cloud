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
package org.incendo.cloud.parser.standard;

import org.incendo.cloud.TestCommandSender;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.suggestion.Suggestion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.google.common.truth.Truth.assertThat;
import static org.incendo.cloud.truth.ArgumentParseResultSubject.assertThat;

@ExtendWith(MockitoExtension.class)
class EnumParserTest {

    @Mock
    private CommandContext<TestCommandSender> context;

    @ParameterizedTest
    @EnumSource(TestEnum.class)
    void Parse_EnumValues_SuccessfulParse(final TestEnum value) {
        // Arrange
        final EnumParser<TestCommandSender, TestEnum> parser = new EnumParser<>(
                TestEnum.class
        );
        CommandInput commandInput = CommandInput.of(value.name());

        // Act
        final ArgumentParseResult<TestEnum> result = parser.parse(
                this.context,
                commandInput
        );

        // Assert
        assertThat(result).hasParsedValue(value);
        assertThat(commandInput.isEmpty()).isTrue();
    }

    @Test
    void Parse_NonEnumValue_FailedParse() {
        // Arrange
        final EnumParser<TestCommandSender, TestEnum> parser = new EnumParser<>(
                TestEnum.class
        );

        // Act
        final ArgumentParseResult<TestEnum> result = parser.parse(
                this.context,
                CommandInput.of("not-an-enum-value")
        );

        // Assert
        assertThat(result).hasFailure(new EnumParser.EnumParseException(
                "not-an-enum-value",
                TestEnum.class,
                this.context
        ));
    }

    @Test
    void Suggestions_ExpectedSuggestions() {
        // Arrange
        final EnumParser<TestCommandSender, TestEnum> parser = new EnumParser<>(
                TestEnum.class
        );

        // Act
        final Iterable<Suggestion> suggestions = parser.suggestions(
                this.context,
                CommandInput.empty()
        );

        // Assert
        assertThat(suggestions).containsExactlyElementsIn(ArgumentTestHelper.suggestionList("aaa", "bbb", "ccc"));
    }

    enum TestEnum {
        AAA,
        BBB,
        CCC
    }
}
