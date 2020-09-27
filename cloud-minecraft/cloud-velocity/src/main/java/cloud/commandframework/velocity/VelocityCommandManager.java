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
package cloud.commandframework.velocity;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * {@link CommandManager} implementation for Velocity
 *
 * @param <C> Command sender type
 */
public class VelocityCommandManager<C> extends CommandManager<C> {

    private final ProxyServer proxyServer;
    private final Function<CommandSource, C> commandSenderMapper;
    private final Function<C, CommandSource> backwardsCommandSenderMapper;

    /**
     * Create a new command manager instance
     *
     * @param proxyServer                  ProxyServer instance
     * @param commandExecutionCoordinator  Coordinator provider
     * @param commandSenderMapper          Function that maps {@link CommandSource} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link CommandSource}
     */
    public VelocityCommandManager(
            @Nonnull final ProxyServer proxyServer,
            @Nonnull final Function<CommandTree<C>, CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            @Nonnull final Function<CommandSource, C> commandSenderMapper,
            @Nonnull final Function<C, CommandSource> backwardsCommandSenderMapper) {
        super(commandExecutionCoordinator, new VelocityPluginRegistrationHandler<>());
        ((VelocityPluginRegistrationHandler<C>) this.getCommandRegistrationHandler()).initialize(this);
        this.proxyServer = proxyServer;
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;
    }

    @Override
    public final boolean hasPermission(@Nonnull final C sender, @Nonnull final String permission) {
        return this.backwardsCommandSenderMapper.apply(sender).hasPermission(permission);
    }

    @Nonnull
    @Override
    public final CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

    @Nonnull
    final ProxyServer getProxyServer() {
        return this.proxyServer;
    }

    @Nonnull
    final Function<CommandSource, C> getCommandSenderMapper() {
        return this.commandSenderMapper;
    }

}
