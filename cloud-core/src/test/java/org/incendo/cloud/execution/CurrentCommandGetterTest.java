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
package org.incendo.cloud.execution;

import java.util.concurrent.atomic.AtomicReference;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.TestCommandSender;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.incendo.cloud.truth.CompletableFutureSubject.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrentCommandGetterTest {

    private CommandManager<TestCommandSender> commandManager;

    @BeforeEach
    void setup() {
        this.commandManager = new CommandManager<TestCommandSender>(
                ExecutionCoordinator.simpleCoordinator(),
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

        final AtomicReference<Command<?>> cmdRef = new AtomicReference<>();
        final Command<TestCommandSender> command = this.commandManager.commandBuilder("command")
                .handler(ctx -> assertEquals(cmdRef.get(), ctx.command()))
                .build();
        cmdRef.set(command);

        this.commandManager.command(command);
    }

    @Test
    void executionTime() {
        assertThat(this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), "command")).hasResult();
    }

    @Test
    void postProcessor() {
        this.commandManager.registerCommandPostProcessor(ctx -> assertEquals(ctx.command(), ctx.commandContext().command()));
        assertThat(this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), "command")).hasResult();
    }
}
