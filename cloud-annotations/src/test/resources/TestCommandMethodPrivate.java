import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

public class TestCommandMethodPrivate {

    @Command("command <required> [optional]")
    private void commandMethod(
            @Argument("required") final String required,
            @Argument("optional") final String optional
    ) {
    }
}
