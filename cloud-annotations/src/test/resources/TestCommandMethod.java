import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.Command;

public class TestCommandMethod {

    @Command("command <required> [optional]")
    public void commandMethod(
            final Object sender,
            @Argument("required") final String required,
            @Argument("optional") final String optional
    ) {
    }
}
