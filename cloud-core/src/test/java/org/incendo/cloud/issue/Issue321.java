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
package org.incendo.cloud.issue;

import org.incendo.cloud.CommandManager;
import org.incendo.cloud.TestCommandSender;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.CommandResult;
import org.incendo.cloud.parser.flag.FlagContext;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.incendo.cloud.parser.standard.StringArrayParser.flagYieldingStringArrayParser;
import static org.incendo.cloud.util.TestUtils.createManager;

/**
 * Test for https://github.com/Incendo/cloud/issues/321.
 */
class Issue321 {

    @Test
    void flagSucceedingFlagWithStringArrayArgument() {
        // Arrange
        final CommandManager<TestCommandSender> commandManager = createManager();
        commandManager.command(
                commandManager.commandBuilder("command")
                        .flag(commandManager.flagBuilder("flag1").withComponent(flagYieldingStringArrayParser()))
                        .flag(commandManager.flagBuilder("flag2").withComponent(flagYieldingStringArrayParser()))
        );

        // Act
        final CommandResult<TestCommandSender> result = commandManager.commandExecutor().executeCommand(
                new TestCommandSender(),
                "command --flag1 one two three --flag2 1 2 3"
        ).join();

        // Assert
        final CommandContext<TestCommandSender> context = result.commandContext();
        final FlagContext flags = context.flags();

        assertThat(flags.<String[]>getValue("flag1")).hasValue(new String[]{"one", "two", "three"});
        assertThat(flags.<String[]>getValue("flag2")).hasValue(new String[]{"1", "2", "3"});
    }
}
