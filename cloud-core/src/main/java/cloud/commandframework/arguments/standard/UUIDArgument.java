//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
package cloud.commandframework.arguments.standard;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public final class UUIDArgument<C> extends CommandArgument<C, UUID> {

    private UUIDArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<@NonNull CommandContext<C>,
                    @NonNull String, @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new UUIDParser<>(), defaultValue, UUID.class, suggestionsProvider, defaultDescription);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the component
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> @NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, UUID> of(final @NonNull String name) {
        return UUIDArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, UUID> optional(final @NonNull String name) {
        return UUIDArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new required command component with a default value
     *
     * @param name        Component name
     * @param defaultUUID Default uuid
     * @param <C>         Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, UUID> optional(
            final @NonNull String name,
            final @NonNull UUID defaultUUID
    ) {
        return UUIDArgument.<C>newBuilder(name).asOptionalWithDefault(defaultUUID.toString()).build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, UUID> {

        private Builder(final @NonNull String name) {
            super(UUID.class, name);
        }

        /**
         * Builder a new example component
         *
         * @return Constructed component
         */
        @Override
        public @NonNull UUIDArgument<C> build() {
            return new UUIDArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

    }


    public static final class UUIDParser<C> implements ArgumentParser<C, UUID> {

        @Override
        public @NonNull ArgumentParseResult<UUID> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        UUIDParser.class,
                        commandContext
                ));
            }

            try {
                UUID uuid = UUID.fromString(input);
                inputQueue.remove();
                return ArgumentParseResult.success(uuid);
            } catch (IllegalArgumentException e) {
                return ArgumentParseResult.failure(new UUIDParseException(input, commandContext));
            }
        }

        @Override
        public boolean isContextFree() {
            return true;
        }

    }


    public static final class UUIDParseException extends ParserException {

        private static final long serialVersionUID = 6399602590976540023L;
        private final String input;

        /**
         * Construct a new UUID parse exception
         *
         * @param input   String input
         * @param context Command context
         */
        public UUIDParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    UUIDParser.class,
                    context,
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_UUID,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Get the supplied input
         *
         * @return String value
         */
        public String getInput() {
            return input;
        }

    }

}
