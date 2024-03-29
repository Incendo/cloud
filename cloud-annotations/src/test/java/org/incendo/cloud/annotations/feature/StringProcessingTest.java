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

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Flag;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.TestCommandManager;
import org.incendo.cloud.annotations.TestCommandSender;
import org.incendo.cloud.annotations.string.PropertyReplacingStringProcessor;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.meta.CommandMeta;
import org.incendo.cloud.parser.flag.CommandFlag;
import org.incendo.cloud.parser.flag.CommandFlagParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class StringProcessingTest {

    private AnnotationParser<TestCommandSender> annotationParser;
    private TestCommandManager commandManager;

    @BeforeEach
    void setup() {
        this.commandManager = new TestCommandManager();
        this.annotationParser = new AnnotationParser<>(
                this.commandManager,
                TestCommandSender.class,
                p -> CommandMeta.builder().build()
        );
    }

    @Test
    @DisplayName("Tests @Command, @Permission, @CommandDescription, @Argument & @Flag")
    @SuppressWarnings("unchecked")
    void testStringProcessing() {
        // Arrange
        final String testProperty = ThreadLocalRandom.current()
                .ints()
                .mapToObj(Integer::toString)
                .limit(32)
                .collect(Collectors.joining());
        final String testFlagName = ThreadLocalRandom.current()
                .ints()
                .mapToObj(Integer::toString)
                .limit(32)
                .collect(Collectors.joining());
        this.annotationParser.stringProcessor(
                new PropertyReplacingStringProcessor(
                        s -> ImmutableMap.of(
                                "property.test", testProperty,
                                "property.arg", "argument",
                                "property.flag", testFlagName
                        ).get(s)
                )
        );
        final TestClassA testClassA = new TestClassA();

        // Act
        this.annotationParser.parse(testClassA);

        // Assert
        final List<org.incendo.cloud.Command<TestCommandSender>> commands = new ArrayList<>(this.commandManager.commands());
        assertThat(commands).hasSize(1);

        final org.incendo.cloud.Command<TestCommandSender> command = commands.get(0);
        assertThat(command.toString()).isEqualTo(String.format("%s argument flags", testProperty));
        assertThat(command.commandPermission().permissionString()).isEqualTo(testProperty);
        assertThat(command.commandDescription().description().textDescription()).isEqualTo(testProperty);

        final List<CommandComponent<TestCommandSender>> components = command.components();
        assertThat(components).hasSize(3);

        final CommandFlagParser<TestCommandSender> flagParser =
                (CommandFlagParser<TestCommandSender>) components.get(2).parser();
        assertThat(flagParser).isNotNull();

        final List<CommandFlag<?>> flags = new ArrayList<>(flagParser.flags());
        assertThat(flags).hasSize(1);
        assertThat(flags.get(0).name()).isEqualTo(testFlagName);
    }


    static class TestClassA {

        @CommandDescription("${property.test}")
        @Permission("${property.test}")
        @Command("${property.test} <argument>")
        public void commandA(
                final TestCommandSender sender,
                @Argument("${property.arg}") final String arg,
                @Flag("${property.flag}") final String flag
        ) {
        }
    }
}
