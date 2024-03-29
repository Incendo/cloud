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
package org.incendo.cloud.issue;

import java.util.concurrent.CompletionException;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.CommandTree;
import org.incendo.cloud.TestCommandSender;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.NoSuchCommandException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.incendo.cloud.util.TestUtils.createManager;

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
