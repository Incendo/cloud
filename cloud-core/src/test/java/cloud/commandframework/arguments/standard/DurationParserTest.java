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
import cloud.commandframework.execution.CommandResult;
import cloud.commandframework.keys.CloudKey;
import java.time.Duration;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.arguments.standard.DurationParser.durationParser;
import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth.assertThat;

class DurationParserTest {

    private static final CloudKey<Duration> DURATION_KEY = CloudKey.of(
            "duration",
            Duration.class
    );

    private static CommandManager<TestCommandSender> manager;

    @BeforeAll
    static void setup() {
        manager = createManager();
        manager.command(manager.commandBuilder("duration")
                .required(DURATION_KEY, durationParser())
                .build());
    }

    @Test
    void single_single_unit() {
        final CommandResult<?> result1 = manager.executeCommand(new TestCommandSender(), "duration 2d").join();
        assertThat(result1.getCommandContext().get(DURATION_KEY)).isEqualTo(Duration.ofDays(2));

        final CommandResult<?> result2 = manager.executeCommand(new TestCommandSender(), "duration 999s").join();
        assertThat(result2.getCommandContext().get(DURATION_KEY)).isEqualTo(Duration.ofSeconds(999));
    }

    @Test
    void single_multiple_units() {
        final CommandResult<?> result1 = manager.executeCommand(new TestCommandSender(), "duration 2d12h7m34s").join();
        assertThat(result1.getCommandContext().get(DURATION_KEY)).
                isEqualTo(Duration.ofDays(2).plusHours(12).plusMinutes(7).plusSeconds(34));

        final CommandResult<?> result2 = manager.executeCommand(new TestCommandSender(), "duration 700h75m1d999s").join();
        assertThat(result2.getCommandContext().get(DURATION_KEY))
                .isEqualTo(Duration.ofDays(1).plusHours(700).plusMinutes(75).plusSeconds(999));
    }

    @Test
    void invalid_format_failing() {
        Assertions.assertThrows(
                CompletionException.class,
                () -> manager.executeCommand(new TestCommandSender(), "duration d").join()
        );

        Assertions.assertThrows(
                CompletionException.class,
                () -> manager.executeCommand(new TestCommandSender(), "duration 1x").join()
        );
    }
}
