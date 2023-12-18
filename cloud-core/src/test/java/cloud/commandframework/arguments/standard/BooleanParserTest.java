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
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

@ExtendWith(MockitoExtension.class)
class BooleanParserTest {

    @Mock
    private CommandContext<TestCommandSender> context;

    @ParameterizedTest
    @MethodSource("Parse_NonLiberal_ValidInputs_SuccessfulParse_Source")
    void Parse_NonLiberal_ValidInputs_SuccessfulParse(
            final Queue<String> input,
            final boolean expectedResult
    ) {
        // Arrange
        final BooleanParser<TestCommandSender> parser = new BooleanParser<>(false /* liberal */);
        final CommandInput commandInput = CommandInput.of(input);

        // Act
        final ArgumentParseResult<Boolean> result = parser.parse(
                this.context,
                commandInput
        );

        // Assert
        assertThat(result.getFailure()).isEmpty();
        assertThat(result.getParsedValue()).hasValue(expectedResult);

        assertThat(commandInput.isEmpty()).isTrue();
    }

    static Stream<Arguments> Parse_NonLiberal_ValidInputs_SuccessfulParse_Source() {
        return Stream.of(
                Arguments.arguments(ArgumentTestHelper.linkedListOf("true"), true),
                Arguments.arguments(ArgumentTestHelper.linkedListOf("false"), false)
        );
    }

    @ParameterizedTest
    @MethodSource("Parse_Liberal_ValidInputs_SuccessfulParse_Source")
    void Parse_Liberal_ValidInputs_SuccessfulParse(
            final String input,
            final boolean expectedResult
    ) {
        // Arrange
        final BooleanParser<TestCommandSender> parser = new BooleanParser<>(true /* liberal */);
        final CommandInput commandInput = CommandInput.of(input);

        // Act
        final ArgumentParseResult<Boolean> result = parser.parse(
                this.context,
                commandInput
        );

        // Assert
        assertThat(result.getFailure()).isEmpty();
        assertThat(result.getParsedValue()).hasValue(expectedResult);

        assertThat(commandInput.isEmpty()).isTrue();
    }

    static Stream<Arguments> Parse_Liberal_ValidInputs_SuccessfulParse_Source() {
        return Stream.concat(
                Stream.of("true", "yes", "on")
                        .flatMap(input -> Stream.of(input, input.toUpperCase(Locale.ROOT)))
                        .map(input -> Arguments.arguments(input, true)),
                Stream.of("false", "no", "off")
                        .flatMap(input -> Stream.of(input, input.toUpperCase(Locale.ROOT)))
                        .map(input -> Arguments.arguments(input, false))
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void Parse_NonBooleanInput_FailedParse(final boolean liberal) {
        // Arrange
        final BooleanParser<TestCommandSender> parser = new BooleanParser<>(liberal);

        // Act
        final ArgumentParseResult<Boolean> result = parser.parse(
                this.context,
                CommandInput.of("not-a-boolean")
        );

        // Assert
        assertThat(result.getFailure()).hasValue(new BooleanParser.BooleanParseException(
                "not-a-boolean",
                liberal,
                this.context
        ));
        assertThat(result.getParsedValue()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("Suggestions_ExpectedSuggestions_Source")
    void Suggestions_ExpectedSuggestions(final boolean liberal, final List<Suggestion> expectedSuggestions) {
        // Arrange
        final BooleanParser<TestCommandSender> parser = new BooleanParser<>(liberal);

        // Act
        final Iterable<Suggestion> suggestions = parser.suggestions(
                this.context,
                ""
        );

        // Assert
        assertThat(suggestions).containsExactlyElementsIn(expectedSuggestions);
    }

    static Stream<Arguments> Suggestions_ExpectedSuggestions_Source() {
        return Stream.of(
                Arguments.arguments(false, ArgumentTestHelper.suggestionList("true", "false")),
                Arguments.arguments(true, ArgumentTestHelper.suggestionList("true", "yes", "on", "false", "no", "off"))
        );
    }
}
