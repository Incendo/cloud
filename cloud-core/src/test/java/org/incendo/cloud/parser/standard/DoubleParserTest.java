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

import java.util.concurrent.ThreadLocalRandom;
import org.incendo.cloud.TestCommandSender;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.google.common.truth.Truth.assertThat;
import static org.incendo.cloud.truth.ArgumentParseResultSubject.assertThat;

@ExtendWith(MockitoExtension.class)
class DoubleParserTest {

    @Mock
    private CommandContext<TestCommandSender> context;

    @Test
    void Parse_NoMinMax_SuccessfulParse() {
        // Arrange
        final DoubleParser<TestCommandSender> parser = new DoubleParser<>(
                DoubleParser.DEFAULT_MINIMUM,
                DoubleParser.DEFAULT_MAXIMUM
        );

        final double doubleInput = ThreadLocalRandom.current().nextDouble();
        final CommandInput commandInput = CommandInput.of(Double.toString(doubleInput));

        // Act
        final ArgumentParseResult<Double> result = parser.parse(
                this.context,
                commandInput
        );

        // Assert
        assertThat(result).hasParsedValue(doubleInput);
        assertThat(commandInput.isEmpty()).isTrue();
    }

    @Test
    void Parse_ValueBelowMin_FailedParse() {
        // Arrange
        final DoubleParser<TestCommandSender> parser = new DoubleParser<>(
                5 /* min */,
                DoubleParser.DEFAULT_MAXIMUM
        );

        // Act
        final ArgumentParseResult<Double> result = parser.parse(
                this.context,
                CommandInput.of("4.0")
        );

        // Assert
        assertThat(result).hasFailure(
                new DoubleParser.DoubleParseException(
                        "4.0",
                        parser,
                        this.context
                )
        );
    }

    @Test
    void Parse_ValueAboveMax_FailedParse() {
        // Arrange
        final DoubleParser<TestCommandSender> parser = new DoubleParser<>(
                DoubleParser.DEFAULT_MINIMUM,
                5.0D /* max */
        );

        // Act
        final ArgumentParseResult<Double> result = parser.parse(
                this.context,
                CommandInput.of("6.0")
        );

        // Assert
        assertThat(result).hasFailure(
                new DoubleParser.DoubleParseException(
                        "6.0",
                        parser,
                        this.context
                )
        );
    }

    @Test
    void Parse_NonDoubleInput_FailedParse() {
        // Arrange
        final DoubleParser<TestCommandSender> parser = new DoubleParser<>(
                DoubleParser.DEFAULT_MINIMUM,
                DoubleParser.DEFAULT_MAXIMUM
        );

        // Act
        final ArgumentParseResult<Double> result = parser.parse(
                this.context,
                CommandInput.of("cow")
        );

        // Assert
        assertThat(result).hasFailure(
                new DoubleParser.DoubleParseException(
                        "cow",
                        parser,
                        this.context
                )
        );
    }
}
