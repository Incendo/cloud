import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

public class TestCommandMethodMissingSyntax {

    @Command("command <required>")
    public void commandMethod(
            @Argument("required") final String required,
            @Argument("optional") final String optional
    ) {
    }
}
