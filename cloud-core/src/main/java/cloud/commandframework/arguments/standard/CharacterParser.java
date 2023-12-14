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
package cloud.commandframework.arguments.standard;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public final class CharacterParser<C> implements ArgumentParser<C, Character>, SuggestionProvider.Empty<C> {

    /**
     * Creates a new character parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Character> characterParser() {
        return ParserDescriptor.of(new CharacterParser<>(), Character.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #characterParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Character> characterComponent() {
        return CommandComponent.<C, Character>builder().parser(characterParser());
    }

    @Override
    public @NonNull ArgumentParseResult<Character> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        if (commandInput.peekString().isEmpty()) {
            return ArgumentParseResult.failure(new NoInputProvidedException(
                    CharacterParser.class,
                    commandContext
            ));
        } else if (commandInput.peekString().length() != 1) {
            return ArgumentParseResult.failure(new CharParseException(commandInput.peekString(), commandContext));
        }

        return ArgumentParseResult.success(commandInput.read());
    }

    @Override
    public boolean isContextFree() {
        return true;
    }


    /**
     * Char parse exception
     */
    @API(status = API.Status.STABLE)
    public static final class CharParseException extends ParserException {

        private static final long serialVersionUID = 6458851071584278854L;
        private final String input;

        /**
         * Construct a new Char parse exception
         *
         * @param input   String input
         * @param context Command context
         */
        public CharParseException(final @NonNull String input, final @NonNull CommandContext<?> context) {
            super(
                    CharacterParser.class,
                    context,
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_CHAR,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Get the supplied input
         *
         * @return Input value
         */
        public @NonNull String getInput() {
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
            final CharParseException that = (CharParseException) o;
            return this.input.equals(that.input);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.input);
        }
    }
}
