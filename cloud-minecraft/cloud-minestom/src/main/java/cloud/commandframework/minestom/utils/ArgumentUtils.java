package cloud.commandframework.minestom.utils;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.arguments.standard.FloatArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.arguments.standard.UUIDArgument;
import cloud.commandframework.minestom.MinestomCommandManager;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
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

public class ArgumentUtils {

    public static <C> Argument<?> createMinestomArgument(CommandArgument<C, ?> arg, MinestomCommandManager<C> manager) {
        Argument<?> minestomArgument = convertArgument(arg);
        addSuggestions(minestomArgument, manager);
        return minestomArgument;
    }

    private static <C> void addSuggestions(Argument<?> argument, MinestomCommandManager<C> manager) {
        argument.setSuggestionCallback(((sender, context, suggestion) -> {
            List<@NonNull String> suggestionList = manager.suggest(
                    manager.mapCommandSender(sender),
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


        if (parser instanceof StringArgument.StringParser<?>) {
            StringArgument.StringParser<?> sp = (StringArgument.StringParser<?>) parser;
            switch (sp.getStringMode()) {
                case SINGLE: {
                    ArgumentWord result = new ArgumentWord((arg.getName()));
                    if (hasDefaultValue) {
                        result.setDefaultValue(defaultValue);
                    }
                    return result;
                }
                case GREEDY: {
                    ArgumentStringArray result = new ArgumentStringArray((arg.getName()));
                    if (hasDefaultValue) {
                        result.setDefaultValue(defaultValue.split(Pattern.quote(" ")));
                    }
                    return result;
                }
                case QUOTED: {
                    ArgumentString result = new ArgumentString((arg.getName()));
                    if (hasDefaultValue) {
                        result.setDefaultValue(defaultValue);
                    }
                    return result;
                }
            }
        }

        if (arg instanceof FloatArgument<?>) {
            final FloatArgument<?> fa = (FloatArgument<?>) arg;
            ArgumentNumber<Float> result = new ArgumentFloat(arg.getName()).min(fa.getMin()).max(fa.getMax());
            if (hasDefaultValue) {
                result.setDefaultValue(Float.parseFloat(defaultValue));
            }
            return result;
        } else if (arg instanceof DoubleArgument<?>) {
            final DoubleArgument<?> da = (DoubleArgument<?>) arg;
            ArgumentNumber<Double> result = new ArgumentDouble(arg.getName()).min(da.getMin()).max(da.getMax());
            if (hasDefaultValue) {
                result.setDefaultValue(Double.parseDouble(defaultValue));
            }
            return result;
        } else if (arg instanceof IntegerArgument<?>) {
            final IntegerArgument<?> ia = (IntegerArgument<?>) arg;
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
