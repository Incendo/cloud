//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public final class UUIDArgument<C> extends CommandArgument<C, UUID> {

    private UUIDArgument(final boolean required,
                         @NonNull final String name,
                         @NonNull final String defaultValue,
                         @Nullable final BiFunction<@NonNull CommandContext<C>,
                                 @NonNull String, @NonNull List<@NonNull String>> suggestionsProvider) {
        super(required, name, new UUIDParser<>(), defaultValue, UUID.class, suggestionsProvider);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the component
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> @NonNull Builder<C> newBuilder(@NonNull final String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, UUID> required(@NonNull final String name) {
        return UUIDArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, UUID> optional(@NonNull final String name) {
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
    public static <C> @NonNull CommandArgument<C, UUID> optional(@NonNull final String name,
                                                                 @NonNull final UUID defaultUUID) {
        return UUIDArgument.<C>newBuilder(name).asOptionalWithDefault(defaultUUID.toString()).build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, UUID> {

        protected Builder(@NonNull final String name) {
            super(UUID.class, name);
        }

        /**
         * Builder a new example component
         *
         * @return Constructed component
         */
        @Override
        public @NonNull UUIDArgument<C> build() {
            return new UUIDArgument<>(this.isRequired(), this.getName(), this.getDefaultValue(), this.getSuggestionsProvider());
        }

    }


    private static final class UUIDParser<C> implements ArgumentParser<C, UUID> {

        @Override
        public @NonNull ArgumentParseResult<UUID> parse(
                @NonNull final CommandContext<C> commandContext,
                @NonNull final Queue<@NonNull String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NullPointerException("No input was provided"));
            }

            try {
                UUID uuid = UUID.fromString(input);
                inputQueue.remove();
                return ArgumentParseResult.success(uuid);
            } catch (IllegalArgumentException e) {
                return ArgumentParseResult.failure(new UUIDParseException(input));
            }
        }

        @Override
        public boolean isContextFree() {
            return true;
        }
    }


    public static final class UUIDParseException extends IllegalArgumentException {

        /**
         * Construct a new example parse exception
         *
         * @param input String input
         */
        public UUIDParseException(@NonNull final String input) {
            super(input);
        }

    }
}
