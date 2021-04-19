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
package cloud.commandframework.sponge;

import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.internal.CommandRegistrationHandler;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;

final class SpongeRegistrationHandler<C> implements CommandRegistrationHandler {

    private SpongeCommandManager<C> commandManager;
    private final Set<cloud.commandframework.Command<C>> registeredCommands = new HashSet<>();

    SpongeRegistrationHandler() {
    }

    //@Listener
    public void handleRegistrationEvent(final RegisterCommandEvent<Command.Raw> event) {
        this.registeredCommands.stream()
                .collect(groupingBy(command -> command.getArguments().get(0).getName()))
                .forEach((label, commands) ->
                        this.registerCommand(event, label, commands));
    }

    @SuppressWarnings("unchecked")
    private void registerCommand(
            final RegisterCommandEvent<Command.Raw> event,
            final String label,
            final List<cloud.commandframework.Command<C>> commands
    ) {
        event.register(
                this.commandManager.getOwningPlugin(),
                new CloudSpongeCommand<>(label, this.commandManager),
                label,
                ((StaticArgument<C>) commands.get(0).getArguments().get(0)).getAlternativeAliases().toArray(new String[0])
        );
    }

    void initialize(
            final @NonNull SpongeCommandManager<C> commandManager,
            final @NonNull SystemSubject subject
    ) {
        this.commandManager = commandManager;
        Sponge.eventManager().registerListener(
                this.commandManager.getOwningPlugin(),
                new TypeToken<RegisterCommandEvent<Command.Raw>>() {
                },
                this::handleRegistrationEvent
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean registerCommand(final cloud.commandframework.@NonNull Command<?> command) {
        this.registeredCommands.add((cloud.commandframework.Command<C>) command);
        return true;
    }

}
