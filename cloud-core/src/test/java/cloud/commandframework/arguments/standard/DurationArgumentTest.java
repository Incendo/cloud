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
package cloud.commandframework.arguments.standard;

import cloud.commandframework.CommandManager;
import cloud.commandframework.TestCommandSender;
import java.time.Duration;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth.assertThat;

public class DurationArgumentTest {

    private static final Duration[] storage = new Duration[1];
    private static CommandManager<TestCommandSender> manager;

    @BeforeAll
    static void setup() {
        manager = createManager();
        manager.command(manager.commandBuilder("duration")
                .argument(DurationArgument.of("duration"))
                .handler(c -> {
                    final Duration duration = c.get("duration");
                    storage[0] = duration;
                })
                .build());
    }

    @AfterEach
    void reset() {
        storage[0] = null;
    }

    @Test
    void single_single_unit() {
        manager.executeCommand(new TestCommandSender(), "duration 2d").join();

        assertThat(storage[0]).isEqualTo(Duration.ofDays(2));

        manager.executeCommand(new TestCommandSender(), "duration 999s").join();

        assertThat(storage[0]).isEqualTo(Duration.ofSeconds(999));
    }

    @Test
    void single_multiple_units() {
        manager.executeCommand(new TestCommandSender(), "duration 2d12h7m34s").join();

        assertThat(storage[0]).isEqualTo(Duration.ofDays(2).plusHours(12).plusMinutes(7).plusSeconds(34));

        manager.executeCommand(new TestCommandSender(), "duration 700h75m1d999s").join();

        assertThat(storage[0]).isEqualTo(Duration.ofDays(1).plusHours(700).plusMinutes(75).plusSeconds(999));
    }

    @Test
    void invalid_format_failing() {
        Assertions.assertThrows(CompletionException.class, () -> manager.executeCommand(
                new TestCommandSender(),
                "duration d"
        ).join());

        Assertions.assertThrows(CompletionException.class, () -> manager.executeCommand(
                new TestCommandSender(),
                "duration 1x"
        ).join());
    }

}
