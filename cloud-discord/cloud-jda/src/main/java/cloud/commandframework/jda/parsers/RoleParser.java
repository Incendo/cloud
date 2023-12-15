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
package cloud.commandframework.jda.parsers;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import java.util.List;
import java.util.Set;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Command Argument for {@link net.dv8tion.jda.api.entities.Role}
 *
 * @param <C> Command sender type
 */
@SuppressWarnings("unused")
public final class RoleParser<C> implements ArgumentParser<C, Role> {

    /**
     * Creates a new server parser.
     *
     * @param <C> command sender type
     * @param modes parser modes to use
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, Role> roleParser(final @NonNull Set<ParserMode> modes) {
        return ParserDescriptor.of(new RoleParser<>(modes), Role.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #roleParser} as the parser.
     *
     * @param <C> the command sender type
     * @param modes parser modes to use
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, Role> roleComponent(final @NonNull Set<ParserMode> modes) {
        return CommandComponent.<C, Role>builder().parser(roleParser(modes));
    }

    private final Set<ParserMode> modes;

    /**
     * Construct a new role parser.
     *
     * @param modes parser modules to use
     */
    public RoleParser(final @NonNull Set<ParserMode> modes) {
        this.modes = modes;
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


    @Override
    public @NonNull ArgumentParseResult<Role> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final String input = commandInput.peekString();
        if (input.isEmpty()) {
            return ArgumentParseResult.failure(new NoInputProvidedException(
                    RoleParser.class,
                    commandContext
            ));
        }

        if (!commandContext.contains("MessageReceivedEvent")) {
            return ArgumentParseResult.failure(new IllegalStateException(
                    "MessageReceivedEvent was not in the command context."
            ));
        }

        final MessageReceivedEvent event = commandContext.get("MessageReceivedEvent");
        Exception exception = null;

        if (!event.isFromGuild()) {
            return ArgumentParseResult.failure(new IllegalArgumentException("Role arguments can only be parsed in guilds"));
        }

        if (this.modes.contains(ParserMode.MENTION)) {
            if (input.startsWith("<@&") && input.endsWith(">")) {
                final String id = input.substring(3, input.length() - 1);

                try {
                    final ArgumentParseResult<Role> role = this.roleFromId(event, input, id);
                    commandInput.readString();
                    return role;
                } catch (final RoleNotFoundException | NumberFormatException e) {
                    exception = e;
                }
            } else {
                exception = new IllegalArgumentException(
                        String.format("Input '%s' is not a role mention.", input)
                );
            }
        }

        if (this.modes.contains(ParserMode.ID)) {
            try {
                final ArgumentParseResult<Role> result = this.roleFromId(event, input, input);
                commandInput.readString();
                return result;
            } catch (final RoleNotFoundException | NumberFormatException e) {
                exception = e;
            }
        }

        if (this.modes.contains(ParserMode.NAME)) {
            final List<Role> roles = event.getGuild().getRolesByName(input, true);

            if (roles.isEmpty()) {
                exception = new RoleNotFoundException(input);
            } else if (roles.size() > 1) {
                exception = new TooManyRolesFoundParseException(input);
            } else {
                commandInput.readString();
                return ArgumentParseResult.success(roles.get(0));
            }
        }

        assert exception != null;
        return ArgumentParseResult.failure(exception);
    }

    private @NonNull ArgumentParseResult<Role> roleFromId(
            final @NonNull MessageReceivedEvent event,
            final @NonNull String input,
            final @NonNull String id
    )
            throws RoleNotFoundException, NumberFormatException {
        final Role role = event.getGuild().getRoleById(id);

        if (role == null) {
            throw new RoleNotFoundException(input);
        }

        return ArgumentParseResult.success(role);
    }

    public static class RoleParseException extends IllegalArgumentException {

        private static final long serialVersionUID = -2451548379508062135L;
        private final String input;

        /**
         * Construct a new role parse exception
         *
         * @param input String input
         */
        public RoleParseException(final @NonNull String input) {
            this.input = input;
        }

        /**
         * Get the users input
         *
         * @return users input
         */
        public final @NonNull String getInput() {
            return this.input;
        }
    }


    public static final class TooManyRolesFoundParseException extends RoleParseException {

        private static final long serialVersionUID = -8604082973199995006L;

        /**
         * Construct a new role parse exception
         *
         * @param input String input
         */
        public TooManyRolesFoundParseException(final @NonNull String input) {
            super(input);
        }

        @Override
        public @NonNull String getMessage() {
            return String.format("Too many roles found for '%s'.", getInput());
        }
    }


    public static final class RoleNotFoundException extends RoleParseException {

        private static final long serialVersionUID = 7931804739792920510L;

        /**
         * Construct a new role parse exception
         *
         * @param input String input
         */
        public RoleNotFoundException(final @NonNull String input) {
            super(input);
        }

        @Override
        public @NonNull String getMessage() {
            return String.format("Role not found for '%s'.", getInput());
        }
    }
}
