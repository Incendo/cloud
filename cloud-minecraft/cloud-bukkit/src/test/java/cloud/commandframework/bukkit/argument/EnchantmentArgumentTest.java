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
import cloud.commandframework.bukkit.parsers.EnchantmentArgument;
import cloud.commandframework.bukkit.util.ServerTest;
import cloud.commandframework.context.CommandInput;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Stream;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
class EnchantmentArgumentTest extends ServerTest {

    @BeforeAll
    static void setupEnchantments() throws Exception {
        final Field keyField = Enchantment.class.getDeclaredField("key");
        keyField.setAccessible(true);

        for (final Field field : Enchantment.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType().isAssignableFrom(Enchantment.class)) {
                final Enchantment mockEnchantment = Mockito.mock(Enchantment.class);
                final NamespacedKey enchantmentKey = ((Enchantment) field.get(null)).getKey();
                when(mockEnchantment.getKey()).thenReturn(enchantmentKey);
                when(mockEnchantment.getName()).thenReturn(enchantmentKey.getKey());

                // Bukkit is bad and accesses this field directly, so we cannot just mock "getKey" but we must also override
                // the inner key.
                keyField.set(mockEnchantment, enchantmentKey);
                try {
                    Enchantment.registerEnchantment(mockEnchantment);
                } catch (final Exception ignored) {
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("Parse_HappyFlow_Success_Source")
    void Parse_HappyFlow_Success(final @NonNull String input, final @NonNull Enchantment enchantment) {
        // Arrange
        final EnchantmentArgument.EnchantmentParser<CommandSender> parser = new EnchantmentArgument.EnchantmentParser<>();
        final CommandInput commandInput = CommandInput.of(input);

        // Act
        final ArgumentParseResult<Enchantment> result = parser.parse(
                this.commandContext(),
                commandInput
        );

        // Assert
        assertThat(result.getFailure()).isEmpty();
        assertThat(result.getParsedValue()).hasValue(enchantment);
        assertThat(commandInput.remainingInput()).isEmpty();
    }

    static @NonNull Stream<@NonNull Arguments> Parse_HappyFlow_Success_Source() {
        return Arrays.stream(Enchantment.values())
                .flatMap(
                        enchantment -> Stream.of(enchantment.getKey().getKey(), enchantment.getKey().toString())
                                .map(input -> arguments(input, enchantment))
                );
    }

    @Test
    void Parse_NonExistentEnchantment_Failure() {
        // Arrange
        final EnchantmentArgument.EnchantmentParser<CommandSender> parser = new EnchantmentArgument.EnchantmentParser<>();
        final CommandInput commandInput = CommandInput.of("enchantment");

        // Act
        final ArgumentParseResult<Enchantment> result = parser.parse(
                this.commandContext(),
                commandInput
        );

        // Assert
        assertThat(result.getFailure()).isPresent();
        assertThat(result.getParsedValue()).isEmpty();
    }
}
