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
package cloud.commandframework.arguments.standard;

import cloud.commandframework.CommandManager;
import cloud.commandframework.TestCommandSender;
import cloud.commandframework.execution.CommandResult;
import cloud.commandframework.keys.CloudKey;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.arguments.standard.StringParser.greedyStringParser;
import static cloud.commandframework.arguments.standard.StringParser.quotedStringParser;
import static cloud.commandframework.arguments.standard.StringParser.stringParser;
import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth.assertThat;

class StringArgumentTest {

    private static final CloudKey<String> MESSAGE1_KEY = CloudKey.of(
            "message1",
            String.class
    );
    private static final CloudKey<String> MESSAGE2_KEY = CloudKey.of(
            "message2",
            String.class
    );

    private CommandManager<TestCommandSender> manager;

    @BeforeEach
    void setup() {
        this.manager = createManager();
    }

    @Test
    void single_single() {
        // Arrange
        this.manager.command(this.manager.commandBuilder("single")
                .required(MESSAGE1_KEY, stringParser())
                .build());

        // Act
        final CommandResult<?> result = this.manager.commandExecutor().executeCommand(new TestCommandSender(), "single string").join();

        // Assert
        assertThat(result.commandContext().get(MESSAGE1_KEY)).isEqualTo("string");
    }

    @Test
    void quoted_single_quoted_string_containing_double_quote_followed_by_unquoted() {
        // Arrange
        this.manager.command(this.manager.commandBuilder("quoted")
                .required(MESSAGE1_KEY, quotedStringParser())
                .required(MESSAGE2_KEY, stringParser())
                .build());

        // Act
        final CommandResult<?> result = this.manager.commandExecutor().executeCommand(new TestCommandSender(),
                "quoted 'quoted \" string' unquoted").join();

        // Assert
        assertThat(result.commandContext().get(MESSAGE1_KEY)).isEqualTo("quoted \" string");
        assertThat(result.commandContext().get(MESSAGE2_KEY)).isEqualTo("unquoted");
    }

    @Test
    void quoted_unquoted_strings() {
        // Arrange
        this.manager.command(this.manager.commandBuilder("quoted")
                .required(MESSAGE1_KEY, quotedStringParser())
                .required(MESSAGE2_KEY, stringParser())
                .build());

        // Act
        final CommandResult<?> result = this.manager.commandExecutor().executeCommand(new TestCommandSender(), "quoted quoted unquoted").join();

        // Assert
        assertThat(result.commandContext().get(MESSAGE1_KEY)).isEqualTo("quoted");
        assertThat(result.commandContext().get(MESSAGE2_KEY)).isEqualTo("unquoted");
    }

    @Test
    void quoted_quoted_string_containing_escaped_quote_followed_by_unquoted() {
        // Arrange
        this.manager.command(this.manager.commandBuilder("quoted")
                .required(MESSAGE1_KEY, quotedStringParser())
                .required(MESSAGE2_KEY, stringParser())
                .build());

        // Act
        final CommandResult<?> result = this.manager.commandExecutor().executeCommand(new TestCommandSender(),
                "quoted \"quoted \\\" string\" unquoted").join();

        // Assert
        assertThat(result.commandContext().get(MESSAGE1_KEY)).isEqualTo("quoted \" string");
        assertThat(result.commandContext().get(MESSAGE2_KEY)).isEqualTo("unquoted");
    }

    @Test
    void quoted_unmatched_quotes_failing() {
        // Arrange
        this.manager.command(this.manager.commandBuilder("quoted")
                .required(MESSAGE1_KEY, quotedStringParser())
                .required(MESSAGE2_KEY, stringParser())
                .build());

        // Act & Assert
        Assertions.assertThrows(
                CompletionException.class,
                () -> manager.commandExecutor().executeCommand(new TestCommandSender(), "'quoted quoted unquoted").join()
        );
    }

    @Test
    void greedy_consumes_all() {
        // Arrange
        this.manager.command(this.manager.commandBuilder("greedy")
                .required(MESSAGE1_KEY, greedyStringParser())
                .build());

        // Act
        final CommandResult<?> result =
                this.manager.commandExecutor().executeCommand(new TestCommandSender(), "greedy greedy string content").join();

        // Assert
        assertThat(result.commandContext().get(MESSAGE1_KEY)).isEqualTo("greedy string content");
    }
}
