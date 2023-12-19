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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static cloud.commandframework.truth.ArgumentParseResultSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;

@ExtendWith(MockitoExtension.class)
class StringParserTest {

    @Mock
    private CommandContext<TestCommandSender> context;

    @Test
    void Parse_GreedyFlagAwareLongFormFlag_EndsAfterFlag() {
        // Arrange
        final StringParser<TestCommandSender> parser = new StringParser<>(StringParser.StringMode.GREEDY_FLAG_YIELDING);
        final CommandInput commandInput = CommandInput.of(ArgumentTestHelper.linkedListOf(
                "this",
                "is",
                "a",
                "string",
                "--flag",
                "more",
                "flag",
                "content"
        ));

        // Act
        final ArgumentParseResult<String> result = parser.parse(
                this.context,
                commandInput
        );

        // Assert
        assertThat(result).hasParsedValue("this is a string");
        assertThat(commandInput.remainingInput()).isEqualTo("--flag more flag content");
    }

    @Test
    void Parse_GreedyFlagAwareShortFormFlag_EndsAfterFlag() {
        // Arrange
        final StringParser<TestCommandSender> parser = new StringParser<>(StringParser.StringMode.GREEDY_FLAG_YIELDING);
        final CommandInput commandInput = CommandInput.of(ArgumentTestHelper.linkedListOf(
                "this",
                "is",
                "a",
                "string",
                "-f",
                "-l",
                "-a",
                "-g"
        ));

        // Act
        final ArgumentParseResult<String> result = parser.parse(
                this.context,
                commandInput
        );

        // Assert
        assertThat(result).hasParsedValue("this is a string");
        assertThat(commandInput.remainingInput()).isEqualTo("-f -l -a -g");
    }
}
