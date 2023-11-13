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

import cloud.commandframework.internal.CommandInputTokenizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Queue;
import org.apiguardian.api.API;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

@API(status = API.Status.EXPERIMENTAL, since = "1.8.0")
public interface CommandInput extends Cloneable {

    List<String> BOOLEAN_STRICT = Collections.unmodifiableList(Arrays.asList("TRUE", "FALSE"));
    List<String> BOOLEAN_LIBERAL = Collections.unmodifiableList(Arrays.asList("TRUE", "YES", "ON", "FALSE", "NO", "OFF"));
    List<String> BOOLEAN_LIBERAL_TRUE = Collections.unmodifiableList(Arrays.asList("TRUE", "YES", "ON"));

    /**
     * Returns a new {@link CommandInput} instance from the given {@code input}.
     *
     * @param input the input string
     * @return the command input instance
     */
    static @NonNull CommandInput of(final @NonNull String input) {
        return new CommandInputImpl(input);
    }

    /**
     * Returns a new {@link CommandInput} instance from the given {@code input}.
     *
     * @param input the input string
     * @return the command input instance
     */
    static @NonNull CommandInput of(final @NonNull Iterable<String> input) {
        return new CommandInputImpl(String.join(" ", input));
    }

    /**
     * Returns a new {@link CommandInput} with an empty string as the input.
     *
     * @return the command input instance
     */
    static @NonNull CommandInput empty() {
        return new CommandInputImpl("");
    }

    /**
     * Returns the complete input string.
     *
     * @return the complete input
     */
    @Pure @NonNull String input();

    /**
     * Returns the cursor position.
     * <p>
     * This must always be non-negative, and less than {@link #length()}.
     *
     * @return the cursor position
     */
    @SideEffectFree @NonNegative int cursor();

    /**
     * Returns the length of the input string.
     * <p>
     * This should always be equal to the length of {@link #input()}.
     *
     * @return the length of the input string
     */
    default @Pure @NonNegative int length() {
        return this.input().length();
    }

    /**
     * Returns the length of the remaining input.
     *
     * @return the length of the remaining input.
     */
    default @SideEffectFree @NonNegative int remainingLength() {
        return this.length() - this.cursor();
    }

    /**
     * Returns the number of remaining tokens.
     *
     * @return the number of remaining tokens
     */
    default @SideEffectFree @NonNegative int remainingTokens() {
        return this.tokenize().size();
    }

    /**
     * Returns the remaining input.
     *
     * @return the remaining input.
     */
    default @SideEffectFree @NonNull String remainingInput() {
        return this.input().substring(this.cursor());
    }

    /**
     * Returns the read input.
     *
     * @return the read input
     */
    default @SideEffectFree @NonNull String readInput() {
        return this.input().substring(0, this.cursor());
    }

    /**
     * Suffixes the {@link #input() input} with the given {@code string}.
     *
     * @param string the string
     */
    void appendString(@NonNull String string);

    /**
     * Returns whether there is any {@link #remainingInput() remaining input} left to read.
     *
     * @return whether there is any remaining input
     */
    default @SideEffectFree boolean hasRemainingInput() {
        return this.cursor() < this.length();
    }

    /**
     * Returns whether there is nothing left to read. This does not ignore whitespace.
     *
     * @return whether there is nothing left to read.
     */
    default @SideEffectFree boolean isEmpty() {
        return this.isEmpty(false);
    }

    /**
     * Returns whether there is nothing left to read.
     *
     * @param ignoreWhitespace whether to ignore whitespace
     * @return whether there is nothing left to read.
     */
    default @SideEffectFree boolean isEmpty(final boolean ignoreWhitespace) {
        return !this.hasRemainingInput(ignoreWhitespace);
    }

    /**
     * Returns whether there is anything left to read.
     *
     * @param ignoreWhitespace whether to ignore whitespace
     * @return whether there is anything left to read.
     */
    default @SideEffectFree boolean hasRemainingInput(final boolean ignoreWhitespace) {
        if (!this.hasRemainingInput()) {
            return false;
        }

        if (ignoreWhitespace) {
            return this.hasNonWhitespace();
        }

        return true;
    }

    /**
     * Moves the cursor {@code chars} positions.
     *
     * @param chars the number of characters to move the cursor
     * @throws CursorOutOfBoundsException If {@code chars} exceeds {@link #remainingLength()}
     */
    void moveCursor(int chars);

    /**
     * Sets the cursor position
     *
     * @param position the new position
     */
    void cursor(@NonNegative int position);

    /**
     * Reads {@code chars} characters of the {@link #remainingInput() remaining input}
     * without moving the cursor.
     *
     * @param chars the number of characters to read
     * @return the read characters
     * @throws CursorOutOfBoundsException If {@code chars} exceeds {@link #remainingLength()}
     */
    default @SideEffectFree @NonNull String peek(final @NonNegative int chars) {
        final String remainingInput = this.remainingInput();
        if (chars > remainingInput.length()) {
            throw new CursorOutOfBoundsException(
                    this.cursor() + chars,
                    this.length()
            );
        }
        return remainingInput.substring(0, chars);
    }

