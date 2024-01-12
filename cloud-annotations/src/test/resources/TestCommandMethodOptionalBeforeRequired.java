import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.Command;

public class TestCommandMethodOptionalBeforeRequired {

    @Command("command [optional] <required>")
    public void commandMethod(
            @Argument("required") final String required,
            @Argument("optional") final String optional
    ) {
    }
}
