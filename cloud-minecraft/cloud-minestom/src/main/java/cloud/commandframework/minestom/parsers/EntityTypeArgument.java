package cloud.commandframework.minestom.parsers;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import cloud.commandframework.minestom.caption.MinestomCaptionKeys;
import net.minestom.server.entity.EntityType;
import net.minestom.server.registry.ProtocolObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityTypeArgument<C> extends CommandArgument<C, EntityType> {

    private EntityTypeArgument(
            final boolean required,
            final @NotNull String name,
            final @NotNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String,
                    List<String>> suggestionsProvider
    ) {
        super(required, name, new EntityTypeParser<>(), defaultValue, EntityType.class, suggestionsProvider);
    }

    public static final class EntityTypeParser<C> implements ArgumentParser<C, EntityType> {

        @Override
        public @NotNull ArgumentParseResult<EntityType> parse(
                final @NotNull CommandContext<C> commandContext,
                final @NotNull Queue<String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        EntityTypeParser.class,
                        commandContext
                ));
            }
            inputQueue.remove();

            EntityType type = EntityType.fromNamespaceId(input);

            if (type == null) {
                return ArgumentParseResult.failure(new EntityTypeParseException(input, commandContext));
            }

            return ArgumentParseResult.success(type);
        }

        @Override
        public @NotNull List<String> suggestions(
                final @NotNull CommandContext<C> commandContext,
                final @NotNull String input
        ) {
            return EntityType.values().stream()
                    .map(ProtocolObject::name)
                    .map(name -> name.substring(10))
                    .toList();
        }

    }


    /**
     * Player parse exception
     */
    public static final class EntityTypeParseException extends ParserException {

        private static final long serialVersionUID = -6325389672935542997L;
        private final String input;

        /**
         * Construct a new Player parse exception
         *
         * @param input   String input
         * @param context Command context
         */
        public EntityTypeParseException(
                final @NotNull String input,
                final @NotNull CommandContext<?> context
        ) {
            super(
                    EntityTypeParser.class,
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
