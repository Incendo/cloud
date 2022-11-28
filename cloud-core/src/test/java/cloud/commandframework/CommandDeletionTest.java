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
package cloud.commandframework;

import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionHandler;
import cloud.commandframework.internal.CommandRegistrationHandler;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import java.util.concurrent.CompletionException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class CommandDeletionTest {

    private CommandManager<TestCommandSender> commandManager;

    @BeforeEach
    void setup() {
        this.commandManager = new CommandManager<TestCommandSender>(
                CommandExecutionCoordinator.simpleCoordinator(),
                CommandRegistrationHandler.nullCommandRegistrationHandler()
        ) {
            {
                this.registerCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION);
            }

            @Override
            public boolean hasPermission(
                    final @NonNull TestCommandSender sender,
                    final @NonNull String permission
            ) {
                return true;
            }

            @Override
            public @NonNull CommandMeta createDefaultCommandMeta() {
                return SimpleCommandMeta.empty();
            }
        };
    }

    @Test
    void deleteSimpleCommand() {
        // Arrange
        this.commandManager.command(this.commandManager.commandBuilder("test").build());
        // Pre-assert.
        this.commandManager.executeCommand(new TestCommandSender(), "test").join();

        // Act
        this.commandManager.deleteRootCommand("test");

        // Assert
        final CompletionException completionException = assertThrows(
                CompletionException.class,
                () -> this.commandManager.executeCommand(new TestCommandSender(), "test").join()
        );
        assertThat(completionException).hasCauseThat().isInstanceOf(NoSuchCommandException.class);

        assertThat(this.commandManager.suggest(new TestCommandSender(), "")).isEmpty();
        assertThat(this.commandManager.commandTree().getRootNodes()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteIntermediateCommand() {
        // Arrange
        final CommandExecutionHandler<TestCommandSender> handler1 = mock(CommandExecutionHandler.class);
        final Command<TestCommandSender> command1 = this.commandManager
                .commandBuilder("test")
                .handler(handler1)
                .build();
        this.commandManager.command(command1);

        final CommandExecutionHandler<TestCommandSender> handler2 = mock(CommandExecutionHandler.class);
        final Command<TestCommandSender> command2 = this.commandManager
                .commandBuilder("test")
                .literal("literal")
                .handler(handler2)
                .build();
        this.commandManager.command(command2);

        final CommandExecutionHandler<TestCommandSender> handler3 = mock(CommandExecutionHandler.class);
        final Command<TestCommandSender> command3 = this.commandManager
                .commandBuilder("test")
                .literal("literal")
                .argument(StringArgument.of("string"))
                .handler(handler3)
                .build();
        this.commandManager.command(command3);

        // Act
        this.commandManager.deleteRootCommand("test");

        // Assert
        final CompletionException completionException = assertThrows(
                CompletionException.class,
                () -> this.commandManager.executeCommand(new TestCommandSender(), "test").join()
        );
        assertThat(completionException).hasCauseThat().isInstanceOf(NoSuchCommandException.class);

        final CompletionException completionException2 = assertThrows(
                CompletionException.class,
                () -> this.commandManager.executeCommand(new TestCommandSender(), "test literal").join()
        );
        assertThat(completionException2).hasCauseThat().isInstanceOf(NoSuchCommandException.class);

        final CompletionException completionException3 = assertThrows(
                CompletionException.class,
                () -> this.commandManager.executeCommand(new TestCommandSender(), "test literal hm").join()
        );
        assertThat(completionException3).hasCauseThat().isInstanceOf(NoSuchCommandException.class);

        verifyNoMoreInteractions(handler1);
        verifyNoMoreInteractions(handler2);
        verifyNoMoreInteractions(handler3);

        assertThat(this.commandManager.commandTree().getRootNodes()).isEmpty();
    }

    @Test
    void deleteCommandWithSameArgumentNameAsRootCommand() {
        // Arrange
        this.commandManager.command(this.commandManager.commandBuilder("test").build());
        this.commandManager.command(this.commandManager.commandBuilder("hello").literal("test").build());

        // Pre-assert.
        this.commandManager.executeCommand(new TestCommandSender(), "test").join();
        this.commandManager.executeCommand(new TestCommandSender(), "hello test").join();

        // Act
        this.commandManager.deleteRootCommand("hello");

        // Assert
        this.commandManager.executeCommand(new TestCommandSender(), "test").join();
        final CompletionException completionException = assertThrows(
                CompletionException.class,
                () -> this.commandManager.executeCommand(new TestCommandSender(), "hello").join()
        );
        assertThat(completionException).hasCauseThat().isInstanceOf(NoSuchCommandException.class);

        assertThat(this.commandManager.suggest(new TestCommandSender(), "")).contains("test");
        assertThat(this.commandManager.commandTree().getRootNodes()).hasSize(1);
    }
}
