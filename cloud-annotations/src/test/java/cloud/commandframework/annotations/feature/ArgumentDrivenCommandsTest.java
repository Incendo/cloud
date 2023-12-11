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
import cloud.commandframework.annotations.ArgumentDescriptor;
import cloud.commandframework.annotations.ArgumentExtractor;
import cloud.commandframework.annotations.ArgumentMode;
import cloud.commandframework.annotations.CommandDescriptor;
import cloud.commandframework.annotations.CommandExtractor;
import cloud.commandframework.annotations.ImmutableCommandDescriptor;
import cloud.commandframework.annotations.SyntaxFragment;
import cloud.commandframework.annotations.SyntaxParser;
import cloud.commandframework.annotations.TestCommandManager;
import cloud.commandframework.annotations.TestCommandSender;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArgumentDrivenCommandsTest {

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
    void testCommandConstruction() throws Exception {
        // Arrange
        this.annotationParser.argumentExtractor(new TestArgumentExtractor());
        this.annotationParser.syntaxParser(new TestSyntaxParser());
        this.annotationParser.commandExtractor(new TestCommandExtractor(this.annotationParser));
        this.annotationParser.parse(new ArgumentDrivenCommandClass());

        // Act
        this.commandManager.executeCommand(new TestCommandSender(), "test 3 literal").get();
    }

    private static class TestArgumentExtractor implements ArgumentExtractor {
        @Override
        public @NonNull Collection<@NonNull ArgumentDescriptor> extractArguments(
                final @NonNull List<@NonNull SyntaxFragment> syntax,
                final @NonNull Method method
        ) {
            final Collection<ArgumentDescriptor> arguments = new ArrayList<>();
            for (final Parameter parameter : method.getParameters()) {
                if (!parameter.isAnnotationPresent(Argument.class)) {
                    continue;
                }
                final Argument argument = parameter.getAnnotation(Argument.class);
                if (argument.literal()) {
                    continue;
                }
                final ArgumentDescriptor argumentDescriptor = ArgumentDescriptor.builder()
                        .parameter(parameter)
                        .name(parameter.getName())
                        .build();
                arguments.add(argumentDescriptor);
            }
            return arguments;
        }
    }

    private static class TestSyntaxParser implements SyntaxParser {

        @Override
        public @NonNull List<@NonNull SyntaxFragment> parseSyntax(final @Nullable Method method, final @NonNull String string) {
            Objects.requireNonNull(method);

            final List<SyntaxFragment> syntaxFragments = new ArrayList<>();
            for (final Parameter parameter : method.getParameters()) {
                if (!parameter.isAnnotationPresent(Argument.class)) {
                    continue;
                }
                final Argument argument = parameter.getAnnotation(Argument.class);

                final ArgumentMode argumentMode;
                if (argument.literal()) {
                    argumentMode = ArgumentMode.LITERAL;
                } else if (argument.required()) {
                    argumentMode = ArgumentMode.REQUIRED;
                } else {
                    argumentMode = ArgumentMode.OPTIONAL;
                }

                syntaxFragments.add(new SyntaxFragment(parameter.getName(), Collections.emptyList(), argumentMode));
            }

            return syntaxFragments;
        }
    }

    private static class TestCommandExtractor implements CommandExtractor {

        private final AnnotationParser<TestCommandSender> annotationParser;

        private TestCommandExtractor(final @NonNull AnnotationParser<TestCommandSender> annotationParser) {
            this.annotationParser = annotationParser;
        }

        @Override
        public @NonNull Collection<@NonNull CommandDescriptor> extractCommands(final @NonNull Object instance) {
            final Collection<CommandDescriptor> commandDescriptors = new ArrayList<>();
            for (final Method method : instance.getClass().getMethods()) {
                boolean commandMethod = false;
                String commandName = "";
                for (final Parameter parameter : method.getParameters()) {
                    if (parameter.isAnnotationPresent(Argument.class)) {
                        commandMethod = true;
                        commandName = parameter.getName();
                        break;
                    }
                }
                if (!commandMethod) {
                    continue;
                }

                commandDescriptors.add(
                        ImmutableCommandDescriptor.builder()
                                .method(method)
                                .syntax(this.annotationParser.syntaxParser().parseSyntax(method, ""))
                                .commandToken(commandName)
                                .requiredSender(Object.class)
                                .build()
                );
            }
            return commandDescriptors;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface Argument {

        boolean literal() default false;

        boolean required() default true;
    }

    public static class ArgumentDrivenCommandClass {

        public void someCommand(
                @Argument(literal = true) String test,
                @Argument int argument,
                @Argument(literal = true) String literal,
                @Argument(required = false) String anotherArgument,
                TestCommandSender commandSender
        ) {
        }
    }
}
