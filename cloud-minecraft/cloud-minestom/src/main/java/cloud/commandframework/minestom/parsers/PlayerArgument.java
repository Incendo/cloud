package cloud.commandframework.minestom.parsers;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import cloud.commandframework.minestom.caption.MinestomCaptionKeys;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Argument that parses into a {@link Player}
 *
 * @param <C> Command sender type
 */
@SuppressWarnings("unused")
public final class PlayerArgument<C> extends CommandArgument<C, Player> {

    private PlayerArgument(
            final boolean required,
            final @NotNull String name,
            final @NotNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String,
                                List<String>> suggestionsProvider
    ) {
        super(required, name, new PlayerParser<>(), defaultValue, Player.class, suggestionsProvider);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the component
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> Builder<C> newBuilder(final @NotNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> CommandArgument<C, Player> of(final @NotNull String name) {
        return PlayerArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> CommandArgument<C, Player> optional(final @NotNull String name) {
        return PlayerArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new required command component with a default value
     *
     * @param name          Component name
     * @param defaultPlayer Default player
     * @param <C>           Command sender type
     * @return Created component
     */
    public static <C> CommandArgument<C, Player> optional(
            final @NotNull String name,
            final @NotNull String defaultPlayer
    ) {
        return PlayerArgument.<C>newBuilder(name).asOptionalWithDefault(defaultPlayer).build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, Player> {

        private Builder(final @NotNull String name) {
            super(Player.class, name);
        }

        @Override
        public @NotNull PlayerArgument<C> build() {
            return new PlayerArgument<>(this.isRequired(), this.getName(), this.getDefaultValue(), this.getSuggestionsProvider());
        }

    }


    public static final class PlayerParser<C> implements ArgumentParser<C, Player> {

        @Override
        public @NotNull ArgumentParseResult<Player> parse(
                final @NotNull CommandContext<C> commandContext,
                final @NotNull Queue<String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        PlayerParser.class,
                        commandContext
                ));
            }
            inputQueue.remove();

            Player player = MinecraftServer.getConnectionManager().getPlayer(input);

            if (player == null) {
                return ArgumentParseResult.failure(new PlayerParseException(input, commandContext));
            }

            return ArgumentParseResult.success(player);
        }

        @Override
        public @NotNull List<String> suggestions(
                final @NotNull CommandContext<C> commandContext,
                final @NotNull String input
        ) {
            List<String> output = new ArrayList<>();

            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                output.add(player.getUsername());
            }

            return output;
        }

    }


    /**
     * Player parse exception
     */
    public static final class PlayerParseException extends ParserException {

        private static final long serialVersionUID = 2044681000582394398L;
        private final String input;

        /**
         * Construct a new Player parse exception
         *
         * @param input   String input
         * @param context Command context
         */
        public PlayerParseException(
                final @NotNull String input,
                final @NotNull CommandContext<?> context
        ) {
            super(
                    PlayerParser.class,
                    context,
                    MinestomCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Get the supplied input
         *
         * @return String value
         */
        public @NotNull String getInput() {
            return input;
        }

    }

}
