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
package cloud.commandframework.arguments.standard;

import cloud.commandframework.TestCommandSender;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static cloud.commandframework.truth.ArgumentParseResultSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;

@ExtendWith(MockitoExtension.class)
class IntegerParserTest {

    @Mock
    private CommandContext<TestCommandSender> context;

    @Test
    void Parse_NoMinMax_SuccessfulParse() {
        // Arrange
        final IntegerParser<TestCommandSender> parser = new IntegerParser<>(
                IntegerParser.DEFAULT_MINIMUM,
                IntegerParser.DEFAULT_MAXIMUM
        );

        final int intInput = ThreadLocalRandom.current().nextInt();
        final CommandInput commandInput = CommandInput.of(Integer.toString(intInput));

        // Act
        final ArgumentParseResult<Integer> result = parser.parse(
                this.context,
                commandInput
        );

        // Assert
        assertThat(result).hasParsedValue(intInput);
        assertThat(commandInput.isEmpty()).isTrue();
    }

    @Test
    void Parse_ValueBelowMin_FailedParse() {
        // Arrange
        final IntegerParser<TestCommandSender> parser = new IntegerParser<>(
                5 /* min */,
                IntegerParser.DEFAULT_MAXIMUM
        );

        // Act
        final ArgumentParseResult<Integer> result = parser.parse(
                this.context,
                CommandInput.of("4")
        );

        // Assert
        assertThat(result).hasFailure(new IntegerParser.IntegerParseException(
                "4",
                parser,
                this.context
        ));
    }

    @Test
    void Parse_ValueAboveMax_FailedParse() {
        // Arrange
        final IntegerParser<TestCommandSender> parser = new IntegerParser<>(
                IntegerParser.DEFAULT_MINIMUM,
                5 /* max */
        );

        // Act
        final ArgumentParseResult<Integer> result = parser.parse(
                this.context,
                CommandInput.of("6")
        );

        // Assert
        assertThat(result).hasFailure(new IntegerParser.IntegerParseException(
                "6",
                parser,
                this.context
        ));
    }

    @Test
    void Parse_NonIntegerInput_FailedParse() {
        // Arrange
        final IntegerParser<TestCommandSender> parser = new IntegerParser<>(
                IntegerParser.DEFAULT_MINIMUM,
                IntegerParser.DEFAULT_MAXIMUM
        );

        // Act
        final ArgumentParseResult<Integer> result = parser.parse(
                this.context,
                CommandInput.of("cow")
        );

        // Assert
        assertThat(result).hasFailure(new IntegerParser.IntegerParseException(
                "cow",
                parser,
                this.context
        ));
    }

    @Test
    void Suggestions_EmptyInput_ExpectedSuggestions() {
        // Arrange
        final IntegerParser<TestCommandSender> parser = new IntegerParser<>(
                IntegerParser.DEFAULT_MINIMUM,
                IntegerParser.DEFAULT_MAXIMUM
        );

        final List<Suggestion> expectedSuggestions = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            expectedSuggestions.add(Suggestion.simple(Integer.toString(i)));
        }

        // Act
        final Iterable<Suggestion> suggestions = parser.suggestions(
                this.context,
                CommandInput.empty()
        );

        // Assert
        assertThat(suggestions).containsExactlyElementsIn(expectedSuggestions);
    }

    @Test
    void Suggestions_NegativeSignInput_ExpectedSuggestions() {
        // Arrange
        final IntegerParser<TestCommandSender> parser = new IntegerParser<>(
                IntegerParser.DEFAULT_MINIMUM,
                IntegerParser.DEFAULT_MAXIMUM
        );

        final List<Suggestion> expectedSuggestions = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            expectedSuggestions.add(Suggestion.simple(Integer.toString(-i)));
        }

        // Act
        final Iterable<Suggestion> suggestions = parser.suggestions(
                this.context,
                CommandInput.of("-")
        );

        // Assert
        assertThat(suggestions).containsExactlyElementsIn(expectedSuggestions);
    }
}
