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
package cloud.commandframework.execution;

import cloud.commandframework.CommandManager;
import cloud.commandframework.TestCommandSender;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.internal.CommandRegistrationHandler;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExecutionCoordinatorTest {

    @ParameterizedTest
    @MethodSource("testErrorMappingSource")
    void testErrorMappingSimpleParserSimpleExecutor(final @NonNull Exception exception) {
        // Arrange
        final CommandRegistrationHandler<TestCommandSender> registrationHandler = (command) -> true;
        final CommandManager<TestCommandSender> commandManager = new CommandManager<TestCommandSender>(
                ExecutionCoordinator.simpleCoordinator(),
                registrationHandler
        ) {
            @Override
            public boolean hasPermission(final @NonNull TestCommandSender sender, final @NonNull String permission) {
                return true;
            }
        };
        commandManager.command(
                commandManager.commandBuilder("test")
                        .required(
                                "arg",
                                ParserDescriptor.of(failingParser(exception), Integer.class)
                        )
        );

        // Act
        final CompletionException completionException = assertThrows(
                CompletionException.class,
                () -> commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test 123").join()
        );

        // Assert
        if (exception instanceof CompletionException) {
            assertThat(completionException).hasCauseThat().isInstanceOf(ArgumentParseException.class);
            assertThat(completionException).hasCauseThat().hasCauseThat().isEqualTo(exception.getCause());
        } else {
            assertThat(completionException).hasCauseThat().isInstanceOf(ArgumentParseException.class);
            assertThat(completionException).hasCauseThat().hasCauseThat().isEqualTo(exception);
        }
    }

    @ParameterizedTest
    @MethodSource("testErrorMappingSource")
    void testErrorMappingFutureParserSimpleExecutor(final @NonNull Exception exception) {
        // Arrange
        final CommandRegistrationHandler<TestCommandSender> registrationHandler = (command) -> true;
        final CommandManager<TestCommandSender> commandManager = new CommandManager<TestCommandSender>(
                ExecutionCoordinator.simpleCoordinator(),
                registrationHandler
        ) {
            @Override
            public boolean hasPermission(final @NonNull TestCommandSender sender, final @NonNull String permission) {
                return true;
            }
        };
        commandManager.command(
                commandManager.commandBuilder("test")
                        .required(
                                "arg",
                                ParserDescriptor.of(failingFutureParser(exception), Integer.class)
                        )
        );

        // Act
        final CompletionException completionException = assertThrows(
                CompletionException.class,
                () -> commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test 123").join()
        );

        // Assert
        if (exception instanceof CompletionException) {
            assertThat(completionException).hasCauseThat().isInstanceOf(ArgumentParseException.class);
            assertThat(completionException).hasCauseThat().hasCauseThat().isEqualTo(exception.getCause());
        } else {
            assertThat(completionException).hasCauseThat().isInstanceOf(ArgumentParseException.class);
            assertThat(completionException).hasCauseThat().hasCauseThat().isEqualTo(exception);
        }
    }

    @ParameterizedTest
    @MethodSource("testErrorMappingSource")
    void testErrorMappingSimpleParserAsyncExecutor(final @NonNull Exception exception) {
        // Arrange
        final CommandRegistrationHandler<TestCommandSender> registrationHandler = (command) -> true;
        final CommandManager<TestCommandSender> commandManager = new CommandManager<TestCommandSender>(
                ExecutionCoordinator.asyncCoordinator(),
                registrationHandler
        ) {
            @Override
            public boolean hasPermission(final @NonNull TestCommandSender sender, final @NonNull String permission) {
                return true;
            }
        };
        final ArgumentParser<TestCommandSender, Integer> parser = failingParser(exception);
        commandManager.command(
                commandManager.commandBuilder("test")
                        .required(
                                "arg",
                                ParserDescriptor.of(parser, Integer.class)
                        )
        );

        // Act
        final CompletionException completionException = assertThrows(
                CompletionException.class,
                () -> commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test 123").join()
        );

        // Assert
        if (exception instanceof CompletionException) {
            assertThat(completionException).hasCauseThat().isInstanceOf(ArgumentParseException.class);
            assertThat(completionException).hasCauseThat().hasCauseThat().isEqualTo(exception.getCause());
        } else {
            assertThat(completionException).hasCauseThat().isInstanceOf(ArgumentParseException.class);
            assertThat(completionException).hasCauseThat().hasCauseThat().isEqualTo(exception);
        }
    }

    private static ArgumentParser<TestCommandSender, Integer> failingParser(final Exception exception) {
        return (commandContext, commandInput) -> ArgumentParseResult.failure(exception);
    }

    private static ArgumentParser<TestCommandSender, Integer> failingFutureParser(final Exception exception) {
        return (ArgumentParser.FutureArgumentParser<TestCommandSender, Integer>) (commandContext, commandInput) ->
                ArgumentParseResult.failureFuture(exception);
    }

    @ParameterizedTest
    @MethodSource("testErrorMappingSource")
    void testErrorMappingFutureParserAsyncExecutor(final @NonNull Exception exception) {
        // Arrange
        final CommandRegistrationHandler<TestCommandSender> registrationHandler = (command) -> true;
        final CommandManager<TestCommandSender> commandManager = new CommandManager<TestCommandSender>(
                ExecutionCoordinator.asyncCoordinator(),
                registrationHandler
        ) {
            @Override
            public boolean hasPermission(final @NonNull TestCommandSender sender, final @NonNull String permission) {
                return true;
            }
        };
        commandManager.command(
                commandManager.commandBuilder("test")
                        .required(
                                "arg",
                                ParserDescriptor.of(failingFutureParser(exception), Integer.class)
                        )
        );

        // Act
        final CompletionException completionException = assertThrows(
                CompletionException.class,
                () -> commandManager.commandExecutor().executeCommand(new TestCommandSender(), "test 123").join()
        );

        // Assert
        if (exception instanceof CompletionException) {
            assertThat(completionException).hasCauseThat().isInstanceOf(ArgumentParseException.class);
            assertThat(completionException).hasCauseThat().hasCauseThat().isEqualTo(exception.getCause());
        } else {
            assertThat(completionException).hasCauseThat().isInstanceOf(ArgumentParseException.class);
            assertThat(completionException).hasCauseThat().hasCauseThat().isEqualTo(exception);
        }
    }

    static Stream<Exception> testErrorMappingSource() {
        return Stream.of(
                new CompletionException(new RuntimeException()),
                new RuntimeException(),
                new CommandExecutionException(new RuntimeException())
        );
    }
}
