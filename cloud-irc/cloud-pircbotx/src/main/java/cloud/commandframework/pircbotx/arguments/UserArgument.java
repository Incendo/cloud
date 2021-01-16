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
package cloud.commandframework.pircbotx.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.pircbotx.PircBotXCommandManager;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.exception.DaoException;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

/**
 * {@link CommandArgument Command argument} that parses PircBotX {@link User users}
 *
 * @param <C> Command sender type
 * @since 1.1.0
 */
public final class UserArgument<C> extends CommandArgument<C, User> {

    private UserArgument(
            final boolean required,
            final @NonNull String name,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider
    ) {
        super(
                required,
                name,
                new UserArgumentParser<>(),
                "",
                TypeToken.get(User.class),
                suggestionsProvider,
                new LinkedList<>()
        );
    }

    /**
     * Create a new user argument builder
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Builder instance
     */
    public static <C> @NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required user argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Argument instance
     */
    public static <C> @NonNull CommandArgument<C, User> of(final @NonNull String name) {
        return UserArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a optional user argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Argument instance
     */
    public static <C> @NonNull CommandArgument<C, User> optional(final @NonNull String name) {
        return UserArgument.<C>newBuilder(name).asOptional().build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, User> {

        private Builder(
                final @NonNull String name
        ) {
            super(
                    TypeToken.get(User.class),
                    name
            );
        }

        @Override
        public @NonNull CommandArgument<@NonNull C, @NonNull User> build() {
            return new UserArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getSuggestionsProvider()
            );
        }

    }


    public static final class UserArgumentParser<C> implements ArgumentParser<C, User> {

        @Override
        public @NonNull ArgumentParseResult<@NonNull User> parse(
                final @NonNull CommandContext<@NonNull C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        UserArgumentParser.class,
                        commandContext
                ));
            }
            final PircBotX pircBotX = commandContext.get(PircBotXCommandManager.PIRCBOTX_META_KEY);
            final User user;
            try {
                user = pircBotX.getUserChannelDao().getUser(input);
                inputQueue.remove();
            } catch (final DaoException exception) {
                return ArgumentParseResult.failure(
                        new UserParseException(
                                commandContext,
                                input
                        )
                );
            }
            return ArgumentParseResult.success(user);
        }

    }


    public static final class UserParseException extends ParserException {

        private static final long serialVersionUID = -1758590697299611905L;

        private UserParseException(
                final @NonNull CommandContext<?> context,
                final @NonNull String input
        ) {
            super(
                    UserArgumentParser.class,
                    context,
                    PircBotXCommandManager.ARGUMENT_PARSE_FAILURE_USER_KEY,
                    CaptionVariable.of(
                            "input",
                            input
                    )
            );
        }

    }

}
