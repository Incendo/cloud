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
package cloud.commandframework.setting;

import java.util.EnumSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

final class EnumConfigurable<S extends Enum<S> & Setting> implements Configurable<S> {

    private final EnumSet<S> settings;

    EnumConfigurable(final @NonNull Class<S> settingClass) {
        this.settings = EnumSet.noneOf(settingClass);
    }

    EnumConfigurable(final @NonNull S defaultSetting) {
        this.settings = EnumSet.of(defaultSetting);
    }

    @Override
    public @This @NonNull EnumConfigurable<S> set(final @NonNull S setting, final boolean value) {
        if (value) {
            this.settings.add(setting);
        } else {
            this.settings.remove(setting);
        }
        return this;
    }

    @Override
    public boolean get(final @NonNull S setting) {
        return this.settings.contains(setting);
    }
}
