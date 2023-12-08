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
package cloud.commandframework.annotations.processing;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class CommandMethodProcessorTest {

    @Test
    void testValidCommandMethodParsing() {
        // Arrange
        final Compiler compiler = javac().withProcessors(new CommandMethodProcessor());

        // Act
        final Compilation compilation = compiler.compile(
                JavaFileObjects.forResource("TestCommandMethod.java")
        );

        // Assert
        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    void testStaticCommandMethodParsing() {
        // Arrange
        final Compiler compiler = javac().withProcessors(new CommandMethodProcessor());

        // Act
        final Compilation compilation = compiler.compile(
                JavaFileObjects.forResource("TestCommandMethodStatic.java")
        );

        // Assert
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("@CommandMethod annotated methods should be non-static (commandMethod)");
    }

    @Test
    void testOptionalBeforeRequiredParsing() {
        // Arrange
        final Compiler compiler = javac().withProcessors(new CommandMethodProcessor());

        // Act
        final Compilation compilation = compiler.compile(
                JavaFileObjects.forResource("TestCommandMethodOptionalBeforeRequired.java")
        );

        // Assert
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining(
                "Required argument 'required' cannot succeed an optional argument (commandMethod)"
        );
    }

    @Test
    void testPrivateCommandMethodParsing() {
        // Arrange
        final Compiler compiler = javac().withProcessors(new CommandMethodProcessor());

        // Act
        final Compilation compilation = compiler.compile(
                JavaFileObjects.forResource("TestCommandMethodPrivate.java")
        );

        // Assert
        assertThat(compilation).succeeded();
        assertThat(compilation).hadWarningContaining("@CommandMethod annotated methods should be public (commandMethod)");
    }

    @Test
    void testCommandMethodMissingArgumentParsing() {
        // Arrange
        final Compiler compiler = javac().withProcessors(new CommandMethodProcessor());

        // Act
        final Compilation compilation = compiler.compile(
                JavaFileObjects.forResource("TestCommandMethodMissingArgument.java")
        );

        // Assert
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("@Argument(\"required\") is missing from @CommandMethod (commandMethod)");
    }

    @Test
    void testCommandMethodMissingSyntaxParsing() {
        // Arrange
        final Compiler compiler = javac().withProcessors(new CommandMethodProcessor());

        // Act
        final Compilation compilation = compiler.compile(
                JavaFileObjects.forResource("TestCommandMethodMissingSyntax.java")
        );

        // Assert
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Argument 'optional' is missing from the @CommandMethod syntax (commandMethod)");
    }

    @Test
    void testCommandMethodWithoutArgumentAnnotations() {
        // Arrange
        final Compiler compiler = javac().withProcessors(new CommandMethodProcessor());

        // Act
        final Compilation compilation = compiler.compile(
                JavaFileObjects.forResource("TestCommandMethodWithoutArgumentAnnotations.java")
        );

        // Assert
        assertThat(compilation).succeededWithoutWarnings();
    }
}
