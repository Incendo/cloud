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
import cloud.commandframework.annotations.Command;
import cloud.commandframework.annotations.TestCommandManager;
import cloud.commandframework.annotations.TestCommandSender;
import cloud.commandframework.annotations.extractor.ParameterNameExtractor;
import cloud.commandframework.annotations.extractor.StandardArgumentExtractor;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandResult;
import com.google.common.base.CaseFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class ArgumentWithoutAnnotationTest {

    private CommandManager<TestCommandSender> commandManager;
    private AnnotationParser<TestCommandSender> annotationParser;

    @BeforeEach
    void setup() {
        this.commandManager = new TestCommandManager();
        this.annotationParser = new AnnotationParser<>(
                this.commandManager,
                TestCommandSender.class
        );
        this.annotationParser.argumentExtractor(
                StandardArgumentExtractor.builder((StandardArgumentExtractor) this.annotationParser.argumentExtractor())
                        .parameterNameExtractor(ParameterNameExtractor.withTransformation(
                                CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE)))
                        .build()
        );
    }

    @Test
    void test() {
        // Act
        this.annotationParser.parse(new TestClass());

        // Assert
        final CommandResult<?> result = this.commandManager.commandExecutor().executeCommand(new TestCommandSender(),
                "command abc 123 true").join();
        final CommandContext<?> context = result.commandContext();
        assertThat(context.<String>get("required")).isEqualTo("abc");
        assertThat(context.<Integer>get("optional")).isEqualTo(123);
        assertThat(context.<Boolean>get("optional_but_weird")).isEqualTo(true);
    }

    static class TestClass {

        @Command("command <required> [optional] [optional_but_weird]")
        public void command(TestCommandSender sender, String required, int optional, boolean optionalButWeird) {
        }
    }
}
