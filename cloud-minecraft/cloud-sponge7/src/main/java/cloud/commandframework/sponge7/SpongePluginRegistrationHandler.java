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
package cloud.commandframework.sponge7;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.internal.CommandRegistrationHandler;
import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;

import java.util.HashMap;
import java.util.Map;

final class SpongePluginRegistrationHandler<C> implements CommandRegistrationHandler {

    private @MonotonicNonNull SpongeCommandManager<C> manager;
    private final Map<CommandArgument<?, ?>, CloudCommandCallable<C>> registeredCommands = new HashMap<>();

    void initialize(final SpongeCommandManager<C> manager) {
        this.manager = manager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean registerCommand(final @NonNull Command<?> command) {
        final StaticArgument<?> commandArgument = (StaticArgument<?>) command.getArguments().get(0);
        if (this.registeredCommands.containsKey(commandArgument)) {
            return false;
        }

        final CloudCommandCallable<C> callable = new CloudCommandCallable<>(
            commandArgument,
            (Command<C>) command,
            this.manager);
        this.registeredCommands.put(commandArgument, callable);

        return Sponge.getGame().getCommandManager().register(
            this.manager.getOwningPlugin(),
            callable,
            ImmutableList.copyOf(commandArgument.getAliases())
        ).isPresent();
    }

}
