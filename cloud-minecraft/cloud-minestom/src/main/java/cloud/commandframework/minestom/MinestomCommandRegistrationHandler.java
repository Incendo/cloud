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

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.arguments.standard.FloatArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.arguments.standard.UUIDArgument;
import cloud.commandframework.internal.CommandRegistrationHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentBoolean;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentUUID;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class MinestomCommandRegistrationHandler<C> implements CommandRegistrationHandler {

    private static final CommandExecutor emptyExecutor = (sender, context) -> {
    };

    private MinestomCommandManager<C> commandManager;

    void initialize(final @NotNull MinestomCommandManager<C> commandManager) {
        this.commandManager = commandManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean registerCommand(@NotNull cloud.commandframework.Command<?> cloudCommand) {
        cloud.commandframework.Command<C> command = (cloud.commandframework.Command<C>) cloudCommand;
        List<CommandArgument<@NonNull C, ?>> arguments = command.getArguments();
        StaticArgument<C> cmdArg = (StaticArgument<C>) arguments.get(0);
        MinestomCloudCommand<C> root = getRootCommand(cmdArg);

        if (command.isHidden()) {
            root.setCondition((sender, commandString) -> commandString != null);
        }

        boolean subcommand = true;
        Command parent = root;

        List<Argument<?>> minestomArguments = new ArrayList<>();
        for (int i = 1; i < arguments.size(); i++) {
            CommandArgument<@NonNull C, ?> argument = arguments.get(i);
            if (subcommand && !(argument instanceof StaticArgument<?>)) {
                subcommand = false;
            }

            if (subcommand) {
                parent = getSubcommand(parent, (StaticArgument<C>) argument);
            } else {
                minestomArguments.add(createMinestomArgument(argument));
            }
        }

        parent.addSyntax(emptyExecutor, minestomArguments.toArray(new Argument[0]));
        return true;
    }

    @SuppressWarnings("unchecked")
    private MinestomCloudCommand<C> getRootCommand(StaticArgument<C> argument) {
        String name = argument.getName();
        String[] aliases = argument.getAlternativeAliases().toArray(new String[0]);

        Command command = MinecraftServer.getCommandManager().getCommand(name);
        if (command instanceof MinestomCloudCommand<?>) {
            return (MinestomCloudCommand<C>) command;
        }

        command = new MinestomCloudCommand<>(commandManager, name, aliases);
        MinecraftServer.getCommandManager().register(command);
        return (MinestomCloudCommand<C>) command;
    }

    private Command getSubcommand(Command parent, StaticArgument<C> argument) {
        String name = argument.getName();
        String[] aliases = argument.getAlternativeAliases().toArray(new String[0]);

        Command command = parent.getSubcommands().stream()
                .filter((it -> it.getName().equals(name)))
                .findFirst().orElse(null);
        if (command != null) {
            return command;
        }

        command = new Command(name, aliases);
        parent.addSubcommand(command);
        return command;
    }

    private Argument<?> createMinestomArgument(CommandArgument<C, ?> arg) {
        Argument<?> minestomArgument = convertArgument(arg);
        addSuggestions(minestomArgument);
        return minestomArgument;
    }

    private void addSuggestions(Argument<?> argument) {
        argument.setSuggestionCallback(((sender, context, suggestion) -> {
            List<@NonNull String> suggestionList = commandManager.suggest(
                    commandManager.mapCommandSender(sender),
                    fixInput(context.getInput())
            );
            for (String sug : suggestionList) {
                suggestion.addEntry(new SuggestionEntry(sug));
            }
        }));
    }

    private static String fixInput(String input) {
        if (input.endsWith("\u0000")) {
            return input.substring(0, input.length() - 1);
        }
        return input;
    }

    private static <C> Argument<?> convertArgument(CommandArgument<C, ?> arg) {
        ArgumentParser<C, ?> parser = arg.getParser();

        String defaultValue = arg.getDefaultValue();
        boolean hasDefaultValue = !defaultValue.equals("");


        if (parser instanceof final StringArgument.StringParser<?> sp) {
            switch (sp.getStringMode()) {
                case SINGLE -> {
                    ArgumentWord result = new ArgumentWord((arg.getName()));
                    if (hasDefaultValue) {
                        result.setDefaultValue(defaultValue);
                    }
                    return result;
                }
                case GREEDY -> {
                    ArgumentStringArray result = new ArgumentStringArray((arg.getName()));
                    if (hasDefaultValue) {
                        result.setDefaultValue(defaultValue.split(Pattern.quote(" ")));
                    }
                    return result;
                }
                case QUOTED -> {
                    ArgumentString result = new ArgumentString((arg.getName()));
                    if (hasDefaultValue) {
                        result.setDefaultValue(defaultValue);
                    }
                    return result;
                }
            }
        }

        if (arg instanceof final FloatArgument<?> fa) {
            ArgumentNumber<Float> result = new ArgumentFloat(arg.getName()).min(fa.getMin()).max(fa.getMax());
            if (hasDefaultValue) {
                result.setDefaultValue(Float.parseFloat(defaultValue));
            }
            return result;
        } else if (arg instanceof final DoubleArgument<?> da) {
            ArgumentNumber<Double> result = new ArgumentDouble(arg.getName()).min(da.getMin()).max(da.getMax());
            if (hasDefaultValue) {
                result.setDefaultValue(Double.parseDouble(defaultValue));
            }
            return result;
        } else if (arg instanceof final IntegerArgument<?> ia) {
            ArgumentNumber<Integer> result = new ArgumentInteger(arg.getName()).min(ia.getMax()).max(ia.getMax());
            if (hasDefaultValue) {
                result.setDefaultValue(Integer.parseInt(defaultValue));
            }
            return result;
        } else if (arg instanceof UUIDArgument) {
            ArgumentUUID result = new ArgumentUUID(arg.getName());
            if (hasDefaultValue) {
                result.setDefaultValue(UUID.fromString(defaultValue));
            }
            return result;
        } else if (arg instanceof BooleanArgument) {
            ArgumentBoolean result = new ArgumentBoolean(arg.getName());
            if (hasDefaultValue) {
                result.setDefaultValue(Boolean.parseBoolean(defaultValue));
            }
            return result;
        } else {
            ArgumentWord result = new ArgumentWord(arg.getName());
            if (hasDefaultValue) {
                result.setDefaultValue(defaultValue);
            }
            return result;
        }
    }
}
