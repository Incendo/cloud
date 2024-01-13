import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.Command;

public class TestCommandMethodMissingArgument {

    @Command("command <required> [optional]")
    public void commandMethod(
            @Argument("optional") final String optional
    ) {
    }
}
