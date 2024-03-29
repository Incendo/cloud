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
package org.incendo.cloud;

import io.leangen.geantyref.TypeToken;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.execution.CommandExecutionHandler;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.google.common.truth.Truth.assertThat;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link CommandManager} integration tests.
 */
@SuppressWarnings("unchecked")
class CommandManagerTest {

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
    }

    @Test
    void testMultiLiteralRouting() {
        // Arrange
        final CommandExecutionHandler<TestCommandSender> handlerA = mock(CommandExecutionHandler.class);
        when(handlerA.executeFuture(any())).thenReturn(CompletableFuture.completedFuture(null));

        final CommandExecutionHandler<TestCommandSender> handlerB = mock(CommandExecutionHandler.class);
        when(handlerB.executeFuture(any())).thenReturn(CompletableFuture.completedFuture(null));

        final CommandExecutionHandler<TestCommandSender> handlerC = mock(CommandExecutionHandler.class);
        when(handlerC.executeFuture(any())).thenReturn(CompletableFuture.completedFuture(null));

        final Command<TestCommandSender> commandA = this.commandManager.commandBuilder("test")
                .literal("a")
                .handler(handlerA)
                .build();
        final Command<TestCommandSender> commandB = this.commandManager.commandBuilder("test")
                .literal("b")
                .handler(handlerB)
                .build();
        final Command<TestCommandSender> commandC = this.commandManager.commandBuilder("test")
                .literal("c")
                .optional("opt", integerParser())
                .handler(handlerC)
                .build();

        this.commandManager.command(commandA);
        this.commandManager.command(commandB);
        this.commandManager.command(commandC);

        // Act
        this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test a").join();
        this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test b").join();
        this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test c").join();
        this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test c 123").join();

        // Assert
        ArgumentCaptor<CommandContext<TestCommandSender>> contextArgumentCaptor = ArgumentCaptor.forClass(
                CommandContext.class
        );

        verify(handlerA).executeFuture(contextArgumentCaptor.capture());
        final CommandContext<TestCommandSender> contextA = contextArgumentCaptor.getValue();
        assertThat(contextA.rawInput().input()).isEqualTo("test a");

        verify(handlerB).executeFuture(contextArgumentCaptor.capture());
        final CommandContext<TestCommandSender> contextB = contextArgumentCaptor.getValue();
        assertThat(contextB.rawInput().input()).isEqualTo("test b");

        // Reset captor to reset the list of values.
        contextArgumentCaptor = ArgumentCaptor.forClass(
                CommandContext.class
        );
        verify(handlerC, times(2)).executeFuture(contextArgumentCaptor.capture());
        final CommandContext<TestCommandSender> contextC1 = contextArgumentCaptor.getAllValues().get(0);
        assertThat(contextC1.rawInput().input()).isEqualTo("test c");
        final CommandContext<TestCommandSender> contextC2 = contextArgumentCaptor.getAllValues().get(1);
        assertThat(contextC2.rawInput().input()).isEqualTo("test c 123");
    }

    @Test
    void testCommandBuilder() {
        // Create and register a command
        Command<TestCommandSender> command = this.commandManager.commandBuilder("component")
                .literal("literal", "literalalias")
                .literal("detail", Description.of("detaildescription"))
                .required(
                        CommandComponent.ofType(int.class, "argument")
                                .description(Description.of("argumentdescription"))
                )
                .build();
        this.commandManager.command(command);

        // Verify all the details we have configured are present
        List<CommandComponent<TestCommandSender>> components = command.components();
        assertThat(components.size()).isEqualTo(4);

        // Argument configuration, we know component has the same argument so no need to test those
        // TODO: Aliases
        assertThat(components.get(0).name()).isEqualTo("component");
        assertThat(components.get(1).name()).isEqualTo("literal");
        assertThat(components.get(2).name()).isEqualTo("detail");
        assertThat(components.get(3).name()).isEqualTo("argument");

        // Check argument is indeed a command argument
        assertThat(TypeToken.get(int.class)).isEqualTo(components.get(3).valueType());

        // Check description is set for all components, is empty when not specified
        assertThat(components.get(0).description().textDescription()).isEmpty();
        assertThat(components.get(1).description().textDescription()).isEmpty();
        assertThat(components.get(2).description().textDescription()).isEqualTo("detaildescription");
        assertThat(components.get(3).description().textDescription()).isEqualTo("argumentdescription");
    }
}
