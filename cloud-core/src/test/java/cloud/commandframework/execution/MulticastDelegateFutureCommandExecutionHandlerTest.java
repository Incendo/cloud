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
package cloud.commandframework.execution;

import cloud.commandframework.TestCommandSender;
import cloud.commandframework.context.CommandContext;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class MulticastDelegateFutureCommandExecutionHandlerTest {

    @Mock
    private CommandContext<TestCommandSender> context;

    @Test
    void ExecuteFuture_HappyFlow_Success() {
        // Arrange
        final CommandExecutionHandler<TestCommandSender> handlerA = mock(CommandExecutionHandler.class);
        when(handlerA.executeFuture(any())).thenReturn(CompletableFuture.completedFuture(null));

        final CommandExecutionHandler<TestCommandSender> handlerB = mock(CommandExecutionHandler.class);
        when(handlerB.executeFuture(any())).thenReturn(CompletableFuture.completedFuture(null));

        final CommandExecutionHandler<TestCommandSender> handlerC = mock(CommandExecutionHandler.class);
        when(handlerC.executeFuture(any())).thenReturn(CompletableFuture.completedFuture(null));

        final CommandExecutionHandler<TestCommandSender> delegatingHandler = CommandExecutionHandler.delegatingExecutionHandler(
                Arrays.asList(handlerA, handlerB, handlerC)
        );

        // Act
        delegatingHandler.executeFuture(this.context).join();

        // Assert
        final InOrder inOrder = Mockito.inOrder(handlerA, handlerB, handlerC);
        inOrder.verify(handlerA).executeFuture(notNull());
        inOrder.verify(handlerB).executeFuture(notNull());
        inOrder.verify(handlerC).executeFuture(notNull());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void ExecuteFuture_FailedFuture_StopsDelegating() {
        // Arrange
        final CommandExecutionHandler<TestCommandSender> handlerA = mock(CommandExecutionHandler.class);
        when(handlerA.executeFuture(any())).thenReturn(CompletableFuture.completedFuture(null));

        final CommandExecutionHandler<TestCommandSender> handlerB = mock(CommandExecutionHandler.class);
        final CompletableFuture<Void> futureB = new CompletableFuture<>();
        futureB.completeExceptionally(new RuntimeException());
        when(handlerB.executeFuture(any())).thenReturn(futureB);

        final CommandExecutionHandler<TestCommandSender> handlerC = mock(CommandExecutionHandler.class);

        final CommandExecutionHandler<TestCommandSender> delegatingHandler = CommandExecutionHandler.delegatingExecutionHandler(
                Arrays.asList(handlerA, handlerB, handlerC)
        );

        // Act
        final CompletableFuture<Void> future = delegatingHandler.executeFuture(this.context);
        assertThrows(
                CompletionException.class,
                future::join
        );

        // Assert
        final InOrder inOrder = Mockito.inOrder(handlerA, handlerB, handlerC);
        inOrder.verify(handlerA).executeFuture(notNull());
        inOrder.verify(handlerB).executeFuture(notNull());
        inOrder.verify(handlerC, never()).executeFuture(notNull());
        inOrder.verifyNoMoreInteractions();
    }
}
