import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

public class TestCommandMethodOptionalBeforeRequired {

    @Command("command [optional] <required>")
    public void commandMethod(
            @Argument("required") final String required,
            @Argument("optional") final String optional
    ) {
    }
}
