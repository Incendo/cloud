package cloud.commandframework.minestom.utils;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.StaticArgument;
import java.util.ArrayList;
import java.util.List;
import cloud.commandframework.minestom.MinestomCloudCommand;
import cloud.commandframework.minestom.MinestomCommandManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.Argument;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SetupUtils {
    private static final CommandExecutor emptyExecutor = (sender, context) -> {
    };

    @SuppressWarnings("unchecked")
    public static <C> void setup(MinestomCommandManager<C> manager, cloud.commandframework.Command<C> cloudCommand) {
        List<CommandArgument<@NonNull C, ?>> arguments = cloudCommand.getArguments();
        StaticArgument<C> cmdArg = (StaticArgument<C>) arguments.get(0);
        MinestomCloudCommand<C> root = getRootCommand(manager, cmdArg);

        if (cloudCommand.isHidden()) {
            root.setCondition((sender, commandString) -> commandString != null);
        }

        boolean subcommand = true;
        Command parent = root;

        List<Argument<?>> minestomArguments = new ArrayList<>();
        for (int i = 1; i < arguments.size(); i++) {
            CommandArgument<@NonNull C, ?> argument = arguments.get(i);
            if (subcommand && !(argument instanceof StaticArgument<?>)) subcommand = false;

            if (subcommand) {
                parent = getSubcommand(parent, (StaticArgument<C>) argument);
            } else {
                minestomArguments.add(ArgumentUtils.createMinestomArgument(argument, manager));
            }
        }

        parent.addSyntax(emptyExecutor, minestomArguments.toArray(new Argument[0]));
    }

    @SuppressWarnings("unchecked")
    private static <C> MinestomCloudCommand<C> getRootCommand(MinestomCommandManager<C> manager, StaticArgument<C> argument) {
        String name = argument.getName();
        String[] aliases = argument.getAlternativeAliases().toArray(new String[0]);

        Command command = MinecraftServer.getCommandManager().getCommand(name);
        if (command instanceof MinestomCloudCommand<?>) return (MinestomCloudCommand<C>) command;

        command = new MinestomCloudCommand<>(manager, name, aliases);
        MinecraftServer.getCommandManager().register(command);
        return (MinestomCloudCommand<C>) command;
    }

    private static <C> Command getSubcommand(Command parent, StaticArgument<C> argument) {
        String name = argument.getName();
        String[] aliases = argument.getAlternativeAliases().toArray(new String[0]);

        Command command = parent.getSubcommands().stream()
                .filter((it -> it.getName().equals(name)))
                .findFirst().orElse(null);
        if (command != null) return command;

        command = new Command(name, aliases);
        parent.addSubcommand(command);
        return command;
    }
}
