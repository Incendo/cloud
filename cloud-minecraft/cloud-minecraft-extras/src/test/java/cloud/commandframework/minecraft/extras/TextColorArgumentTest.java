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
package cloud.commandframework.minecraft.extras;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import java.util.Locale;
import java.util.stream.Stream;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class TextColorArgumentTest {

    @Mock
    private CommandContext<Object> commandContext;

    @ParameterizedTest
    @MethodSource("Parse_LegacyColor_Success_Source")
    void Parse_LegacyColor_Success(final @NonNull String input, final @NonNull TextColor color) {
        // Arrange
        final TextColorParser<Object> parser = new TextColorParser<>();
        final CommandInput commandInput = CommandInput.of(input);

        // Act
        final ArgumentParseResult<TextColor> result = parser.parse(this.commandContext, commandInput);

        // Assert
        assertThat(result.failure()).isEmpty();
        assertThat(result.parsedValue()).hasValue(color);
        assertThat(commandInput.remainingInput()).isEmpty();
    }

    static @NonNull Stream<@NonNull Arguments> Parse_LegacyColor_Success_Source() {
        return Stream.of(
                arguments("&0", NamedTextColor.BLACK),
                arguments("&1", NamedTextColor.DARK_BLUE),
                arguments("&2", NamedTextColor.DARK_GREEN),
                arguments("&3", NamedTextColor.DARK_AQUA),
                arguments("&4", NamedTextColor.DARK_RED),
                arguments("&5", NamedTextColor.DARK_PURPLE),
                arguments("&6", NamedTextColor.GOLD),
                arguments("&7", NamedTextColor.GRAY),
                arguments("&8", NamedTextColor.DARK_GRAY),
                arguments("&9", NamedTextColor.BLUE),
                arguments("&a", NamedTextColor.GREEN),
                arguments("&b", NamedTextColor.AQUA),
                arguments("&c", NamedTextColor.RED),
                arguments("&d", NamedTextColor.LIGHT_PURPLE),
                arguments("&e", NamedTextColor.YELLOW),
                arguments("&f", NamedTextColor.WHITE)
        );
    }

    @ParameterizedTest
    @MethodSource("Parse_NamedColor_Success_Source")
    void Parse_NamedColor_Success(final @NonNull String input, final @NonNull TextColor color) {
        // Arrange
        final TextColorParser<Object> parser = new TextColorParser<>();
        final CommandInput commandInput = CommandInput.of(input);

        // Act
        final ArgumentParseResult<TextColor> result = parser.parse(this.commandContext, commandInput);

        // Assert
        assertThat(result.failure()).isEmpty();
        assertThat(result.parsedValue()).hasValue(color);
        assertThat(commandInput.remainingInput()).isEmpty();
    }

    static @NonNull Stream<@NonNull Arguments> Parse_NamedColor_Success_Source() {
        return NamedTextColor.NAMES.values().stream().map(color -> arguments(color.toString().toLowerCase(Locale.ROOT), color));
    }

    @ParameterizedTest
    @MethodSource("Parse_HexColor_Success_Source")
    void Parse_HexColor_Success(final @NonNull String input, final @NonNull TextColor color) {
        // Arrange
        final TextColorParser<Object> parser = new TextColorParser<>();
        final CommandInput commandInput = CommandInput.of(input);

        // Act
        final ArgumentParseResult<TextColor> result = parser.parse(this.commandContext, commandInput);

        // Assert
        assertThat(result.failure()).isEmpty();
        assertThat(result.parsedValue()).hasValue(color);
        assertThat(commandInput.remainingInput()).isEmpty();
    }

    static @NonNull Stream<@NonNull Arguments> Parse_HexColor_Success_Source() {
        return Stream.of(
                arguments("#000000", NamedTextColor.BLACK),
                arguments("#ffffff", NamedTextColor.WHITE),
                arguments("000000", NamedTextColor.BLACK),
                arguments("ffffff", NamedTextColor.WHITE)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = { "notacolor", "", "woo" } )
    void Parse_InvalidColor_Failure(final @NonNull String input) {
        // Arrange
        final TextColorParser<Object> parser = new TextColorParser<>();
        final CommandInput commandInput = CommandInput.of(input);

        // Act
        final ArgumentParseResult<TextColor> result = parser.parse(this.commandContext, commandInput);

        // Assert
        assertThat(result.failure()).isPresent();
        assertThat(result.parsedValue()).isEmpty();
    }
}