    /**
     * Reads {@code chars} characters of the {@link #remainingInput() remaining input}
     * and move the cursor {@code chars} characters.
     *
     * @param chars the number of characters to read
     * @return the read characters
     * @throws CursorOutOfBoundsException If {@code chars} exceeds {@link #remainingLength()}
     */
    default @NonNull String read(final @NonNegative int chars) {
        final String readString = this.peek(chars);
        this.moveCursor(chars);
        return readString;
    }

    /**
     * Reads the character at the cursor without moving the cursor.
     *
     * @return the character at the cursor.
     */
    default @SideEffectFree char peek() {
        return this.input().charAt(this.cursor());
    }

    /**
     * Reads the character at the cursor and move the cursor one character.
     *
     * @return the character at the cursor.
     */
    default char read() {
        final char readChar = this.peek();
        this.moveCursor(1 /* chars */);
        return readChar;
    }

    /**
     * {@link #peek() Peeks} until the next whitespace is encountered.
     *
     * @return the peeked string
     */
    default @NonNull String peekString() {
        if (!this.hasRemainingInput()) {
            return "";
        }

        final String remainingInput = this.remainingInput();
        final int indexOfWhitespace = remainingInput.indexOf(' ');
        if (indexOfWhitespace == -1) {
            return remainingInput;
        } else {
            // We want to read until the whitespace. Thus, we do add
            // to account for the 0-indexing.
            return this.peek(indexOfWhitespace);
        }
    }

    /**
     * {@link #read() Reads} until the next whitespace is encountered. Any
     * trailing whitespace will be skipped.
     *
     * @param preserveSingleSpace whether a single trailing space should be preserved
     * @return the read string
     */
    default @NonNull String readString(final boolean preserveSingleSpace) {
        final String readString = this.readStringPreserveWhitespace();
        this.skipWhitespace(preserveSingleSpace);
        return readString;
    }

    /**
     * {@link #read() Reads} until the next whitespace is encountered. Any
     * trailing whitespace will be skipped.
     *
     * @return the read string
     */
    default @NonNull String readString() {
        return this.readString(true /* preserveSingleSpace */);
    }

    /**
     * Like {@link #readString()}, but whitespace is preserved.
     *
     * @return the read string
     */
    default @NonNull String readStringPreserveWhitespace() {
        if (!this.hasRemainingInput()) {
            return "";
        }

        final String remainingInput = this.remainingInput();
        final int indexOfWhitespace = remainingInput.indexOf(' ');
        if (indexOfWhitespace == -1) {
            this.moveCursor(this.remainingLength());
            return remainingInput;
        } else {
            // We want to read until the whitespace. Thus, we do add
            // to account for the 0-indexing.
            return this.read(indexOfWhitespace);
        }
    }

    /**
     * Skips any whitespace characters at the head of the input.
     *
     * @param preserveSingleSpace whether a single space should be ignored
     */
    default void skipWhitespace(final boolean preserveSingleSpace) {
        // We only skip the whitespace if the input doesn't end with a space. If it does, we do not want to consume it.
        if (preserveSingleSpace && this.remainingLength() == 1 && this.peek() == ' ') {
            return;
        }
        while (this.hasRemainingInput() && Character.isWhitespace(this.peek())) {
            this.read();
        }
    }


    /**
     * Skips any whitespace characters at the head of the input.
     */
    default void skipWhitespace() {
        this.skipWhitespace(true /* preserveSingleSpace */);
    }

    /**
     * Returns whether the {@link #remainingInput() remaining input} contains any non-whitespace characters.
     *
     * @return whether the remaining input contains any non-whitespace characters
     */
    default boolean hasNonWhitespace() {
        return this.remainingInput().chars().anyMatch(c -> !Character.isWhitespace(c));
    }

