package cloud.commandframework.minestom;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.minestom.caption.MinestomCaptionRegistry;
import cloud.commandframework.minestom.parsers.EntityTypeArgument;
import cloud.commandframework.minestom.parsers.PlayerArgument;
import io.leangen.geantyref.TypeToken;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Command manager for the Minestom platform
 *
 * @param <C> Command sender type
 */
public class MinestomCommandManager<C> extends CommandManager<C> {

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
     */
    public MinestomCommandManager(
            Function<CommandTree<C>, CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @NotNull Function<CommandSender, C> commandSenderMapper,
            final @NotNull Function<C, CommandSender> backwardsCommandSenderMapper
    ) {
        super(commandExecutionCoordinator, new MinestomCommandRegistrationHandler<>());

        MinestomCommandRegistrationHandler<C> registrationHandler = (MinestomCommandRegistrationHandler<C>) commandRegistrationHandler();
        registrationHandler.initialize(this);

        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;

        captionRegistry(new MinestomCaptionRegistry<>());

        setupParsers();
    }

    @NotNull
    public C mapCommandSender(CommandSender sender) {
        return commandSenderMapper.apply(sender);
    }

    @NotNull
    public CommandSender backwardsMapCommandSender(C sender) {
        return backwardsCommandSenderMapper.apply(sender);
    }

    @Override
    public boolean hasPermission(@NotNull C sender, @NotNull String permission) {
        CommandSender minestomSender = backwardsMapCommandSender(sender);
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
