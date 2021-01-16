//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
package cloud.commandframework.velocity;

import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.execution.CommandResult;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;

final class VelocityExecutor<C> implements Command<CommandSource> {

    private static final String MESSAGE_INTERNAL_ERROR = "An internal error occurred while attempting to perform this command.";
    private static final String MESSAGE_NO_PERMS =
            "I'm sorry, but you do not have permission to perform this command. "
                    + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command. Type \"/help\" for help.";

    private final VelocityCommandManager<C> manager;

    VelocityExecutor(final @NonNull VelocityCommandManager<C> commandManager) {
        this.manager = commandManager;
    }

    @Override
    public int run(final @NonNull CommandContext<CommandSource> commandContext) {
        final CommandSource source = commandContext.getSource();
        final String input = commandContext.getInput();
        final C sender = this.manager.getCommandSenderMapper().apply(
                source);
        this.manager.executeCommand(sender, input).whenComplete(this.getResultConsumer(source, sender));
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private @NonNull BiConsumer<@NonNull CommandResult<C>, ? super Throwable> getResultConsumer(
            final @NonNull CommandSource source,
            final @NonNull C sender
    ) {
        return (result, throwable) -> {
            if (throwable != null) {
                if (throwable instanceof CompletionException) {
                    throwable = throwable.getCause();
                }
                final Throwable finalThrowable = throwable;
                if (throwable instanceof InvalidSyntaxException) {
                    this.manager.handleException(
                            sender,
                            InvalidSyntaxException.class,
                            (InvalidSyntaxException) throwable,
                            (c, e) ->
                                    source.sendMessage(
                                            Identity.nil(),
                                            Component
                                                    .text()
                                                    .append(
                                                            Component.text(
                                                                    "Invalid Command Syntax. Correct command syntax is: ",
                                                                    NamedTextColor.RED
                                                            )
                                                    ).append(
                                                    Component.text(
                                                            e.getCorrectSyntax(),
                                                            NamedTextColor.GRAY
                                                    )
                                            ).build()
                                    )
                    );
                } else if (throwable instanceof InvalidCommandSenderException) {
                    this.manager.handleException(
                            sender,
                            InvalidCommandSenderException.class,
                            (InvalidCommandSenderException) throwable,
                            (c, e) ->
                                    source.sendMessage(
                                            Identity.nil(),
                                            Component.text(
                                                    finalThrowable.getMessage(),
                                                    NamedTextColor.RED
                                            )
                                    )
                    );
                } else if (throwable instanceof NoPermissionException) {
                    this.manager.handleException(
                            sender,
                            NoPermissionException.class,
                            (NoPermissionException) throwable,
                            (c, e) -> source.sendMessage(Identity.nil(), Component.text(MESSAGE_NO_PERMS))
                    );
                } else if (throwable instanceof NoSuchCommandException) {
                    this.manager.handleException(
                            sender,
                            NoSuchCommandException.class,
                            (NoSuchCommandException) throwable,
                            (c, e) -> source.sendMessage(Identity.nil(), Component.text(MESSAGE_UNKNOWN_COMMAND))
                    );
                } else if (throwable instanceof ArgumentParseException) {
                    this.manager.handleException(
                            sender,
                            ArgumentParseException.class,
                            (ArgumentParseException) throwable,
                            (c, e) -> source.sendMessage(
                                    Identity.nil(),
                                    Component.text()
                                            .append(Component.text(
                                                    "Invalid Command Argument: ",
                                                    NamedTextColor.RED
                                            ))
                                            .append(Component.text(
                                                    finalThrowable.getCause().getMessage(),
                                                    NamedTextColor.GRAY
                                            ))
                            )
                    );
                } else if (throwable instanceof CommandExecutionException) {
                    this.manager.handleException(
                            sender,
                            CommandExecutionException.class,
                            (CommandExecutionException) throwable,
                            (c, e) -> {
                                source.sendMessage(
                                        Identity.nil(),
                                        Component.text(
                                                MESSAGE_INTERNAL_ERROR,
                                                NamedTextColor.RED
                                        )
                                );
                                finalThrowable.getCause().printStackTrace();
                            }
                    );
                } else {
                    source.sendMessage(
                            Identity.nil(),
                            Component.text(MESSAGE_INTERNAL_ERROR, NamedTextColor.RED)
                    );
                    throwable.printStackTrace();
                }
            }
        };
    }

}