    /**
     * Returns whether {@link #peekString()} contain a valid {@link Byte}
     * within the given range.
     *
     * @param min the min value
     * @param max the max value
     * @return whether the input until the next whitespace contains a valid {@link Byte}
     */
    default @SideEffectFree boolean isValidByte(final byte min, final byte max) {
        try {
            final byte parsedByte = Byte.parseByte(this.peekString());
            return parsedByte >= min && parsedByte <= max;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    /**
     * Reads the {@link #readString() string until the next whitespace} and parses
     * in into a {@link Byte}.
     *
     * @return the parsed byte
     * @throws NumberFormatException If the string cannot be parsed into a {@link Byte}.
     */
    default byte readByte() {
        return Byte.parseByte(this.readString());
    }

    /**
     * Returns whether {@link #peekString()} contain a valid {@link Short}
     * within the given range.
     *
     * @param min the min value
     * @param max the max value
     * @return whether the input until the next whitespace contains a valid {@link Short}
     */
    default @SideEffectFree boolean isValidShort(final short min, final short max) {
        try {
            final short parsedShort = Short.parseShort(this.peekString());
            return parsedShort >= min && parsedShort <= max;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    /**
     * Reads the {@link #readString() string until the next whitespace} and parses
     * in into a {@link Short}.
     *
     * @return the parsed short
     * @throws NumberFormatException If the string cannot be parsed into a {@link Short}.
     */
    default short readShort() {
        return Short.parseShort(this.readString());
    }

    /**
     * Returns whether {@link #peekString()} contain a valid {@link Integer}
     * within the given range.
     *
     * @param min the min value
     * @param max the max value
     * @return whether the input until the next whitespace contains a valid {@link Integer}
     */
    default @SideEffectFree boolean isValidInteger(final int min, final int max) {
        try {
            final int parsedInteger = Integer.parseInt(this.peekString());
            return parsedInteger >= min && parsedInteger <= max;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    /**
     * Reads the {@link #readString() string until the next whitespace} and parses
     * in into a {@link Integer}.
     *
     * @return the parsed integer
     * @throws NumberFormatException If the string cannot be parsed into a {@link Integer}.
     */
    default int readInteger() {
        return Integer.parseInt(this.readString());
    }

    /**
     * Returns whether {@link #peekString()} contain a valid {@link Long}
     * within the given range.
     *
     * @param min the min value
     * @param max the max value
     * @return whether the input until the next whitespace contains a valid {@link Long}
     */
    default @SideEffectFree boolean isValidLong(final long min, final long max) {
        try {
            final long parsedLong = Long.parseLong(this.peekString());
            return parsedLong >= min && parsedLong <= max;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    /**
     * Reads the {@link #readString() string until the next whitespace} and parses
     * in into a {@link Long}.
     *
     * @return the parsed long
     * @throws NumberFormatException If the string cannot be parsed into a {@link Long}.
     */
    default long readLong() {
        return Long.parseLong(this.readString());
    }

    /**
     * Returns whether {@link #peekString()} contain a valid {@link Double}
     * within the given range.
     *
     * @param min the min value
     * @param max the max value
     * @return whether the input until the next whitespace contains a valid {@link Double}
     */
    default @SideEffectFree boolean isValidDouble(final double min, final double max) {
        try {
            final double parsedDouble = Double.parseDouble(this.peekString());
            return parsedDouble >= min && parsedDouble <= max;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    /**
     * Reads the {@link #readString() string until the next whitespace} and parses
     * in into a {@link Double}.
     *
     * @return the parsed double
     * @throws NumberFormatException If the string cannot be parsed into a {@link Double}.
     */
    default double readDouble() {
        return Double.parseDouble(this.readString());
    }

    /**
     * Returns whether {@link #peekString()} contain a valid {@link Float}
     * within the given range.
     *
     * @param min the min value
     * @param max the max value
     * @return whether the input until the next whitespace contains a valid {@link Float}
     */
    default @SideEffectFree boolean isValidFloat(final float min, final float max) {
        try {
            final float parsedFloat = Float.parseFloat(this.peekString());
            return parsedFloat >= min && parsedFloat <= max;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    /**
     * Reads the {@link #readString() string until the next whitespace} and parses
     * in into a {@link Float}.
     *
     * @return the parsed float
     * @throws NumberFormatException If the string cannot be parsed into a {@link Float}.
     */
    default float readFloat() {
        return Float.parseFloat(this.readString());
    }

    /**
     * Returns whether {@link #peekString()} contain a valid {@link Boolean}
     * within the given range.
     *
     * @param liberal whether non-strict boolean values like "yes", "no", "on" and "off" should be allowed
     * @return whether the input until the next whitespace contains a valid {@link Boolean}
     */
    default @SideEffectFree boolean isValidBoolean(final boolean liberal) {
        if (liberal) {
            return BOOLEAN_LIBERAL.contains(this.peekString().toUpperCase(Locale.ROOT));
        } else {
            return BOOLEAN_STRICT.contains(this.peekString().toUpperCase(Locale.ROOT));
        }
    }

    /**
     * Reads the {@link #readString() string until the next whitespace} and parses
     * in into a {@link Boolean}.
     *
     * @return the parsed boolean
     * @throws IllegalArgumentException If the string cannot be parsed into a {@link Boolean}.
     */
    default boolean readBoolean() {
        return BOOLEAN_LIBERAL_TRUE.contains(this.readString().toUpperCase(Locale.ROOT));
    }

    /**
     * Returns a {@link Queue} that wraps this instance.
     * <p>
     * Modifying the queue will also modify this instance.
     *
     * @return the wrapping queue
     */
    default @NonNull LinkedList<@NonNull String> tokenize() {
        if (this.isEmpty()) {
            return new LinkedList<>();
        }
        return new CommandInputTokenizer(this.remainingInput()).tokenize();
    }

    /**
     * Returns a copy of this instance.
     *
     * @return copy of this instance
     */
    @NonNull CommandInput copy();

    @SuppressWarnings("serial")
    @API(status = API.Status.STABLE, since = "1.7.0")
    class CursorOutOfBoundsException extends NoSuchElementException {

        CursorOutOfBoundsException(
                final @NonNegative int cursor,
                final @NonNegative int length
        ) {
            super(
                    String.format(
                            "Cursor exceeds input length (%d > %d)",
                            cursor,
                            length - 1
                    )
            );
        }
    }
}
