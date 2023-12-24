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
package cloud.commandframework.bukkit.argument;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.bukkit.parsers.location.LocationParser;
import cloud.commandframework.bukkit.util.ServerTest;
import cloud.commandframework.context.CommandInput;
import java.util.Collections;
import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

class LocationArgumentTest extends ServerTest {

    @Mock
    private World world;

    @ParameterizedTest
    @MethodSource("Parse_HappyFlow_Success_Source")
    void Parse_HappyFlow_Success(final @NonNull String input, final @NonNull Vector expectedLocation) {
        // Arrange
        when(this.server().getWorlds()).thenReturn(Collections.singletonList(this.world));
        final LocationParser<CommandSender> parser = new LocationParser<>();
        final CommandInput commandInput = CommandInput.of(input);

        // Act
        final ArgumentParseResult<Location> result = parser.parse(
                this.commandContext(),
                commandInput
        );

        // Assert
        assertThat(result.failure()).isEmpty();
        assertThat(result.parsedValue().isPresent()).isTrue();
        assertThat(result.parsedValue().get().toVector()).isEqualTo(expectedLocation);
        assertThat(commandInput.remainingInput()).isEmpty();
    }

    static @NonNull Stream<@NonNull Arguments> Parse_HappyFlow_Success_Source() {
        return Stream.of(
                arguments("~ ~ ~", new Vector(0, 0, 0)),
                arguments("~10 ~10 ~10", new Vector(10, 10, 10)),
                arguments("~-10 ~-10 ~-10", new Vector(-10, -10, -10)),
                arguments("^ ^ ^", new Vector(0, 0, 0)),
                arguments("^0 ^0 ^0", new Vector(0, 0, 0)),
                arguments("0 0 0", new Vector(0, 0, 0)),
                arguments("10 10 10", new Vector(10, 10, 10)),
                arguments("-10 -10 -10", new Vector(-10, -10, -10))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = { "0 0", "not a location" })
    void Parse_InvalidLocation_Failure(final @NonNull String input) {
        // Arrange
        final LocationParser<CommandSender> parser = new LocationParser<>();
        final CommandInput commandInput = CommandInput.of(input);

        // Act
        final ArgumentParseResult<Location> result = parser.parse(
                this.commandContext(),
                commandInput
        );

        // Assert
        assertThat(result.failure()).isPresent();
        assertThat(result.parsedValue()).isEmpty();
    }
}
