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
package cloud.commandframework.minestom.arguments;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.minestom.MinestomCaptionKeys;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import net.minestom.server.entity.EntityType;
import net.minestom.server.registry.ProtocolObject;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Argument that parses into a {@link EntityType}
 *
 * @param <C> Command sender type
 * @since 1.9.0
 */
public final class EntityTypeArgument<C> extends CommandArgument<C, EntityType> {

    private EntityTypeArgument(
            final boolean required,
            final @NotNull String name,
            final @NotNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String,
                    @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new EntityTypeParser<>(), defaultValue, EntityType.class, suggestionsProvider, defaultDescription);
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return new {@link Builder}
     */
    @API(status = API.Status.STABLE)
    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, EntityType> of(final @NonNull String name) {
        return EntityTypeArgument.<C>builder(name).asRequired().build();
    }

    /**
     * Create a new optional command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, EntityType> optional(final @NonNull String name) {
        return EntityTypeArgument.<C>builder(name).asOptional().build();
    }

    /**
     * Create a new required command component with a default value
     *
     * @param name              Component name
     * @param defaultEntityType Default entity type
     * @param <C>               Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, EntityType> optional(
            final @NonNull String name,
            final @NonNull String defaultEntityType
    ) {
        return EntityTypeArgument.<C>builder(name).asOptionalWithDefault(defaultEntityType).build();
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, EntityType> {

        private Builder(final @NonNull String name) {
            super(EntityType.class, name);
        }

        /**
         * Builder a new boolean component
         *
         * @return Constructed component
         */
        @Override
        public @NonNull EntityTypeArgument<C> build() {
            return new EntityTypeArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }
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
                    .toList();
        }
    }

    /**
     * EntityType parse exception
     */
    public static final class EntityTypeParseException extends ParserException {

        private static final long serialVersionUID = -6325389672935542997L;
        private final String input;

        /**
         * Construct a new EntityType parse exception
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
                    MinestomCaptionKeys.ARGUMENT_PARSE_FAILURE_ENTITY_TYPE,
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
            return this.input;
        }
    }
}
