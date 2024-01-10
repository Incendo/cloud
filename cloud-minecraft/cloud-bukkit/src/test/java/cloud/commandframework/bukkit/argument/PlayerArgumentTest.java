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
package cloud.commandframework.bukkit.argument;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.bukkit.parsers.PlayerParser;
import cloud.commandframework.bukkit.util.ServerTest;
import cloud.commandframework.context.CommandInput;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
class PlayerArgumentTest extends ServerTest {

    @Mock
    private Player player;

    @Test
    void Parse_HappyFlow_Success() {
        // Arrange
        when(this.server().getPlayer("player")).thenReturn(this.player);
        final PlayerParser<CommandSender> parser = new PlayerParser<>();
        final CommandInput commandInput = CommandInput.of("player");

        // Act
        final ArgumentParseResult<Player> result = parser.parse(
                this.commandContext(),
                commandInput
        );

        // Assert
        assertThat(result.failure()).isEmpty();
        assertThat(result.parsedValue()).hasValue(player);
        assertThat(commandInput.remainingInput()).isEmpty();

        verify(this.server()).getPlayer("player");
    }

    @Test
    void Parse_NonExistentPlayer_Failure() {
        // Arrange
        final PlayerParser<CommandSender> parser = new PlayerParser<>();
        final CommandInput commandInput = CommandInput.of("player");

        // Act
        final ArgumentParseResult<Player> result = parser.parse(
                this.commandContext(),
                commandInput
        );

        // Assert
        assertThat(result.failure()).isPresent();
        assertThat(result.parsedValue()).isEmpty();

        verify(this.server()).getPlayer("player");
    }
}
