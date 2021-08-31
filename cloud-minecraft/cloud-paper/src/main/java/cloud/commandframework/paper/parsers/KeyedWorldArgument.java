package cloud.commandframework.paper.parsers;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.bukkit.parsers.WorldArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

/**
 * cloud argument type that parses Bukkit {@link World worlds} from a namespaced key
 *
 * @param <C> Command sender type
 */
public class KeyedWorldArgument<C> extends CommandArgument<C, World> {

    protected KeyedWorldArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new KeyedWorldParser<>(), defaultValue, World.class, suggestionsProvider, defaultDescription);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> CommandArgument.@NonNull Builder<C, World> newBuilder(final @NonNull String name) {
        return new KeyedWorldArgument.Builder<>(name);
    }

    /**
     * Create a new required argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, World> of(final @NonNull String name) {
        return KeyedWorldArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, World> optional(final @NonNull String name) {
        return KeyedWorldArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new optional argument with a default value
     *
     * @param name         Argument name
     * @param defaultValue Default value
     * @param <C>          Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, World> optional(
            final @NonNull String name,
            final @NonNull String defaultValue
    ) {
        return KeyedWorldArgument.<C>newBuilder(name).asOptionalWithDefault(defaultValue).build();
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, World> {

        private Builder(final @NonNull String name) {
            super(World.class, name);
        }

        @Override
        public @NonNull CommandArgument<@NonNull C, @NonNull World> build() {
            return new KeyedWorldArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

    }

    public static final class KeyedWorldParser<C> implements ArgumentParser<C, World> {

        @Override
        public @NonNull ArgumentParseResult<@NonNull World> parse(
                @NonNull final CommandContext<@NonNull C> commandContext,
                @NonNull final Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        KeyedWorldParser.class,
                        commandContext
                ));
            }

            final NamespacedKey key = NamespacedKey.fromString(input);
            if (key == null) {
                return ArgumentParseResult.failure(new WorldArgument.WorldParseException(input, commandContext));
            }

            final World world = Bukkit.getWorld(key);
            if (world == null) {
                return ArgumentParseResult.failure(new WorldArgument.WorldParseException(input, commandContext));
            }

            inputQueue.remove();
            return ArgumentParseResult.success(world);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            final List<String> completions = new ArrayList<>();
            for (final World world : Bukkit.getWorlds()) {
                if (world.getKey().getNamespace().equals(NamespacedKey.MINECRAFT)) {
                    completions.add(world.getKey().getKey());
                } else {
                    completions.add(world.getKey().toString());
                }
            }
            return completions;
        }

    }
}
