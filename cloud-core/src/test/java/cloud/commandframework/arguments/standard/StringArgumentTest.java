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
package cloud.commandframework.arguments.standard;

import cloud.commandframework.CommandManager;
import cloud.commandframework.TestCommandSender;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth.assertThat;

class StringArgumentTest {

    private static final String[] storage = new String[2];
    private static CommandManager<TestCommandSender> manager;

    @BeforeAll
    static void setup() {
        manager = createManager();
        manager.command(manager.commandBuilder("quoted")
                .required(StringArgument.of("message1", StringArgument.StringMode.QUOTED))
                .required(StringArgument.of("message2"))
                .handler(c -> {
                    final String message1 = c.get("message1");
                    final String message2 = c.get("message2");
                    storage[0] = message1;
                    storage[1] = message2;
                })
                .build());
        manager.command(manager.commandBuilder("single")
                .required(StringArgument.of("message"))
                .handler(c -> {
                    final String message = c.get("message");
                    storage[0] = message;
                })
                .build());
        manager.command(manager.commandBuilder("greedy")
                .required(StringArgument.of("message", StringArgument.StringMode.GREEDY))
                .handler(c -> {
                    final String message = c.get("message");
                    storage[0] = message;
                })
                .build());
    }

    @AfterEach
    void reset() {
        storage[0] = storage[1] = null;
    }

    @Test
    void single_single() {
        manager.executeCommand(new TestCommandSender(), "single string").join();

        assertThat(storage[0]).isEqualTo("string");
    }

    @Test
    void quoted_single_quoted_string_containing_double_quote_followed_by_unquoted() {
        manager.executeCommand(new TestCommandSender(), "quoted 'quoted \" string' unquoted").join();

        assertThat(storage[0]).isEqualTo("quoted \" string");
        assertThat(storage[1]).isEqualTo("unquoted");
    }

    @Test
    void quoted_unquoted_strings() {
        manager.executeCommand(new TestCommandSender(), "quoted quoted unquoted").join();

        assertThat(storage[0]).isEqualTo("quoted");
        assertThat(storage[1]).isEqualTo("unquoted");
    }

    @Test
    void quoted_quoted_string_containing_escaped_quote_followed_by_unquoted() {
        manager.executeCommand(new TestCommandSender(), "quoted \"quoted \\\" string\" unquoted").join();

        assertThat(storage[0]).isEqualTo("quoted \" string");
        assertThat(storage[1]).isEqualTo("unquoted");
    }

    @Test
    void quoted_unmatched_quotes_failing() {
        Assertions.assertThrows(CompletionException.class, () -> manager.executeCommand(
                new TestCommandSender(),
                "'quoted quoted unquoted"
        ).join());
    }

    @Test
    void greedy_consumes_all() {
        manager.executeCommand(new TestCommandSender(), "greedy greedy string content").join();

        assertThat(storage[0]).isEqualTo("greedy string content");
    }
}
