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

package cloud.commandframework.quilt.mixin;

import cloud.commandframework.brigadier.argument.StringReaderAsQueue;
import cloud.commandframework.quilt.internal.CloudStringReader;
import com.mojang.brigadier.StringReader;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Objects;


/**
 * Mix in to our own class in order to implement the Queue interface without signature conflicts.
 *
 * <p>This must be kept in sync with the wrapping implementation in {@code cloud-brigadier}</p>
 */
@Mixin(value = CloudStringReader.class, remap = false)
public class CloudStringReaderMixin implements StringReaderAsQueue {

    private int cloud$nextSpaceIdx; /* the character before the start of a new word */
    private @Nullable String cloud$nextWord;

    /* Next whitespace index starting at startIdx, or -1 if none is found */
    private static int cloud$nextWhitespace(final String input, final int startIdx) {
        for (int i = startIdx, length = input.length(); i < length; ++i) {
            if (Character.isWhitespace(input.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public StringReader getOriginal() {
        return (StringReader) (Object) this;
    }

    /* Brigadier doesn't automatically consume whitespace... in order to get the matched behaviour, we consume whitespace
     * after every popped string.
     */
    private void cloud$advance() {
        final int startOfNextWord = this.cloud$nextSpaceIdx + 1;
        this.cloud$nextSpaceIdx = cloud$nextWhitespace(((StringReader) (Object) this).getString(), startOfNextWord);
        if (this.cloud$nextSpaceIdx != -1) {
            this.cloud$nextWord = ((StringReader) (Object) this).getString().substring(startOfNextWord, this.cloud$nextSpaceIdx);
        } else if (startOfNextWord < ((StringReader) (Object) this).getTotalLength()) {
            this.cloud$nextWord = ((StringReader) (Object) this).getString().substring(startOfNextWord);
            this.cloud$nextSpaceIdx = ((StringReader) (Object) this).getTotalLength() + 1;
        } else {
            this.cloud$nextWord = null;
        }
        ((StringReader) (Object) this).setCursor(startOfNextWord);
    }

    @Override
    public String poll() {
        /* peek and then advance */
        final String next = this.peek();
        if (next != null) {
            this.cloud$advance();
        }
        return next;
    }

    @Override
    public String peek() {
        return this.cloud$nextWord;
    }

    @Override
    public int size() {
        if (this.cloud$nextWord == null) {
            return 0;
        }
        int counter = 1;
        for (int i = this.cloud$nextSpaceIdx;
             i != -1 && i < ((StringReader) (Object) this).getTotalLength();
             i = cloud$nextWhitespace(((StringReader) (Object) this).getString(), i + 1)) {
            counter++;
        }
        return counter;
    }

    @Override
    public boolean remove(final Object o) {
        if (Objects.equals(o, this.cloud$nextWord)) {
            this.cloud$advance();
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        StringReaderAsQueue.super.clear();
        this.cloud$nextWord = null;
        this.cloud$nextSpaceIdx = -1;
    }
}
