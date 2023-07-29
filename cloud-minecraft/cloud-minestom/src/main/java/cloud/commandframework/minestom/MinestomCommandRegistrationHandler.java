package cloud.commandframework.minestom;

import cloud.commandframework.Command;
import cloud.commandframework.internal.CommandRegistrationHandler;
import cloud.commandframework.minestom.utils.SetupUtils;
import org.jetbrains.annotations.NotNull;

public class MinestomCommandRegistrationHandler<C> implements CommandRegistrationHandler {

    private MinestomCommandManager<C> commandManager;

    void initialize(final @NotNull MinestomCommandManager<C> commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public boolean registerCommand(@NotNull Command<?> command) {
        SetupUtils.setup(commandManager, (Command<C>) command);
        return true;
    }
}
