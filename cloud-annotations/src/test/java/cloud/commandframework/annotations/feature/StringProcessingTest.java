//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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

import cloud.commandframework.Command;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.Flag;
import cloud.commandframework.annotations.PropertyReplacingStringProcessor;
import cloud.commandframework.annotations.TestCommandManager;
import cloud.commandframework.annotations.TestCommandSender;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.compound.FlagArgument;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.meta.CommandMeta;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

class StringProcessingTest {

    private AnnotationParser<TestCommandSender> annotationParser;
    private TestCommandManager commandManager;

    @BeforeEach
    void setup() {
        this.commandManager = new TestCommandManager();
        this.annotationParser = new AnnotationParser<>(
                this.commandManager,
                TestCommandSender.class,
                p -> CommandMeta.simple()
                        .with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description"))
                        .build()
        );
    }

    @Test
    @DisplayName("Tests @CommandMethod, @CommandPermission, @CommandDescription, @Argument & @Flag")
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
        final List<Command<TestCommandSender>> commands = new ArrayList<>(this.commandManager.getCommands());
        assertThat(commands).hasSize(1);

        final Command<TestCommandSender> command = commands.get(0);
        assertThat(command.toString()).isEqualTo(String.format("%s argument flags", testProperty));
        assertThat(command.getCommandPermission().toString()).isEqualTo(testProperty);
        assertThat(command.getCommandMeta().get(CommandMeta.DESCRIPTION)).hasValue(testProperty);

        final List<CommandArgument<TestCommandSender, ?>> arguments = command.getArguments();
        assertThat(arguments).hasSize(3);

        final FlagArgument<TestCommandSender> flagArgument = (FlagArgument<TestCommandSender>) arguments.get(2);
        assertThat(flagArgument).isNotNull();

        final List<CommandFlag<?>> flags = new ArrayList<>(flagArgument.getFlags());
        assertThat(flags).hasSize(1);
        assertThat(flags.get(0).getName()).isEqualTo(testFlagName);
    }


    private static class TestClassA {

        @CommandDescription("${property.test}")
        @CommandPermission("${property.test}")
        @CommandMethod("${property.test} <argument>")
        public void commandA(
                final TestCommandSender sender,
                @Argument("${property.arg}") final String arg,
                @Flag("${property.flag}") final String flag
        ) {
        }
    }
}
