import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;

public class TestCommandMethodPrivate {

    @CommandMethod("command <required> [optional]")
    private void commandMethod(
            @Argument("required") final String required,
            @Argument("optional") final String optional
    ) {
    }
}
