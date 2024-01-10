//
// MIT License
//
// Copyright (c) 2024 Incendo
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
import cloud.commandframework.CommandComponent;
import cloud.commandframework.internal.CommandRegistrationHandler;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

final class BungeePluginRegistrationHandler<C> implements CommandRegistrationHandler<C> {

    private final Map<CommandComponent<C>, net.md_5.bungee.api.plugin.Command> registeredCommands = new HashMap<>();

    private BungeeCommandManager<C> bungeeCommandManager;

    BungeePluginRegistrationHandler() {
    }

    void initialize(final @NonNull BungeeCommandManager<C> bungeeCommandManager) {
        this.bungeeCommandManager = bungeeCommandManager;
    }

    @Override
    public boolean registerCommand(final @NonNull Command<C> command) {
        /* We only care about the root command argument */
        final CommandComponent<C> component = command.rootComponent();
        if (this.registeredCommands.containsKey(component)) {
            return false;
        }
        final BungeeCommand<C> bungeeCommand = new BungeeCommand<>(
                command,
                component,
                this.bungeeCommandManager
        );
        this.registeredCommands.put(component, bungeeCommand);
        this.bungeeCommandManager.getOwningPlugin().getProxy().getPluginManager()
                .registerCommand(this.bungeeCommandManager.getOwningPlugin(), bungeeCommand);
        return true;
    }
}
