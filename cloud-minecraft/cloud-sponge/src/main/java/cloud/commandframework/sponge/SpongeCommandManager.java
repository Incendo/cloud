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
import cloud.commandframework.sponge.argument.MultipleEntitySelectorArgument;
import cloud.commandframework.sponge.argument.MultiplePlayerSelectorArgument;
import cloud.commandframework.sponge.argument.NamedTextColorArgument;
import cloud.commandframework.sponge.argument.OperatorArgument;
import cloud.commandframework.sponge.argument.RegistryEntryArgument;
import cloud.commandframework.sponge.argument.SingleEntitySelectorArgument;
import cloud.commandframework.sponge.argument.SinglePlayerSelectorArgument;
import cloud.commandframework.sponge.argument.WorldArgument;
import cloud.commandframework.sponge.data.MultipleEntitySelector;
import cloud.commandframework.sponge.data.MultiplePlayerSelector;
import cloud.commandframework.sponge.data.SingleEntitySelector;
import cloud.commandframework.sponge.data.SinglePlayerSelector;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Module;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.parameter.managed.operator.Operator;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.PluginContainer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;
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
    private final SpongeParserMapper<C> parserMapper;

    /**
     * Create a new command manager instance
     *
     * @param pluginContainer             Owning plugin
     * @param commandExecutionCoordinator Execution coordinator instance
     * @param causeMapper                 Function mapping the custom command sender type to a Sponge CommandCause
     * @param backwardsCauseMapper        Function mapping Sponge CommandCause to the custom command sender type
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
        ((SpongeRegistrationHandler<C>) this.getCommandRegistrationHandler()).initialize(this);
        this.causeMapper = causeMapper;
        this.backwardsCauseMapper = backwardsCauseMapper;
        this.parserMapper = new SpongeParserMapper<>();
        this.registerCommandPreProcessor(new SpongeCommandPreprocessor<>(this));
        this.registerParsers();
        Sponge.eventManager().registerListeners(this.pluginContainer, this.getCommandRegistrationHandler());
    }

    private void registerParsers() {
        this.getParserRegistry().registerParserSupplier(
                TypeToken.get(NamedTextColor.class),
                params -> new NamedTextColorArgument.Parser<>()
        );
        this.getParserRegistry().registerParserSupplier(
                TypeToken.get(Operator.class),
                params -> new OperatorArgument.Parser<>()
        );
        this.getParserRegistry().registerParserSupplier(
                TypeToken.get(ServerWorld.class),
                params -> new WorldArgument.Parser<>()
        );

        // Entity selectors
        this.getParserRegistry().registerParserSupplier(
                TypeToken.get(SinglePlayerSelector.class),
                params -> new SinglePlayerSelectorArgument.Parser<>()
        );
        this.getParserRegistry().registerParserSupplier(
                TypeToken.get(MultiplePlayerSelector.class),
                params -> new MultiplePlayerSelectorArgument.Parser<>()
        );
        this.getParserRegistry().registerParserSupplier(
                TypeToken.get(SingleEntitySelector.class),
                params -> new SingleEntitySelectorArgument.Parser<>()
        );
        this.getParserRegistry().registerParserSupplier(
                TypeToken.get(MultipleEntitySelector.class),
                params -> new MultipleEntitySelectorArgument.Parser<>()
        );

        this.registerRegistryParsers();
    }

    private void registerRegistryParsers() {
        final Set<RegistryType<?>> ignoredRegistryTypes = ImmutableSet.of(
                RegistryTypes.OPERATOR // We have a different Operator parser that doesn't use a ResourceKey as input
        );
        for (final Field field : RegistryTypes.class.getDeclaredFields()) {
            final Type generic = field.getGenericType(); /* RegistryType<?> */
            if (!(generic instanceof ParameterizedType)) {
                continue;
            }

            final RegistryType<?> key;
            try {
                key = (RegistryType<?>) field.get(null);
            } catch (final IllegalAccessException ex) {
                throw new RuntimeException("Failed to access RegistryTypes." + field.getName(), ex);
            }
            if (ignoredRegistryTypes.contains(key) || !(key instanceof DefaultedRegistryType)) {
                continue;
            }
            final DefaultedRegistryType<?> registryType = (DefaultedRegistryType<?>) key;
            final Type valueType = ((ParameterizedType) generic).getActualTypeArguments()[0];

            this.getParserRegistry().registerParserSupplier(
                    TypeToken.get(valueType),
                    params -> new RegistryEntryArgument.Parser<>(registryType)
            );
        }
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
     * Get the {@link PluginContainer} of the plugin that owns this command manager.
     *
     * @return plugin container
     */
    public @NonNull PluginContainer owningPluginContainer() {
        return this.pluginContainer;
    }

    /**
     * Get the {@link SpongeParserMapper}, responsible for mapping Cloud
     * {@link cloud.commandframework.arguments.CommandArgument CommandArguments} to Sponge
     * {@link org.spongepowered.api.command.registrar.tree.CommandTreeNode.Argument CommandTreeNode.Arguments}.
     *
     * @return the parser mapper
     */
    public @NonNull SpongeParserMapper<C> parserMapper() {
        return this.parserMapper;
    }

    /**
     * Get the {@link Function} used to map {@link C command senders} to {@link CommandCause CommandCauses}.
     *
     * @return command cause mapper
     */
    public @NonNull Function<@NonNull C, @NonNull CommandCause> causeMapper() {
        return this.causeMapper;
    }

    /**
     * Get the {@link Function} used to map {@link CommandCause CommandCauses} to {@link C command senders}.
     *
     * @return backwards command cause mapper
     */
    public @NonNull Function<@NonNull CommandCause, @NonNull C> backwardsCauseMapper() {
        return this.backwardsCauseMapper;
    }

    void registrationCalled() {
        this.lockRegistration();
    }

}
