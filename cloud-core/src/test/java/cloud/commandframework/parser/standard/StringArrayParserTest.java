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
package cloud.commandframework.parser.standard;

import cloud.commandframework.TestCommandSender;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.parser.ArgumentParseResult;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static cloud.commandframework.truth.ArgumentParseResultSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;

@ExtendWith(MockitoExtension.class)
class StringArrayParserTest {

    private StringArrayParser<TestCommandSender> parser;

    @Mock
    private CommandContext<TestCommandSender> context;

    @BeforeEach
    void setup() {
        this.parser = new StringArrayParser<>();
    }

    @Test
    void Parse_RandomInput_CapturesAll() {
        // Arrange
        final LinkedList<String> input = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            input.add(
                    ThreadLocalRandom.current()
                            .ints()
                            .mapToObj(Integer::toString)
                            .limit(32)
                            .collect(Collectors.joining())
            );
        }
        final CommandInput commandInput = CommandInput.of(input);
        final LinkedList<String> inputCopy = new LinkedList<>(input);

        // Act
        final ArgumentParseResult<String[]> result = this.parser.parse(
                this.context,
                commandInput
        );

        // Assert
        assertThat(result).hasParsedValue(inputCopy.toArray(new String[0]));
        assertThat(commandInput.isEmpty()).isTrue();
    }

    @Test
    void Parse_GreedyFlagAwareLongFormFlag_EndsAfterFlag() {
        // Arrange
        final StringArrayParser<TestCommandSender> parser = new StringArrayParser<>(true);
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
        final ArgumentParseResult<String[]> result = parser.parse(
                this.context,
                commandInput
        );

        // Assert
        assertThat(result).hasParsedValue(new String[] { "this", "is", "a", "string" });
        assertThat(commandInput.remainingInput()).isEqualTo(" --flag more flag content");
    }

    @Test
    void Parse_GreedyFlagAwareShortFormFlag_EndsAfterFlag() {
        // Arrange
        final StringArrayParser<TestCommandSender> parser = new StringArrayParser<>(true);
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
        final ArgumentParseResult<String[]> result = parser.parse(
                this.context,
                commandInput
        );

        // Assert
        assertThat(result).hasParsedValue(new String[] { "this", "is", "a", "string" });
        assertThat(commandInput.remainingInput()).isEqualTo(" -f -l -a -g");
    }
}
