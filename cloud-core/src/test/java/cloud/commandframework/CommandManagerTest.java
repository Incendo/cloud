//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionHandler;
import cloud.commandframework.internal.CommandRegistrationHandler;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import io.leangen.geantyref.TypeToken;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.google.common.truth.Truth.assertThat;
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
                CommandExecutionCoordinator.simpleCoordinator(),
                CommandRegistrationHandler.nullCommandRegistrationHandler()
        ) {
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
                .argument(IntegerArgument.optional("opt"))
                .handler(handlerC)
                .build();

        this.commandManager.command(commandA);
        this.commandManager.command(commandB);
        this.commandManager.command(commandC);

        // Act
        this.commandManager.executeCommand(new TestCommandSender(), "test a").join();
        this.commandManager.executeCommand(new TestCommandSender(), "test b").join();
        this.commandManager.executeCommand(new TestCommandSender(), "test c").join();
        this.commandManager.executeCommand(new TestCommandSender(), "test c 123").join();

        // Assert
        ArgumentCaptor<CommandContext<TestCommandSender>> contextArgumentCaptor = ArgumentCaptor.forClass(
                CommandContext.class
        );

        verify(handlerA).executeFuture(contextArgumentCaptor.capture());
        final CommandContext<TestCommandSender> contextA = contextArgumentCaptor.getValue();
        assertThat(contextA.getRawInputJoined()).isEqualTo("test a");

        verify(handlerB).executeFuture(contextArgumentCaptor.capture());
        final CommandContext<TestCommandSender> contextB = contextArgumentCaptor.getValue();
        assertThat(contextB.getRawInputJoined()).isEqualTo("test b");

        // Reset captor to reset the list of values.
        contextArgumentCaptor = ArgumentCaptor.forClass(
                CommandContext.class
        );
        verify(handlerC, times(2)).executeFuture(contextArgumentCaptor.capture());
        final CommandContext<TestCommandSender> contextC1 = contextArgumentCaptor.getAllValues().get(0);
        assertThat(contextC1.getRawInputJoined()).isEqualTo("test c");
        final CommandContext<TestCommandSender> contextC2 = contextArgumentCaptor.getAllValues().get(1);
        assertThat(contextC2.getRawInputJoined()).isEqualTo("test c 123");
    }

    @Test
    void testCommandBuilder() {
        // Create and register a command
        Command<TestCommandSender> command = this.commandManager.commandBuilder("component")
                .literal("literal", "literalalias")
                .literal("detail", ArgumentDescription.of("detaildescription"))
                .argument(
                        CommandArgument.ofType(int.class, "argument"),
                        ArgumentDescription.of("argumentdescription")
                )
                .build();
        this.commandManager.command(command);

        // Verify all the details we have configured are present
        List<CommandArgument<TestCommandSender, ?>> arguments = command.getArguments();
        List<CommandComponent<TestCommandSender>> components = command.getComponents();
        assertThat(arguments.size()).isEqualTo(components.size());
        assertThat(components.size()).isEqualTo(4);;

        // Arguments should exactly match the component getArgument() result, in same order
        for (int i = 0; i < components.size(); i++) {
            assertThat(components.get(i).getArgument()).isEqualTo(arguments.get(i));
        }

        // Argument configuration, we know component has the same argument so no need to test those
        // TODO: Aliases
        assertThat(arguments.get(0).getName()).isEqualTo("component");;
        assertThat(arguments.get(1).getName()).isEqualTo("literal");;
        assertThat(arguments.get(2).getName()).isEqualTo("detail");;
        assertThat(arguments.get(3).getName()).isEqualTo("argument");;

        // Check argument is indeed a command argument
        assertThat(TypeToken.get(int.class)).isEqualTo(arguments.get(3).getValueType());

        // Check description is set for all components, is empty when not specified
        assertThat(components.get(0).getArgumentDescription().getDescription()).isEmpty();
        assertThat(components.get(1).getArgumentDescription().getDescription()).isEmpty();
        assertThat(components.get(2).getArgumentDescription().getDescription()).isEqualTo("detaildescription");;
        assertThat(components.get(3).getArgumentDescription().getDescription()).isEqualTo("argumentdescription");;
    }
}
