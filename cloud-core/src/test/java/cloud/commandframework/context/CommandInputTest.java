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
package cloud.commandframework.context;

import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

class CommandInputTest {

    @ParameterizedTest
    @ValueSource(strings = { "hello world", "does this work", "trailing space? "} )
    void Input_AnyInput_ReturnsInput(final @NonNull String input) {
        // Arrange
        final CommandInput commandInput = CommandInput.of(input);

        // Act
        final String result = commandInput.input();

        // Assert
        assertThat(result).isEqualTo(input);
    }

    @ParameterizedTest
    @ValueSource(strings = { "hello world", "does this work", "trailing space? "} )
    void Length_AnyInput_ReturnsInputLength(final @NonNull String input) {
        // Arrange
        final CommandInput commandInput = CommandInput.of(input);

        // Act
        final int result = commandInput.length();

        // Assert
        assertThat(result).isEqualTo(input.length());
    }

    @Test
    void MoveCursor_WithinString_CursorMoves() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello world");

        // Act
        commandInput.moveCursor(6);

        // Assert
        assertThat(commandInput.cursor()).isEqualTo(6);
        assertThat(commandInput.remainingInput()).isEqualTo("world");
    }

    @Test
    void MoveCursor_OutsideString_ThrowsException() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello");

        // Act
        final CommandInput.CursorOutOfBoundsException exception = assertThrows(
                CommandInput.CursorOutOfBoundsException.class,
                () -> commandInput.moveCursor(6)
        );

        // Assert
        assertThat(exception).isNotNull();
    }

    @Test
    void Peek_SeveralCharacters_ReturnsSubstring() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello world");
        commandInput.moveCursor(6);

        // Act
        final String result = commandInput.peek(5);

        // Assert
        assertThat(result).isEqualTo("world");
        assertThat(commandInput.cursor()).isEqualTo(6);
    }

    @Test
    void Peek_OutsideString_ThrowsException() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello world");
        commandInput.moveCursor(6);

        // Act
        final CommandInput.CursorOutOfBoundsException exception = assertThrows(
                CommandInput.CursorOutOfBoundsException.class,
                () -> commandInput.peek(6)
        );

        // Assert
        assertThat(exception).isNotNull();
    }

    @Test
    void Read_SeveralCharacters_ReturnsSubstring() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello world");
        commandInput.moveCursor(6);

        // Act
        final String result = commandInput.read(5);

        // Assert
        assertThat(result).isEqualTo("world");
        assertThat(commandInput.cursor()).isEqualTo(11);
    }

    @Test
    void Read_OutsideString_ThrowsException() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello world");
        commandInput.moveCursor(6);

        // Act
        final CommandInput.CursorOutOfBoundsException exception = assertThrows(
                CommandInput.CursorOutOfBoundsException.class,
                () -> commandInput.read(6)
        );

        // Assert
        assertThat(exception).isNotNull();
    }

    @Test
    void Peek_SingleCharacter_ReturnsCharacter() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello world");
        commandInput.moveCursor(6);

        // Act
        final char result = commandInput.peek();

        // Assert
        assertThat(result).isEqualTo('w');
        assertThat(commandInput.cursor()).isEqualTo(6);
    }

    @Test
    void Peek_SingleCharacterOutsideString_ThrowsException() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello");
        commandInput.moveCursor(5);

        // Act
        final CommandInput.CursorOutOfBoundsException exception = assertThrows(
                CommandInput.CursorOutOfBoundsException.class,
                commandInput::peek
        );

        // Assert
        assertThat(exception).isNotNull();
    }

    @Test
    void Read_SingleCharacter_ReturnsCharacter() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello world");
        commandInput.moveCursor(6);

        // Act
        final char result = commandInput.read();

        // Assert
        assertThat(result).isEqualTo('w');
        assertThat(commandInput.cursor()).isEqualTo(7);
    }

    @Test
    void Read_SingleCharacterOutsideString_ThrowsException() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello");
        commandInput.moveCursor(5);

        // Act
        final CommandInput.CursorOutOfBoundsException exception = assertThrows(
                CommandInput.CursorOutOfBoundsException.class,
                commandInput::read
        );

        // Assert
        assertThat(exception).isNotNull();
    }

    @Test
    void PeekString_EmptyString_ReturnsEmptyString() {
        // Arrange
        final CommandInput commandInput = CommandInput.empty();

        // Act
        final String result = commandInput.peekString();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void PeekString_SingleToken_ReturnsToken() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello");

        // Act
        final String result = commandInput.peekString();

        // Assert
        assertThat(result).isEqualTo("hello");
        assertThat(commandInput.cursor()).isEqualTo(0);
    }
    @Test
    void PeekString_MultipleTokens_ReturnsFirstToken() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello cruel world");

        // Act
        final String result = commandInput.peekString();

        // Assert
        assertThat(result).isEqualTo("hello");
        assertThat(commandInput.cursor()).isEqualTo(0);
    }

    @Test
    void ReadString_EmptyString_ReturnsEmptyString() {
        // Arrange
        final CommandInput commandInput = CommandInput.empty();

        // Act
        final String result = commandInput.readString();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void ReadString_SingleToken_ReturnsToken() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello");

        // Act
        final String result = commandInput.readString();

        // Assert
        assertThat(result).isEqualTo("hello");
        assertThat(commandInput.remainingInput()).isEmpty();
        assertThat(commandInput.cursor()).isEqualTo(5);
        assertThat(commandInput.remainingLength()).isEqualTo(0);
        assertThat(commandInput.readInput()).isEqualTo("hello");
    }

    @Test
    void ReadString_MultipleTokens_ReturnsFirstTokenAndConsumesWhitespace() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello cruel world");

        // Act
        final String result = commandInput.readString();

        // Assert
        assertThat(result).isEqualTo("hello");
        assertThat(commandInput.remainingInput()).isEqualTo("cruel world");
        assertThat(commandInput.cursor()).isEqualTo(6);
        assertThat(commandInput.remainingLength()).isEqualTo(11);
        assertThat(commandInput.readInput()).isEqualTo("hello ");
    }

    @Test
    void ReadStringPreserveWhitespace_MultipleTokens_ReturnsFirstTokenAndPreservesWhitespace() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello cruel world");

        // Act
        final String result = commandInput.readStringPreserveWhitespace();

        // Assert
        assertThat(result).isEqualTo("hello");
        assertThat(commandInput.remainingInput()).isEqualTo(" cruel world");
        assertThat(commandInput.cursor()).isEqualTo(5);
        assertThat(commandInput.remainingLength()).isEqualTo(12);
        assertThat(commandInput.readInput()).isEqualTo("hello");
    }

    @Test
    void Tokenize_EmptyString_ReturnsEmptyList() {
        // Arrange
        final CommandInput commandInput = CommandInput.empty();

        // Act
        final List<String> result = commandInput.tokenize();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void Tokenize_SingleToken_ReturnsSingleToken() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello");

        // Act
        final List<String> result = commandInput.tokenize();

        // Assert
        assertThat(result).containsExactly("hello");
    }

    @Test
    void Tokenize_MultipleTokens_ReturnsAllTokens() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello cruel world");

        // Act
        final List<String> result = commandInput.tokenize();

        // Assert
        assertThat(result).containsExactly("hello", "cruel", "world");
    }

    @Test
    void ReadUntilAndSkip_EmptyString_ReturnsEmptyString() {
        // Arrange
        final CommandInput commandInput = CommandInput.empty();

        // Act
        final String result = commandInput.readUntilAndSkip(':');

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void ReadUntilAndSkip_NonEmptyString_ReturnsAndSkips() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello:world");

        // Act
        final String result = commandInput.readUntilAndSkip(':');

        // Assert
        assertThat(result).isEqualTo("hello");
        assertThat(commandInput.remainingInput()).isEqualTo("world");
        assertThat(commandInput.readInput()).isEqualTo("hello:");
    }

    @Test
    void ReadUntilAndSkip_DoesNotContainSeparator_ReadsEntireString() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello world");

        // Act
        final String result = commandInput.readUntilAndSkip(':');

        // Assert
        assertThat(result).isEqualTo("hello world");
    }

    @Test
    void SkipWhitespace_AtWhitespace_Skips() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("  hello");

        // Act
        commandInput.skipWhitespace();

        // Assert
        assertThat(commandInput.remainingInput()).isEqualTo("hello");
    }

    @Test
    void SkipWhitespace_OnlyWhitespace_Skips() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("  ");

        // Act
        commandInput.skipWhitespace();

        // Assert
        assertThat(commandInput.remainingInput()).isEmpty();
    }

    @Test
    void SkipWhitespace_NotAtWhitespace_DoesNotSkip() {
        // Arrange
        final CommandInput commandInput = CommandInput.of("hello");

        // Act
        commandInput.skipWhitespace();

        // Assert
        assertThat(commandInput.remainingInput()).isEqualTo("hello");
    }

    @Test
    void Difference_SameInput_ReturnsDifference() {
        // Arrange
        final CommandInput original = CommandInput.of("hello world");
        final CommandInput input = original.copy();
        input.readString();

        // Act
        final String result = original.difference(input);

        // Assert
        assertThat(result).isEqualTo("hello");
    }
}
