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
package cloud.commandframework.cloudburst;

import cloud.commandframework.Command;
import cloud.commandframework.CommandComponent;
import java.util.Collection;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.cloudburstmc.server.command.CommandSender;
import org.cloudburstmc.server.command.PluginCommand;
import org.cloudburstmc.server.command.data.CommandData;
import org.cloudburstmc.server.plugin.Plugin;

final class CloudburstCommand<C> extends PluginCommand<Plugin> {

    private final CommandComponent<C> command;
    private final CloudburstCommandManager<C> manager;

    CloudburstCommand(
            final @NonNull String label,
            final @NonNull Collection<@NonNull String> aliases,
            final @NonNull Command<C> cloudCommand,
            final @NonNull CommandComponent<C> command,
            final @NonNull CloudburstCommandManager<C> manager
    ) {
        super(manager.getOwningPlugin(), CommandData.builder(label)
                .addAliases(aliases.toArray(new String[0]))
                .addPermission(cloudCommand.commandPermission().toString())
                .setDescription(cloudCommand.commandDescription().description().textDescription())
                .build());
        this.command = command;
        this.manager = manager;
    }

    @Override
    public boolean execute(
            final CommandSender commandSender,
            final String commandLabel,
            final String[] strings
    ) {
        /* Join input */
        final StringBuilder builder = new StringBuilder(this.command.name());
        for (final String string : strings) {
            builder.append(" ").append(string);
        }
        final C sender = this.manager.getCommandSenderMapper().apply(commandSender);
        this.manager.commandExecutor().executeCommand(sender, builder.toString());
        return true;
    }
}
