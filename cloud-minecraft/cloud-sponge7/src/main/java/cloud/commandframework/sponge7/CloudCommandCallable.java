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
package cloud.commandframework.sponge7;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.meta.CommandMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.TextMessageException;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

final class CloudCommandCallable<C> implements CommandCallable {

    private static final Text MESSAGE_INTERNAL_ERROR = Text.of(TextColors.RED,
        "An internal error occurred while attempting to perform this command.");
    private static final Text MESSAGE_NO_PERMS = Text.of(TextColors.RED,
        "I'm sorry, but you do not have permission to perform this command. "
            + "Please contact the server administrators if you believe that this is in error.");
    private static final Text MESSAGE_UNKNOWN_COMMAND = Text.of("Unknown command. Type \"/help\" for help.");

    private final CommandArgument<?, ?> command;
    private final Command<C> cloudCommand;
    private final SpongeCommandManager<C> manager;

    CloudCommandCallable(
        final CommandArgument<?, ?> command,
        final Command<C> cloudCommand,
        final SpongeCommandManager<C> manager
    ) {
        this.command = command;
        this.cloudCommand = cloudCommand;
        this.manager = manager;
    }

    @Override
    public CommandResult process(final @NonNull CommandSource source, final @NonNull String arguments) {
        final C cloudSender = this.manager.getCommandSourceMapper().apply(source);

        this.manager.executeCommand(cloudSender, this.formatCommand(arguments))
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    if (throwable instanceof CompletionException) {
                        throwable = throwable.getCause();
                    }
                    final Throwable finalThrowable = throwable;
                    if (throwable instanceof InvalidSyntaxException) {
                        this.manager.handleException(cloudSender,
                            InvalidSyntaxException.class,
                            (InvalidSyntaxException) throwable, (c, e) ->
                                source.sendMessage(Text.of(TextColors.RED,
                                    "Invalid Command Syntax. Correct command syntax is: ",
                                    Text.of(TextColors.GRAY, ((InvalidSyntaxException) finalThrowable).getCorrectSyntax())))
                        );
                    } else if (throwable instanceof InvalidCommandSenderException) {
                        this.manager.handleException(cloudSender,
                            InvalidCommandSenderException.class,
                            (InvalidCommandSenderException) throwable, (c, e) ->
                                source.sendMessage(Text.of(TextColors.RED, finalThrowable.getMessage()))
                        );
                    } else if (throwable instanceof NoPermissionException) {
                        this.manager.handleException(cloudSender,
                            NoPermissionException.class,
                            (NoPermissionException) throwable, (c, e) ->
                                source.sendMessage(MESSAGE_NO_PERMS)
                        );
                    } else if (throwable instanceof NoSuchCommandException) {
                        this.manager.handleException(cloudSender,
                            NoSuchCommandException.class,
                            (NoSuchCommandException) throwable, (c, e) ->
                                source.sendMessage(MESSAGE_UNKNOWN_COMMAND)
                        );
                    } else if (throwable instanceof ArgumentParseException) {
                        this.manager.handleException(cloudSender,
                            ArgumentParseException.class,
                            (ArgumentParseException) throwable, (c, e) ->
                                source.sendMessage(Text.of("Invalid Command Argument: ",
                                        this.formatMessage(finalThrowable.getCause())))
                        );
                    } else if (throwable instanceof CommandExecutionException) {
                        this.manager.handleException(cloudSender,
                            CommandExecutionException.class,
                            (CommandExecutionException) throwable, (c, e) -> {
                                source.sendMessage(MESSAGE_INTERNAL_ERROR);
                                this.manager.getOwningPlugin().getLogger().error(
                                    "Exception executing command handler",
                                    finalThrowable.getCause()
                                );
                            }
                        );
                    } else {
                        source.sendMessage(MESSAGE_INTERNAL_ERROR);
                        this.manager.getOwningPlugin().getLogger().error(
                            "An unhandled exception was thrown during command execution",
                            throwable
                        );
                    }
                }
            });
        return CommandResult.success();
    }

    private Text formatMessage(final Throwable exc) {
        if (exc instanceof TextMessageException) {
            final Text response = ((TextMessageException) exc).getText();
            if (response == null) {
                return Text.of(TextColors.GRAY, "null");
            } else if (response.getColor() == TextColors.NONE) {
                return response.toBuilder().color(TextColors.GRAY).build();
            } else {
                return response;
            }
        } else {
            return Text.of(TextColors.GRAY, exc.getMessage());
        }
    }

    @Override
    public List<String> getSuggestions(
            final @NonNull CommandSource source,
            final @NonNull String arguments,
            final @Nullable Location<World> targetPosition
    ) {
        return this.manager.suggest(this.manager.getCommandSourceMapper().apply(source), this.formatCommand(arguments));
    }

    private String formatCommand(final String arguments) {
        if (arguments.isEmpty()) {
            return this.command.getName();
        } else {
            return this.command.getName() + " " + arguments;
        }
    }

    @Override
    public boolean testPermission(final @NonNull CommandSource source) {
        return this.manager.hasPermission(this.manager.getCommandSourceMapper().apply(source), this.cloudCommand.getCommandPermission());
    }

    @Override
    public @NonNull Optional<Text> getShortDescription(final @NonNull CommandSource source) {
        final Optional<Text> richDesc = this.cloudCommand.getCommandMeta().get(SpongeMetaKeys.RICH_DESCRIPTION);
        if (richDesc.isPresent()) {
            return richDesc;
        }

        return this.cloudCommand.getCommandMeta().get(CommandMeta.DESCRIPTION).map(Text::of);
    }

    @Override
    public @NonNull Optional<Text> getHelp(final @NonNull CommandSource source) {
        final Optional<Text> richLongDesc = this.cloudCommand.getCommandMeta().get(SpongeMetaKeys.RICH_LONG_DESCRIPTION);
        if (richLongDesc.isPresent()) {
            return richLongDesc;
        }

        return this.cloudCommand.getCommandMeta().get(CommandMeta.LONG_DESCRIPTION).map(Text::of);
    }

    @Override
    public Text getUsage(final @NonNull CommandSource source) {
        return Text.of(this.manager.getCommandSyntaxFormatter().apply(
            Collections.emptyList(),
            this.manager.getCommandTree().getNamedNode(this.command.getName())
        ));
    }

}
