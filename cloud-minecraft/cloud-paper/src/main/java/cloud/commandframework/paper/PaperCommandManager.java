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
import cloud.commandframework.SenderMapper;
import cloud.commandframework.brigadier.BrigadierSetting;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.ExecutionCoordinator;
import cloud.commandframework.paper.suggestions.SuggestionListener;
import cloud.commandframework.paper.suggestions.SuggestionListenerFactory;
import cloud.commandframework.state.RegistrationState;
import java.util.concurrent.Executor;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Paper command manager that extends {@link BukkitCommandManager}
 *
 * @param <C> command sender type
 */
public class PaperCommandManager<C> extends BukkitCommandManager<C> {

    private @Nullable PaperBrigadierListener<C> paperBrigadierListener = null;

    /**
     * Create a new Paper command manager.
     *
     * @param owningPlugin                Plugin constructing the manager. Used when registering commands to the command map,
     *                                    registering event listeners, etc.
     * @param commandExecutionCoordinator Execution coordinator instance. Due to Bukkit blocking the main thread for
     *                                    suggestion requests, it's potentially unsafe to use anything other than
     *                                    {@link ExecutionCoordinator#nonSchedulingExecutor()} for
     *                                    {@link ExecutionCoordinator.Builder#suggestionsExecutor(Executor)}. Once the
     *                                    coordinator, a suggestion provider, parser, or similar routes suggestion logic
     *                                    off of the calling (main) thread, it won't be possible to schedule further logic
     *                                    back to the main thread without a deadlock. When Brigadier support is active, this issue
     *                                    is avoided, as it allows for non-blocking suggestions.
     *                                    Paper's asynchronous completion API can also
     *                                    be used to avoid this issue: {@link #registerAsynchronousCompletions()}
     * @param senderMapper                Mapper between Bukkit's {@link CommandSender} and the command sender type {@code C}.
     * @see #registerBrigadier()
     * @throws InitializationException if construction of the manager fails
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public PaperCommandManager(
            final @NonNull Plugin owningPlugin,
            final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
            final @NonNull SenderMapper<CommandSender, C> senderMapper
    ) throws InitializationException {
        super(owningPlugin, commandExecutionCoordinator, senderMapper);

        this.registerCommandPreProcessor(new PaperCommandPreprocessor<>(this));
    }

    /**
     * Create a command manager using Bukkit's {@link CommandSender} as the sender type.
     *
     * @param owningPlugin                plugin owning the command manager
     * @param commandExecutionCoordinator execution coordinator instance
     * @return a new command manager
     * @throws InitializationException if the construction of the manager fails
     * @see #PaperCommandManager(Plugin, ExecutionCoordinator, SenderMapper) for a more thorough explanation
     * @since 1.5.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static @NonNull PaperCommandManager<@NonNull CommandSender> createNative(
            final @NonNull Plugin owningPlugin,
            final @NonNull ExecutionCoordinator<CommandSender> commandExecutionCoordinator
    ) throws InitializationException {
        return new PaperCommandManager<>(
                owningPlugin,
                commandExecutionCoordinator,
                SenderMapper.identity()
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
     * @see #hasCapability(CloudCapability)
     * @throws BrigadierInitializationException when the prerequisite capabilities are not present or some other issue occurs
     * during registration of Brigadier support
     */
    @Override
    public void registerBrigadier() throws BrigadierInitializationException {
        this.requireState(RegistrationState.BEFORE_REGISTRATION);
        this.checkBrigadierCompatibility();
        if (!this.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            super.registerBrigadier();
        } else {
            try {
                this.paperBrigadierListener = new PaperBrigadierListener<>(this);
                Bukkit.getPluginManager().registerEvents(
                        this.paperBrigadierListener,
                        this.owningPlugin()
                );
                this.paperBrigadierListener.brigadierManager().settings().set(BrigadierSetting.FORCE_EXECUTABLE, true);
            } catch (final Exception e) {
                throw new BrigadierInitializationException(
                        "Failed to register " + PaperBrigadierListener.class.getSimpleName(), e);
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
     * Registers asynchronous completions using the Paper API. This means the calling thread for suggestion queries will be a
     * thread other than the {@link Server#isPrimaryThread() main server thread} (or, the sender's thread context on Folia).
     *
     * <p>Requires the {@link CloudBukkitCapabilities#ASYNCHRONOUS_COMPLETION} capability to be present.</p>
     *
     * <p>It's not recommended to use this in combination with {@link #registerBrigadier()}, as Brigadier allows for
     * non-blocking suggestions and the async completion API reduces the fidelity of suggestions compared to using Brigadier
     * directly (see {@link PaperCommandManager#PaperCommandManager(Plugin, ExecutionCoordinator, SenderMapper)}).</p>
     *
     * @throws IllegalStateException when the server does not support asynchronous completions
     * @see #hasCapability(CloudCapability)
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
                this.owningPlugin()
        );
    }
}
