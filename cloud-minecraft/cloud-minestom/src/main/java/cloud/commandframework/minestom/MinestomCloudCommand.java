package cloud.commandframework.minestom;

import cloud.commandframework.minestom.utils.ExceptionUtils;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MinestomCloudCommand<C> extends Command {
    private final MinestomCommandManager<C> manager;

    public MinestomCloudCommand(MinestomCommandManager<C> manager, @NotNull String name, @Nullable String... aliases) {
        super(name, aliases);
        this.manager = manager;
    }

    @Override
    public void globalListener(@NotNull CommandSender commandSender, @NotNull CommandContext context, @NotNull String command) {
        C sender = manager.mapCommandSender(commandSender);
        manager.executeCommand(sender, command).whenComplete((c, t) -> {
            if (t != null) ExceptionUtils.onException(t, manager, sender, commandSender);
        });
    }
}
