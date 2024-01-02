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
package cloud.commandframework.sponge7;

import cloud.commandframework.Command;
import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.suggestion.Suggestion;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

final class CloudCommandCallable<C> implements CommandCallable {

    private final CommandComponent<C> command;
    private final Command<C> cloudCommand;
    private final SpongeCommandManager<C> manager;

    CloudCommandCallable(
            final CommandComponent<C> command,
            final Command<C> cloudCommand,
            final SpongeCommandManager<C> manager
    ) {
        this.command = command;
        this.cloudCommand = cloudCommand;
        this.manager = manager;
    }

    @Override
    public CommandResult process(final @NonNull CommandSource source, final @NonNull String arguments) {
        final C cloudSender = this.manager.senderMapper().map(source);

        this.manager.commandExecutor().executeCommand(cloudSender, this.formatCommand(arguments), ctx ->
                        ctx.store(SpongeCommandManager.SPONGE_COMMAND_SOURCE_KEY, source));
        return CommandResult.success();
    }

    @Override
    public @NonNull List<@NonNull String> getSuggestions(
            final @NonNull CommandSource source,
            final @NonNull String arguments,
            final @Nullable Location<World> targetPosition
    ) {
        return this.manager.suggestionFactory()
                .suggestImmediately(
                        this.manager.senderMapper().map(source),
                        this.formatCommand(arguments)
                ).stream()
                .map(Suggestion::suggestion)
                .collect(Collectors.toList());
    }

    private String formatCommand(final String arguments) {
        if (arguments.isEmpty()) {
            return this.command.name();
        } else {
            return this.command.name() + " " + arguments;
        }
    }

    @Override
    public boolean testPermission(final @NonNull CommandSource source) {
        return this.manager.hasPermission(
                this.manager.senderMapper().map(source),
                this.cloudCommand.commandPermission()
        );
    }

    @Override
    public @NonNull Optional<Text> getShortDescription(final @NonNull CommandSource source) {
        final Optional<Text> richDesc = this.cloudCommand.commandMeta().optional(SpongeMetaKeys.RICH_DESCRIPTION);
        if (richDesc.isPresent()) {
            return richDesc;
        }

        return Optional.of(Text.of(this.cloudCommand.commandDescription().description().textDescription()));
    }

    @Override
    public @NonNull Optional<Text> getHelp(final @NonNull CommandSource source) {
        final Optional<Text> richLongDesc = this.cloudCommand.commandMeta().optional(SpongeMetaKeys.RICH_LONG_DESCRIPTION);
        if (richLongDesc.isPresent()) {
            return richLongDesc;
        }

        return Optional.of(Text.of(this.cloudCommand.commandDescription().verboseDescription().textDescription()));
    }

    @Override
    public Text getUsage(final @NonNull CommandSource source) {
        return Text.of(this.manager.commandSyntaxFormatter().apply(
                Collections.emptyList(),
                this.manager.commandTree().getNamedNode(this.command.name())
        ));
    }
}
