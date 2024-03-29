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
package org.incendo.cloud.context;

import org.incendo.cloud.CommandManager;
import org.incendo.cloud.TestCommandSender;
import org.incendo.cloud.execution.CommandResult;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.incendo.cloud.parser.standard.StringParser.greedyStringParser;
import static org.incendo.cloud.util.TestUtils.createManager;

class ParsingContextTest {

    private CommandManager<TestCommandSender> commandManager;

    @BeforeEach
    void setup() {
        this.commandManager = createManager();
    }

    @Test
    void testConsumedInput() throws Exception {
        // Arrange
        this.commandManager.command(
                this.commandManager.commandBuilder("test", "t")
                        .required("int", IntegerParser.integerParser())
                        .required("string", greedyStringParser())
        );
        final String commandInput = "t 1337 roflmao xd";

        // Act
        final CommandResult<TestCommandSender> result = this.commandManager.commandExecutor().executeCommand(
                new TestCommandSender(),
                commandInput
        ).get();

        // Assert
        final CommandContext<TestCommandSender> context = result.commandContext();
        assertThat(context.parsingContext("test").consumedInput()).isEqualTo("t");
        assertThat(context.parsingContext("int").consumedInput()).isEqualTo("1337");
        assertThat(context.parsingContext("string").consumedInput()).isEqualTo("roflmao xd");
    }

    @Test
    void testExactAlias() throws Exception {
        // Arrange
        this.commandManager.command(
                this.commandManager.commandBuilder("test", "t")
                        .literal("foo", "f")
                        .literal("bar", "b")
        );
        final String commandInput = "t f bar";

        // Act
        final CommandResult<TestCommandSender> result = this.commandManager.commandExecutor().executeCommand(
                new TestCommandSender(),
                commandInput
        ).get();

        // Assert
        final CommandContext<TestCommandSender> context = result.commandContext();
        assertThat(context.parsingContext("test").exactAlias()).isEqualTo("t");
        assertThat(context.parsingContext("foo").exactAlias()).isEqualTo("f");
        assertThat(context.parsingContext("bar").exactAlias()).isEqualTo("bar");
    }
}
