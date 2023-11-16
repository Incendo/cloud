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
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.bukkit.util.ServerTest;
import cloud.commandframework.context.CommandInput;
import com.google.common.truth.Truth;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static com.google.common.truth.Truth8.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
class OfflinePlayerArgumentTest extends ServerTest {

    @Mock
    private OfflinePlayer player;

    @BeforeEach
    void setup() {
        when(this.server().getOfflinePlayer("player")).thenReturn(this.player);
    }

    @Test
    void Parse_HappyFlow_Success() {
        // Arrange
        when(this.player.hasPlayedBefore()).thenReturn(true);
        final OfflinePlayerArgument.OfflinePlayerParser<CommandSender> parser = new OfflinePlayerArgument.OfflinePlayerParser<>();
        final CommandInput commandInput = CommandInput.of("player");

        // Act
        final ArgumentParseResult<OfflinePlayer> result = parser.parse(
                this.commandContext(),
                commandInput
        );

        // Assert
        assertThat(result.getFailure()).isEmpty();
        assertThat(result.getParsedValue()).hasValue(player);
        Truth.assertThat(commandInput.remainingInput()).isEmpty();

        verify(this.server()).getOfflinePlayer("player");
    }

    @Test
    void Parse_NonExistentPlayer_Failure() {
        // Arrange
        final OfflinePlayerArgument.OfflinePlayerParser<CommandSender> parser = new OfflinePlayerArgument.OfflinePlayerParser<>();
        final CommandInput commandInput = CommandInput.of("player");

        // Act
        final ArgumentParseResult<OfflinePlayer> result = parser.parse(
                this.commandContext(),
                commandInput
        );

        // Assert
        assertThat(result.getFailure()).isPresent();
        assertThat(result.getParsedValue()).isEmpty();

        verify(this.server()).getOfflinePlayer("player");
    }
}
