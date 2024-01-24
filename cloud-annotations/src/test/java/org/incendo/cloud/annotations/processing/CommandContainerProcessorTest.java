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
package org.incendo.cloud.annotations.processing;

import com.google.common.truth.StringSubject;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

class CommandContainerProcessorTest {

    @Test
    void testCommandContainerParsing() {
        // Arrange
        final Compiler compiler = javac().withProcessors(new CommandContainerProcessor());

        // Act
        final Compilation compilation = compiler.compile(
                JavaFileObjects.forResource("TestCommandContainer.java"),
                JavaFileObjects.forResource("TestCommandContainer2.java")
        );

        // Assert
        assertThat(compilation).succeeded();

        final StringSubject contentSubject = assertThat(compilation).generatedFile(
                StandardLocation.CLASS_OUTPUT,
                "" /* package */,
                CommandContainerProcessor.PATH
        ).contentsAsUtf8String();
        contentSubject.contains("TestCommandContainer");
        contentSubject.contains("TestCommandContainer2");
    }
}
