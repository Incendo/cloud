import org.incendo.cloud.annotations.Command;

public class TestCommandMethodWithoutArgumentAnnotations {

    @Command("command <required> [optional]")
    public void commandMethod(
            final Object sender,
            final String required,
            final String optional
    ) {
    }
}
