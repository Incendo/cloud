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
import org.incendo.cloud.type.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.google.common.truth.Truth.assertThat;
import static org.incendo.cloud.truth.ArgumentParseResultSubject.assertThat;

@ExtendWith(MockitoExtension.class)
class EitherParserTest {

    @Mock
    private CommandContext<TestCommandSender> context;

    private EitherParser<TestCommandSender, Integer, Boolean> parser;

    @BeforeEach
    void setup() {
        this.parser = new EitherParser<>(
                IntegerParser.integerParser(1, 3),
                BooleanParser.booleanParser(false)
        );
    }

    @Test
    void testParsingSuccessfulPrimary() {
        // Arrange
        final CommandInput input = CommandInput.of("1");

        // Act
        final ArgumentParseResult<Either<Integer, Boolean>> result = this.parser.parseFuture(this.context, input).join();

        // Assert
        assertThat(result).hasParsedValue(Either.ofPrimary(1));
    }

    @Test
    void testParsingSuccessfulFallback() {
        // Arrange
        final CommandInput input = CommandInput.of("false");

        // Act
        final ArgumentParseResult<Either<Integer, Boolean>> result = this.parser.parseFuture(this.context, input).join();

        // Assert
        assertThat(result).hasParsedValue(Either.ofFallback(false));
    }

    @Test
    void testParsingFailing() {
        // Arrange
        final CommandInput input = CommandInput.of("sausage");

        // Act
        final ArgumentParseResult<Either<Integer, Boolean>> result = this.parser.parseFuture(this.context, input).join();

        // Assert
        assertThat(result).hasFailureThat().isInstanceOf(EitherParser.EitherParseException.class);
    }

    @Test
    void testSuggestions() {
        // Act
        final Iterable<? extends Suggestion> suggestions = this.parser.suggestionsFuture(this.context, CommandInput.empty()).join();

        // Assert
        assertThat(suggestions).containsExactly(
                Suggestion.simple("1"),
                Suggestion.simple("2"),
                Suggestion.simple("3"),
                Suggestion.simple("true"),
                Suggestion.simple("false")
        );
    }
}
