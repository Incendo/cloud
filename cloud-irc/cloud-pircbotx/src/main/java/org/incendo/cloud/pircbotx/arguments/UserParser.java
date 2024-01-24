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
package org.incendo.cloud.pircbotx.arguments;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.pircbotx.PircBotXCommandManager;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.exception.DaoException;

/**
 * {@link ArgumentParser Argument parser} that parses PircBotX {@link User users}
 *
 * @param <C> command sender type
 */
public final class UserParser<C> implements ArgumentParser<C, User> {

    /**
     * Creates a new server parser.
     *
     * @param <C> command sender type
     * @return the created parser
     */
    @API(status = API.Status.STABLE)
    public static <C> @NonNull ParserDescriptor<C, User> userParser() {
        return ParserDescriptor.of(new UserParser<>(), User.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #userParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     */
    @API(status = API.Status.STABLE)
    public static <C> CommandComponent.@NonNull Builder<C, User> userComponent() {
        return CommandComponent.<C, User>builder().parser(userParser());
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull User> parse(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.readString();
        final PircBotX pircBotX = commandContext.get(PircBotXCommandManager.PIRCBOTX_META_KEY);
        final User user;
        try {
            user = pircBotX.getUserChannelDao().getUser(input);
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

    public static final class UserParseException extends ParserException {


        private UserParseException(
                final @NonNull CommandContext<?> context,
                final @NonNull String input
        ) {
            super(
                    UserParser.class,
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
