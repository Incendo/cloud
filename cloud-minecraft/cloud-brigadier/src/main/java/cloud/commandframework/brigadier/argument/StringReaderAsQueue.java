//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * An interface combining {@link Queue} behaviour with a Brigadier {@link StringReader}.
 *
 * <p>This can be implemented either by wrapping an existing {@link StringReader} instance, or extending {@link StringReader}
 * at its creation time to implement this interface.</p>
 */
public interface StringReaderAsQueue extends Queue<String> {

    /**
     * Given an existing Brigadier {@code StringReader}, get a view of it as a {@link Queue}
     * @param reader the input reader
     * @return a view of the contents of the reader as a {@link Queue} split by word.
     */
    static StringReaderAsQueue from(final StringReader reader) {
        if (reader instanceof StringReaderAsQueue) {
            return (StringReaderAsQueue) reader;
        } else {
            return new StringReaderAsQueueImpl.Wrapping(reader);
        }
    }

    /**
     * Get the backing {@link StringReader} used to source data.
     *
     * @return the original reader
     */
    StringReader getOriginal();

    @Override
    default boolean isEmpty() {
        return !this.getOriginal().canRead();
    }

    @Override
    default boolean contains(final Object element) {
        if (element == null) {
            return false;
        }

        // check if the string is in the collection, and
        final int cursor = this.getOriginal().getCursor();
        final String contents = this.getOriginal().getString();
        final int idx = contents.indexOf((String) element, cursor);
        if (idx == -1) {
            return false;
        }
        final int length = this.getOriginal().getTotalLength();
        final int end = idx + contents.length();

        return (idx == cursor || Character.isWhitespace(contents.charAt(idx - 1)))
                && (end == length || Character.isWhitespace(contents.charAt(end)));
    }

    @Override
    default @NonNull Iterator<String> iterator() {
        // lazily break into words -- doesn't consume though!
        return new Iterator<String>() {
            private final String contents = StringReaderAsQueue.this.getOriginal().getString();
            private int rangeStart = StringReaderAsQueue.this.getOriginal().getCursor();
            private int rangeEnd = this.calculateNextEnd();

            private int calculateNextEnd() {
                if (this.rangeStart >= this.contents.length()) {
                    return -1;
                }
                final int nextSpace = StringReaderAsQueueImpl.nextWhitespace(this.contents, this.rangeStart);
                return nextSpace == -1 ? this.contents.length() : nextSpace;
            }

            private void computeNext() {
                this.rangeStart = this.rangeEnd + 1;
                this.rangeEnd = this.calculateNextEnd();
            }

            @Override
            public boolean hasNext() {
                return this.rangeEnd > 0;
            }

            @Override
            public String next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                final String next = this.contents.substring(this.rangeStart, this.rangeEnd);
                this.computeNext();
                return next;
            }
        };
    }

    @Override
    default Object[] toArray() {
        if (this.isEmpty()) {
            return new Object[0];
        }
        final ArrayList<String> out = new ArrayList<>(5);
        for (final String element : this) {
            /* addAll calls toArray on us... which would create a stack overflow */
            //noinspection UseBulkOperation
            out.add(element);
        }
        return out.toArray();
    }

    @Override
    default <T> T[] toArray(final T[] a) {
        if (this.isEmpty()) {
            return Arrays.copyOf(a, 0);
        }
        final ArrayList<String> out = new ArrayList<>(5);
        for (final String element : this) {
            /* addAll calls toArray on us... which would create a stack overflow */
            //noinspection UseBulkOperation
            out.add(element);
        }
        return out.toArray(a);
    }

    @Override
    default boolean add(final String element) {
        throw new IllegalStateException("StringReaders cannot have elements appended");
    }

    @Override
    default boolean offer(final String element) {
        return false;
    }

    @Override
    default String remove() {
        final String result = this.poll();
        if (result == null) {
            throw new NoSuchElementException();
        }
        return result;
    }

    @Override
    default String element() {
        final String result = this.peek();
        if (result == null) {
            throw new NoSuchElementException();
        }
        return result;
    }

    @Override
    default boolean containsAll(final @NonNull Collection<?> elements) {
        throw new UnsupportedOperationException("Complex Queue operations are not yet implemented in Cloud");
    }

    @Override
    default boolean addAll(final @NonNull Collection<? extends String> elements) {
        throw new UnsupportedOperationException("Complex Queue operations are not yet implemented in Cloud");
    }

    @Override
    default boolean removeAll(final @NonNull Collection<?> elements) {
        throw new UnsupportedOperationException("Complex Queue operations are not yet implemented in Cloud");
    }

    @Override
    default boolean retainAll(final @NonNull Collection<?> elements) {
        throw new UnsupportedOperationException("Complex Queue operations are not yet implemented in Cloud");
    }

    @Override
    default void clear() { // consume all
        this.getOriginal().setCursor(this.getOriginal().getTotalLength());
    }

}
