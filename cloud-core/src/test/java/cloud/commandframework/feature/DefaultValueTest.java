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
package cloud.commandframework.feature;

import cloud.commandframework.CommandManager;
import cloud.commandframework.TestCommandSender;
import cloud.commandframework.arguments.DefaultValue;
import cloud.commandframework.execution.CommandResult;
import cloud.commandframework.keys.CloudKey;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.parser.standard.IntegerParser.integerParser;
import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth.assertThat;

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
        final DefaultValue<TestCommandSender, Integer> defaultValue = ctx -> ThreadLocalRandom.current().nextInt();
        this.commandManager.command(
                this.commandManager.commandBuilder("test").optional(key, integerParser(), defaultValue)
        );

        // Act
        final CommandResult<TestCommandSender> result = this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test").get();

        // Assert
        assertThat(result.commandContext().get(key)).isNotNull();
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
