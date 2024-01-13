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
package cloud.commandframework.jda.parsers;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Command Argument for {@link User}
 *
 * @param <C> Command sender type
 * @since 2.0.0
 */
@SuppressWarnings("unused")
public final class UserParser<C> implements ArgumentParser<C, User> {

    /**
     * Creates a new server parser.
     *
     * @param <C> command sender type
     * @param modes parser modes to use
     * @param isolationLevel isolation level to allow
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, User> userParser(
            final @NonNull Set<ParserMode> modes,
            final @NonNull Isolation isolationLevel
    ) {
        return ParserDescriptor.of(new UserParser<>(modes, isolationLevel), User.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #userParser} as the parser.
     *
     * @param <C> the command sender type
     * @param modes parser modes to use
     * @param isolationLevel isolation level to allow
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, User> userComponent(
            final @NonNull Set<ParserMode> modes,
            final @NonNull Isolation isolationLevel
    ) {
        return CommandComponent.<C, User>builder().parser(userParser(modes, isolationLevel));
    }

    private final Set<ParserMode> modes;
    private final Isolation isolationLevel;

    /**
     * Construct a new user parser.
     *
     * @param modes parser modes to use
     * @param isolationLevel isolation level to allow
     */
    public UserParser(
            final @NonNull Set<ParserMode> modes,
            final @NonNull Isolation isolationLevel
    ) {
        this.modes = modes;
        this.isolationLevel = isolationLevel;
    }

    /**
     * Get the modes enabled on the parser
     *
     * @return Set of Modes
     */
    public @NotNull Set<ParserMode> getModes() {
        return this.modes;
    }


    public enum ParserMode {
        MENTION,
        ID,
        NAME
    }

    public enum Isolation {
        GLOBAL,
        GUILD
    }

    @Override
    public @NonNull ArgumentParseResult<User> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.peekString();

        if (!commandContext.contains("MessageReceivedEvent")) {
            return ArgumentParseResult.failure(new IllegalStateException(
                    "MessageReceivedEvent was not in the command context."
            ));
        }

        final MessageReceivedEvent event = commandContext.get("MessageReceivedEvent");
        Exception exception = null;

        if (this.modes.contains(ParserMode.MENTION)) {
            if (input.startsWith("<@") && input.endsWith(">")) {
                final String id;
                if (input.startsWith("<@!")) {
                    id = input.substring(3, input.length() - 1);
                } else {
                    id = input.substring(2, input.length() - 1);
                }

                try {
                    final ArgumentParseResult<User> result = this.userFromId(event, input, id);
                    commandInput.readString();
                    return result;
                } catch (final UserNotFoundParseException | NumberFormatException e) {
                    exception = e;
                }
            } else {
                exception = new IllegalArgumentException(
                        String.format("Input '%s' is not a user mention.", input)
                );
            }
        }

        if (this.modes.contains(ParserMode.ID)) {
            try {
                final ArgumentParseResult<User> result = this.userFromId(event, input, input);
                commandInput.readString();
                return result;
            } catch (final UserNotFoundParseException | NumberFormatException e) {
                exception = e;
            }
        }

        if (this.modes.contains(ParserMode.NAME)) {
            final List<User> users;

            if (this.isolationLevel == Isolation.GLOBAL) {
                users = event.getJDA().getUsersByName(input, true);
            } else if (event.isFromGuild()) {
                users = event.getGuild().getMembersByEffectiveName(input, true)
                        .stream().map(Member::getUser)
                        .collect(Collectors.toList());
            } else if (event.getAuthor().getName().equalsIgnoreCase(input)) {
                users = Collections.singletonList(event.getAuthor());
            } else {
                users = Collections.emptyList();
            }

            if (users.isEmpty()) {
                exception = new UserNotFoundParseException(input);
            } else if (users.size() > 1) {
                exception = new TooManyUsersFoundParseException(input);
            } else {
                commandInput.readString();
                return ArgumentParseResult.success(users.get(0));
            }
        }

        assert exception != null;
        return ArgumentParseResult.failure(exception);
    }

    private @NonNull ArgumentParseResult<User> userFromId(
            final @NonNull MessageReceivedEvent event,
            final @NonNull String input,
            final @NonNull String id
    )
            throws UserNotFoundParseException, NumberFormatException {
        final User user;
        if (this.isolationLevel == Isolation.GLOBAL) {
            user = event.getJDA().getUserById(id);
        } else if (event.isFromGuild()) {
            Member member = event.getGuild().getMemberById(id);

            user = member != null ? member.getUser() : null;
        } else if (event.getAuthor().getId().equalsIgnoreCase(id)) {
            user = event.getAuthor();
        } else {
            user = null;
        }

        if (user == null) {
            throw new UserNotFoundParseException(input);
        } else {
            return ArgumentParseResult.success(user);
        }
    }


    public static class UserParseException extends IllegalArgumentException {

        private final String input;

        /**
         * Construct a new user parse exception
         *
         * @param input String input
         */
        public UserParseException(final @NonNull String input) {
            this.input = input;
        }

        /**
         * Get the users input
         *
         * @return Users input
         */
        public final @NonNull String input() {
            return this.input;
        }
    }


    public static final class TooManyUsersFoundParseException extends UserParseException {


        /**
         * Construct a new user parse exception
         *
         * @param input String input
         */
        public TooManyUsersFoundParseException(final @NonNull String input) {
            super(input);
        }

        @Override
        public @NonNull String getMessage() {
            return String.format("Too many users found for '%s'.", input());
        }
    }


    public static final class UserNotFoundParseException extends UserParseException {


        /**
         * Construct a new user parse exception
         *
         * @param input String input
         */
        public UserNotFoundParseException(final @NonNull String input) {
            super(input);
        }

        @Override
        public @NonNull String getMessage() {
            return String.format("User not found for '%s'.", input());
        }
    }
}
