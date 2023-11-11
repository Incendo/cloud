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
package cloud.commandframework.feature;

import cloud.commandframework.CommandManager;
import cloud.commandframework.TestCommandSender;
import cloud.commandframework.arguments.compound.FlagArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.execution.CommandResult;
import com.google.common.truth.ThrowableSubject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArbitraryPositionFlagTest {

    private CommandManager<TestCommandSender> commandManager;

    @BeforeEach
    void setup() {
        this.commandManager = createManager();
        this.commandManager.setSetting(CommandManager.ManagerSettings.LIBERAL_FLAG_PARSING, true);

        this.commandManager.command(
                this.commandManager.commandBuilder("test")
                        .literal("literal")
                        .required(StringArgument.greedyFlagYielding("text"))
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
            CommandResult<TestCommandSender> result = this.commandManager.executeCommand(new TestCommandSender(), cmd).join();
            assertThat(result.getCommandContext().flags().isPresent("flag")).isEqualTo(true);
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
        argParse.hasCauseThat().isInstanceOf(FlagArgument.FlagParseException.class);
    }

    private Executable commandExecutable(String cmd) {
        return () -> this.commandManager.executeCommand(new TestCommandSender(), cmd).join();
    }

}
