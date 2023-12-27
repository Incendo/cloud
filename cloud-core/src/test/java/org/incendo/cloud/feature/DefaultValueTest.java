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

import com.google.common.truth.ThrowableSubject;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadLocalRandom;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.TestCommandSender;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.execution.CommandResult;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.util.TestUtils.createManager;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultValueTest {

    private CommandManager<TestCommandSender> commandManager;

    @BeforeEach
    void setup() {
        this.commandManager = createManager();
    }

    @Test
    void Constant_HappyFlow_Success() throws Exception {
        // Arrange
        final CloudKey<Integer> key = CloudKey.of("int", Integer.class);
        this.commandManager.command(
                this.commandManager.commandBuilder("test").optional(key, integerParser(), DefaultValue.constant(5))
        );

        // Act
        final CommandResult<TestCommandSender> result = this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test").get();

        // Assert
        assertThat(result.commandContext().get(key)).isEqualTo(5);
    }

    @Test
    void Dynamic_HappyFlow_Success() throws Exception {
        // Arrange
        final CloudKey<Integer> key = CloudKey.of("int", Integer.class);
        final DefaultValue<TestCommandSender, Integer> defaultValue =
                ctx -> ArgumentParseResult.success(ThreadLocalRandom.current().nextInt());
        this.commandManager.command(
                this.commandManager.commandBuilder("test").optional(key, integerParser(), defaultValue)
        );

        // Act
        final CommandResult<TestCommandSender> result = this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test").get();

        // Assert
        assertThat(result.commandContext().get(key)).isNotNull();
    }

    @Test
    void Dynamic_HappyFlow_Failure() {
        // Arrange
        final CloudKey<Integer> key = CloudKey.of("int", Integer.class);
        final DefaultValue<TestCommandSender, Integer> defaultValue =
                ctx -> ArgumentParseResult.failure(new RuntimeException("Oops this argument isn't really optional :D"));
        this.commandManager.command(
                this.commandManager.commandBuilder("test").optional(key, integerParser(), defaultValue)
        );

        // Act & Assert
        final CompletionException completionException = assertThrows(CompletionException.class, () -> {
            this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test").join();
        });
        final ThrowableSubject argParse = assertThat(completionException).hasCauseThat();
        argParse.isInstanceOf(ArgumentParseException.class);
        argParse.hasCauseThat().isInstanceOf(RuntimeException.class);
    }

    @Test
    void Parsed_HappyFlow_Success() throws Exception {
        // Arrange
        final CloudKey<Integer> key = CloudKey.of("int", Integer.class);
        this.commandManager.command(
                this.commandManager.commandBuilder("test").optional(key, integerParser(), DefaultValue.parsed("5"))
        );

        // Act
        final CommandResult<TestCommandSender> result = this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test").get();

        // Assert
        assertThat(result.commandContext().get(key)).isEqualTo(5);
    }
}
