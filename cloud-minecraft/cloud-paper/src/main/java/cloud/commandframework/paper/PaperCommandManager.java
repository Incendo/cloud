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
package cloud.commandframework.paper;

import cloud.commandframework.CloudCapability;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.ExecutionCoordinator;
import cloud.commandframework.paper.suggestions.SuggestionListener;
import cloud.commandframework.paper.suggestions.SuggestionListenerFactory;
import cloud.commandframework.state.RegistrationState;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

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
     *                                     {@link ExecutionCoordinator#simpleCoordinator()} and
     *                                     {@link ExecutionCoordinator#asyncCoordinator()}.
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
     * @throws InitializationException if the construction of the manager fails
     */
    public PaperCommandManager(
            final @NonNull Plugin owningPlugin,
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull Function<CommandSender, C> commandSenderMapper,
            final @NonNull Function<C, CommandSender> backwardsCommandSenderMapper
    ) throws InitializationException {
        super(owningPlugin, commandExecutionCoordinator, commandSenderMapper, backwardsCommandSenderMapper);

        this.registerCommandPreProcessor(new PaperCommandPreprocessor<>(this));
    }

    /**
     * Create a command manager using Bukkit's {@link CommandSender} as the sender type.
     *
     * @param owningPlugin                plugin owning the command manager
     * @param commandExecutionCoordinator execution coordinator instance
     * @return a new command manager
     * @throws InitializationException if the construction of the manager fails
     * @see #PaperCommandManager(Plugin, ExecutionCoordinator, Function, Function) for a more thorough explanation
     * @since 1.5.0
     */
    public static @NonNull PaperCommandManager<@NonNull CommandSender> createNative(
            final @NonNull Plugin owningPlugin,
            final @NonNull ExecutionCoordinator<CommandSender> commandExecutionCoordinator
    ) throws InitializationException {
        return new PaperCommandManager<>(
                owningPlugin,
                commandExecutionCoordinator,
                UnaryOperator.identity(),
                UnaryOperator.identity()
        );
    }

    /**
     * Attempts to enable Brigadier command registration through the Paper API, falling
     * back to {@link BukkitCommandManager#registerBrigadier()} if that fails.
     *
     * <p>Callers should check for {@link CloudBukkitCapabilities#NATIVE_BRIGADIER} first
     * to avoid exceptions.</p>
     *
     * <p>A check for {@link CloudBukkitCapabilities#NATIVE_BRIGADIER} {@code ||} {@link CloudBukkitCapabilities#COMMODORE_BRIGADIER}
     * may also be appropriate for some use cases (because of the fallback behavior), but not most, as Commodore does not offer
     * any functionality on modern
     * versions (see the documentation for {@link CloudBukkitCapabilities#COMMODORE_BRIGADIER}).</p>
     *
     * @throws BrigadierFailureException Exception thrown if the mappings cannot be registered
     */
    @Override
    public void registerBrigadier() throws BrigadierFailureException {
        this.requireState(RegistrationState.BEFORE_REGISTRATION);
        this.checkBrigadierCompatibility();
        if (!this.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
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
     * @return {@inheritDoc}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @Override
    public boolean hasBrigadierManager() {
        return this.paperBrigadierListener != null || super.hasBrigadierManager();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws BrigadierManagerNotPresent when {@link #hasBrigadierManager()} is false
     * @since 1.2.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    @Override
    public @NonNull CloudBrigadierManager<C, ?> brigadierManager() {
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
     * @see #hasCapability(CloudCapability) Check if the capability is present
     */
    public void registerAsynchronousCompletions() throws IllegalStateException {
        this.requireState(RegistrationState.BEFORE_REGISTRATION);
        if (!this.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            throw new IllegalStateException("Failed to register asynchronous command completion listener.");
        }

        final SuggestionListenerFactory<C> suggestionListenerFactory = SuggestionListenerFactory.create(this);
        final SuggestionListener<C> suggestionListener = suggestionListenerFactory.createListener();

        Bukkit.getServer().getPluginManager().registerEvents(
                suggestionListener,
                this.getOwningPlugin()
        );
    }
}
