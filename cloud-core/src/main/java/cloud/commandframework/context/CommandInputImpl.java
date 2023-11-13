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

import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.internal.CommandInputTokenizer;
import java.util.LinkedList;
import java.util.Locale;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

final class CommandInputImpl implements CommandInput {

    private String input;
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
    public void input(final @NonNull String input) {
        this.input = input;
    }

    @Override
    public void appendString(final @NonNull String string) {
        if (this.hasRemainingInput() && !this.remainingInput().endsWith(" ")) {
            this.input = String.format("%s %s", this.input, string);
        } else {
            this.input += string;
        }
        System.out.println("Input after: " + this.input());
    }

    @Override
    public @NonNegative int cursor() {
        return this.cursor;
    }

    @Override
    public @NonNegative int length() {
        return this.input.length();
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
        if (cursor < 0 || cursor >= this.length()) {
            throw new CursorOutOfBoundsException(cursor, this.length());
        }
        this.cursor = cursor;
    }

    @Override
    public boolean isValidByte(final byte min, final byte max) {
        try {
            final byte parsedByte = Byte.parseByte(this.peekString());
            return parsedByte >= min && parsedByte <= max;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    @Override
    public byte readByte() {
        return Byte.parseByte(this.readString());
    }

    @Override
    public boolean isValidShort(final short min, final short max) {
        try {
            final short parsedShort = Short.parseShort(this.peekString());
            return parsedShort >= min && parsedShort <= max;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    @Override
    public short readShort() {
        return Short.parseShort(this.readString());
    }

    @Override
    public boolean isValidInteger(final int min, final int max) {
        try {
            final int parsedInteger = Integer.parseInt(this.peekString());
            return parsedInteger >= min && parsedInteger <= max;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    @Override
    public int readInteger() {
        return Integer.parseInt(this.readString());
    }

    @Override
    public boolean isValidLong(final long min, final long max) {
        try {
            final long parsedLong = Long.parseLong(this.peekString());
            return parsedLong >= min && parsedLong <= max;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    @Override
    public long readLong() {
        return Long.parseLong(this.readString());
    }

    @Override
    public boolean isValidDouble(final double min, final double max) {
        try {
            final double parsedDouble = Double.parseDouble(this.peekString());
            return parsedDouble >= min && parsedDouble <= max;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    @Override
    public double readDouble() {
        return Double.parseDouble(this.readString());
    }

    @Override
    public boolean isValidFloat(final float min, final float max) {
        try {
            final float parsedFloat = Float.parseFloat(this.peekString());
            return parsedFloat >= min && parsedFloat <= max;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    @Override
    public float readFloat() {
        return Float.parseFloat(this.readString());
    }

    @Override
    public boolean isValidBoolean(final boolean liberal) {
        if (liberal) {
            return BooleanArgument.BooleanParser.LIBERAL.contains(this.peekString().toUpperCase(Locale.ROOT));
        } else {
            return BooleanArgument.BooleanParser.STRICT.contains(this.peekString().toUpperCase(Locale.ROOT));
        }
    }

    @Override
    public boolean readBoolean() {
        return BooleanArgument.BooleanParser.LIBERAL_TRUE.contains(this.readString().toUpperCase(Locale.ROOT));
    }

    @Override
    public @NonNull CommandInput copy() {
        return new CommandInputImpl(this.input, this.cursor);
    }

    @Override
    public @NonNull LinkedList<@NonNull String> tokenize() {
        if (this.isEmpty()) {
            return new LinkedList<>();
        }
        return new CommandInputTokenizer(this.remainingInput()).tokenize();
    }
}
