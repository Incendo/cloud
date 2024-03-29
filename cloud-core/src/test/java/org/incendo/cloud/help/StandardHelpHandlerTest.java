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
package org.incendo.cloud.help;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.TestCommandSender;
import org.incendo.cloud.help.result.CommandEntry;
import org.incendo.cloud.help.result.HelpQueryResult;
import org.incendo.cloud.help.result.IndexCommandResult;
import org.incendo.cloud.help.result.MultipleCommandResult;
import org.incendo.cloud.help.result.VerboseCommandResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.google.common.truth.Truth.assertThat;
import static org.incendo.cloud.description.CommandDescription.commandDescription;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;
import static org.incendo.cloud.util.TestUtils.createManager;

class StandardHelpHandlerTest {

    private CommandManager<TestCommandSender> commandManager;
    private HelpHandler<TestCommandSender> helpHandler;

    @BeforeEach
    void setup() {
        this.commandManager = createManager();
        this.helpHandler = this.commandManager.createHelpHandler();

        this.commandManager.command(
                this.commandManager.commandBuilder("test")
                        .commandDescription(commandDescription("Command with only literals"))
                        .literal("foo")
                        .literal("bar")
        );
        this.commandManager.command(
                this.commandManager.commandBuilder("test")
                        .commandDescription(commandDescription("Command with variables"))
                        .literal("int")
                        .required("int", integerParser())
        );
        this.commandManager.command(
                this.commandManager.commandBuilder("test")
                        .commandDescription(commandDescription("Command with only literals"))
                        .required("string", stringParser())
        );
        this.commandManager.command(this.commandManager.commandBuilder("other"));
    }

    @Test
    void testRootIndex() {
        // Act
        final IndexCommandResult<?> result = this.helpHandler.queryRootIndex(new TestCommandSender());

        // Assert
        assertThat(result.entries()).hasSize(4);
        assertThat(result.entries().stream().map(CommandEntry::syntax)).containsExactly(
                "test <string>",
                "test int <int>",
                "test foo bar",
                "other"
        );
    }

    @ParameterizedTest
    @CsvSource({
            "test int, test int <int>",
            "test int <int>, test int <int>",
            "test foo, test foo bar",
            "test foo bar, test foo bar",
            "test <string>, test <string>"
    })
    void testVerbose(final @NonNull String queryString, final @NonNull String syntax) {
        // Arrange
        final HelpQuery<TestCommandSender> query = HelpQuery.of(new TestCommandSender(), queryString);

        // Act
        final HelpQueryResult<?> result = this.helpHandler.query(query);

        // Assert
        assertThat(result).isInstanceOf(VerboseCommandResult.class);
        assertThat(result.query()).isEqualTo(query);

        final VerboseCommandResult<?> verboseCommandResult = (VerboseCommandResult<?>) result;
        assertThat(verboseCommandResult.entry().syntax()).isEqualTo(syntax);
    }

    @Test
    void testMulti() {
        // Arrange
        final HelpQuery<TestCommandSender> query = HelpQuery.of(new TestCommandSender(), "test");

        // Act
        final HelpQueryResult<?> result = this.helpHandler.query(query);

        // Assert
        assertThat(result).isInstanceOf(MultipleCommandResult.class);
        assertThat(result.query()).isEqualTo(query);

        final MultipleCommandResult<?> multipleCommandResult = (MultipleCommandResult<?>) result;
        assertThat(multipleCommandResult.longestPath()).isEqualTo("test");
        assertThat(multipleCommandResult.childSuggestions()).containsExactly(
                "test foo bar",
                "test int <int>",
                "test <string>"
        );
    }

    @Test
    void testFilter() {
        // Arrange
        final HelpHandler<TestCommandSender> helpHandler = this.commandManager.createHelpHandler(
                command -> command.toString().startsWith("test ") && !command.toString().endsWith(" bar")
        );

        // Act
        final IndexCommandResult<?> result = helpHandler.queryRootIndex(new TestCommandSender());

        // Assert
        assertThat(result.entries().stream().map(CommandEntry::syntax)).containsExactly(
                "test <string>",
                "test int <int>"
        );
    }
}
