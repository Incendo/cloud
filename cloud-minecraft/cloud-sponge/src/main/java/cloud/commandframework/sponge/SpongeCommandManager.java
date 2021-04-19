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

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import com.google.inject.Inject;
import com.google.inject.Module;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.plugin.PluginContainer;

import java.util.function.Function;

/**
 * Command manager for Sponge API v8.
 * <p>
 * The manager supports Guice injection
 * as long as the {@link CloudInjectionModule} is present in the injector.
 * This can be achieved by using {@link com.google.inject.Injector#createChildInjector(Module...)}
 *
 * @param <C> Command sender type
 */
public final class SpongeCommandManager<C> extends CommandManager<C> {

    private final PluginContainer pluginContainer;
    private final Function<C, CommandCause> causeMapper;
    private final Function<CommandCause, C> backwardsCauseMapper;

    /**
     * Create a new command manager instance
     *
     * @param pluginContainer             Owning plugin
     * @param commandExecutionCoordinator Execution coordinator instance
     * @param causeMapper                 Function mapping the custom command sender type to a Sponge subject
     * @param backwardsCauseMapper        Function mapping Sponge subjects to the custom command sender type
     */
    @SuppressWarnings("unchecked")
    @Inject
    public SpongeCommandManager(
            final @NonNull PluginContainer pluginContainer,
            final @NonNull Function<@NonNull CommandTree<C>, @NonNull CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @NonNull Function<@NonNull C, @NonNull CommandCause> causeMapper,
            final @NonNull Function<@NonNull CommandCause, @NonNull C> backwardsCauseMapper
    ) {
        super(commandExecutionCoordinator, new SpongeRegistrationHandler<C>());
        this.pluginContainer = pluginContainer;
        ((SpongeRegistrationHandler<C>) this.getCommandRegistrationHandler()).initialize(this, Sponge.systemSubject());
        this.causeMapper = causeMapper;
        this.backwardsCauseMapper = backwardsCauseMapper;
        Sponge.eventManager().registerListeners(this.pluginContainer, this.getCommandRegistrationHandler());
    }

    @Override
    public boolean hasPermission(
            @NonNull final C sender,
            @NonNull final String permission
    ) {
        return this.causeMapper.apply(sender).hasPermission(permission);
    }

    @Override
    public @NonNull CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

    /**
     * Get the plugin that owns this command manager instance
     *
     * @return Owning plugin
     */
    public @NonNull PluginContainer getOwningPlugin() {
        return this.pluginContainer;
    }

    @NonNull Function<@NonNull C, @NonNull CommandCause> causeMapper() {
        return this.causeMapper;
    }

    @NonNull Function<@NonNull CommandCause, @NonNull C> backwardsCauseMapper() {
        return this.backwardsCauseMapper;
    }

}
