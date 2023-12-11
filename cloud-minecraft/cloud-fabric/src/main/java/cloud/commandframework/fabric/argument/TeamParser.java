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
package cloud.commandframework.fabric.argument;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.fabric.FabricCaptionKeys;
import cloud.commandframework.fabric.FabricCommandContextKeys;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.scores.PlayerTeam;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An argument for parsing {@link PlayerTeam Teams}.
 *
 * @param <C> the sender type
 * @since 2.0.0
 */
public final class TeamParser<C> extends SidedArgumentParser<C, String, PlayerTeam> {

    /**
     * Creates a new server parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, PlayerTeam> teamParser() {
        return ParserDescriptor.of(new TeamParser<>(), PlayerTeam.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #teamParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, PlayerTeam> teamComponent() {
        return CommandComponent.<C, PlayerTeam>builder().parser(teamParser());
    }

    @Override
    public @NonNull List<@NonNull String> stringSuggestions(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull String input
    ) {
        return new ArrayList<>(commandContext.get(FabricCommandContextKeys.NATIVE_COMMAND_SOURCE).getAllTeams());
    }

    @Override
    protected @NonNull ArgumentParseResult<String> parseIntermediate(
            @NonNull final CommandContext<@NonNull C> commandContext,
            @NonNull final CommandInput commandInput
    ) {
        final String input = commandInput.readString();
        if (input.isEmpty()) {
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
