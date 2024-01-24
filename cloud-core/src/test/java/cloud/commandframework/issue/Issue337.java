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
package cloud.commandframework.issue;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.TestCommandSender;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exception.NoSuchCommandException;
import cloud.commandframework.execution.ExecutionCoordinator;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth.assertThat;

/**
 * Test for https://github.com/Incendo/cloud/issues/337.
 */
class Issue337 {

    @Test
    void emptyCommandTreeThrowsNoSuchCommandException() {
        // Arrange
        final TestCommandSender commandSender = new TestCommandSender();
        final CommandManager<TestCommandSender> commandManager = createManager();
        final CommandTree<TestCommandSender> commandTree = CommandTree.newTree(commandManager);
        final CommandInput commandInput = CommandInput.of("test this thing");

        // Act
        final CompletionException exception = Assertions.assertThrows(
                CompletionException.class,
                () -> commandTree.parse(new CommandContext<>(commandSender, commandManager), commandInput,
                        ExecutionCoordinator.nonSchedulingExecutor()).join()
        );

        // Assert
        assertThat(exception).hasCauseThat().isInstanceOf(NoSuchCommandException.class);
        final NoSuchCommandException noSuchCommandException = (NoSuchCommandException) exception.getCause();
        assertThat(noSuchCommandException.suppliedCommand()).isEqualTo("test");
        assertThat(noSuchCommandException).hasCauseThat().isNull();
    }
}
