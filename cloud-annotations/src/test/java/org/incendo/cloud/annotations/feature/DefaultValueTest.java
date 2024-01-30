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
package org.incendo.cloud.annotations.feature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Default;
import org.incendo.cloud.annotations.TestCommandManager;
import org.incendo.cloud.annotations.TestCommandSender;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.execution.CommandResult;
import org.incendo.cloud.util.annotation.AnnotationAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;

class DefaultValueTest {

    private CommandManager<TestCommandSender> commandManager;
    private AnnotationParser<TestCommandSender> annotationParser;

    @BeforeEach
    void setup() {
        this.commandManager = new TestCommandManager();
        this.annotationParser = new AnnotationParser<>(
                this.commandManager,
                TestCommandSender.class
        );
    }

    @ParameterizedTest
    @ValueSource(strings = { "parsed", "named" })
    void test(final @NonNull String command) {
        // Arrange
        this.annotationParser.parse(new TestClass());

        // Act
        final CommandResult<?> result =
                this.commandManager.commandExecutor().executeCommand(new TestCommandSender(), command).join();

        // Assert
        assertThat(result.commandContext().<Integer>get("arg")).isEqualTo(3);
    }

     static class TestClass {

        @Command("parsed [arg]")
        public void parsed(@Default("3") int arg) {
        }

        @Command("named [arg]")
        public void named(@Default(name = "named") @TestAnnotation(3) int arg) {
        }

        @Default
        public @NonNull DefaultValue<TestCommandSender, Integer> named(final @NonNull Parameter parameter) {
            final AnnotationAccessor annotationAccessor = AnnotationAccessor.of(parameter);
            return DefaultValue.constant(annotationAccessor.annotation(TestAnnotation.class).value());
        }
     }

     @Target(ElementType.PARAMETER)
     @Retention(RetentionPolicy.RUNTIME)
     @interface TestAnnotation {

        int value();
     }
}
