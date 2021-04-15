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

import java.util.Deque;
import java.util.Queue;

final class QueueAsStringReader extends StringReader {
    private boolean closed;
    private final Queue<String> input;

    QueueAsStringReader(final Queue<String> input) {
        super(String.join(" ", input));
        this.input = input;
    }

    /**
     * Update the underlying queue based on the reader state.
     *
     * <p>Can only be run once.</p>
     */
    void updateQueue() {
        if (this.closed) {
            throw new IllegalStateException("double-closed");
        }
        this.closed = true;

        /* Update elements in the queue to align it with the Brigadier cursor position */
        int idx = this.getCursor();

        while (idx > 0) {
            final String next = this.input.element();
            this.input.remove();
            if (idx >= next.length()) {
                idx -= next.length() + 1 /* whitespace */;
            } else {
                /* we've gotten to a partial word consumed by brigadier... let's try and modify the underlying queue */
                if (!(this.input instanceof Deque<?>)) {
                    throw new IllegalArgumentException();
                }
                ((Deque<String>) this.input).addFirst(next.substring(idx));
                break;
            }
        }
    }

}
