package cloud.commandframework.minestom.utils;

import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import java.util.concurrent.CompletionException;
import cloud.commandframework.minestom.MinestomCommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ExceptionUtils {
    private static final Component MESSAGE_INTERNAL_ERROR = Component.text("An internal error occurred while attempting to perform this command.", NamedTextColor.RED);
    private static final Component MESSAGE_NO_PERMS = Component.text("I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.", NamedTextColor.RED);
    private static final Component MESSAGE_UNKNOWN_COMMAND = Component.text("Unknown command. Type \"/help\" for help.");

    private static Component invalidSyntax(InvalidSyntaxException exception) {
        TextComponent part1 = Component.text("Invalid Command Syntax. Correct command syntax is: ", NamedTextColor.RED);
        TextComponent part2 = Component.text("/" + exception.getCorrectSyntax(), NamedTextColor.GRAY);
        return part1.append(part2);
    }

    private static Component invalidCommandArgument(Throwable exception) {
        TextComponent part1 = Component.text("Invalid Command Argument: ", NamedTextColor.RED);
        TextComponent part2 = Component.text(exception.getCause().getMessage(), NamedTextColor.GRAY);
        return part1.append(part2);
    }

    public static <C> void onException(@NotNull Throwable throwable, MinestomCommandManager<C> manager, C sender, CommandSender commandSender) {
        if (throwable instanceof CompletionException) {
            throwable = throwable.getCause();
        }
        final Throwable finalThrowable = throwable;
        if (throwable instanceof InvalidSyntaxException) {
            manager.handleException(sender,
                    InvalidSyntaxException.class,
                    (InvalidSyntaxException) throwable, (c, e) ->
                            commandSender.sendMessage(invalidSyntax((InvalidSyntaxException) finalThrowable))
            );
        } else if (throwable instanceof InvalidCommandSenderException) {
            manager.handleException(sender,
                    InvalidCommandSenderException.class,
                    (InvalidCommandSenderException) throwable, (c, e) ->
                            commandSender.sendMessage(Component.text(finalThrowable.getMessage(), NamedTextColor.RED))
            );
        } else if (throwable instanceof NoPermissionException) {
            manager.handleException(sender,
                    NoPermissionException.class,
                    (NoPermissionException) throwable, (c, e) ->
                            commandSender.sendMessage(MESSAGE_NO_PERMS)
            );
        } else if (throwable instanceof NoSuchCommandException) {
            manager.handleException(sender,
                    NoSuchCommandException.class,
                    (NoSuchCommandException) throwable, (c, e) ->
                            commandSender.sendMessage(MESSAGE_UNKNOWN_COMMAND)
            );
        } else if (throwable instanceof ArgumentParseException) {
            manager.handleException(sender,
                    ArgumentParseException.class,
                    (ArgumentParseException) throwable, (c, e) ->
                            commandSender.sendMessage(invalidCommandArgument(finalThrowable))
            );
        } else if (throwable instanceof CommandExecutionException) {
            manager.handleException(sender,
                    CommandExecutionException.class,
                    (CommandExecutionException) throwable, (c, e) -> {
                        commandSender.sendMessage(MESSAGE_INTERNAL_ERROR);
                        MinecraftServer.LOGGER.error("Exception executing command handler", finalThrowable.getCause());
                    }
            );
        } else {
            commandSender.sendMessage(MESSAGE_INTERNAL_ERROR);
            MinecraftServer.LOGGER.error("An unhandled exception was thrown during command execution", throwable);
        }
    }
}
