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
package cloud.commandframework.keys;

import cloud.commandframework.TestCommandSender;
import cloud.commandframework.context.CommandContext;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.keys.CloudKey.cloudKey;
import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link CloudKeyContainer} using {@link CommandContext}.
 */
class CloudKeyContainerTest {

    private CommandContext<?> context;

    @BeforeEach
    void setup() {
        context = new CommandContext<TestCommandSender>(
                new TestCommandSender(),
                createManager()
        );
    }

    @Test
    void Optional_CloudKey_ValuePresent_ReturnsValue() {
        // Arrange
        this.context.store("foo", "bar");

        // Act
        final Optional<String> result = this.context.optional(cloudKey("foo", String.class));

        // Assert
        assertThat(result).hasValue("bar");
    }

    @Test
    void Optional_CloudKey_ValueMissing_ReturnsEmptyOptional() {
        // Act
        final Optional<String> result = this.context.optional(cloudKey("foo", String.class));

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void Optional_String_ValuePresent_ReturnsValue() {
        // Arrange
        this.context.store("foo", "bar");

        // Act
        final Optional<String> result = this.context.optional("foo");

        // Assert
        assertThat(result).hasValue("bar");
    }

    @Test
    void Optional_String_ValueMissing_ReturnsEmptyOptional() {
        // Act
        final Optional<String> result = this.context.optional("foo");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void GetOrDefault_CloudKey_ValuePresent_ReturnsValue() {
        // Arrange
        this.context.store("foo", "bar");

        // Act
        final String result = this.context.getOrDefault(cloudKey("foo", String.class), "default");

        // Assert
        assertThat(result).isEqualTo("bar");
    }

    @Test
    void GetOrDefault_CloudKey_ValueMissing_ReturnsDefault() {
        // Act
        final String result = this.context.getOrDefault(cloudKey("foo", String.class), "default");

        // Assert
        assertThat(result).isEqualTo("default");
    }

    @Test
    void GetOrDefault_String_ValuePresent_ReturnsValue() {
        // Arrange
        this.context.store("foo", "bar");

        // Act
        final String result = this.context.getOrDefault("foo", "default");

        // Assert
        assertThat(result).isEqualTo("bar");
    }

    @Test
    void GetOrDefault_String_ValueMissing_ReturnsDefault() {
        // Act
        final String result = this.context.getOrDefault("foo", "default");

        // Assert
        assertThat(result).isEqualTo("default");
    }

    @Test
    void GetOrSupplyDefault_CloudKey_ValuePresent_ReturnsValue() {
        // Arrange
        this.context.store("foo", "bar");

        // Act
        final String result = this.context.getOrSupplyDefault(cloudKey("foo", String.class), () -> "default");

        // Assert
        assertThat(result).isEqualTo("bar");
    }

    @Test
    void GetOrSupplyDefault_CloudKey_ValueMissing_ReturnsDefault() {
        // Act
        final String result = this.context.getOrSupplyDefault(cloudKey("foo", String.class), () -> "default");

        // Assert
        assertThat(result).isEqualTo("default");
    }

    @Test
    void GetOrSupplyDefault_String_ValuePresent_ReturnsValue() {
        // Arrange
        this.context.store("foo", "bar");

        // Act
        final String result = this.context.getOrSupplyDefault("foo", () -> "default");

        // Assert
        assertThat(result).isEqualTo("bar");
    }

    @Test
    void GetOrSupplyDefault_String_ValueMissing_ReturnsDefault() {
        // Act
        final String result = this.context.getOrSupplyDefault("foo", () -> "default");

        // Assert
        assertThat(result).isEqualTo("default");
    }

    @Test
    void Get_CloudKey_ValuePresent_ReturnsValue() {
        // Arrange
        this.context.store("foo", "bar");

        // Act
        final String result = this.context.get(cloudKey("foo", String.class));

        // Assert
        assertThat(result).isEqualTo("bar");
    }

    @Test
    void Get_CloudKey_ValueMissing_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> this.context.get(cloudKey("foo", String.class)));
    }

    @Test
    void Get_String_ValuePresent_ReturnsValue() {
        // Arrange
        this.context.store("foo", "bar");

        // Act
        final String result = this.context.get("foo");

        // Assert
        assertThat(result).isEqualTo("bar");
    }

    @Test
    void Get_String_ValueMissing_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> this.context.get("foo"));
    }
}
