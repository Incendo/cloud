//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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
package cloud.commandframework.annotations.feature;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.TestCommandManager;
import cloud.commandframework.annotations.TestCommandSender;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandResult;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * Test that verifies that {@link CommandMethod} is repeatable.
 */
class RepeatableCommandMethodTest {

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

    @Test
    void test() {
        // Arrange
        this.annotationParser.parse(new TestClass());

        // Act
        final CommandResult<?> result1 = this.commandManager.executeCommand(new TestCommandSender(), "test").join();
        final CommandResult<?> result2 = this.commandManager.executeCommand(new TestCommandSender(), "test foo").join();

        // Assert
        assertThat(result1.commandContext().<Boolean>get("handled")).isTrue();
        assertThat(result2.commandContext().<Boolean>get("handled")).isTrue();
    }

    final static class TestClass {

        @CommandMethod("test foo")
        @CommandMethod("test")
        public void commandTest(final @NonNull CommandContext<TestCommandSender> context) {
            context.set("handled", true);
        }
    }
}
