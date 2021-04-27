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
package cloud.commandframework.brigadier.argument;

import com.mojang.brigadier.StringReader;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.Queue;

/**
 * A wrapper around Mojang's {@link StringReader} that implements the {@link Queue} interface.
 *
 * <p>This allows passing the full Brigadier state around through Cloud's parsing chain.</p>
 */
final class StringReaderAsQueueImpl {

    private StringReaderAsQueueImpl() {
    }

    /* Next whitespace index starting at startIdx, or -1 if none is found */
    static int nextWhitespace(final String input, final int startIdx) {
        for (int i = startIdx, length = input.length(); i < length; ++i) {
            if (Character.isWhitespace(input.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /* Wrapping variant is implemented here
     * Extending variant is only implementable with Mixin, because of clashing return types on the two interfaces (on `peek`). */

    static final class Wrapping implements StringReaderAsQueue {
        private final StringReader original;
        private int nextSpaceIdx; /* the character before the start of a new word */
        private @Nullable String nextWord;

        Wrapping(final StringReader original) {
            this.original = original;
            this.nextSpaceIdx = original.getCursor() - 1;
            this.advance();
        }

        @Override
        public StringReader getOriginal() {
            return this.original;
        }

        /* Brigadier doesn't automatically consume whitespace... in order to get the matched behaviour, we consume whitespace
         * after every popped string.
         */
        private void advance() {
            final int startOfNextWord = this.nextSpaceIdx + 1;
            this.nextSpaceIdx = nextWhitespace(this.original.getString(), startOfNextWord);
            if (this.nextSpaceIdx != -1) {
                this.nextWord = this.original.getString().substring(startOfNextWord, this.nextSpaceIdx);
            } else if (startOfNextWord < this.original.getTotalLength()) {
                this.nextWord = this.original.getString().substring(startOfNextWord);
                this.nextSpaceIdx = this.original.getTotalLength() + 1;
            } else {
                this.nextWord = null;
            }
            this.original.setCursor(startOfNextWord);
        }

        @Override
        public String poll() {
            /* peek and then advance */
            final String next = this.peek();
            if (next != null) {
                this.advance();
            }
            return next;
        }

        @Override
        public String peek() {
            return this.nextWord;
        }

        @Override
        public int size() {
            if (this.nextWord == null) {
                return 0;
            }
            int counter = 1;
            for (int i = this.nextSpaceIdx;
                 i != -1 && i < this.original.getTotalLength();
                 i = nextWhitespace(this.original.getString(), i + 1)) {
                counter++;
            }
            return counter;
        }

        @Override
        public boolean remove(final Object o) {
            if (Objects.equals(o, this.nextWord)) {
                this.advance();
                return true;
            }
            return false;
        }

        @Override
        public void clear() {
            StringReaderAsQueue.super.clear();
            this.nextWord = null;
            this.nextSpaceIdx = -1;
        }

    }

}
