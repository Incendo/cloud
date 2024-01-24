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
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.parser.ArgumentParseResult;
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
class ByteParserTest {

    @Mock
    private CommandContext<TestCommandSender> context;

    @Test
    void Parse_NoMinMax_SuccessfulParse() {
        // Arrange
        final ByteParser<TestCommandSender> parser = new ByteParser<>(
                ByteParser.DEFAULT_MINIMUM,
                ByteParser.DEFAULT_MAXIMUM
        );

        final byte byteInput = (byte) ThreadLocalRandom.current().nextInt(Byte.MAX_VALUE);
        final CommandInput commandInput = CommandInput.of(Byte.toString(byteInput));

        // Act
        final ArgumentParseResult<Byte> result = parser.parse(
                this.context,
                commandInput
        );

        // Assert
        assertThat(result).hasParsedValue(byteInput);
        assertThat(commandInput.isEmpty()).isTrue();
    }

    @Test
    void Parse_ValueBelowMin_FailedParse() {
        // Arrange
        final ByteParser<TestCommandSender> parser = new ByteParser<>(
                (byte) 5 /* min */,
                ByteParser.DEFAULT_MAXIMUM
        );

        // Act
        final ArgumentParseResult<Byte> result = parser.parse(
                this.context,
                CommandInput.of("4")
        );

        // Assert
        assertThat(result).hasFailure(
                new ByteParser.ByteParseException(
                        "4",
                        parser,
                        this.context
                )
        );
    }

    @Test
    void Parse_ValueAboveMax_FailedParse() {
        // Arrange
        final ByteParser<TestCommandSender> parser = new ByteParser<>(
                ByteParser.DEFAULT_MINIMUM,
                (byte) 5 /* max */
        );

        // Act
        final ArgumentParseResult<Byte> result = parser.parse(
                this.context,
                CommandInput.of("6")
        );

        // Assert
        assertThat(result).hasFailure(
                new ByteParser.ByteParseException(
                        "6",
                        parser,
                        this.context
                )
        );
    }

    @Test
    void Parse_NonByteInput_FailedParse() {
        // Arrange
        final ByteParser<TestCommandSender> parser = new ByteParser<>(
                ByteParser.DEFAULT_MINIMUM,
                ByteParser.DEFAULT_MAXIMUM
        );

        // Act
        final ArgumentParseResult<Byte> result = parser.parse(
                this.context,
                CommandInput.of("cow")
        );

        // Assert
        assertThat(result).hasFailure(
                new ByteParser.ByteParseException(
                        "cow",
                        parser,
                        this.context
                )
        );
    }

    @Test
    void Suggestions_EmptyInput_ExpectedSuggestions() {
        // Arrange
        final ByteParser<TestCommandSender> parser = new ByteParser<>(
                ByteParser.DEFAULT_MINIMUM,
                ByteParser.DEFAULT_MAXIMUM
        );

        final List<Suggestion> expectedSuggestions = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            expectedSuggestions.add(Suggestion.simple(Byte.toString((byte) i)));
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
        final ByteParser<TestCommandSender> parser = new ByteParser<>(
                ByteParser.DEFAULT_MINIMUM,
                ByteParser.DEFAULT_MAXIMUM
        );

        final List<Suggestion> expectedSuggestions = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            expectedSuggestions.add(Suggestion.simple(Byte.toString((byte) -i)));
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
