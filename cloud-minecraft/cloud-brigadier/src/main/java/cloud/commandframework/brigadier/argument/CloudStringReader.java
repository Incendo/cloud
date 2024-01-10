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
package cloud.commandframework.brigadier.argument;

import cloud.commandframework.context.CommandInput;
import com.mojang.brigadier.StringReader;
import org.checkerframework.checker.nullness.qual.NonNull;

final class CloudStringReader extends StringReader {

    private final CommandInput commandInput;

    static @NonNull CloudStringReader of(final @NonNull CommandInput commandInput) {
        return new CloudStringReader(commandInput);
    }

    private CloudStringReader(final @NonNull CommandInput commandInput) {
        super(commandInput.input());
        this.commandInput = commandInput;
        super.setCursor(commandInput.cursor());
    }

    @Override
    public void setCursor(final int cursor) {
        super.setCursor(cursor);
        this.commandInput.cursor(cursor);
    }

    @Override
    public char read() {
        super.read(); // We need to advance the cursor.
        return this.commandInput.read();
    }

    @Override
    public void skip() {
        super.skip();
        this.commandInput.moveCursor(1);
    }
}
