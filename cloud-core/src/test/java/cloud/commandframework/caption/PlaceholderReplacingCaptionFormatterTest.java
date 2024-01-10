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
package cloud.commandframework.caption;

import cloud.commandframework.TestCommandSender;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionFormatter;
import cloud.commandframework.captions.CaptionVariable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class PlaceholderReplacingCaptionFormatterTest {

    private CaptionFormatter<TestCommandSender, String> captionFormatter;

    @BeforeEach
    void setup() {
        this.captionFormatter = CaptionFormatter.placeholderReplacing();
    }

    @Test
    void replacesPlaceholders() {
        // Arrange
        final Caption captionKey = Caption.of("key");
        final TestCommandSender commandSender = new TestCommandSender();
        final String caption = "caption with a <variable> and maybe another <one> but also a missing <var>";
        final CaptionVariable[] variables = new CaptionVariable[] {
                CaptionVariable.of("variable", "foo"),
                CaptionVariable.of("one", "bar")
        };

        // Act
        final String formatted = this.captionFormatter.formatCaption(captionKey, commandSender, caption, variables);

        // Assert
        assertThat(formatted).isEqualTo("caption with a foo and maybe another bar but also a missing <var>");
    }
}
