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
package org.incendo.cloud.settings;

import org.incendo.cloud.setting.Configurable;
import org.incendo.cloud.setting.Setting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class EnumConfigurableTest {

    private Configurable<TestSetting> configurable;

    @BeforeEach
    void setup() {
        this.configurable = Configurable.enumConfigurable(TestSetting.class);
    }

    @Test
    void testGetNonExisting() {
        // Arrange
        final TestSetting setting = TestSetting.FOO;

        // Act
        final boolean result = this.configurable.get(setting);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testSetTrue() {
        // Arrange
        final TestSetting setting = TestSetting.FOO;

        // Act
        this.configurable.set(setting, true);

        // Assert
        assertThat(this.configurable.get(setting)).isTrue();
    }

    @Test
    void testSetFalse() {
        // Arrange
        final TestSetting setting = TestSetting.FOO;
        this.configurable.set(setting, true);

        // Act
        this.configurable.set(setting, false);

        // Assert
        assertThat(this.configurable.get(setting)).isFalse();
    }


    enum TestSetting implements Setting {
        FOO,
        BAR
    }
}
