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

package cloud.commandframework.quilt.argument;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.quilt.QuiltCaptionKeys;
import cloud.commandframework.quilt.QuiltCommandContextKeys;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ServerCommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

/**
 * An argument parsing an entity anchor.
 *
 * @param <C> the sender type
 * @since 1.5.0
 */
public final class TeamArgument<C> extends CommandArgument<C, Team> {

    TeamArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider
    ) {
        super(
                required,
                name,
                new TeamParser<>(),
                defaultValue,
                Team.class,
                suggestionsProvider
        );
    }

    /**
     * Create a new builder.
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     * @since 1.5.0
     */
    public static <C> TeamArgument.@NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new TeamArgument.Builder<>(name);
    }

    /**
     * Create a new required command argument.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull TeamArgument<C> of(final @NonNull String name) {
        return TeamArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command argument.
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull TeamArgument<C> optional(final @NonNull String name) {
        return TeamArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new optional command argument with a default value.
     *
     * @param name         Argument name
     * @param defaultValue Default value
     * @param <C>          Command sender type
     * @return Created argument
     * @since 1.5.0
     */
    public static <C> @NonNull TeamArgument<C> optional(
            final @NonNull String name,
            final EntityAnchorArgumentType.@NonNull EntityAnchor defaultValue
    ) {
        return TeamArgument.<C>newBuilder(name).asOptionalWithDefault(defaultValue.name()).build();
    }

    /**
     * Argument parser for {@link Team}.
     *
     * @param <C> sender type
     * @since 1.5.0
     */
    public static final class TeamParser<C> extends SidedArgumentParser<C, String, Team> {

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return new ArrayList<>(commandContext.get(QuiltCommandContextKeys.NATIVE_COMMAND_SOURCE).getTeamNames());
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
        protected @NonNull ArgumentParseResult<Team> resolveClient(
                final @NonNull CommandContext<C> context,
                final @NonNull CommandSource source,
                final @NonNull String value
        ) {
            final Team result = MinecraftClient.getInstance().getNetworkHandler().getWorld().getScoreboard().getTeam(value);
            if (result == null) {
                return ArgumentParseResult.failure(new UnknownTeamException(context, value));
            }
            return ArgumentParseResult.success(result);
        }

        @Override
        protected @NonNull ArgumentParseResult<Team> resolveServer(
                final @NonNull CommandContext<C> context,
                final @NonNull CommandSource source,
                final @NonNull String value
        ) {
            final Team result = ((ServerCommandSource) source).getWorld().getScoreboard().getTeam(value);
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
    public static final class Builder<C> extends TypedBuilder<C, Team, Builder<C>> {

        Builder(final @NonNull String name) {
            super(Team.class, name);
        }

        /**
         * Build a new team argument.
         *
         * @return Constructed argument
         * @since 1.5.0
         */
        @Override
        public @NonNull TeamArgument<C> build() {
            return new TeamArgument<>(this.isRequired(), this.getName(), this.getDefaultValue(), this.getSuggestionsProvider());
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
                    QuiltCaptionKeys.ARGUMENT_PARSE_FAILURE_TEAM_UNKNOWN,
                    CaptionVariable.of("input", input)
            );
        }

    }

}
