import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;

public class TestCommandMethod {

    @CommandMethod("command <required> [optional]")
    public void commandMethod(
            final Object sender,
            @Argument("required") final String required,
            @Argument("optional") final String optional
    ) {
    }
}
