import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.Command;

public class TestCommandMethodMissingSyntax {

    @Command("command <required>")
    public void commandMethod(
            @Argument("required") final String required,
            @Argument("optional") final String optional
    ) {
    }
}
