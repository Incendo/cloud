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
package cloud.commandframework.brigadier.argument;

import cloud.commandframework.context.CommandInput;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class CloudStringReaderTest {

    @Test
    void testUnchanged() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello world");

        // Act
        CloudStringReader.of(commandInput);

        // Assert
        assertThat(commandInput.remainingInput()).isEqualTo("hello world");
    }

    @Test
    void testSingleWorldRemoved() throws CommandSyntaxException {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello some worlds");
        final StringReader stringReader = CloudStringReader.of(commandInput);

        // Act
        final String readString = stringReader.readString();
        stringReader.skipWhitespace();

        // Assert
        assertThat(readString).isEqualTo("hello");
        assertThat(commandInput.remainingInput()).isEqualTo("some worlds");
    }

    @Test
    void testBeginningAndMiddleRemoved() throws CommandSyntaxException {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello some worlds");
        final StringReader stringReader = CloudStringReader.of(commandInput);

        // Act
        final String readString1 = stringReader.readString();
        stringReader.skipWhitespace();
        final String readString2 = stringReader.readString();
        stringReader.skipWhitespace();

        // Assert
        assertThat(readString1).isEqualTo("hello");
        assertThat(readString2).isEqualTo("some");
        assertThat(commandInput.remainingInput()).isEqualTo("worlds");
    }

    @Test
    void testAllThreeWordsRead() throws CommandSyntaxException {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello some worlds");
        final StringReader stringReader = CloudStringReader.of(commandInput);

        // Act
        final String readString1 = stringReader.readString();
        stringReader.skipWhitespace();
        final String readString2 = stringReader.readString();
        stringReader.skipWhitespace();
        final String readString3 = stringReader.readString();

        // Assert
        assertThat(readString1).isEqualTo("hello");
        assertThat(readString2).isEqualTo("some");
        assertThat(readString3).isEqualTo("worlds");
        assertThat(commandInput.isEmpty()).isTrue();
    }

    @Test
    void testPartialWorldRead() throws CommandSyntaxException {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hi minecraft:pig");
        final StringReader stringReader = CloudStringReader.of(commandInput);

        // Act
        final String readString1 = stringReader.readString();
        stringReader.skipWhitespace();
        final String readString2 = stringReader.readStringUntil(':');

        // Assert
        assertThat(readString1).isEqualTo("hi");
        assertThat(readString2).isEqualTo("minecraft");
        assertThat(commandInput.remainingInput()).isEqualTo("pig");
    }

    @Test
    void testIntRead() throws CommandSyntaxException {
        // Arrange
        final CommandInput commandInput = CommandInput.of("123 abc");
        final StringReader stringReader = CloudStringReader.of(commandInput);

        // Act
        final int readInt = stringReader.readInt();
        stringReader.skipWhitespace();

        // Assert
        assertThat(readInt).isEqualTo(123);
        assertThat(commandInput.remainingInput()).isEqualTo("abc");
    }
}
