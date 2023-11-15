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
package cloud.commandframework.context;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

final class CommandInputImpl implements CommandInput {

    private final String input;
    private int cursor;

    CommandInputImpl(final @NonNull String input) {
        this(input, 0 /* cursor */);
    }

    CommandInputImpl(final @NonNull String input, final @NonNegative int cursor) {
        this.input = input;
        this.cursor = cursor;
    }

    @Override
    public @NonNull String input() {
        return this.input;
    }

    @Override
    public @NonNull CommandInput appendString(final @NonNull String string) {
        if (this.hasRemainingInput() && !this.remainingInput().endsWith(" ")) {
            return new CommandInputImpl(String.format("%s %s", this.input, string), this.cursor);
        } else {
            return new CommandInputImpl(this.input + string, this.cursor);
        }
    }

    @Override
    public @NonNegative int cursor() {
        return this.cursor;
    }

    @Override
    public void moveCursor(final int chars) {
        if (this.cursor() + chars > this.length()) {
            throw new CursorOutOfBoundsException(this.cursor() + chars, this.length());
        }
        this.cursor += chars;
    }

    @Override
    public void cursor(final int cursor) {
        if (cursor < 0 || cursor > this.length()) {
            throw new CursorOutOfBoundsException(cursor, this.length());
        }
        this.cursor = cursor;
    }

    @Override
    public @NonNull CommandInput copy() {
        return new CommandInputImpl(this.input, this.cursor);
    }
}
