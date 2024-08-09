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
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.TestCommandManager;
import org.incendo.cloud.annotations.TestCommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for https://github.com/Incendo/cloud/issues/450.
 */
class Issue450 {

    private CommandManager<TestCommandSender> manager;
    private AnnotationParser<TestCommandSender> annotationParser;

    @BeforeEach
    void setup() {
        this.manager = new TestCommandManager();
        this.annotationParser = new AnnotationParser<>(
                this.manager,
                TestCommandSender.class
        );
    }

    @Test
    void testPrefixedAlias() {
        // Act
        this.annotationParser.parse(new TestCommandClass());

        // Assert
        this.manager.commandExecutor().executeCommand(
                new TestCommandSender(),
                "/command"
        ).join();
        this.manager.commandExecutor().executeCommand(
                new TestCommandSender(),
                "/cmd"
        ).join();
    }

    private static class TestCommandClass {

        @Command("/command|/cmd")
        public void testCommand() {
        }
    }
}
