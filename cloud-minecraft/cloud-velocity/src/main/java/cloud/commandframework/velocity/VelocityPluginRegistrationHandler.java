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
package cloud.commandframework.velocity;

import cloud.commandframework.Command;
import cloud.commandframework.CommandComponent;
import cloud.commandframework.brigadier.CloudBrigadierCommand;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.internal.CommandRegistrationHandler;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import java.util.Collection;
import org.checkerframework.checker.nullness.qual.NonNull;

final class VelocityPluginRegistrationHandler<C> implements CommandRegistrationHandler<C> {

    private CloudBrigadierManager<C, CommandSource> brigadierManager;
    private VelocityCommandManager<C> manager;

    void initialize(final @NonNull VelocityCommandManager<C> velocityCommandManager) {
        this.manager = velocityCommandManager;
        this.brigadierManager = new CloudBrigadierManager<>(
                velocityCommandManager,
                () -> new CommandContext<>(
                        velocityCommandManager.senderMapper()
                                .map(velocityCommandManager.proxyServer()
                                        .getConsoleCommandSource()),
                        velocityCommandManager
                ),
                velocityCommandManager.suggestionFactory(),
                velocityCommandManager.senderMapper()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean registerCommand(final @NonNull Command<C> command) {
        final CommandComponent<C> component = command.rootComponent();
        final Collection<String> aliases = component.alternativeAliases();
        final BrigadierCommand brigadierCommand = new BrigadierCommand(
                this.brigadierManager.literalBrigadierNodeFactory().createNode(
                        command.rootComponent().name(),
                        command,
                        new CloudBrigadierCommand<>(this.manager, this.brigadierManager)
                )
        );
        final CommandMeta commandMeta = this.manager.proxyServer().getCommandManager()
                .metaBuilder(brigadierCommand)
                .aliases(aliases.toArray(new String[0])).build();
        aliases.forEach(this.manager.proxyServer().getCommandManager()::unregister);
        this.manager.proxyServer().getCommandManager().register(commandMeta, brigadierCommand);
        return true;
    }

    @NonNull CloudBrigadierManager<C, CommandSource> brigadierManager() {
        return this.brigadierManager;
    }
}
