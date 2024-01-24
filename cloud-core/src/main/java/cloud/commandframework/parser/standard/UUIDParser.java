//
// MIT License
//
// Copyright (c) 2024 Incendo
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
package cloud.commandframework.parser.standard;

import cloud.commandframework.caption.CaptionVariable;
import cloud.commandframework.caption.StandardCaptionKeys;
import cloud.commandframework.component.CommandComponent;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exception.parsing.ParserException;
import cloud.commandframework.parser.ArgumentParseResult;
import cloud.commandframework.parser.ArgumentParser;
import cloud.commandframework.parser.ParserDescriptor;
import java.util.Objects;
import java.util.UUID;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public final class UUIDParser<C> implements ArgumentParser<C, UUID> {

    /**
     * Creates a new UUID parser.
     *
     * @param <C> command sender type
     * @return the created parser
     */
    @API(status = API.Status.STABLE)
    public static <C> @NonNull ParserDescriptor<C, UUID> uuidParser() {
        return ParserDescriptor.of(new UUIDParser<>(), UUID.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #uuidParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     */
    @API(status = API.Status.STABLE)
    public static <C> CommandComponent.@NonNull Builder<C, UUID> uuidComponent() {
        return CommandComponent.<C, UUID>builder().parser(uuidParser());
    }

    @Override
    public @NonNull ArgumentParseResult<UUID> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.readString();

        try {
            final UUID uuid = UUID.fromString(input);
            return ArgumentParseResult.success(uuid);
        } catch (IllegalArgumentException e) {
            return ArgumentParseResult.failure(new UUIDParseException(input, commandContext));
        }
    }


    @API(status = API.Status.STABLE)
    public static final class UUIDParseException extends ParserException {

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
         * Returns the supplied input.
         *
         * @return string value
         */
        public String input() {
            return this.input;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final UUIDParseException that = (UUIDParseException) o;
            return this.input.equals(that.input);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.input);
        }
    }
}
