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
package cloud.commandframework.paper;

import cloud.commandframework.CommandTree;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

/**
 * Paper command manager that extends {@link BukkitCommandManager}
 *
 * @param <C> Command sender type
 */
public class PaperCommandManager<C> extends BukkitCommandManager<C> {

    private PaperBrigadierListener<C> paperBrigadierListener = null;

    /**
     * Construct a new Paper command manager
     *
     * @param owningPlugin                 Plugin that is constructing the manager. This will be used when registering the
     *                                     commands to the Bukkit command map.
     * @param commandExecutionCoordinator  Execution coordinator instance. The coordinator is in charge of executing incoming
     *                                     commands. Some considerations must be made when picking a suitable execution
     *                                     coordinator. For example, an entirely asynchronous coordinator is not suitable
     *                                     when the parsers used in your commands are not thread safe. If you have
     *                                     commands that perform blocking operations, however, it might not be a good idea to
     *                                     use a synchronous execution coordinator. In most cases you will want to pick between
     *                                     {@link CommandExecutionCoordinator#simpleCoordinator()} and
     *                                     {@link cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator}.
     *                                     <p>
     *                                     A word of caution: When using the asynchronous command executor in Bukkit, it is very
     *                                     likely that you will have to perform manual synchronization when executing the commands
     *                                     in many cases, as Bukkit makes no guarantees of thread safety in common classes. To
     *                                     make this easier, {@link #taskRecipe()} is provided. Furthermore, it may be unwise to
     *                                     use asynchronous command parsing, especially when dealing with things such as players
     *                                     and entities. To make this more safe, the asynchronous command execution allows you
     *                                     to state that you want synchronous command parsing.
     *                                     <p>
     *                                     The execution coordinator will not have an impact on command suggestions. More
     *                                     specifically, using an asynchronous command executor does not mean that command
     *                                     suggestions will be provided asynchronously. Instead, use
     *                                     {@link #registerAsynchronousCompletions()} to register asynchronous completions. This
     *                                     will only work on Paper servers. Be aware that cloud does not synchronize the command
     *                                     suggestions for you, and this should only be used if your command suggestion providers
     *                                     are thread safe.
     * @param commandSenderMapper          Function that maps {@link CommandSender} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link CommandSender}
     * @throws Exception If the construction of the manager fails
     */
    public PaperCommandManager(
            final @NonNull Plugin owningPlugin,
            final @NonNull Function<CommandTree<C>,
                    CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @NonNull Function<CommandSender, C> commandSenderMapper,
            final @NonNull Function<C, CommandSender> backwardsCommandSenderMapper
    ) throws Exception {
        super(owningPlugin, commandExecutionCoordinator, commandSenderMapper, backwardsCommandSenderMapper);
    }

    /**
     * Register Brigadier mappings using the native paper events
     *
     * @throws BrigadierFailureException Exception thrown if the mappings cannot be registered
     */
    @Override
    public void registerBrigadier() throws BrigadierFailureException {
        this.requireState(RegistrationState.BEFORE_REGISTRATION);
        this.checkBrigadierCompatibility();
        if (!this.queryCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            super.registerBrigadier();
        } else {
            try {
                this.paperBrigadierListener = new PaperBrigadierListener<>(this);
                Bukkit.getPluginManager().registerEvents(
                        this.paperBrigadierListener,
                        this.getOwningPlugin()
                );
            } catch (final Throwable e) {
                throw new BrigadierFailureException(BrigadierFailureReason.PAPER_BRIGADIER_INITIALIZATION_FAILURE, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.2.0
     */
    @Override
    public @Nullable CloudBrigadierManager<C, ?> brigadierManager() {
        if (this.paperBrigadierListener != null) {
            return this.paperBrigadierListener.brigadierManager();
        }
        return super.brigadierManager();
    }

    /**
     * Register asynchronous completions. This requires all argument parsers to be thread safe, and it
     * is up to the caller to guarantee that such is the case
     *
     * @throws IllegalStateException when the server does not support asynchronous completions.
     * @see #queryCapability(CloudBukkitCapabilities) Check if the capability is present
     */
    public void registerAsynchronousCompletions() throws IllegalStateException {
        this.requireState(RegistrationState.BEFORE_REGISTRATION);
        if (!this.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            throw new IllegalStateException("Failed to register asynchronous command completion listener.");
        }
        Bukkit.getServer().getPluginManager().registerEvents(
                new AsyncCommandSuggestionsListener<>(this),
                this.getOwningPlugin()
        );
    }

}
