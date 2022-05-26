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
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatternReplacingStringProcessorTest {

    private static final Pattern TEST_PATTERN = Pattern.compile("\\[(\\S+)]");

    private PatternReplacingStringProcessor patternReplacingStringProcessor;

    @Mock
    private Function<MatchResult, String> replacementProvider;

    @BeforeEach
    void setup() {
        this.patternReplacingStringProcessor = new PatternReplacingStringProcessor(
                TEST_PATTERN,
                this.replacementProvider
        );
    }

    @Test
    void ProcessString_MatchingInput_ReplacesGroups() {
        // Arrange
        when(this.replacementProvider.apply(any())).thenAnswer(iom -> iom.getArgument(0, MatchResult.class).group(1));

        final String input = "[hello] [world]!";

        // Act
        final String output = this.patternReplacingStringProcessor.processString(input);

        // Act
        assertThat(output).isEqualTo("hello world!");

        verify(this.replacementProvider, times(2)).apply(notNull());
        verifyNoMoreInteractions(this.replacementProvider);
    }

    @Test
    void ProcessString_NullReplacement_InputPreserved() {
        // Arrange
        final String input = "[input] ...";

        // Act
        final String output = this.patternReplacingStringProcessor.processString(input);

        // Act
        assertThat(output).isEqualTo(input);

        verify(this.replacementProvider).apply(notNull());
        verifyNoMoreInteractions(this.replacementProvider);
    }
}
