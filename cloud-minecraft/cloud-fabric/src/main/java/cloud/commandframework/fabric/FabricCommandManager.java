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

package cloud.commandframework.fabric;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.brigadier.BrigadierManagerHolder;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

public class FabricCommandManager<C> extends CommandManager<C> implements BrigadierManagerHolder<C> {
    private final Function<ServerCommandSource, C> commandSourceMapper;
    private final Function<C, ServerCommandSource> backwardsCommandSourceMapper;
    private final CloudBrigadierManager<C, ServerCommandSource> brigadierManager;

    /**
     * Create a command manager using native source types.
     *
     * @param execCoordinator Execution coordinator instance.
     * @return a new command manager
     * @see #FabricCommandManager(Function, Function, Function) for a more thorough explanation
     */
    public static FabricCommandManager<ServerCommandSource> createNative(
            final Function<CommandTree<ServerCommandSource>, CommandExecutionCoordinator<ServerCommandSource>> execCoordinator
            ) {
        return new FabricCommandManager<>(execCoordinator, Function.identity(), Function.identity());
    }


    /**
     * Create a new command manager instance.
     *
     * @param commandExecutionCoordinator Execution coordinator instance. The coordinator is in charge of executing incoming
     *                                    commands. Some considerations must be made when picking a suitable execution coordinator
     *                                    for your platform. For example, an entirely asynchronous coordinator is not suitable
     *                                    when the parsers used in that particular platform are not thread safe. If you have
     *                                    commands that perform blocking operations, however, it might not be a good idea to
     *                                    use a synchronous execution coordinator. In most cases you will want to pick between
     *                                    {@link CommandExecutionCoordinator#simpleCoordinator()} and
     *                                    {@link AsynchronousCommandExecutionCoordinator}
     * @param commandSourceMapper          Function that maps {@link ServerCommandSource} to the command sender type
     * @param backwardsCommandSourceMapper Function that maps the command sender type to {@link ServerCommandSource}
     */
    @SuppressWarnings("unchecked")
    protected FabricCommandManager(
            final @NonNull Function<@NonNull CommandTree<C>, @NonNull CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final Function<ServerCommandSource, C> commandSourceMapper,
            final Function<C, ServerCommandSource> backwardsCommandSourceMapper
    ) {
        super(commandExecutionCoordinator, new FabricCommandRegistrationHandler<>());
        this.commandSourceMapper = commandSourceMapper;
        this.backwardsCommandSourceMapper = backwardsCommandSourceMapper;

        // We're always brigadier
        this.brigadierManager = new CloudBrigadierManager<>(this, () -> new CommandContext<>(
                // This looks ugly, but it's what the server does when loading datapack functions in 1.16+
                // See net.minecraft.server.function.FunctionLoader.reload for reference
                this.commandSourceMapper.apply(new ServerCommandSource(
                                CommandOutput.DUMMY,
                                Vec3d.ZERO,
                                Vec2f.ZERO,
                                null,
                                4,
                                "",
                                LiteralText.EMPTY,
                                null,
                                null
                        )),
                        this.getCaptionRegistry()
                ));

        ((FabricCommandRegistrationHandler<C>) this.getCommandRegistrationHandler()).initialize(this);
    }

    /**
     * Check if a sender has a certain permission.
     *
     * <p>The current implementation checks op level, pending a full Fabric permissions api.</p>
     *
     * @param sender     Command sender
     * @param permission Permission node
     * @return whether the sender has the specified permission
     */
    @Override
    public boolean hasPermission(@NonNull final C sender, @NonNull final String permission) {
        final ServerCommandSource source = this.backwardsCommandSourceMapper.apply(sender);
        return source.hasPermissionLevel(source.getMinecraftServer().getOpPermissionLevel());
    }

    @Override
    public final @NonNull CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

    /**
     * Gets the mapper from a game {@link ServerCommandSource} to the manager's {@code C} type.
     *
     * @return Command source mapper
     */
    public final @NonNull Function<@NonNull ServerCommandSource, @NonNull C> getCommandSourceMapper() {
        return this.commandSourceMapper;
    }

    /**
     * Gets the mapper from the manager's {@code C} type to a game {@link ServerCommandSource}.
     *
     * @return Command source mapper
     */
    public final @NonNull Function<@NonNull C, @NonNull ServerCommandSource> getBackwardsCommandSourceMapper() {
        return this.backwardsCommandSourceMapper;
    }

    @Override
    public final @NonNull CloudBrigadierManager<C, ServerCommandSource> brigadierManager() {
        return this.brigadierManager;
    }

    final void registrationCalled() {
        this.transitionOrThrow(RegistrationState.REGISTERING, RegistrationState.AFTER_REGISTRATION);
    }

}
