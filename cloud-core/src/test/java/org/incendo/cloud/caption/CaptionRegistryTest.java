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
package org.incendo.cloud.caption;

import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.TestCommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CaptionRegistryTest {

    private CaptionRegistry<TestCommandSender> captionRegistry;

    @BeforeEach
    void setup() {
        this.captionRegistry = CaptionRegistry.captionRegistry();
        this.captionRegistry.registerProvider(new StandardCaptionsProvider<>());
    }

    @ParameterizedTest
    @MethodSource("registryProvidersValuesForAllStandardKeysSource")
    void registryProvidersValuesForAllStandardKeys(final @NonNull Caption caption) {
        // Act
        final String value = this.captionRegistry.caption(caption, new TestCommandSender());

        // Assert
        assertThat(value).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("registryProvidersValuesForAllStandardKeysSource")
    void laterRegistryTakesPriority(final @NonNull Caption caption) {
        // Arrange
        this.captionRegistry.registerProvider(CaptionProvider.constantProvider(caption, "no!!"));

        // Act
        final String value = this.captionRegistry.caption(caption, new TestCommandSender());

        // Assert
        assertThat(value).isEqualTo("no!!");
    }

    @Test
    void unrecognizedCaptionThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> this.captionRegistry.caption(Caption.of("uwu"), new TestCommandSender())
        );
    }

    static Stream<@NonNull Caption> registryProvidersValuesForAllStandardKeysSource() {
        return StandardCaptionKeys.standardCaptionKeys().stream();
    }
}
