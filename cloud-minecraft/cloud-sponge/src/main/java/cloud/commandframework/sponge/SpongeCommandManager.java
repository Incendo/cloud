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
package cloud.commandframework.sponge;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import com.google.inject.Inject;
import com.google.inject.Module;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCatalogRegistryEvent;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.plugin.PluginContainer;

import java.util.function.Function;

/**
 * Command manager for Sponge v8.
 * <p>
 * The manager supports Guice injection
 * as long as the {@link CloudInjectionModule} is present in the injector.
 * This can be achieved by using {@link com.google.inject.Injector#createChildInjector(Module...)}
 *
 * @param <C> Command sender type
 */
public class SpongeCommandManager<C> extends CommandManager<C> {

    private final PluginContainer pluginContainer;
    private final Function<C, Subject> subjectMapper;
    private final Function<Subject, C> backwardsSubjectMapper;

    /**
     * Create a new command manager instance
     *
     * @param pluginContainer             Owning plugin
     * @param commandExecutionCoordinator Execution coordinator instance
     * @param subjectMapper               Function mapping the custom command sender type to a Sponge subject
     * @param backwardsSubjectMapper      Function mapping Sponge subjects to the custom command sender type
     */
    @Inject
    public SpongeCommandManager(
            final @NonNull PluginContainer pluginContainer,
            final @NonNull Function<@NonNull CommandTree<C>, @NonNull CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @NonNull Function<@NonNull C, @NonNull Subject> subjectMapper,
            final @NonNull Function<@NonNull Subject, @NonNull C> backwardsSubjectMapper
    ) {
        super(commandExecutionCoordinator, new SpongeRegistrationHandler());
        this.pluginContainer = pluginContainer;
        this.subjectMapper = subjectMapper;
        this.backwardsSubjectMapper = backwardsSubjectMapper;
        Sponge.getEventManager().registerListeners(this.pluginContainer, this);
    }

    @Override
    public final boolean hasPermission(
            @NonNull final C sender,
            @NonNull final String permission
    ) {
        return this.subjectMapper.apply(sender).hasPermission(permission);
    }

    @Override
    public final @NonNull CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

    /**
     * Get the plugin that owns this command manager instance
     *
     * @return Owning plugin
     */
    public final @NonNull PluginContainer getOwningPlugin() {
        return this.pluginContainer;
    }

    @NonNull
    final Function<@NonNull Subject, @NonNull C> getBackwardsSubjectMapper() {
        return this.backwardsSubjectMapper;
    }

    @Listener
    private void onRegistryEvent(final RegisterCatalogRegistryEvent event) {
        event.register(
                CloudCommandRegistrar.class,
                this.getResourceKey()
        );
    }

    @NonNull
    final ResourceKey getResourceKey() {
        return ResourceKey.builder().namespace(this.pluginContainer).value("cloud-command-manager").build();
    }

}
