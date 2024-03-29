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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.TestCommandSender;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for https://github.com/Incendo/cloud/issues/281.
 */
@SuppressWarnings("unchecked")
class Issue281 {

    @Test
    void commandExceptionShouldNotBeSwallowed() {
        // Arrange
        final CommandManager<TestCommandSender> commandManager = new CommandManager<TestCommandSender>(
                ExecutionCoordinator.<TestCommandSender>builder()
                        .parsingExecutor(ExecutionCoordinator.nonSchedulingExecutor()).build(),
                CommandRegistrationHandler.nullCommandRegistrationHandler()
        ) {
            @Override
            public boolean hasPermission(
                    final @NonNull TestCommandSender sender,
                    final @NonNull String permission
            ) {
                return true;
            }
        };

        final CustomException customException = new CustomException();

        commandManager.command(
                commandManager.commandBuilder("test")
                        .handler((context) -> {
                            throw customException;
                        })
                        .build()
        );

        // Act
        final CompletionException exception = assertThrows(
                CompletionException.class,
                () -> commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test").join()
        );

        // Assert
        assertThat(exception).hasCauseThat().isInstanceOf(CommandExecutionException.class);
        assertThat(exception).hasCauseThat().hasCauseThat().isInstanceOf(CustomException.class);
    }

    private static final class CustomException extends RuntimeException {

    }
}
