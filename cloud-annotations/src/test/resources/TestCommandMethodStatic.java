import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;

public class TestCommandMethodStatic {

    @CommandMethod("command <required> [optional]")
    public static void commandMethod(
            @Argument("required") final String required,
            @Argument("optional") final String optional
    ) {
    }
}
