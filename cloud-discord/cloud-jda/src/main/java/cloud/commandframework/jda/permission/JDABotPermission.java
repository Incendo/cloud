package cloud.commandframework.jda.permission;

import net.dv8tion.jda.api.Permission;

/**
 * Annotation to set the permissions required by the bot to execute this command.
 */
public @interface JDABotPermission {

    Permission[] permissions();

}
