//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
package cloud.commandframework.annotations;

import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyReplacingStringProcessorTest {

    private PropertyReplacingStringProcessor propertyReplacingStringProcessor;

    @Mock
    private Function<String, String> propertyProvider;

    @BeforeEach
    void setup() {
        this.propertyReplacingStringProcessor = new PropertyReplacingStringProcessor(this.propertyProvider);
    }

    @Test
    void ProcessString_KnownProperty_ReplacesWithValue() {
        // Arrange
        when(this.propertyProvider.apply(anyString())).thenAnswer(iom -> "transformed: " + iom.getArgument(0, String.class));

        final String input = "${hello.world}";

        // Act
        final String output = this.propertyReplacingStringProcessor.processString(input);

        // Assert
        assertThat(output).isEqualTo("transformed: hello.world");

        verify(this.propertyProvider).apply("hello.world");
        verifyNoMoreInteractions(this.propertyProvider);
    }

    @Test
    void ProcessString_MultipleProperties_ReplacesAll() {
        // Arrange
        when(this.propertyProvider.apply(anyString())).thenAnswer(iom -> iom.getArgument(0, String.class));

        final String input = "${cats} are cute, and so are ${dogs}!";

        // Act
        final String output = this.propertyReplacingStringProcessor.processString(input);

        // Assert
        assertThat(output).isEqualTo("cats are cute, and so are dogs!");

        verify(this.propertyProvider).apply("cats");
        verify(this.propertyProvider).apply("dogs");
        verifyNoMoreInteractions(this.propertyProvider);
    }

    @Test
    void ProcessString_NullProperty_InputPreserved() {
        // Arrange
        final String input = "${input} ...";

        // Act
        final String output = this.propertyReplacingStringProcessor.processString(input);

        // Act
        assertThat(output).isEqualTo(input);

        verify(this.propertyProvider).apply(notNull());
        verifyNoMoreInteractions(this.propertyProvider);
    }
}
