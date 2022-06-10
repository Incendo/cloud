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

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import org.junit.jupiter.api.Test;

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
