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
package cloud.commandframework.issue;

import cloud.commandframework.CommandManager;
import cloud.commandframework.TestCommandSender;
import cloud.commandframework.arguments.flags.FlagContext;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandResult;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.arguments.standard.StringArrayParser.flagYieldingStringArrayParser;
import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth8.assertThat;

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
        final CommandResult<TestCommandSender> result = commandManager.executeCommand(
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
