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

import cloud.commandframework.TestCommandSender;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static cloud.commandframework.truth.ArgumentParseResultSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;

@ExtendWith(MockitoExtension.class)
class CharacterParserTest {

    private CharacterParser<TestCommandSender> parser;

    @Mock
    private CommandContext<TestCommandSender> context;

    @BeforeEach
    void setup() {
        this.parser = new CharacterParser<>();
    }

    @Test
    void Parse_ValidChar_SuccessfulParse() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("a");

        // Act
        final ArgumentParseResult<Character> result = this.parser.parse(
                this.context,
                commandInput
        );

        // Assert
        assertThat(result).hasParsedValue('a');
        assertThat(commandInput.isEmpty()).isTrue();
    }

    @Test
    void Parse_TooLongString_FailedParse() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("aa");

        // Act
        final ArgumentParseResult<Character> result = this.parser.parse(
                this.context,
                commandInput
        );

        // Assert
        assertThat(result).hasFailure(
                new CharacterParser.CharParseException(
                        "aa",
                        this.context
                )
        );
    }
}
