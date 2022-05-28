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
package cloud.commandframework.annotations.issue;

import cloud.commandframework.CommandHelpHandler;
import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.TestCommandManager;
import cloud.commandframework.annotations.TestCommandSender;
import cloud.commandframework.meta.SimpleCommandMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.google.common.truth.Truth.assertThat;

/**
 * Test for https://github.com/Incendo/cloud/issues/262.
 * <p>
 * It is in the annotations module, rather than the core module, in order
 * to rule out any issues with annotation parsing.
 */
@ExtendWith(MockitoExtension.class)
class Issue262 {

    private CommandHelpHandler<TestCommandSender> commandHelpHandler;
    private CommandManager<TestCommandSender> manager;
    private AnnotationParser<TestCommandSender> annotationParser;

    @BeforeEach
    void setup() {
        this.manager = new TestCommandManager();
        this.commandHelpHandler = this.manager.getCommandHelpHandler();
        this.annotationParser = new AnnotationParser<>(
                this.manager,
                TestCommandSender.class,
                (parameters) -> SimpleCommandMeta.empty()
        );
    }

    @Test
    void queryForCommandAliasesWorks() {
        // Arrange
        this.annotationParser.parse(this);

        // Act
        final CommandHelpHandler.HelpTopic<TestCommandSender> result = this.commandHelpHandler.queryHelp("cc sub2");

        // Assert
        assertThat(result).isInstanceOf(CommandHelpHandler.VerboseHelpTopic.class);

        final CommandHelpHandler.VerboseHelpTopic<TestCommandSender> topic =
                (CommandHelpHandler.VerboseHelpTopic<TestCommandSender>) result;
        assertThat(topic.getCommand().toString()).isEqualTo("cloudcommand sub2 argument");
    }

    @Test
    void queryForCommandChildAliasesWorks() {
        // Arrange
        this.annotationParser.parse(this);

        // Act
        final CommandHelpHandler.HelpTopic<TestCommandSender> result = this.commandHelpHandler.queryHelp("cc s");

        // Assert
        assertThat(result).isInstanceOf(CommandHelpHandler.VerboseHelpTopic.class);

        final CommandHelpHandler.VerboseHelpTopic<TestCommandSender> topic =
                (CommandHelpHandler.VerboseHelpTopic<TestCommandSender>) result;
        assertThat(topic.getCommand().toString()).isEqualTo("cloudcommand sub2 argument");
    }

    @Test
    void queryForRootCommandWorks() {
        // Arrange
        this.annotationParser.parse(this);

        // Act
        final CommandHelpHandler.HelpTopic<TestCommandSender> result = this.commandHelpHandler.queryHelp(
                "cloudcommand sub2"
        );

        // Assert
        assertThat(result).isInstanceOf(CommandHelpHandler.VerboseHelpTopic.class);

        final CommandHelpHandler.VerboseHelpTopic<TestCommandSender> topic =
                (CommandHelpHandler.VerboseHelpTopic<TestCommandSender>) result;
        assertThat(topic.getCommand().toString()).isEqualTo("cloudcommand sub2 argument");
    }

    @Test
    void queryForRootCommandWorks2() {
        // Arrange
        this.annotationParser.parse(this);

        // Act
        final CommandHelpHandler.HelpTopic<TestCommandSender> result = this.commandHelpHandler.queryHelp(
                "cloudcommand sub1"
        );

        // Assert
        assertThat(result).isInstanceOf(CommandHelpHandler.VerboseHelpTopic.class);

        final CommandHelpHandler.VerboseHelpTopic<TestCommandSender> topic =
                (CommandHelpHandler.VerboseHelpTopic<TestCommandSender>) result;
        assertThat(topic.getCommand().toString()).isEqualTo("cloudcommand sub1 argument");
    }

    @Test
    void queryForOnlyRootCommandWorks() {
        // Arrange
        this.annotationParser.parse(this);

        // Act
        final CommandHelpHandler.HelpTopic<TestCommandSender> result = this.commandHelpHandler.queryHelp(
                "cc"
        );

        // Assert
        assertThat(result).isInstanceOf(CommandHelpHandler.MultiHelpTopic.class);

        final CommandHelpHandler.MultiHelpTopic<TestCommandSender> topic =
                (CommandHelpHandler.MultiHelpTopic<TestCommandSender>) result;
        assertThat(topic.getChildSuggestions()).containsExactly(
                "cloudcommand sub1 [argument]",
                "cloudcommand sub2 [argument]"
        );
        assertThat(topic.getLongestPath()).isEqualTo("cloudcommand");
    }

    @CommandMethod("cloudcommand|cc sub1 [argument]")
    public void commandSource(@Argument("argument") final String argument) {
    }

    @CommandMethod("cloudcommand sub2|s [argument]")
    public void commandToken(@Argument("argument") final String argument) {
    }
}
