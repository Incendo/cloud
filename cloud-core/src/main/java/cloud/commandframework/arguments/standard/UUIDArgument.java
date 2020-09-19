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
import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.arguments.parser.ArgumentParseResult;
import com.intellectualsites.commands.arguments.parser.ArgumentParser;
import com.intellectualsites.commands.context.CommandContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public final class UUIDArgument<C> extends CommandArgument<C, UUID> {

    private UUIDArgument(final boolean required,
                         @Nonnull final String name,
                         final String defaultValue,
                         @Nonnull final BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider) {
        super(required, name, new UUIDParser<>(suggestionsProvider), defaultValue, UUID.class, suggestionsProvider);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the component
     * @param <C>  Command sender type
     * @return Created builder
     */
    @Nonnull
    public static <C> Builder<C> newBuilder(@Nonnull final String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    @Nonnull
    public static <C> CommandArgument<C, UUID> required(@Nonnull final String name) {
        return UUIDArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    @Nonnull
    public static <C> CommandArgument<C, UUID> optional(@Nonnull final String name) {
        return UUIDArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new required command component with a default value
     *
     * @param name       Component name
     * @param defaultUUID Default uuid
     * @param <C>        Command sender type
     * @return Created component
     */
    @Nonnull
    public static <C> CommandArgument<C, UUID> optional(@Nonnull final String name,
                                                        final UUID defaultUUID) {
        return UUIDArgument.<C>newBuilder(name).asOptionalWithDefault(defaultUUID.toString()).build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, UUID> {

        protected Builder(@Nonnull final String name) {
            super(UUID.class, name);
        }

        /**
         * Builder a new example component
         *
         * @return Constructed component
         */
        @Nonnull
        @Override
        public UUIDArgument<C> build() {
            return new UUIDArgument<>(this.isRequired(), this.getName(), this.getDefaultValue(), this.getSuggestionsProvider());
        }

    }


    private static final class UUIDParser<C> implements ArgumentParser<C, UUID> {

        private final BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider;

        public UUIDParser(@Nonnull final BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider) {
            this.suggestionsProvider = suggestionsProvider;
        }

        @Nonnull
        @Override
        public ArgumentParseResult<UUID> parse(
                @Nonnull final CommandContext<C> commandContext,
                @Nonnull final Queue<String> inputQueue) {
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

        @Nonnull
        @Override
        public List<String> suggestions(@Nonnull final CommandContext<C> commandContext,
                                        @Nonnull final String input) {
            return this.suggestionsProvider.apply(commandContext, input);
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
        public UUIDParseException(@Nonnull final String input) {
            super(input);
        }

    }
}
