package cloud.commandframework.arguments.standard;

import cloud.commandframework.CommandManager;
import cloud.commandframework.TestCommandSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cloud.commandframework.util.TestUtils.createManager;

public class DurationArgumentSuggestionsTest {

    private static CommandManager<TestCommandSender> manager;

    @BeforeAll
    static void setupManager() {
        manager = createManager();
        manager.command(manager.commandBuilder("duration")
                .argument(DurationArgument.of("duration")));
    }


    @Test
    void testDurationSuggestions() {
        final String input = "duration ";
        final List<String> suggestions = manager.suggest(new TestCommandSender(), input);
        Assertions.assertEquals(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9"), suggestions);

        final String input2 = "duration 1";
        final List<String> suggestions2 = manager.suggest(new TestCommandSender(), input2);
        Assertions.assertEquals(Arrays.asList("1d", "1h", "1m", "1s"), suggestions2);

        final String input3 = "duration 1d";
        final List<String> suggestions3 = manager.suggest(new TestCommandSender(), input3);
        Assertions.assertEquals(Collections.emptyList(), suggestions3);

        final String input4 = "duration 1d2";
        final List<String> suggestions4 = manager.suggest(new TestCommandSender(), input4);
        Assertions.assertTrue(suggestions4.containsAll(Arrays.asList("1d2h", "1d2m", "1d2s")));
        Assertions.assertFalse(suggestions4.contains("1d2d"));

        final String input9 = "duration 1d22";
        final List<String> suggestions9 = manager.suggest(new TestCommandSender(), input9);
        Assertions.assertTrue(suggestions9.containsAll(Arrays.asList("1d22h", "1d22m", "1d22s")));
        Assertions.assertFalse(suggestions9.contains("1d22d"));

        final String input5 = "duration d";
        final List<String> suggestions5 = manager.suggest(new TestCommandSender(), input5);
        Assertions.assertEquals(Collections.emptyList(), suggestions5);

        final String input6 = "duration 1d2d";
        final List<String> suggestions6 = manager.suggest(new TestCommandSender(), input6);
        Assertions.assertEquals(Collections.emptyList(), suggestions6);

        final String input7 = "duration 1d2h3m4s";
        final List<String> suggestions7 = manager.suggest(new TestCommandSender(), input7);
        Assertions.assertEquals(Collections.emptyList(), suggestions7);

        final String input8 = "duration dd";
        final List<String> suggestions8 = manager.suggest(new TestCommandSender(), input8);
        Assertions.assertEquals(Collections.emptyList(), suggestions8);
    }


}
