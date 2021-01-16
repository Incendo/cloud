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
package cloud.commandframework.sponge7;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.plugin.PluginContainer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * A command manager for SpongeAPI 7.
 *
 * @param <C> the command source type
 * @since 1.4.0
 */
@Singleton
public class SpongeCommandManager<C> extends CommandManager<C> {

    private final PluginContainer owningPlugin;
    private final Function<CommandSource, C> forwardMapper;
    private final Function<C, CommandSource> reverseMapper;

    /**
     * Create a new command manager instance.
     *
     * @param container                   The plugin that owns this command manager
     * @param commandExecutionCoordinator Execution coordinator instance. The coordinator is in charge of executing incoming
     *                                    commands. Some considerations must be made when picking a suitable execution coordinator
     *                                    for your platform. For example, an entirely asynchronous coordinator is not suitable
     *                                    when the parsers used in that particular platform are not thread safe. If you have
     *                                    commands that perform blocking operations, however, it might not be a good idea to
     *                                    use a synchronous execution coordinator. In most cases you will want to pick between
     *                                    {@link CommandExecutionCoordinator#simpleCoordinator()} and
     *                                    {@link AsynchronousCommandExecutionCoordinator}
     * @param forwardMapper               A function converting from a native {@link CommandSource} to this manager's sender type
     * @param reverseMapper               A function converting from this manager's sender type to a native {@link CommandSource}
     */
    @Inject
    @SuppressWarnings("unchecked")
    public SpongeCommandManager(
        final @NonNull PluginContainer container,
        final @NonNull Function<CommandTree<C>, CommandExecutionCoordinator<C>> commandExecutionCoordinator,
        final Function<CommandSource, C> forwardMapper,
        final Function<C, CommandSource> reverseMapper
    ) {
        super(commandExecutionCoordinator, new SpongePluginRegistrationHandler<>());
        this.owningPlugin = requireNonNull(container, "container");
        this.forwardMapper = requireNonNull(forwardMapper, "forwardMapper");
        this.reverseMapper = requireNonNull(reverseMapper, "reverseMapper");
        ((SpongePluginRegistrationHandler<C>) this.getCommandRegistrationHandler()).initialize(this);
    }

    @Override
    public final boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        return this.reverseMapper.apply(sender).hasPermission(permission);
    }

    @Override
    public final @NonNull CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

    /**
     * Get a mapper from a Sponge {@link CommandSource} to this manager's command source type.
     *
     * @return the command source mapper
     */
    public @NonNull Function<CommandSource, C> getCommandSourceMapper() {
        return this.forwardMapper;
    }

    /**
     * Get a mapper from this manager's command source type back to Sponge's {@link CommandSource}.
     *
     * @return the command source mapper
     */
    public final @NonNull Function<C, CommandSource> getReverseCommandSourceMapper() {
        return this.reverseMapper;
    }

    final PluginContainer getOwningPlugin() {
        return this.owningPlugin;
    }

}
