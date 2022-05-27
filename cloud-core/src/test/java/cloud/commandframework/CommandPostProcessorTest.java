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

import cloud.commandframework.execution.CommandExecutionHandler;
import cloud.commandframework.execution.postprocessor.CommandPostprocessingContext;
import cloud.commandframework.execution.postprocessor.CommandPostprocessor;
import cloud.commandframework.services.types.ConsumerService;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.util.TestUtils.createManager;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class CommandPostProcessorTest {

    private CommandManager<TestCommandSender> commandManager;

    @BeforeEach
    void setup() {
        this.commandManager = createManager();
    }

    @Test
    void testPostProcessing() {
        // Arrange
        final CommandPostprocessor<TestCommandSender> postprocessor = spy(new SamplePostprocessor());
        this.commandManager.registerCommandPostProcessor(postprocessor);

        final CommandExecutionHandler<TestCommandSender> executionHandler = mock(CommandExecutionHandler.class);
        when(executionHandler.executeFuture(any())).thenReturn(CompletableFuture.completedFuture(null));

        this.commandManager.command(
                this.commandManager.commandBuilder("test")
                        .handler(executionHandler)
                        .build()
        );

        // Act
        this.commandManager.executeCommand(new TestCommandSender(), "test").join();

        // Assert
        verify(executionHandler, never()).executeFuture(notNull());
        verify(postprocessor).accept(notNull());
    }

    static class SamplePostprocessor implements CommandPostprocessor<TestCommandSender> {

        @Override
        public void accept(final @NonNull CommandPostprocessingContext<TestCommandSender> context) {
            ConsumerService.interrupt();
        }
    }
}
