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
package org.incendo.cloud.parser.aggregate;

import java.util.Arrays;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.TestCommandSender;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.context.StandardCommandContextFactory;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;
import static org.incendo.cloud.truth.ArgumentParseResultSubject.assertThat;
import static org.incendo.cloud.util.TestUtils.createManager;

class AggregateParserTest {

    private CommandContext<TestCommandSender> commandContext;

    @BeforeEach
    void setup() {
        final CommandManager<TestCommandSender> commandManager = createManager();
        this.commandContext = new StandardCommandContextFactory<>(commandManager).create(false, new TestCommandSender());
    }

    @Test
    void testParsing() {
        // Arrange
        final AggregateParser<TestCommandSender, OutputType> parser = AggregateParser.<TestCommandSender>builder()
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
        final AggregateParser<TestCommandSender, OutputType> parser = AggregateParser.<TestCommandSender>builder()
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
        assertThat(outputType).hasFailureThat().isInstanceOf(AggregateParser.AggregateParseException.class);
        assertThat(outputType).hasFailureThat().hasCauseThat().isInstanceOf(IntegerParser.IntegerParseException.class);
    }

    @Test
    void testMultiLevelAggregateParsing() {
        // Arrange
        final AggregateParser<TestCommandSender, OutputType> inner = AggregateParser.<TestCommandSender>builder()
                .withComponent("number", integerParser())
                .withComponent("string", stringParser())
                .withMapper(
                        OutputType.class,
                        (commandContext, context) ->
                                ArgumentParseResult.successFuture(
                                        new OutputType(context.get("number"), context.get("string")))
                ).build();
        final AggregateParser<TestCommandSender, OutputType> parser = AggregateParser.<TestCommandSender>builder()
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
        final AggregateParser<TestCommandSender, OutputType> parser = AggregateParser.<TestCommandSender>builder()
                .withComponent("number", integerParser(), SuggestionProvider.blocking((ctx, in) -> Arrays.asList(
                        Suggestion.suggestion("1"),
                        Suggestion.suggestion("2"),
                        Suggestion.suggestion("3")
                ))).withComponent("string", stringParser())
                .withMapper(
                        OutputType.class,
                        (commandContext, context) ->
                                ArgumentParseResult.successFuture(
                                        new OutputType(context.get("number"), context.get("string"))))
                .build();

        // Act
        final Iterable<? extends Suggestion> suggestions = parser.suggestionProvider()
                .suggestionsFuture(this.commandContext, CommandInput.empty()).join();

        // Assert
        assertThat(suggestions).containsExactly(
                Suggestion.suggestion("1"),
                Suggestion.suggestion("2"),
                Suggestion.suggestion("3")
        );
    }

    @Test
    void testSuggestionsSecondArgument() {
        // Arrange
        final AggregateParser<TestCommandSender, OutputType> parser = AggregateParser.<TestCommandSender>builder()
                .withComponent("number", integerParser())
                .withComponent("string", stringParser(), SuggestionProvider.blocking((ctx, in) -> Arrays.asList(
                        Suggestion.suggestion("a"),
                        Suggestion.suggestion("b"),
                        Suggestion.suggestion("c")
                )))
                .withMapper(
                        OutputType.class,
                        (commandContext, context) ->
                                ArgumentParseResult.successFuture(
                                        new OutputType(context.get("number"), context.get("string"))))
                .build();

        // Act
        final Iterable<? extends Suggestion> suggestions = parser.suggestionProvider()
                .suggestionsFuture(this.commandContext, CommandInput.of("123 ")).join();

        // Assert
        assertThat(suggestions).containsExactly(
                Suggestion.suggestion("123 a"),
                Suggestion.suggestion("123 b"),
                Suggestion.suggestion("123 c")
        );
    }

    @Test
    void testFailureMissingInput() {
        // Arrange
        final AggregateParser<TestCommandSender, OutputType> parser = AggregateParser.<TestCommandSender>builder()
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
        assertThat(outputType).hasFailureThat().isInstanceOf(AggregateParser.AggregateParseException.class);
        final ParserException parserException = (ParserException) outputType.failure().get();
        assertThat(parserException.errorCaption()).isEqualTo(StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_AGGREGATE_MISSING_INPUT);
        assertThat(parserException.getMessage()).isEqualTo("Missing component 'number'");
    }

    @Test
    void testFailureComponentParsingFailure() {
        // Arrange
        final AggregateParser<TestCommandSender, OutputType> parser = AggregateParser.<TestCommandSender>builder()
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
        assertThat(outputType).hasFailureThat().isInstanceOf(AggregateParser.AggregateParseException.class);
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
