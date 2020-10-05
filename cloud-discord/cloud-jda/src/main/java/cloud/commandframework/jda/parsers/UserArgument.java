//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Command Argument for {@link User}
 *
 * @param <C> Command sender type
 */
@SuppressWarnings("unused")
public final class UserArgument<C> extends CommandArgument<C, User> {
    private final List<ParserMode> modes;

    private UserArgument(final boolean required, final @NonNull String name,
                         final @NonNull JDA jda, final @NonNull List<ParserMode> modes) {
        super(required, name, new UserParser<>(jda, modes), User.class);
        this.modes = modes;
    }

    /**
     * Create a new builder
     *
     * @param name Name of the component
     * @param jda  JDA instance
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> @NonNull Builder<C> newBuilder(final @NonNull String name, final @NonNull JDA jda) {
        return new Builder<>(name, jda);
    }

    /**
     * Create a new required command component
     *
     * @param name Component name
     * @param jda  JDA instance
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, User> of(final @NonNull String name, final @NonNull JDA jda) {
        return UserArgument.<C>newBuilder(name, jda).asRequired().build();
    }

    /**
     * Create a new optional command component
     *
     * @param name Component name
     * @param jda  JDA instance
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, User> optional(final @NonNull String name, final @NonNull JDA jda) {
        return UserArgument.<C>newBuilder(name, jda).asOptional().build();
    }


    public enum ParserMode {
        MENTION,
        ID,
        NAME
    }

    /**
     * Get the modes enabled on the parser
     *
     * @return List of Modes
     */
    public @NotNull List<ParserMode> getModes() {
        return modes;
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, User> {
        private final JDA jda;
        private List<ParserMode> modes = new ArrayList<>();

        protected Builder(final @NonNull String name, final @NonNull JDA jda) {
            super(User.class, name);
            this.jda = jda;
        }

        /**
         * Set the modes for the parsers to use
         *
         * @param modes List of Modes
         * @return Builder instance
         */
        public @NonNull Builder<C> withParsers(final @NonNull List<ParserMode> modes) {
            this.modes = modes;
            return this;
        }

        /**
         * Builder a new example component
         *
         * @return Constructed component
         */
        @Override
        public @NonNull UserArgument<C> build() {
            return new UserArgument<>(this.isRequired(), this.getName(), jda, modes);
        }

    }


    public static final class UserParser<C> implements ArgumentParser<C, User> {
        private final JDA jda;
        private final List<ParserMode> modes;

        private UserParser(final @NonNull JDA jda, final @NonNull List<ParserMode> modes) {
            this.jda = jda;
            this.modes = modes;
        }

        @Override
        public @NonNull ArgumentParseResult<User> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NullPointerException("No input was provided"));
            }

            Exception exception = null;

            if (modes.contains(ParserMode.MENTION)) {
                if (input.endsWith(">")) {
                    String id;
                    if (input.startsWith("<@!")) {
                        id = input.substring(3, input.length() - 1);
                    } else {
                        id = input.substring(2, input.length() - 1);
                    }

                    try {
                        final ArgumentParseResult<User> result = userFromId(input, id);
                        inputQueue.remove();
                        return result;
                    } catch (UserNotFoundParseException | NumberFormatException e) {
                        exception = e;
                    }
                }
            }

            if (modes.contains(ParserMode.ID)) {
                try {
                    final ArgumentParseResult<User> result = userFromId(input, input);
                    inputQueue.remove();
                    return result;
                } catch (UserNotFoundParseException | NumberFormatException e) {
                    exception = e;
                }
            }

            if (modes.contains(ParserMode.NAME)) {
                List<User> users = jda.getUsersByName(input, true);

                if (users.size() == 0) {
                    exception = new UserNotFoundParseException(input);
                } else if (users.size() > 1) {
                    exception = new TooManyUsersFoundParseException(input);
                } else {
                    inputQueue.remove();
                    return ArgumentParseResult.success(users.get(0));
                }
            }

            assert exception != null;
            return ArgumentParseResult.failure(exception);
        }

        @Override
        public boolean isContextFree() {
            return true;
        }

        private @NonNull ArgumentParseResult<User> userFromId(final @NonNull String input, final @NonNull String id)
                throws UserNotFoundParseException, NumberFormatException {
            User user = jda.getUserById(id);

            if (user == null) {
                throw new UserNotFoundParseException(input);
            } else {
                return ArgumentParseResult.success(user);
            }
        }
    }


    public static class UserParseException extends IllegalArgumentException {

        private final String input;

        /**
         * Construct a new UUID parse exception
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
        public final @NonNull String getInput() {
            return input;
        }
    }


    public static final class TooManyUsersFoundParseException extends UserParseException {
        /**
         * Construct a new UUID parse exception
         *
         * @param input String input
         */
        public TooManyUsersFoundParseException(final @NonNull String input) {
            super(input);
        }

        @Override
        public @NonNull String getMessage() {
            return String.format("Too many users found for '%s'.", getInput());
        }
    }


    public static final class UserNotFoundParseException extends UserParseException {
        /**
         * Construct a new UUID parse exception
         *
         * @param input String input
         */
        public UserNotFoundParseException(final @NonNull String input) {
            super(input);
        }

        @Override
        public @NonNull String getMessage() {
            return String.format("User not found for '%s'.", getInput());
        }
    }
}
