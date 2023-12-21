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
package cloud.commandframework.bukkit;

import cloud.commandframework.Command;
import cloud.commandframework.CommandComponent;
import cloud.commandframework.CommandDescription;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.internal.CommandNode;
import cloud.commandframework.permission.Permission;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class BukkitCommand<C> extends org.bukkit.command.Command implements PluginIdentifiableCommand {

    private final CommandComponent<C> command;
    private final BukkitCommandManager<C> manager;
    private final Command<C> cloudCommand;

    private boolean disabled;

    private static @NonNull String description(final @NonNull Command<?> command) {
        final Optional<String> bukkitDescription = command.commandMeta().optional(BukkitCommandMeta.BUKKIT_DESCRIPTION);
        if (bukkitDescription.isPresent()) {
            return bukkitDescription.get();
        }

        final CommandDescription description = command.commandDescription();
        if (!description.isEmpty()) {
            return description.description().textDescription();
        }

        return command.rootComponent().description().textDescription();
    }

    BukkitCommand(
            final @NonNull String label,
            final @NonNull List<@NonNull String> aliases,
            final @NonNull Command<C> cloudCommand,
            final @NonNull CommandComponent<C> command,
            final @NonNull BukkitCommandManager<C> manager
    ) {
        super(
                label,
                description(cloudCommand),
                "",
                aliases
        );
        this.command = command;
        this.manager = manager;
        this.cloudCommand = cloudCommand;
        if (this.command.owningCommand() != null) {
            this.setPermission(this.command.owningCommand().commandPermission().toString());
        }
        this.disabled = false;
    }

    @Override
    public @NonNull List<@NonNull String> tabComplete(
            final @NonNull CommandSender sender,
            final @NonNull String alias,
            final @NonNull String @NonNull [] args
    ) throws IllegalArgumentException {
        final StringBuilder builder = new StringBuilder(this.command.name());
        for (final String string : args) {
            builder.append(" ").append(string);
        }
        return this.manager.suggestionFactory().suggestImmediately(
                this.manager.getCommandSenderMapper().apply(sender),
                builder.toString()
        ).stream().map(Suggestion::suggestion).collect(Collectors.toList());
    }

    @Override
    public boolean execute(
            final @NonNull CommandSender commandSender,
            final @NonNull String commandLabel,
            final @NonNull String @NonNull [] strings
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

    @Override
    public @NonNull String getDescription() {
        return description(this.cloudCommand);
    }

    @Override
    public @NonNull Plugin getPlugin() {
        return this.manager.getOwningPlugin();
    }

    @Override
    public @NonNull String getUsage() {
        return this.manager.commandSyntaxFormatter().apply(
                Collections.singletonList(Objects.requireNonNull(this.namedNode().component())),
                this.namedNode()
        );
    }

    @Override
    public boolean testPermissionSilent(final @NonNull CommandSender target) {
        final CommandNode<C> node = this.namedNode();
        if (this.disabled || node == null) {
            return false;
        }

        final Permission permission = (Permission) node
                .nodeMeta()
                .getOrDefault("permission", Permission.empty());

        return this.manager.hasPermission(this.manager.getCommandSenderMapper().apply(target), permission);
    }

    @API(status = API.Status.INTERNAL, since = "1.7.0")
    void disable() {
        this.disabled = true;
    }

    @Override
    public boolean isRegistered() {
        // This allows us to prevent the command from showing
        // in Bukkit help topics.
        return !this.disabled;
    }

    private @Nullable CommandNode<C> namedNode() {
        return this.manager.commandTree().getNamedNode(this.command.name());
    }
}
