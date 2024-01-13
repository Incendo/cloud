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
package cloud.commandframework.arguments.aggregate;

import cloud.commandframework.CommandManager;
import cloud.commandframework.TestCommandSender;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.standard.IntegerParser;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.context.StandardCommandContextFactory;
import cloud.commandframework.exceptions.parsing.ParserException;
import java.util.Arrays;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.arguments.standard.IntegerParser.integerParser;
import static cloud.commandframework.arguments.standard.StringParser.stringParser;
import static cloud.commandframework.truth.ArgumentParseResultSubject.assertThat;
import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

class AggregateCommandParserTest {

    private CommandContext<TestCommandSender> commandContext;

    @BeforeEach
    void setup() {
        final CommandManager<TestCommandSender> commandManager = createManager();
        this.commandContext = new StandardCommandContextFactory<>(commandManager).create(false, new TestCommandSender());
    }

    @Test
    void testParsing() {
        // Arrange
        final AggregateCommandParser<TestCommandSender, OutputType> parser = AggregateCommandParser.<TestCommandSender>builder()
                .withComponent("number", integerParser())
                .withComponent("string", stringParser())
                .withMapper(
                        OutputType.class,
                        (commandContext, context) -> ArgumentParseResult.successFuture(
                                new OutputType(context.get("number"), context.get("string"))))
                .build();

        // Act
        final ArgumentParseResult<OutputType> outputType =
                parser.parseFuture(this.commandContext, CommandInput.of("10 abc")).join();

        // Assert
        assertThat(outputType).isNotNull();
        assertThat(outputType).hasParsedValue(new OutputType(10, "abc"));
    }

    @Test
    void testExceptionForwarding() {
        // Arrange
        final AggregateCommandParser<TestCommandSender, OutputType> parser = AggregateCommandParser.<TestCommandSender>builder()
                .withComponent("number", integerParser())
                .withComponent("string", stringParser())
                .withMapper(
                        OutputType.class,
                        (commandContext, context) -> ArgumentParseResult.successFuture(
                                new OutputType(context.get("number"), context.get("string"))))
                .build();

        // Act
        final ArgumentParseResult<OutputType> outputType =
                parser.parseFuture(this.commandContext, CommandInput.of("abc abc")).join();

        // Assert
        assertThat(outputType).hasFailureThat().isInstanceOf(AggregateCommandParser.AggregateParseException.class);
        assertThat(outputType).hasFailureThat().hasCauseThat().isInstanceOf(IntegerParser.IntegerParseException.class);
    }

    @Test
    void testMultiLevelAggregateParsing() {
        // Arrange
        final AggregateCommandParser<TestCommandSender, OutputType> inner = AggregateCommandParser.<TestCommandSender>builder()
                .withComponent("number", integerParser())
                .withComponent("string", stringParser())
                .withMapper(
                        OutputType.class,
                        (commandContext, context) ->
                                ArgumentParseResult.successFuture(
                                        new OutputType(context.get("number"), context.get("string")))
                ).build();
        final AggregateCommandParser<TestCommandSender, OutputType> parser = AggregateCommandParser.<TestCommandSender>builder()
                .withComponent("inner", inner)
                .withMapper(
                        OutputType.class,
                        (commandContext, context) -> ArgumentParseResult.successFuture(context.get("inner"))
                ).build();

        // Act
        final ArgumentParseResult<OutputType> outputType = parser.parseFuture(
                this.commandContext, CommandInput.of("10 abc")).join();

        // Assert
        assertThat(outputType).isNotNull();
        assertThat(outputType).hasParsedValue(new OutputType(10, "abc"));
    }

