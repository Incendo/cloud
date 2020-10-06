//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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

import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.internal.CommandRegistrationHandler;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.concurrent.CompletionException;

final class VelocityPluginRegistrationHandler<C> implements CommandRegistrationHandler {

    private static final String MESSAGE_NO_PERMS =
            "I'm sorry, but you do not have permission to perform this command. "
                    + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command. Type \"/help\" for help.";

    private CloudBrigadierManager<C, CommandSource> brigadierManager;
    private VelocityCommandManager<C> manager;

    void initialize(final @NonNull VelocityCommandManager<C> velocityCommandManager) {
        this.manager = velocityCommandManager;
        this.brigadierManager = new CloudBrigadierManager<>(velocityCommandManager,
            () -> new CommandContext<>(
                    velocityCommandManager.getCommandSenderMapper()
                                          .apply(velocityCommandManager.getProxyServer()
                                                                       .getConsoleCommandSource()))
        );
    }

    @Override
    public boolean registerCommand(final @NonNull Command<?> command) {
        final CommandArgument<?, ?> argument = command.getArguments().get(0);
        final List<String> aliases = ((StaticArgument<C>) argument).getAlternativeAliases();
        final BrigadierCommand brigadierCommand = new BrigadierCommand(
                this.brigadierManager.createLiteralCommandNode(command.getArguments().get(0).getName(), (Command<C>) command,
               (c, p) -> this.manager.hasPermission(
                       this.manager.getCommandSenderMapper()
                                   .apply(c), p), true,
               commandContext -> {
                   final CommandSource source = commandContext.getSource();
                   final String input = commandContext.getInput();
                   final C sender = this.manager.getCommandSenderMapper().apply(source);
                   this.manager.executeCommand(sender, input).whenComplete((result, throwable) -> {
                       if (throwable != null) {
                           if (throwable instanceof CompletionException) {
                               throwable = throwable.getCause();
                           }
                           final Throwable finalThrowable = throwable;
                           if (throwable instanceof InvalidSyntaxException) {
                               this.manager.handleException(sender,
                                                            InvalidSyntaxException.class,
                                                            (InvalidSyntaxException) throwable, (c, e) ->
                                source.sendMessage(TextComponent.builder("Invalid Command Syntax. Correct command syntax is: ",
                                                                         NamedTextColor.RED)
                                                                .append(e.getCorrectSyntax(), NamedTextColor.GRAY).build())
                               );
                           } else if (throwable instanceof InvalidCommandSenderException) {
                               this.manager.handleException(sender,
                                                            InvalidCommandSenderException.class,
                                                            (InvalidCommandSenderException) throwable, (c, e) ->
                                   source.sendMessage(TextComponent.of(finalThrowable.getMessage()).color(NamedTextColor.RED))
                               );
                           } else if (throwable instanceof NoPermissionException) {
                               this.manager.handleException(sender,
                                                            NoPermissionException.class,
                                                            (NoPermissionException) throwable, (c, e) ->
                                   source.sendMessage(TextComponent.of(MESSAGE_NO_PERMS))
                               );
                           } else if (throwable instanceof NoSuchCommandException) {
                               this.manager.handleException(sender,
                                                            NoSuchCommandException.class,
                                                            (NoSuchCommandException) throwable, (c, e) ->
                                   source.sendMessage(TextComponent.of(MESSAGE_UNKNOWN_COMMAND))
                               );
                           } else if (throwable instanceof ArgumentParseException) {
                               this.manager.handleException(sender,
                                                            ArgumentParseException.class,
                                                            (ArgumentParseException) throwable, (c, e) ->
                                   source.sendMessage(TextComponent.builder("Invalid Command Argument: ",
                                                                            NamedTextColor.RED)
                                                                   .append(finalThrowable.getCause().getMessage(),
                                                                           NamedTextColor.GRAY)
                                                                   .build())
                               );
                           } else {
                               source.sendMessage(TextComponent.of(throwable.getMessage()).color(NamedTextColor.RED));
                               throwable.printStackTrace();
                           }
                       }
                   });
                   return com.mojang.brigadier.Command.SINGLE_SUCCESS;
               })
        );
        final CommandMeta commandMeta = this.manager.getProxyServer().getCommandManager()
                                                    .metaBuilder(brigadierCommand)
                                                    .aliases(aliases.toArray(new String[0])).build();
        aliases.forEach(this.manager.getProxyServer().getCommandManager()::unregister);
        this.manager.getProxyServer().getCommandManager().register(commandMeta, brigadierCommand);
        return true;
    }

}
