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

import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.internal.CommandRegistrationHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.server.Server;
import org.cloudburstmc.server.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

class CloudburstPluginRegistrationHandler<C> implements CommandRegistrationHandler {

    private final Map<CommandArgument<?, ?>, org.cloudburstmc.server.command.Command> registeredCommands = new HashMap<>();

    private CloudburstCommandManager<C> cloudburstCommandManager;

    CloudburstPluginRegistrationHandler() {
    }

    void initialize(final @NonNull CloudburstCommandManager<C> cloudburstCommandManager) {
        this.cloudburstCommandManager = cloudburstCommandManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final boolean registerCommand(final @NonNull Command<?> command) {
        /* We only care about the root command argument */
        final CommandArgument<?, ?> commandArgument = command.getArguments().get(0);
        if (this.registeredCommands.containsKey(commandArgument)) {
            return false;
        }
        final Plugin plugin = this.cloudburstCommandManager.getOwningPlugin();
        final CloudburstCommand<C> cloudburstCommand = new CloudburstCommand<>(
                commandArgument.getName(),
                ((StaticArgument<C>) commandArgument).getAlternativeAliases(),
                (Command<C>) command,
                (CommandArgument<C, ?>) commandArgument,
                this.cloudburstCommandManager
        );
        this.registeredCommands.put(commandArgument, cloudburstCommand);
        Server.getInstance().getCommandRegistry().register(plugin, cloudburstCommand);
        return true;
    }

}
