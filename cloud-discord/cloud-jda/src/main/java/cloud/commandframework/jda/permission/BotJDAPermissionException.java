package cloud.commandframework.jda.permission;

import cloud.commandframework.context.CommandContext;
import net.dv8tion.jda.api.Permission;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception thrown when the bot is lacking a specific permission.
 */
public class BotJDAPermissionException extends IllegalArgumentException {

    private static final long serialVersionUID = 2035924515750548930L;
    private final @NonNull CommandContext<?> commandContext;
    private final @NonNull List<Permission> missingPermissions;

    /**
     * Construct a new no JDA permission exception
     *
     * @param commandContext     Command context
     * @param missingPermissions The permissions that are missing
     */
    BotJDAPermissionException(
            final @NonNull CommandContext<?> commandContext,
            final @NonNull List<Permission> missingPermissions
    ) {
        this.commandContext = commandContext;
        this.missingPermissions = missingPermissions;
    }


    @Override
    public String getMessage() {
        return String.format(
                "Cannot execute command due to insufficient permission. The bot requires the following permission(s) to execute this command: %s",
                missingPermissions.stream().map(Permission::getName).collect(Collectors.joining(", "))
        );
    }

    public CommandContext<?> getCommandContext() {
        return commandContext;
    }

    public List<Permission> getMissingPermissions() {
        return missingPermissions;
    }

}
