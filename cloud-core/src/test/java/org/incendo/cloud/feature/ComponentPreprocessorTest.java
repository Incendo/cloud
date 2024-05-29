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
package org.incendo.cloud.feature;

import java.util.concurrent.CompletionException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.TestCommandSender;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.component.preprocessor.ComponentPreprocessor;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.execution.CommandResult;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;
import static org.incendo.cloud.util.TestUtils.createManager;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComponentPreprocessorTest {

    private CommandManager<TestCommandSender> commandManager;

    @BeforeEach void setup() {
        this.commandManager = createManager();
    }

    @Test
    void Parse_HappyFlow_Success() throws Exception {
        // Arrange
        final ComponentPreprocessor<TestCommandSender> preprocessor = new TestPreprocessor(true);
        this.commandManager.command(
                this.commandManager.commandBuilder("test")
                        .required(CommandComponent.<TestCommandSender, String>builder("arg",
                                stringParser()).preprocessor(preprocessor))
        );

        // Act
        final CommandResult<TestCommandSender> result =
                this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test abc").join();

        // Assert
        assertThat(result.commandContext().<String>get("arg")).isEqualTo("abc");
    }

    @Test
    void Parse_Failing_ThrowsParseException() throws Exception {
        // Arrange
        final ComponentPreprocessor<TestCommandSender> preprocessor = new TestPreprocessor(false);
        this.commandManager.command(
                this.commandManager.commandBuilder("test")
                        .required(CommandComponent.<TestCommandSender, String>builder("arg",
                                stringParser()).preprocessor(preprocessor))
        );

        // Act & Assert
        final CompletionException failure = assertThrows(CompletionException.class, () ->
                this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test abc").join());
        assertThat(failure).hasCauseThat().isInstanceOf(ArgumentParseException.class);
    }

    private static class TestPreprocessor implements ComponentPreprocessor<TestCommandSender> {

        private final boolean result;

        TestPreprocessor(final boolean result) {
            this.result = result;
        }

        @Override
        public @NonNull ArgumentParseResult<Boolean> preprocess(
                final @NonNull CommandContext<TestCommandSender> context,
                final @NonNull CommandInput commandInput
        ) {
            if (this.result) {
                return ArgumentParseResult.success(true);
            }
            return ArgumentParseResult.failure(new RuntimeException("no"));
        }
    }
}
