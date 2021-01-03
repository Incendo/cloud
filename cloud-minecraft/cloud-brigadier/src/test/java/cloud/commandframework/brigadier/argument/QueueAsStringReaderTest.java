package cloud.commandframework.brigadier.argument;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueueAsStringReaderTest {

    private static Queue<String> words(final String... elements) {
        return new LinkedList<>(Arrays.asList(elements));
    }

    @Test
    void testUnchanged() {
        final Queue<String> contents = words("hello", "world");
        final QueueAsStringReader reader = new QueueAsStringReader(contents);
        reader.updateQueue();

        assertEquals(words("hello", "world"), contents);
    }

    @Test
    void testSingleWordRemoved() throws CommandSyntaxException {
        final Queue<String> contents = words("hello", "some", "worlds");
        final QueueAsStringReader reader = new QueueAsStringReader(contents);
        assertEquals("hello", reader.readString());
        reader.updateQueue();

        assertEquals(words("some", "worlds"), contents);
    }

    @Test
    void testBeginningAndMiddleRemoved() throws CommandSyntaxException {
        final Queue<String> contents = words("hello", "some", "worlds");
        final QueueAsStringReader reader = new QueueAsStringReader(contents);
        assertEquals("hello", reader.readString());
        reader.skipWhitespace();
        assertEquals("some", reader.readString());
        reader.updateQueue();

        assertEquals(words("worlds"), contents);
    }

    @Test
    void testAllThreeWordsRead() throws CommandSyntaxException {
        final Queue<String> contents = words("hello", "some", "worlds");
        final QueueAsStringReader reader = new QueueAsStringReader(contents);
        assertEquals("hello", reader.readString());
        reader.skipWhitespace();
        assertEquals("some", reader.readString());
        reader.skipWhitespace();
        assertEquals("worlds", reader.readString());
        reader.updateQueue();

        assertTrue(contents.isEmpty());
    }

    @Test
    void testPartialWordRead() throws CommandSyntaxException {
        final Queue<String> contents = words("hi", "minecraft:pig");
        final QueueAsStringReader reader = new QueueAsStringReader(contents);
        assertEquals("hi", reader.readString());
        reader.skipWhitespace();
        assertEquals("minecraft", reader.readStringUntil(':'));
        reader.updateQueue();

        assertEquals(words("pig"), contents);
    }

}
