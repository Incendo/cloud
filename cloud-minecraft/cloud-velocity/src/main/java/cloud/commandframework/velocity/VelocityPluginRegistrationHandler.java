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
package cloud.commandframework.velocity;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.internal.CommandRegistrationHandler;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

final class VelocityPluginRegistrationHandler<C> implements CommandRegistrationHandler {

    private CloudBrigadierManager<C, CommandSource> brigadierManager;
    private VelocityCommandManager<C> manager;

    void initialize(final @NonNull VelocityCommandManager<C> velocityCommandManager) {
        this.manager = velocityCommandManager;
        this.brigadierManager = new CloudBrigadierManager<>(
                velocityCommandManager,
                () -> new CommandContext<>(
                        velocityCommandManager.getCommandSenderMapper()
                                .apply(velocityCommandManager.getProxyServer()
                                        .getConsoleCommandSource()),
                        velocityCommandManager
                )
        );
        this.brigadierManager.brigadierSenderMapper(
                sender -> this.manager.getCommandSenderMapper().apply(sender)
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean registerCommand(final @NonNull Command<?> command) {
        final CommandArgument<?, ?> argument = command.getArguments().get(0);
        final List<String> aliases = ((StaticArgument<C>) argument).getAlternativeAliases();
        final BrigadierCommand brigadierCommand = new BrigadierCommand(
                this.brigadierManager.createLiteralCommandNode(
                        command.getArguments().get(0).getName(),
                        (Command<C>) command,
                        (c, p) -> this.manager.hasPermission(
                                this.manager.getCommandSenderMapper().apply(c),
                                p
                        ),
                        true,
                        new VelocityExecutor<>(manager)
                )
        );
        final CommandMeta commandMeta = this.manager.getProxyServer().getCommandManager()
                .metaBuilder(brigadierCommand)
                .aliases(aliases.toArray(new String[0])).build();
        aliases.forEach(this.manager.getProxyServer().getCommandManager()::unregister);
        this.manager.getProxyServer().getCommandManager().register(commandMeta, brigadierCommand);
        return true;
    }

    @NonNull CloudBrigadierManager<C, CommandSource> brigadierManager() {
        return this.brigadierManager;
    }

}
