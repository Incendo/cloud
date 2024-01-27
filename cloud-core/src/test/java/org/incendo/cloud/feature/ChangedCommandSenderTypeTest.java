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

import io.leangen.geantyref.TypeToken;
import java.util.concurrent.CompletionException;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.TestCommandSender;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.util.TestUtils.createManager;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChangedCommandSenderTypeTest {

    private CommandManager<TestCommandSender> commandManager;

    @BeforeEach
    void setup() {
        this.commandManager = createManager();
    }

    @Test
    void testSimple() {
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

    @Test
    void testGeneric() {
        // Arrange
        final Command.Builder<GenericSender<String>> a = this.commandManager.commandBuilder("test")
                .literal("string")
                .senderType(new TypeToken<GenericSender<String>>() {});
        this.commandManager.command(a);

        // Act & assert
        this.commandManager.commandExecutor().executeCommand(new GenericStringSender(), "test string").join();
        assertThat(assertThrows(CompletionException.class, () -> {
            this.commandManager.commandExecutor().executeCommand(new GenericIntSender(), "test string").join();
        })).hasCauseThat().isInstanceOf(InvalidCommandSenderException.class);
        assertThat(assertThrows(CompletionException.class, () -> {
            this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test string").join();
        })).hasCauseThat().isInstanceOf(InvalidCommandSenderException.class);
    }

    @Test
    void testGenericWildcard() {
        // Arrange
        final Command.Builder<GenericSender<?>> wildcard = this.commandManager.commandBuilder("test")
                .literal("wildcard")
                .senderType(new TypeToken<GenericSender<?>>() {});
        this.commandManager.command(wildcard);

        // Act & assert
        this.commandManager.commandExecutor().executeCommand(new GenericStringSender(), "test wildcard").join();
        this.commandManager.commandExecutor().executeCommand(new GenericIntSender(), "test wildcard").join();
        assertThat(assertThrows(CompletionException.class, () -> {
            this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test wildcard").join();
        })).hasCauseThat().isInstanceOf(InvalidCommandSenderException.class);
    }

    public static final class SubType extends TestCommandSender {}

    public static abstract class GenericSender<T> extends TestCommandSender {}

    public static final class GenericStringSender extends GenericSender<String> {}

    public static final class GenericIntSender extends GenericSender<Integer> {}
}
