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
package cloud.commandframework.minestom;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.minestom.arguments.EntityTypeArgument;
import cloud.commandframework.minestom.arguments.PlayerArgument;
import io.leangen.geantyref.TypeToken;
import java.util.function.Function;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command manager for the Minestom platform
 *
 * @param <C> Command sender type
 */
public final class MinestomCommandManager<C> extends CommandManager<C> {

    @NotNull
    private final Function<CommandSender, C> commandSenderMapper;

    @NotNull
    private final Function<C, CommandSender> backwardsCommandSenderMapper;

    /**
     * Create a new command manager instance
     * <p>
     * The coordinator is in charge of executing incoming commands. Some considerations must be made when picking a
     * suitable execution coordinator for your platform. For example, an entirely asynchronous coordinator is not
     * suitable when the parsers used in that particular platform are not thread safe. If you have commands that
     * perform blocking operations, however, it might not be a good idea to use a synchronous execution coordinator.
     * In most cases you will want to pick between {@link CommandExecutionCoordinator#simpleCoordinator()} and
     * {@link AsynchronousCommandExecutionCoordinator}
     *
     * @param commandExecutionCoordinator Execution coordinator instance.
     * @param commandSenderMapper          Function that maps {@link CommandSender} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link CommandSender}
     */
    @SuppressWarnings("unchecked")
    public MinestomCommandManager(
            final Function<CommandTree<C>, CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @NotNull Function<CommandSender, C> commandSenderMapper,
            final @NotNull Function<C, CommandSender> backwardsCommandSenderMapper
    ) {
        super(commandExecutionCoordinator, new MinestomCommandRegistrationHandler<C>());

        MinestomCommandRegistrationHandler<C> registrationHandler = (MinestomCommandRegistrationHandler<C>) commandRegistrationHandler();
        registrationHandler.initialize(this);

        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;

        captionRegistry(new MinestomCaptionRegistry<>());

        this.setupParsers();
    }

    @NotNull
    C mapCommandSender(final @NotNull CommandSender sender) {
        return this.commandSenderMapper.apply(sender);
    }

    @NotNull
    CommandSender backwardsMapCommandSender(final @NotNull C sender) {
        return this.backwardsCommandSenderMapper.apply(sender);
    }

    @Override
    public boolean hasPermission(final @NotNull C sender, final @NotNull String permission) {
        CommandSender minestomSender = this.backwardsMapCommandSender(sender);
        return minestomSender instanceof ConsoleSender
                || minestomSender.hasPermission(permission)
                || (minestomSender instanceof Player && ((Player) minestomSender).getPermissionLevel() >= 4);
    }

    @Override
    public @NotNull CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.simple().build();
    }

    private void setupParsers() {
        parserRegistry().registerParserSupplier(TypeToken.get(Player.class), parserParameters ->
                new PlayerArgument.PlayerParser<>());

        parserRegistry().registerParserSupplier(TypeToken.get(EntityType.class), parserParameters ->
                new EntityTypeArgument.EntityTypeParser<>());
    }
}
