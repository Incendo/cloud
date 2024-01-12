import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.Command;

public class TestCommandMethodStatic {

    @Command("command <required> [optional]")
    public static void commandMethod(
            @Argument("required") final String required,
            @Argument("optional") final String optional
    ) {
    }
}
