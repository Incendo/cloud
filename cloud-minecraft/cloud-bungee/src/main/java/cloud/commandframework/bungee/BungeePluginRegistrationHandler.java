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
package cloud.commandframework.bungee;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.internal.CommandRegistrationHandler;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;

final class BungeePluginRegistrationHandler<C> implements CommandRegistrationHandler {

    private final Map<CommandArgument<?, ?>, net.md_5.bungee.api.plugin.Command> registeredCommands = new HashMap<>();

    private BungeeCommandManager<C> bungeeCommandManager;

    BungeePluginRegistrationHandler() {
    }

    void initialize(final @NonNull BungeeCommandManager<C> bungeeCommandManager) {
        this.bungeeCommandManager = bungeeCommandManager;
    }

    @Override
    public boolean registerCommand(final @NonNull Command<?> command) {
        /* We only care about the root command argument */
        final CommandArgument<?, ?> commandArgument = command.getArguments().get(0);
        if (this.registeredCommands.containsKey(commandArgument)) {
            return false;
        }
        @SuppressWarnings("unchecked") final BungeeCommand<C> bungeeCommand = new BungeeCommand<>(
                (Command<C>) command,
                (CommandArgument<C, ?>) commandArgument,
                this.bungeeCommandManager
        );
        this.registeredCommands.put(commandArgument, bungeeCommand);
        this.bungeeCommandManager.getOwningPlugin().getProxy().getPluginManager()
                .registerCommand(this.bungeeCommandManager.getOwningPlugin(), bungeeCommand);
        return true;
    }

}
