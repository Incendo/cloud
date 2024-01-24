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

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.TestCommandSender;
import cloud.commandframework.arguments.DefaultValue;
import cloud.commandframework.parser.ParserDescriptor;
import cloud.commandframework.suggestion.SuggestionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.parser.standard.IntegerParser.integerParser;
import static cloud.commandframework.util.TestUtils.createManager;

class ChangedCommandSenderTypeTest {

    private CommandManager<TestCommandSender> commandManager;

    @BeforeEach
    void setup() {
        this.commandManager = createManager();
    }

    @Test
    void test() {
        // Arrange
        final Command.Builder<TestCommandSender> root = commandManager.commandBuilder("root");
        final ParserDescriptor<TestCommandSender, Integer> integerParser = integerParser();
        final SuggestionProvider<TestCommandSender> suggestionProvider = integerParser.parser().suggestionProvider();
        final DefaultValue<TestCommandSender, Integer> defaultValue = DefaultValue.constant(5);

        // Act
        this.commandManager.command(
                root.literal("literal").senderType(SubType.class).optional(
                        "integer",
                        integerParser,
                        defaultValue,
                        suggestionProvider
                )
        );

        // Assert
        this.commandManager.commandExecutor().executeCommand(new SubType(), "root literal 10").join();
    }


    public static final class SubType extends TestCommandSender {
    }
}
