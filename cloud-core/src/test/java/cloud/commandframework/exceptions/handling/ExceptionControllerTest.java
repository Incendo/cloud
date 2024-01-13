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
package cloud.commandframework.exceptions.handling;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class ExceptionControllerTest {

    @Mock
    private CommandContext<Object> commandContext;

    private ExceptionController<Object> exceptionController;
    private ExceptionContextFactory<Object> exceptionContextFactory;

    @BeforeEach
    void setup() {
        this.exceptionController = new ExceptionController<>();
        this.exceptionContextFactory = new ExceptionContextFactory<>(this.exceptionController);
    }

    @Test
    void HandleException_PreciseType_ExceptionHandled() throws Throwable {
        // Arrange
        final ExceptionHandler<Object, NoSuchCommandException> exceptionHandler = mock(ExceptionHandler.class);
        this.exceptionController.registerHandler(NoSuchCommandException.class, exceptionHandler);
        final NoSuchCommandException exception =  new NoSuchCommandException(
                new Object(),
                Collections.emptyList(),
                ""
        );

        // Act
        this.exceptionController.handleException(this.commandContext, exception);

        // Assert
        verify(exceptionHandler).handle(this.exceptionContextFactory.createContext(this.commandContext, exception));
    }

    @Test
    void HandleException_ParentType_ExceptionHandled() throws Throwable {
        // Arrange
        final ExceptionHandler<Object, Throwable> exceptionHandler = mock(ExceptionHandler.class);
        this.exceptionController.registerHandler(Throwable.class, exceptionHandler);
        final NoSuchCommandException exception =  new NoSuchCommandException(
                new Object(),
                Collections.emptyList(),
                ""
        );

        // Act
        this.exceptionController.handleException(this.commandContext, exception);

        // Assert
        verify(exceptionHandler).handle(this.exceptionContextFactory.createContext(this.commandContext, exception));
    }

    @Test
    void HandleException_NoFallback_ExceptionReThrown() {
        // Arrange
        final NoSuchCommandException exception =  new NoSuchCommandException(
                new Object(),
                Collections.emptyList(),
                ""
        );

        // Act
        final NoSuchCommandException result = assertThrows(
                NoSuchCommandException.class,
                () -> this.exceptionController.handleException(this.commandContext, exception)
        );

        // Assert
        assertThat(result).isEqualTo(exception);
    }

    @Test
    void HandleException_NewExceptionThrown_ExceptionHandled() throws Throwable {
        // Arrange
        final RuntimeException runtimeException = new RuntimeException("test :)");
        final ExceptionHandler<Object, NoSuchCommandException> exceptionHandler = ctx -> {
            throw runtimeException;
        };
        final ExceptionHandler<Object, Throwable> fallbackHandler = mock(ExceptionHandler.class);
        this.exceptionController.registerHandler(NoSuchCommandException.class, exceptionHandler);
        this.exceptionController.registerHandler(Throwable.class, fallbackHandler);
        final NoSuchCommandException exception =  new NoSuchCommandException(
                new Object(),
                Collections.emptyList(),
                ""
        );

        // Act
        this.exceptionController.handleException(this.commandContext, exception);

        // Assert
        verify(fallbackHandler).handle(this.exceptionContextFactory.createContext(this.commandContext, runtimeException));
    }

    @Test
    void HandleException_PassThroughHandler_ExceptionHandled() throws Throwable {
        // Arrange
        final ExceptionHandler<Object, NoSuchCommandException> exceptionHandler = ExceptionHandler.passThroughHandler();
        final ExceptionHandler<Object, NoSuchCommandException> fallbackHandler = mock(ExceptionHandler.class);
        this.exceptionController.registerHandler(NoSuchCommandException.class, fallbackHandler);
        this.exceptionController.registerHandler(NoSuchCommandException.class, exceptionHandler);
        final NoSuchCommandException exception =  new NoSuchCommandException(
                new Object(),
                Collections.emptyList(),
                ""
        );

        // Act
        this.exceptionController.handleException(this.commandContext, exception);

        // Assert
        verify(fallbackHandler).handle(this.exceptionContextFactory.createContext(this.commandContext, exception));
    }

    @Test
    void HandleException_PassThroughHandlerWithConsumer_ExceptionHandled() throws Throwable {
        // Arrange
        final AtomicBoolean handled = new AtomicBoolean(false);
        final ExceptionHandler<Object, NoSuchCommandException> exceptionHandler = ExceptionHandler.passThroughHandler(ctx ->
                handled.set(true));
        final ExceptionHandler<Object, NoSuchCommandException> fallbackHandler = mock(ExceptionHandler.class);
        this.exceptionController.registerHandler(NoSuchCommandException.class, fallbackHandler);
        this.exceptionController.registerHandler(NoSuchCommandException.class, exceptionHandler);
        final NoSuchCommandException exception =  new NoSuchCommandException(
                new Object(),
                Collections.emptyList(),
                ""
        );

        // Act
        this.exceptionController.handleException(this.commandContext, exception);

        // Assert
        verify(fallbackHandler).handle(this.exceptionContextFactory.createContext(this.commandContext, exception));

        assertThat(handled.get()).isTrue();
    }

    @Test
    void HandleException_UnwrappingHandler_ExceptionHandled() throws Throwable {
        // Arrange
        final ExceptionHandler<Object, CommandExecutionException> commandExecutionHandler = ExceptionHandler.unwrappingHandler();
        final ExceptionHandler<Object, IllegalStateException> illegalStateHandler = mock(ExceptionHandler.class);
        this.exceptionController.registerHandler(IllegalStateException.class, illegalStateHandler);
        this.exceptionController.registerHandler(CommandExecutionException.class, commandExecutionHandler);

        final IllegalStateException illegalStateException = new IllegalStateException();
        final CommandExecutionException commandExecutionException = new CommandExecutionException(illegalStateException);

        // Act
        this.exceptionController.handleException(this.commandContext, commandExecutionException);

        // Assert
        verify(illegalStateHandler).handle(this.exceptionContextFactory.createContext(this.commandContext, illegalStateException));
    }
}
