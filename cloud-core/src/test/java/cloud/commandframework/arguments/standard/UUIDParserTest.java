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
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.parser.ArgumentParseResult;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static cloud.commandframework.truth.ArgumentParseResultSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;

@ExtendWith(MockitoExtension.class)
class UUIDParserTest {

    private UUIDParser<TestCommandSender> parser;

    @Mock
    private CommandContext<TestCommandSender> context;

    @BeforeEach
    void setup() {
        this.parser = new UUIDParser<>();
    }

    @Test
    void Parse_ValidUUID_SuccessfulParse() {
        // Arrange
        final UUID inputUUID = UUID.randomUUID();
        final CommandInput commandInput = CommandInput.of(inputUUID.toString());

        // Act
        final ArgumentParseResult<UUID> result = this.parser.parse(
                this.context,
                commandInput
        );

        // Assert
        assertThat(result).hasParsedValue(inputUUID);
        assertThat(commandInput.isEmpty()).isTrue();
    }

    @Test
    void Parse_NonUUID_FailedParse() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("non-uuid");

        // Act
        final ArgumentParseResult<UUID> result = this.parser.parse(
                this.context,
                commandInput
        );

        // Assert
        assertThat(result).hasFailure(new UUIDParser.UUIDParseException(
                "non-uuid",
                this.context
        ));
    }
}
