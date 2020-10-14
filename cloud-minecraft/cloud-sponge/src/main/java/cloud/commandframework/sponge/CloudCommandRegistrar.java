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
package cloud.commandframework.sponge;

import cloud.commandframework.Command;
import com.google.common.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.manager.CommandFailedRegistrationException;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.plugin.PluginContainer;

import java.util.List;
import java.util.Optional;

final class CloudCommandRegistrar<C> implements CommandRegistrar<Command<C>> {

    private final SpongeCommandManager<C> commandManager;

    CloudCommandRegistrar(final @NonNull SpongeCommandManager<C> commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public TypeToken<Command<C>> handledType() {
        return new TypeToken<Command<C>>() {};
    }

    @Override
    public CommandMapping register(
            final PluginContainer container, final Command<C> command, final String primaryAlias, final String... secondaryAliases
    ) throws CommandFailedRegistrationException {
        Sponge.getCommandManager().registerAlias(
                this,
                this.commandManager.getOwningPlugin(),
                null, /* Fix */
                primaryAlias,
                secondaryAliases
        );
        return null;
    }

    @Override
    public CommandResult process(
            final CommandCause cause,
            final CommandMapping mapping,
            final String command,
            final String arguments
    ) {
        String input = command;
        if (!arguments.isEmpty()) {
            input = input + ' ' + arguments;
        }
        commandManager.executeCommand(
                this.commandManager.getBackwardsSubjectMapper().apply(cause.getSubject()),
                input
        ).whenComplete((result, throwable) -> {
           /* Copy from cloud-velocity */
        });
        /* We do our own error handling */
        return CommandResult.success();
    }

    @Override
    public List<String> suggestions(
            final CommandCause cause,
            final CommandMapping mapping,
            final String command,
            final String arguments
    ) {
        String suggestionQuery = command + ' ';
        if (!arguments.isEmpty()) {
            suggestionQuery = suggestionQuery + arguments + ' ';
        }
        return this.commandManager.suggest(
                this.commandManager.getBackwardsSubjectMapper().apply(cause.getSubject()),
                suggestionQuery
        );
    }

    @Override
    public Optional<Component> help(
            final CommandCause cause,
            final CommandMapping mapping
    ) {
        return Optional.empty();
    }

    @Override
    public boolean canExecute(final CommandCause cause,
                              final CommandMapping mapping) {
        /* Check permission & also check the executor type */
        final C sender = this.commandManager.getBackwardsSubjectMapper().apply(cause.getSubject());
        /* We also need to find the command */
        return true;
    }

    @Override
    public void reset() {
        /* We need to figure out how to implement this */
    }

    @Override
    public ResourceKey getKey() {
       return this.commandManager.getResourceKey();
    }

}
