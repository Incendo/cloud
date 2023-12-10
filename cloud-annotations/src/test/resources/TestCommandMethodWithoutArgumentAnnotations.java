import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;

public class TestCommandMethodWithoutArgumentAnnotations {

    @CommandMethod("command <required> [optional]")
    public void commandMethod(
            final Object sender,
            final String required,
            final String optional
    ) {
    }
}
