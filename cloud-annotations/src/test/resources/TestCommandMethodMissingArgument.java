import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;

public class TestCommandMethodMissingArgument {

    @CommandMethod("command <required> [optional]")
    public void commandMethod(
            @Argument("optional") final String optional
    ) {
    }
}
