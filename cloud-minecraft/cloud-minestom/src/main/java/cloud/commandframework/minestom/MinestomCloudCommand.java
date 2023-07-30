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
package cloud.commandframework.minestom;

import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.execution.CommandResult;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MinestomCloudCommand<C> extends Command {

    private static final String MESSAGE_INTERNAL_ERROR = "An internal error occurred while attempting to perform this command.";
    private static final String MESSAGE_NO_PERMS =
            "I'm sorry, but you do not have permission to perform this command. "
                    + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command. Type \"/help\" for help.";

    private final MinestomCommandManager<C> manager;

    MinestomCloudCommand(final MinestomCommandManager<C> manager, final @NotNull String name, final @Nullable String... aliases) {
        super(name, aliases);
        this.manager = manager;
    }

    @Override
    public void globalListener(
            final @NotNull CommandSender commandSender,
            final @NotNull CommandContext context,
            final @NotNull String command
    ) {
        C sender = this.manager.mapCommandSender(commandSender);
        this.manager.executeCommand(sender, command).whenComplete(this.getResultConsumer(commandSender, sender));
    }

    private @NonNull BiConsumer<@NonNull CommandResult<C>, ? super Throwable> getResultConsumer(
            final @NonNull CommandSender commandSender,
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
                                    commandSender.sendMessage(
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
                                    commandSender.sendMessage(
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
                            (c, e) -> commandSender.sendMessage(Component.text(MESSAGE_NO_PERMS))
                    );
                } else if (throwable instanceof NoSuchCommandException) {
                    this.manager.handleException(
                            sender,
                            NoSuchCommandException.class,
                            (NoSuchCommandException) throwable,
                            (c, e) -> commandSender.sendMessage(Component.text(MESSAGE_UNKNOWN_COMMAND))
                    );
                } else if (throwable instanceof ArgumentParseException) {
                    this.manager.handleException(
                            sender,
                            ArgumentParseException.class,
                            (ArgumentParseException) throwable,
                            (c, e) -> commandSender.sendMessage(
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
                                commandSender.sendMessage(
                                        Component.text(
                                                MESSAGE_INTERNAL_ERROR,
                                                NamedTextColor.RED
                                        )
                                );
                                finalThrowable.getCause().printStackTrace();
                            }
                    );
                } else {
                    commandSender.sendMessage(
                            Component.text(MESSAGE_INTERNAL_ERROR, NamedTextColor.RED)
                    );
                    throwable.printStackTrace();
                }
            }
        };
    }
}
