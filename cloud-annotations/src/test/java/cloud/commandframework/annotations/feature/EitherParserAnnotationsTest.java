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

import cloud.commandframework.CommandComponent;
import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Command;
import cloud.commandframework.annotations.TestCommandManager;
import cloud.commandframework.annotations.TestCommandSender;
import cloud.commandframework.parser.EitherParser;
import cloud.commandframework.parser.standard.BooleanParser;
import cloud.commandframework.parser.standard.IntegerParser;
import cloud.commandframework.types.Either;
import java.util.Collection;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class EitherParserAnnotationsTest {

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
    @SuppressWarnings("unchecked")
    void test() {
        // Act
        final Collection<cloud.commandframework.Command<TestCommandSender>> result = this.annotationParser.parse(
                new EitherParserAnnotationsTest()
        );

        // Assert
        final cloud.commandframework.Command<TestCommandSender> command = result.stream().findFirst().get();
        final CommandComponent<TestCommandSender> eitherComponent = command.components().get(1);
        assertThat(eitherComponent.parser()).isInstanceOf(EitherParser.class);
        final EitherParser<?, Integer, Boolean> eitherParser = (EitherParser<?, Integer, Boolean>) eitherComponent.parser();
        assertThat(eitherParser.primary().parser()).isInstanceOf(IntegerParser.class);
        assertThat(eitherParser.fallback().parser()).isInstanceOf(BooleanParser.class);
    }

    @Command("command <arg>")
    public void command(@NonNull Either<Integer, Boolean> arg) {
    }
}
