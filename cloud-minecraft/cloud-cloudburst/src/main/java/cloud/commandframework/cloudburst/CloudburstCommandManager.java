//
// MIT License
//
// Copyright (c) 2024 Incendo
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
package cloud.commandframework.cloudburst;

import cloud.commandframework.CommandManager;
import cloud.commandframework.SenderMapper;
import cloud.commandframework.SenderMapperHolder;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.exceptions.handling.ExceptionContext;
import cloud.commandframework.exceptions.handling.ExceptionHandler;
import cloud.commandframework.execution.ExecutionCoordinator;
import cloud.commandframework.state.RegistrationState;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.server.command.CommandSender;
import org.cloudburstmc.server.event.EventPriority;
import org.cloudburstmc.server.event.Listener;
import org.cloudburstmc.server.event.server.RegistriesClosedEvent;
import org.cloudburstmc.server.plugin.Plugin;

/**
 * Command manager for the Cloudburst platform
 *
 * @param <C> Command sender type
 */
public class CloudburstCommandManager<C> extends CommandManager<C> implements SenderMapperHolder<CommandSender, C> {

    private static final String MESSAGE_INTERNAL_ERROR = "An internal error occurred while attempting to perform this command.";
    private static final String MESSAGE_NO_PERMS =
            "I'm sorry, but you do not have permission to perform this command. "
                    + "Please contact the server administrators if you believe that this is in error.";
    private static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command. Type \"/help\" for help.";

    private final SenderMapper<CommandSender, C> senderMapper;

    private final Plugin owningPlugin;

    /**
     * Construct a new Cloudburst command manager
     *
     * @param owningPlugin                 Plugin that is constructing the manager
     * @param commandExecutionCoordinator  Coordinator provider
     * @param senderMapper                 Function that maps {@link CommandSender} to the command sender type
     */
    @SuppressWarnings("unchecked")
    public CloudburstCommandManager(
            final @NonNull Plugin owningPlugin,
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull SenderMapper<CommandSender, C> senderMapper
    ) {
        super(commandExecutionCoordinator, new CloudburstPluginRegistrationHandler<>());
        ((CloudburstPluginRegistrationHandler<C>) this.commandRegistrationHandler()).initialize(this);
        this.senderMapper = senderMapper;
        this.owningPlugin = owningPlugin;
        this.parameterInjectorRegistry().registerInjector(
                CommandSender.class,
                (context, annotations) -> this.senderMapper.reverse(context.sender())
        );

        // Prevent commands from being registered when the server would reject them anyways
        this.owningPlugin.getServer().getPluginManager().registerEvent(
                RegistriesClosedEvent.class,
                CloudListener.INSTANCE,
                EventPriority.NORMAL,
                (listener, event) -> this.lockRegistration(),
                this.owningPlugin
        );

        this.registerDefaultExceptionHandlers();
    }

    @Override
    public final boolean hasPermission(
            final @NonNull C sender,
            final @NonNull String permission
    ) {
        return this.senderMapper.reverse(sender).hasPermission(permission);
    }

    @Override
    public final boolean isCommandRegistrationAllowed() {
        return this.state() != RegistrationState.AFTER_REGISTRATION;
    }

    /**
     * Get the plugin that owns the manager
     *
     * @return Owning plugin
     */
    public final @NonNull Plugin getOwningPlugin() {
        return this.owningPlugin;
    }

    private void registerDefaultExceptionHandlers() {
        this.registerHandler(Throwable.class, (commandSender, throwable) -> {
            commandSender.sendMessage(MESSAGE_INTERNAL_ERROR);
            this.getOwningPlugin().getLogger().error(
                    "An unhandled exception was thrown during command execution",
                    throwable
            );
        });
        this.registerHandler(CommandExecutionException.class, (commandSender, throwable) -> {
            commandSender.sendMessage(MESSAGE_INTERNAL_ERROR);
            this.getOwningPlugin().getLogger().error(
                    "Exception executing command handler",
                    throwable.getCause()
            );
        });
        this.registerHandler(ArgumentParseException.class, (commandSender, throwable) ->
                commandSender.sendMessage("Invalid Command Argument: " + throwable.getCause().getMessage())
        );
        this.registerHandler(NoSuchCommandException.class, (commandSender, throwable) ->
                commandSender.sendMessage(MESSAGE_UNKNOWN_COMMAND)
        );
        this.registerHandler(NoPermissionException.class, (commandSender, throwable) ->
                commandSender.sendMessage(MESSAGE_NO_PERMS)
        );
        this.registerHandler(InvalidCommandSenderException.class, (commandSender, throwable) ->
                commandSender.sendMessage(throwable.getMessage())
        );
        this.registerHandler(InvalidSyntaxException.class, (commandSender, throwable) ->
                commandSender.sendMessage("Invalid Command Syntax. Correct command syntax is: /" + throwable.correctSyntax())
        );
    }

    private <T extends Throwable> void registerHandler(
            final @NonNull Class<T> exceptionClass,
            final @NonNull CloudburstExceptionHandler<C, T> handler
    ) {
        this.exceptionController().registerHandler(exceptionClass, handler);
    }

    @Override
    public final @NonNull SenderMapper<CommandSender, C> senderMapper() {
        return this.senderMapper;
    }


    static final class CloudListener implements Listener {

        static final CloudListener INSTANCE = new CloudListener();

        private CloudListener() {
        }
    }


    private interface CloudburstExceptionHandler<C, T extends Throwable> extends ExceptionHandler<C, T> {

        @Override
        default void handle(final @NonNull ExceptionContext<C, T> context) throws Throwable {
            final CommandSender commandSender = context.context().inject(CommandSender.class)
                    .orElseThrow(NullPointerException::new);
            this.handle(commandSender, context.exception());
        }

        void handle(@NonNull CommandSender commandSender, @NonNull T throwable);
    }
}
