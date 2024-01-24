import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

public class TestCommandMethodStatic {

    @Command("command <required> [optional]")
    public static void commandMethod(
            @Argument("required") final String required,
            @Argument("optional") final String optional
    ) {
    }
}
