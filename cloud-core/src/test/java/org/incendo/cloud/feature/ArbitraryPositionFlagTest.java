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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionException;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.TestCommandSender;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.execution.CommandResult;
import org.incendo.cloud.parser.flag.CommandFlagParser;
import org.incendo.cloud.setting.ManagerSetting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static com.google.common.truth.Truth.assertThat;
import static org.incendo.cloud.parser.standard.StringParser.greedyFlagYieldingStringParser;
import static org.incendo.cloud.util.TestUtils.createManager;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArbitraryPositionFlagTest {

    private CommandManager<TestCommandSender> commandManager;

    @BeforeEach
    void setup() {
        this.commandManager = createManager();
        this.commandManager.settings().set(ManagerSetting.LIBERAL_FLAG_PARSING, true);

        this.commandManager.command(
                this.commandManager.commandBuilder("test")
                        .literal("literal")
                        .required("text", greedyFlagYieldingStringParser())
                        .flag(this.commandManager.flagBuilder("flag").withAliases("f")));
    }

    @Test
    void testParsingAllLocations() {
        List<String> passing = Arrays.asList(
                "test literal -f foo bar",
                "test literal foo bar -f",
                "test literal --flag foo bar",
                "test literal foo bar --flag");

        for (String cmd : passing) {
            CommandResult<TestCommandSender> result = this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), cmd).join();
            assertThat(result.commandContext().flags().isPresent("flag")).isEqualTo(true);
        }
    }

    @Test
    void testFailBeforeLiterals() {
        List<String> failing = Arrays.asList(
                "test -f literal foo bar",
                "test --flag literal foo bar");

        for (String cmd : failing) {
            assertThrows(CompletionException.class, commandExecutable(cmd));
        }
    }

    @Test
    void testMultiFlagThrows() {
        final CompletionException completionException = assertThrows(CompletionException.class,
                commandExecutable("test literal -f foo bar -f"));

        ThrowableSubject argParse = assertThat(completionException).hasCauseThat();
        argParse.isInstanceOf(ArgumentParseException.class);
        argParse.hasCauseThat().isInstanceOf(CommandFlagParser.FlagParseException.class);
    }

    private Executable commandExecutable(String cmd) {
        return () -> this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), cmd).join();
    }

}
