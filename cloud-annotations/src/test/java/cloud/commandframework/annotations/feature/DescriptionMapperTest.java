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
package cloud.commandframework.annotations.feature;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.Command;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.TestCommandManager;
import cloud.commandframework.annotations.TestCommandSender;
import cloud.commandframework.description.Description;
import cloud.commandframework.internal.CommandNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class DescriptionMapperTest {

    private static final TestDescription testDescription = new TestDescription();

    private CommandManager<TestCommandSender> commandManager;
    private AnnotationParser<TestCommandSender> annotationParser;

    @BeforeEach
    void setup() {
        this.commandManager = new TestCommandManager();
        this.annotationParser = new AnnotationParser<>(
                this.commandManager,
                TestCommandSender.class
        );
        this.annotationParser.descriptionMapper(string -> testDescription);
    }

    @Test
    void test() {
        // Act
        this.annotationParser.parse(new TestClass());

        // Assert
        final CommandNode<?> node = this.commandManager.commandTree().getNamedNode("command");
        final CommandNode<?> argumentNode = node.children().get(0);
        assertThat(argumentNode.command().commandDescription().description()).isEqualTo(testDescription);
        assertThat(node.children().get(0).component().description()).isEqualTo(testDescription);
    }

    static class TestClass {

        @CommandDescription("some epic description")
        @Command("command <argument>")
        public void command(@Argument(description = "another epic description") String argument) {
        }
    }

    static class TestDescription implements Description {

        @Override
        public @NonNull String textDescription() {
            return "yay";
        }
    }
}
