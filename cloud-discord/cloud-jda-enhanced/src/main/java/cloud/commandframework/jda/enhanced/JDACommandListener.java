//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package cloud.commandframework.jda.enhanced;

import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.jda.enhanced.permission.BotJDAPermissionException;
import cloud.commandframework.jda.enhanced.permission.UserJDAPermissionException;
import cloud.commandframework.jda.enhanced.sender.JDACommandSender;
import cloud.commandframework.jda.enhanced.sender.JDAMessageCommandSender;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;


/**
 * JDA Command Listener
 *
 * @param <C> Command sender type
 */
public final class JDACommandListener<C> extends ListenerAdapter {

    private static final String MESSAGE_INTERNAL_ERROR = "An internal error occurred while attempting to perform this command.";
    private static final String MESSAGE_INVALID_SYNTAX = "Invalid Command Syntax. Correct command syntax is:";
    private static final String MESSAGE_NO_PERMS = "I'm sorry, but you do not have permission to perform this command. "
            + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command";

    private final EnhancedJDACommandManager<C> commandManager;
    private final boolean slashCommandsDisabled;
    private final boolean messageCommandsDisabled;

    /**
     * Construct a new JDA Command Listener
     *
     * @param commandManager Command Manager instance
     */
    public JDACommandListener(
            final @NonNull EnhancedJDACommandManager<C> commandManager,
            final boolean slashCommandsEnabled,
            final boolean messageCommandsEnabled
    ) {
        super();
        this.commandManager = commandManager;
        this.slashCommandsDisabled = !slashCommandsEnabled;
        this.messageCommandsDisabled = !messageCommandsEnabled;
    }

    @Override
    public void onSlashCommand(@NotNull final SlashCommandEvent event) {
        if (slashCommandsDisabled) {
            return;
        }
        List<String> args = event.getOptions().stream().map(OptionMapping::getAsString).collect(Collectors.toList());
//        event.getSubcommandGroup()
//        event.getOptions()
    }

    @Override
    public void onMessageReceived(final @NonNull MessageReceivedEvent event) {
        if (messageCommandsDisabled) {
            return;
        }

        final Message message = event.getMessage();
        final JDAMessageCommandSender commandSender = JDACommandSender.of(event);
        final C sender = this.commandManager.getCommandSenderMapper().apply(commandSender);

        if (this.commandManager.getBotId() == event.getAuthor().getIdLong()) {
            return;
        }

        attemptCommand(commandSender, sender, message.getContentRaw());
    }

    private void attemptCommand(
            final @NonNull JDACommandSender commandSender,
            final @NonNull C sender,
            final @NonNull String contentRaw
    ) {
        final String commandContent = commandManager.getPrefixMatcher().apply(sender, contentRaw);
        final String prefix = contentRaw.substring(0, commandContent.length());

        this.commandManager.executeCommand(sender, commandContent)
                .whenComplete((commandResult, throwable) -> {
                    if (throwable == null) {
                        return;
                    }

                    if (throwable instanceof InvalidSyntaxException) {
                        this.commandManager.handleException(
                                sender,
                                InvalidSyntaxException.class,
                                (InvalidSyntaxException) throwable,
                                (c, e) -> this.sendMessage(
                                        commandSender,
                                        String.format(
                                                "%s %s%s",
                                                MESSAGE_INVALID_SYNTAX,
                                                prefix,
                                                ((InvalidSyntaxException) throwable).getCorrectSyntax()
                                        )
                                )
                        );
                    } else if (throwable instanceof InvalidCommandSenderException) {
                        this.commandManager.handleException(
                                sender,
                                InvalidCommandSenderException.class,
                                (InvalidCommandSenderException) throwable,
                                (c, e) -> this.sendMessage(commandSender, throwable.getMessage())
                        );
                    } else if (throwable instanceof NoPermissionException) {
                        this.commandManager.handleException(
                                sender,
                                NoPermissionException.class,
                                (NoPermissionException) throwable,
                                (c, e) -> this.sendMessage(commandSender, MESSAGE_NO_PERMS)
                        );
                    } else if (throwable instanceof NoSuchCommandException) {
                        this.commandManager.handleException(
                                sender,
                                NoSuchCommandException.class,
                                (NoSuchCommandException) throwable,
                                (c, e) -> this.sendMessage(commandSender, MESSAGE_UNKNOWN_COMMAND)
                        );
                    } else if (throwable instanceof ArgumentParseException) {
                        this.commandManager.handleException(
                                sender,
                                ArgumentParseException.class,
                                (ArgumentParseException) throwable,
                                (c, e) -> this.sendMessage(
                                        commandSender,
                                        "Invalid Command Argument: " + throwable.getCause().getMessage()
                                )
                        );
                    } else if (throwable instanceof CommandExecutionException) {
                        this.commandManager.handleException(
                                sender,
                                CommandExecutionException.class,
                                (CommandExecutionException) throwable,
                                (c, e) -> {
                                    this.sendMessage(commandSender, MESSAGE_INTERNAL_ERROR);
                                    throwable.getCause().printStackTrace();
                                }
                        );
                    } else if (throwable instanceof BotJDAPermissionException) {
                        this.commandManager.handleException(
                                sender,
                                BotJDAPermissionException.class,
                                (BotJDAPermissionException) throwable,
                                (c, e) -> this.sendMessage(commandSender, e.getMessage())
                        );
                    } else if (throwable instanceof UserJDAPermissionException) {
                        this.commandManager.handleException(
                                sender,
                                UserJDAPermissionException.class,
                                (UserJDAPermissionException) throwable,
                                (c, e) -> this.sendMessage(commandSender, e.getMessage())
                        );
                    } else if (throwable instanceof Exception) {
                        final Exception exception = (Exception) throwable;

                        //noinspection unchecked
                        this.commandManager.handleException(
                                sender,
                                (Class<Exception>) exception.getClass(),
                                exception,
                                (c, e) -> this.sendMessage(commandSender, e.getMessage())
                        );

                    }
                });
    }

    private void sendMessage(final @NonNull JDACommandSender sender, final @NonNull String message) {
        sender.getChannel().sendMessage(message).queue();
    }

}
