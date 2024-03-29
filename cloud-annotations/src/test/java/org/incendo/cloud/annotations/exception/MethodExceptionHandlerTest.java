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
package org.incendo.cloud.annotations.exception;

import java.util.Collections;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.TestCommandManager;
import org.incendo.cloud.annotations.TestCommandSender;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.StandardCommandContextFactory;
import org.incendo.cloud.exception.NoSuchCommandException;
import org.incendo.cloud.exception.handling.ExceptionContext;
import org.incendo.cloud.exception.handling.ExceptionController;
import org.incendo.cloud.injection.ParameterInjector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class MethodExceptionHandlerTest {

    private ExceptionController<TestCommandSender> exceptionController;
    private AnnotationParser<TestCommandSender> annotationParser;
    private CommandContext<TestCommandSender> context;

    @BeforeEach
    void setup() {
        final CommandManager<TestCommandSender> commandManager = new TestCommandManager();
        this.exceptionController = commandManager.exceptionController();
        this.annotationParser = new AnnotationParser<>(commandManager, TestCommandSender.class);
        this.context = new StandardCommandContextFactory<>(commandManager).create(false, new TestCommandSender());
        commandManager.parameterInjectorRegistry().registerInjector(Integer.class, ParameterInjector.constantInjector(5));
    }

    @Test
    void testPrecise() throws Throwable {
        // Arrange
        this.annotationParser.parse(new TestClass());

        // Act
        this.exceptionController.handleException(
                this.context,
                new NoSuchCommandException(this.context.sender(), Collections.emptyList(), "")
        );

        // Assert
        assertThat(context.<String>get("handled-by")).isEqualTo("noSuchCommandException");
    }

    @Test
    void testFallback() throws Throwable {
        // Arrange
        this.annotationParser.parse(new TestClass());

        // Act
        this.exceptionController.handleException(
                this.context,
                new RuntimeException()
        );

        // Assert
        assertThat(context.<String>get("handled-by")).isEqualTo("throwable");
    }

    static class TestClass {

        @ExceptionHandler(NoSuchCommandException.class)
        public void noSuchCommandException(final @NonNull ExceptionContext<TestCommandSender, NoSuchCommandException> context) {
            context.context().set("handled-by", "noSuchCommandException");
        }

        @ExceptionHandler(Throwable.class)
        public void throwable(
                final @NonNull Throwable throwable,
                final @NonNull CommandContext<TestCommandSender> context,
                final @NonNull Integer injectedNumber
        ) {
            context.set("handled-by", "throwable");
        }
    }
}
