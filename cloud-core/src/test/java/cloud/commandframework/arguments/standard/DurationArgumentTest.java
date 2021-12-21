package cloud.commandframework.arguments.standard;

import cloud.commandframework.CommandManager;
import cloud.commandframework.TestCommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletionException;

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
    }

    @Test
    void single_multiple_units() {
        manager.executeCommand(new TestCommandSender(), "duration 2d12h7m34s").join();

        assertThat(storage[0]).isEqualTo(Duration.ofDays(2).plusHours(12).plusMinutes(7).plusSeconds(34));
    }

    @Test
    void invalid_format_failing() {
        Assertions.assertThrows(CompletionException.class, () -> manager.executeCommand(
                new TestCommandSender(),
                "duration d"
        ).join());
    }

}
