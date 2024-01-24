import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

public class TestCommandMethodMissingArgument {

    @Command("command <required> [optional]")
    public void commandMethod(
            @Argument("optional") final String optional
    ) {
    }
}
