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

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.TestCommandManager;
import org.incendo.cloud.annotations.TestCommandSender;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.CommandResult;
import org.incendo.cloud.injection.ParameterInjector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class ParameterInjectionTest {

    private CommandManager<TestCommandSender> commandManager;
    private AnnotationParser<TestCommandSender> annotationParser;

    @BeforeEach
    void setup() {
        this.commandManager = new TestCommandManager();
        this.annotationParser = new AnnotationParser<>(this.commandManager, TestCommandSender.class);
    }

    @Test
    void testInjection() {
        // Arrange
        this.commandManager.parameterInjectorRegistry()
                .registerInjector(Integer.class, ParameterInjector.constantInjector(5))
                .registerInjector(new TypeToken<StringWrapper>() {}, ParameterInjector.constantInjector(new StringWrapper("abc")))
                .registerInjector(
                        new TypeToken<Wrapper<Boolean>>() {},
                        ParameterInjector.constantInjector(new Wrapper<>(true))
                ).registerInjector(
                        SomeImplementation.class,
                        ParameterInjector.constantInjector(new SomeImplementation())
                );
        this.annotationParser.parse(new TestClass());

        // Act
        final CommandResult<?> result = this.commandManager.commandExecutor().executeCommand(
                new TestCommandSender(),
                "command"
        ).join();

        // Assert
        assertThat(result.commandContext().<Integer>get("result-integer")).isEqualTo(5);
        assertThat(result.commandContext().<String>get("result-string")).isEqualTo("abc");
        assertThat(result.commandContext().<Boolean>get("result-boolean")).isEqualTo(true);
        assertThat(result.commandContext().<SomeInterface>get("result-someInterface")).isInstanceOf(SomeImplementation.class);
    }


    static class TestClass {

        @Command("command")
        public void injectedMethod(
                final @NonNull CommandContext<?> context,
                final Integer integer,
                final @NonNull Wrapper<String> stringWrapper,
                final @NonNull Wrapper<Boolean> booleanWrapper,
                final @NonNull SomeInterface someInterface
        ) {
            context.set("result-integer", integer);
            context.set("result-string", stringWrapper.object());
            context.set("result-boolean", booleanWrapper.object());
            context.set("result-someInterface", someInterface);
        }
    }

    static class Wrapper<T> {

        private final T object;

        Wrapper(final @NonNull T object) {
            this.object = object;
        }

        final @NonNull T object() {
            return this.object;
        }
    }

    static class StringWrapper extends Wrapper<String> {

        StringWrapper(final @NonNull String string) {
            super(string);
        }
    }

    interface SomeInterface {

    }

    static class SomeImplementation implements SomeInterface {

    }
}
