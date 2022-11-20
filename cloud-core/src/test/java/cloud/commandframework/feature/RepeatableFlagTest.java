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
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.execution.CommandResult;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth.assertThat;

class RepeatableFlagTest {

    private CommandManager<TestCommandSender> commandManager;

    @BeforeEach
    void setup() {
        this.commandManager = createManager();
    }

    @Test
    void testParsingRepeatingValueFlags() {
        // Arrange
        this.commandManager.command(
                this.commandManager.commandBuilder("test")
                        .flag(
                                this.commandManager.flagBuilder("flag")
                                        .asRepeatable()
                                        .withArgument(StringArgument.single("string"))
                        )
        );

        // Act
        final CommandResult<TestCommandSender> result = this.commandManager.executeCommand(
                new TestCommandSender(),
                "test --flag one --flag two --flag three"
        ).join();

        // Assert
        assertThat(result.getCommandContext().flags().getAll("flag")).containsExactly("one", "two", "three");
    }

    @Test
    void testParsingRepeatingPresenceFlags() {
        // Arrange
        this.commandManager.command(
                this.commandManager.commandBuilder("test")
                        .flag(
                                this.commandManager.flagBuilder("flag")
                                        .withAliases("f")
                                        .asRepeatable()
                        )
        );

        // Act
        final CommandResult<TestCommandSender> result = this.commandManager.executeCommand(
                new TestCommandSender(),
                "test --flag -fff"
        ).join();

        // Assert
        assertThat(result.getCommandContext().flags().count("flag")).isEqualTo(4);
    }

    @Test
    void testSuggestingRepeatableFlags() {
        // Arrange
        this.commandManager.command(
                this.commandManager.commandBuilder("test")
                        .flag(
                                this.commandManager.flagBuilder("flag")
                                        .withAliases("f")
                                        .asRepeatable()
                        )
        );

        // Act
        final List<String> suggestions = this.commandManager.suggest(
                new TestCommandSender(),
                "test --flag --"
        );

        // Assert
        assertThat(suggestions).containsExactly("--flag");
    }
}
