//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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

import javax.annotation.Nonnull;
import java.util.List;

final class SyntaxFragment {

    private final String major;
    private final List<String> minor;
    private final ArgumentMode argumentMode;

    SyntaxFragment(@Nonnull final String major,
                           @Nonnull final List<String> minor,
                           @Nonnull final ArgumentMode argumentMode) {
        this.major = major;
        this.minor = minor;
        this.argumentMode = argumentMode;
    }

    @Nonnull
    String getMajor() {
        return this.major;
    }

    @Nonnull
    List<String> getMinor() {
        return this.minor;
    }

    @Nonnull
    ArgumentMode getArgumentMode() {
        return this.argumentMode;
    }

}
