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
package cloud.commandframework.bungee;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.suggestion.Suggestion;
import java.util.stream.Collectors;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class BungeeCommand<C> extends Command implements TabExecutor {

    private final BungeeCommandManager<C> manager;
    private final CommandComponent<C> command;

    BungeeCommand(
            final cloud.commandframework.@NonNull Command<C> cloudCommand,
            final @NonNull CommandComponent<C> command,
            final @NonNull BungeeCommandManager<C> manager
    ) {
        super(
                command.name(),
                cloudCommand.commandPermission().toString(),
                command.alternativeAliases().toArray(new String[0])
        );
        this.command = command;
        this.manager = manager;
    }

    @Override
    public void execute(final CommandSender commandSender, final String[] strings) {
        /* Join input */
        final StringBuilder builder = new StringBuilder(this.command.name());
        for (final String string : strings) {
            builder.append(" ").append(string);
        }
        final C sender = this.manager.getCommandSenderMapper().apply(commandSender);
        this.manager.commandExecutor().executeCommand(sender, builder.toString());
    }

    @Override
    public Iterable<String> onTabComplete(
            final CommandSender sender,
            final String[] args
    ) {
        final StringBuilder builder = new StringBuilder(this.command.name());
        for (final String string : args) {
            builder.append(" ").append(string);
        }
        return this.manager.suggestionFactory().suggestImmediately(
                this.manager.getCommandSenderMapper().apply(sender),
                builder.toString()
        ).stream().map(Suggestion::suggestion).collect(Collectors.toList());
    }
}
