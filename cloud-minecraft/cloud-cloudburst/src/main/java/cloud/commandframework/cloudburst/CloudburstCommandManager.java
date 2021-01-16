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
package cloud.commandframework.cloudburst;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.server.command.CommandSender;
import org.cloudburstmc.server.event.EventPriority;
import org.cloudburstmc.server.event.Listener;
import org.cloudburstmc.server.event.server.RegistriesClosedEvent;
import org.cloudburstmc.server.plugin.Plugin;

import java.util.function.Function;

/**
 * Command manager for the Cloudburst platform
 *
 * @param <C> Command sender type
 */
public class CloudburstCommandManager<C> extends CommandManager<C> {

    private final Function<CommandSender, C> commandSenderMapper;
    private final Function<C, CommandSender> backwardsCommandSenderMapper;

    private final Plugin owningPlugin;

    /**
     * Construct a new Cloudburst command manager
     *
     * @param owningPlugin                 Plugin that is constructing the manager
     * @param commandExecutionCoordinator  Coordinator provider
     * @param commandSenderMapper          Function that maps {@link CommandSender} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link CommandSender}
     */
    @SuppressWarnings("unchecked")
    public CloudburstCommandManager(
            final @NonNull Plugin owningPlugin,
            final @NonNull Function<@NonNull CommandTree<C>,
                    @NonNull CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @NonNull Function<@NonNull CommandSender, @NonNull C> commandSenderMapper,
            final @NonNull Function<@NonNull C, @NonNull CommandSender> backwardsCommandSenderMapper
    ) {
        super(commandExecutionCoordinator, new CloudburstPluginRegistrationHandler<>());
        ((CloudburstPluginRegistrationHandler<C>) this.getCommandRegistrationHandler()).initialize(this);
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;
        this.owningPlugin = owningPlugin;

        // Prevent commands from being registered when the server would reject them anyways
        this.owningPlugin.getServer().getPluginManager().registerEvent(
                RegistriesClosedEvent.class,
                CloudListener.INSTANCE,
                EventPriority.NORMAL,
                (listener, event) -> this.lockRegistration(),
                this.owningPlugin
        );
    }

    @Override
    public final boolean hasPermission(
            final @NonNull C sender,
            final @NonNull String permission
    ) {
        return this.backwardsCommandSenderMapper.apply(sender).hasPermission(permission);
    }

    @Override
    public final @NonNull CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.builder().build();
    }

    @Override
    public final boolean isCommandRegistrationAllowed() {
        return this.getRegistrationState() != RegistrationState.AFTER_REGISTRATION;
    }

    final @NonNull Function<@NonNull CommandSender, @NonNull C> getCommandSenderMapper() {
        return this.commandSenderMapper;
    }

    /**
     * Get the plugin that owns the manager
     *
     * @return Owning plugin
     */
    public final @NonNull Plugin getOwningPlugin() {
        return this.owningPlugin;
    }

    static final class CloudListener implements Listener {

        static final CloudListener INSTANCE = new CloudListener();

        private CloudListener() {
        }

    }

}