    @Test
    void testSuggestionsFirstArgument() {
        // Arrange
        final AggregateCommandParser<TestCommandSender, OutputType> parser = AggregateCommandParser.<TestCommandSender>builder()
                .withComponent("number", integerParser(), SuggestionProvider.blocking((ctx, in) -> Arrays.asList(
                        Suggestion.simple("1"),
                        Suggestion.simple("2"),
                        Suggestion.simple("3")
                ))).withComponent("string", stringParser())
                .withMapper(
                        OutputType.class,
                        (commandContext, context) ->
                                ArgumentParseResult.successFuture(
                                        new OutputType(context.get("number"), context.get("string"))))
                .build();

        // Act
        final Iterable<Suggestion> suggestions = parser.suggestionProvider()
                .suggestionsFuture(this.commandContext, CommandInput.empty()).join();

        // Assert
        assertThat(suggestions).containsExactly(
                Suggestion.simple("1"),
                Suggestion.simple("2"),
                Suggestion.simple("3")
        );
    }

    @Test
    void testSuggestionsSecondArgument() {
        // Arrange
        final AggregateCommandParser<TestCommandSender, OutputType> parser = AggregateCommandParser.<TestCommandSender>builder()
                .withComponent("number", integerParser())
                .withComponent("string", stringParser(), SuggestionProvider.blocking((ctx, in) -> Arrays.asList(
                        Suggestion.simple("a"),
                        Suggestion.simple("b"),
                        Suggestion.simple("c")
                )))
                .withMapper(
                        OutputType.class,
                        (commandContext, context) ->
                                ArgumentParseResult.successFuture(
                                        new OutputType(context.get("number"), context.get("string"))))
                .build();

        // Act
        final Iterable<Suggestion> suggestions = parser.suggestionProvider()
                .suggestionsFuture(this.commandContext, CommandInput.of("123 ")).join();

        // Assert
        assertThat(suggestions).containsExactly(
                Suggestion.simple("123 a"),
                Suggestion.simple("123 b"),
                Suggestion.simple("123 c")
        );
    }

    @Test
    void testFailureMissingInput() {
        // Arrange
        final AggregateCommandParser<TestCommandSender, OutputType> parser = AggregateCommandParser.<TestCommandSender>builder()
                .withComponent("number", integerParser())
                .withComponent("string", stringParser())
                .withMapper(
                        OutputType.class,
                        (commandContext, context) -> ArgumentParseResult.successFuture(
                                new OutputType(context.get("number"), context.get("string"))))
                .build();

        // Act
        final ArgumentParseResult<OutputType> outputType =
                parser.parseFuture(this.commandContext, CommandInput.empty()).join();

        // Assert
        assertThat(outputType).hasFailureThat().isInstanceOf(AggregateCommandParser.AggregateParseException.class);
        final ParserException parserException = (ParserException) outputType.failure().get();
        assertThat(parserException.errorCaption()).isEqualTo(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_AGGREGATE_MISSING_INPUT);
        assertThat(parserException.getMessage()).isEqualTo("Missing component 'number'");
    }

    @Test
    void testFailureComponentParsingFailure() {
        // Arrange
        final AggregateCommandParser<TestCommandSender, OutputType> parser = AggregateCommandParser.<TestCommandSender>builder()
                .withComponent("number", integerParser())
                .withComponent("string", stringParser())
                .withMapper(
                        OutputType.class,
                        (commandContext, context) -> ArgumentParseResult.successFuture(
                                new OutputType(context.get("number"), context.get("string"))))
                .build();

        // Act
        final ArgumentParseResult<OutputType> outputType =
                parser.parseFuture(this.commandContext, CommandInput.of("abc")).join();

        // Assert
        assertThat(outputType).hasFailureThat().isInstanceOf(AggregateCommandParser.AggregateParseException.class);
        final ParserException parserException = (ParserException) outputType.failure().get();
        assertThat(parserException.errorCaption())
                .isEqualTo(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_AGGREGATE_COMPONENT_FAILURE);
        assertThat(parserException.getMessage()).startsWith("Invalid component 'number':");
    }


    private static final class OutputType {

        private final int number;
        private final String string;

        OutputType(final int number, final @NonNull String string) {
            this.number = number;
            this.string = string;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final OutputType that = (OutputType) o;
            return this.number == that.number && Objects.equals(this.string, that.string);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.number, this.string);
        }
    }
}
