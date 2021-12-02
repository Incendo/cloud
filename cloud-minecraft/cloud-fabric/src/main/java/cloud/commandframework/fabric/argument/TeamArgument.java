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
package cloud.commandframework.fabric.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.fabric.FabricCaptionKeys;
import cloud.commandframework.fabric.FabricCommandContextKeys;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.scores.PlayerTeam;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An argument for parsing {@link PlayerTeam Teams}.
 *
 * @param <C> the sender type
 * @since 1.5.0
 */
public final class TeamArgument<C> extends CommandArgument<C, PlayerTeam> {

    TeamArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                new TeamParser<>(),
                defaultValue,
                PlayerTeam.class,
                suggestionsProvider,
                defaultDescription
        );
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     * @since 1.5.0
     */
    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required {@link TeamArgument}.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull TeamArgument<C> of(final @NonNull String name) {
        return TeamArgument.<C>builder(name).asRequired().build();
    }

    /**
     * Create a new optional {@link TeamArgument}.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull TeamArgument<C> optional(final @NonNull String name) {
        return TeamArgument.<C>builder(name).asOptional().build();
    }

    /**
     * Create a new optional {@link TeamArgument} with the specified default value.
     *
     * @param name         Argument name
     * @param defaultValue Default value
     * @param <C>          Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull TeamArgument<C> optional(
            final @NonNull String name,
            final @NonNull PlayerTeam defaultValue
    ) {
        return TeamArgument.<C>builder(name).asOptionalWithDefault(defaultValue).build();
    }

    /**
     * Argument parser for {@link PlayerTeam Teams}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class TeamParser<C> extends SidedArgumentParser<C, String, PlayerTeam> {

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return new ArrayList<>(commandContext.get(FabricCommandContextKeys.NATIVE_COMMAND_SOURCE).getAllTeams());
        }

        @Override
        protected @NonNull ArgumentParseResult<String> parseIntermediate(
                @NonNull final CommandContext<@NonNull C> commandContext,
                @NonNull final Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(TeamParser.class, commandContext));
            }
            return ArgumentParseResult.success(input);
        }

        @Override
        protected @NonNull ArgumentParseResult<PlayerTeam> resolveClient(
                final @NonNull CommandContext<C> context,
                final @NonNull FabricClientCommandSource source,
                final @NonNull String value
        ) {
            final PlayerTeam result = source.getClient().getConnection().getLevel().getScoreboard().getPlayerTeam(value);
            if (result == null) {
                return ArgumentParseResult.failure(new UnknownTeamException(context, value));
            }
            return ArgumentParseResult.success(result);
        }

        @Override
        protected @NonNull ArgumentParseResult<PlayerTeam> resolveServer(
                final @NonNull CommandContext<C> context,
                final @NonNull CommandSourceStack source,
                final @NonNull String value
        ) {
            final PlayerTeam result = source.getLevel().getScoreboard().getPlayerTeam(value);
            if (result == null) {
                return ArgumentParseResult.failure(new UnknownTeamException(context, value));
            }
            return ArgumentParseResult.success(result);
        }

    }

    /**
     * Builder for {@link TeamArgument}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class Builder<C> extends TypedBuilder<C, PlayerTeam, Builder<C>> {

        Builder(final @NonNull String name) {
            super(PlayerTeam.class, name);
        }

        /**
         * Build a new {@link TeamArgument}.
         *
         * @return Constructed argument
         * @since 1.5.0
         */
        @Override
        public @NonNull TeamArgument<C> build() {
            return new TeamArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

        /**
         * Sets the command argument to be optional, with the specified default value.
         *
         * @param defaultValue default value
         * @return this builder
         * @see CommandArgument.Builder#asOptionalWithDefault(String)
         * @since 1.5.0
         */
        public @NonNull Builder<C> asOptionalWithDefault(final @NonNull PlayerTeam defaultValue) {
            return this.asOptionalWithDefault(defaultValue.getName());
        }

    }

    /**
     * Exception for when a team cannot be found for supplied input.
     *
     * @since 1.5.0
     */
    public static final class UnknownTeamException extends ParserException {

        private static final long serialVersionUID = 4249139487412603424L;

        UnknownTeamException(
                final @NonNull CommandContext<?> context,
                final @NonNull String input
        ) {
            super(
                    TeamParser.class,
                    context,
                    FabricCaptionKeys.ARGUMENT_PARSE_FAILURE_TEAM_UNKNOWN,
                    CaptionVariable.of("input", input)
            );
        }

    }

}
