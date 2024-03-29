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
package org.incendo.cloud.annotations.issue;

import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.TestCommandManager;
import org.incendo.cloud.annotations.TestCommandSender;
import org.incendo.cloud.help.HelpHandler;
import org.incendo.cloud.help.HelpQuery;
import org.incendo.cloud.help.result.HelpQueryResult;
import org.incendo.cloud.help.result.MultipleCommandResult;
import org.incendo.cloud.help.result.VerboseCommandResult;
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

    private HelpHandler<TestCommandSender> commandHelpHandler;
    private CommandManager<TestCommandSender> manager;
    private AnnotationParser<TestCommandSender> annotationParser;

    @BeforeEach
    void setup() {
        this.manager = new TestCommandManager();
        this.commandHelpHandler = this.manager.createHelpHandler();
        this.annotationParser = new AnnotationParser<>(
                this.manager,
                TestCommandSender.class
        );
    }

    @Test
    void queryForCommandAliasesWorks() {
        // Arrange
        this.annotationParser.parse(this);

        // Act
        final HelpQueryResult<TestCommandSender> result = this.commandHelpHandler.query(
                HelpQuery.of(new TestCommandSender(), "cc sub2")
        );

        // Assert
        assertThat(result).isInstanceOf(VerboseCommandResult.class);

        final VerboseCommandResult<TestCommandSender> topic =
                (VerboseCommandResult<TestCommandSender>) result;
        assertThat(topic.entry().command().toString()).isEqualTo("cloudcommand sub2 argument");
    }

    @Test
    void queryForCommandChildAliasesWorks() {
        // Arrange
        this.annotationParser.parse(this);

        // Act
        final HelpQueryResult<TestCommandSender> result = this.commandHelpHandler.query(
                HelpQuery.of(new TestCommandSender(), "cc s")
        );

        // Assert
        assertThat(result).isInstanceOf(VerboseCommandResult.class);

        final VerboseCommandResult<TestCommandSender> topic =
                (VerboseCommandResult<TestCommandSender>) result;
        assertThat(topic.entry().command().toString()).isEqualTo("cloudcommand sub2 argument");
    }

    @Test
    void queryForRootCommandWorks() {
        // Arrange
        this.annotationParser.parse(this);

        // Act
        final HelpQueryResult<TestCommandSender> result = this.commandHelpHandler.query(
                HelpQuery.of(new TestCommandSender(), "cloudcommand sub2")
        );

        // Assert
        assertThat(result).isInstanceOf(VerboseCommandResult.class);

        final VerboseCommandResult<TestCommandSender> topic =
                (VerboseCommandResult<TestCommandSender>) result;
        assertThat(topic.entry().command().toString()).isEqualTo("cloudcommand sub2 argument");
    }

    @Test
    void queryForRootCommandWorks2() {
        // Arrange
        this.annotationParser.parse(this);

        // Act
        final HelpQueryResult<TestCommandSender> result = this.commandHelpHandler.query(
                HelpQuery.of(new TestCommandSender(), "cloudcommand sub1")
        );

        // Assert
        assertThat(result).isInstanceOf(VerboseCommandResult.class);

        final VerboseCommandResult<TestCommandSender> topic =
                (VerboseCommandResult<TestCommandSender>) result;
        assertThat(topic.entry().command().toString()).isEqualTo("cloudcommand sub1 argument");
    }

    @Test
    void queryForOnlyRootCommandWorks() {
        // Arrange
        this.annotationParser.parse(this);

        // Act
        final HelpQueryResult<TestCommandSender> result = this.commandHelpHandler.query(
                HelpQuery.of(new TestCommandSender(), "cc")
        );

        // Assert
        assertThat(result).isInstanceOf(MultipleCommandResult.class);

        final MultipleCommandResult<TestCommandSender> topic =
                (MultipleCommandResult<TestCommandSender>) result;
        assertThat(topic.childSuggestions()).containsExactly(
                "cloudcommand sub1 [argument]",
                "cloudcommand sub2 [argument]"
        );
        assertThat(topic.longestPath()).isEqualTo("cloudcommand");
    }

    @Command("cloudcommand|cc sub1 [argument]")
    public void commandSource(@Argument("argument") final String argument) {
    }

    @Command("cloudcommand sub2|s [argument]")
    public void commandToken(@Argument("argument") final String argument) {
    }
}
