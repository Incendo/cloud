package cloud.commandframework.brigadier.argument;

import cloud.commandframework.types.tuples.Pair;
import com.mojang.brigadier.StringReader;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link StringReaderAsQueue}
 *
 * Most operations have 4 cases: 0 arguments, 1 argument, 2 arguments, and 3 or more arguments.
 * At that point every whitespace handling path should be exercised.
 */
class StringReaderAsQueueTest {

    @Test
    void testIsNotEmpty() {
        final StringReader reader = new StringReader("hello there");
        final Queue<String> queue = StringReaderAsQueue.from(reader);
        assertFalse(queue.isEmpty());
    }

    @Test
    void testCreateEmpty() {
        final Queue<String> queue = StringReaderAsQueue.from(new StringReader(""));
        assertTrue(queue.isEmpty());
        assertNull(queue.peek());
        assertNull(queue.poll());
    }

    @Test
    void testReadWord() {
        final StringReader original = new StringReader("meow purr");
        final Queue<String> queue = StringReaderAsQueue.from(original);
        assertEquals("meow", queue.poll());
        assertEquals("purr", original.getRemaining());
    }

    @Test
    void testReadSingleWordContents() {
        final StringReader reader = new StringReader("hello");
        final Queue<String> queue = StringReaderAsQueue.from(reader);

        assertFalse(queue.isEmpty());
        assertEquals("hello", queue.peek());
        assertEquals(1, queue.size());
    }

    @Test
    void testReadTwoWords() {
        final StringReader original = new StringReader("meow purr");
        final Queue<String> queue = StringReaderAsQueue.from(original);
        assertEquals("meow", queue.poll());
        assertEquals("purr", queue.poll());
        assertTrue(queue.isEmpty());
    }

    @Test
    void testReadThreeWords() {
        final Queue<String> queue = StringReaderAsQueue.from(new StringReader("we enjoy commands"));
        assertEquals("we", queue.poll());
        assertEquals("enjoy", queue.poll());
        assertEquals("commands", queue.poll());
        assertNull(queue.poll());
    }

    @Test
    void testPeekRepeatedly() {
        final Queue<String> queue = StringReaderAsQueue.from(new StringReader("commands are fun"));
        for (int i = 0; i < 3; ++i) {
            assertEquals("commands", queue.peek());
        }
    }

    @Test
    void testMultiElementIterator() {
        final StringReader reader = new StringReader("tell @a :3");
        final Iterator<String> elements = StringReaderAsQueue.from(reader).iterator();
        assertTrue(elements.hasNext());
        assertEquals("tell", elements.next());
        assertTrue(elements.hasNext());
        assertEquals("@a", elements.next());
        assertTrue(elements.hasNext());
        assertEquals(":3", elements.next());
        assertFalse(elements.hasNext());

        assertThrows(NoSuchElementException.class, elements::next);
    }

    @Test
    void testDoubleElementIterator() {
        final Iterator<String> elements = StringReaderAsQueue.from(new StringReader("cloud good")).iterator();
        assertTrue(elements.hasNext());
        assertEquals("cloud", elements.next());
        assertTrue(elements.hasNext());
        assertEquals("good", elements.next());
        assertFalse(elements.hasNext());

        assertThrows(NoSuchElementException.class, elements::next);
    }

    @Test
    void testSingleElementIterator() {
        final Iterator<String> elements = StringReaderAsQueue.from(new StringReader("word")).iterator();
        assertTrue(elements.hasNext());
        assertEquals("word", elements.next());
        assertFalse(elements.hasNext());

        assertThrows(NoSuchElementException.class, elements::next);
    }

    @Test
    void testEmptyIterator() {
        final Iterator<String> empty = StringReaderAsQueue.from(new StringReader("")).iterator();

        assertFalse(empty.hasNext());
        assertThrows(NoSuchElementException.class, empty::next);
    }

    @Test
    void testPartlyStartedIteration() {
        final Queue<String> queue = StringReaderAsQueue.from(new StringReader("let's go for a walk"));
        assertEquals("let's", queue.poll());

        final Iterator<String> it = queue.iterator();
        assertEquals("go", it.next());
    }

    @Test
    void testToArrayMultiple() {
        final Queue<String> elements = StringReaderAsQueue.from(new StringReader("one two three four"));
        assertArrayEquals(new String[] { "one", "two", "three", "four" }, elements.toArray());
    }

    @Test
    void testSizes() {
        Stream.of(
                Pair.of("", 0),
                Pair.of("hi", 1),
                Pair.of("the second!", 2),
                Pair.of("a third entry", 3),
                Pair.of("one two three, four", 4)
        ).forEach(pair -> {
            assertEquals(pair.getSecond(), StringReaderAsQueue.from(new StringReader(pair.getFirst())).size());
        });
    }

}
