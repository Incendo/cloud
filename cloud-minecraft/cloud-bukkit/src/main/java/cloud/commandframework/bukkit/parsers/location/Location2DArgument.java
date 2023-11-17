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
package cloud.commandframework.bukkit.parsers.location;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.bukkit.BukkitCommandContextKeys;
import cloud.commandframework.bukkit.parsers.location.LocationArgument.LocationParseException;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import io.leangen.geantyref.TypeToken;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Argument parser that parses {@link Location2D} from two doubles. This will use the command
 * senders world when it exists, or else it'll use the first loaded Bukkit world
 *
 * @param <C> Command sender type
 * @since 1.4.0
 */
public final class Location2DArgument<C> extends CommandArgument<C, Location2D> {

    private Location2DArgument(
            final @NonNull String name,
            final @NonNull ArgumentDescription defaultDescription,
            final @Nullable SuggestionProvider<C> suggestionProvider,
            final @NonNull Collection<@NonNull BiFunction<@NonNull CommandContext<C>,
                    @NonNull CommandInput, @NonNull ArgumentParseResult<Boolean>>> argumentPreprocessors
    ) {
        super(
                name,
                new Location2DParser<>(),
                TypeToken.get(Location2D.class),
                suggestionProvider,
                defaultDescription,
                argumentPreprocessors
        );
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return new {@link Builder}
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Constructed argument
     */
    public static <C> @NonNull CommandArgument<C, Location2D> of(
            final @NonNull String name
    ) {
        return Location2DArgument.<C>builder(name).build();
    }


    public static final class Builder<C> extends CommandArgument.TypedBuilder<C, Location2D, Builder<C>> {

        private Builder(
                final @NonNull String name
        ) {
            super(
                    TypeToken.get(Location2D.class),
                    name
            );
        }

        @Override
        public @NonNull CommandArgument<@NonNull C, @NonNull Location2D> build() {
            return new Location2DArgument<>(
                    this.getName(),
                    this.getDefaultDescription(),
                    this.suggestionProvider(),
                    new LinkedList<>()
            );
        }
    }


    public static final class Location2DParser<C> implements ArgumentParser<C, Location2D> {

        private static final int EXPECTED_PARAMETER_COUNT = 2;

        private final LocationCoordinateParser<C> locationCoordinateParser = new LocationCoordinateParser<>();

        @Override
        public @NonNull ArgumentParseResult<@NonNull Location2D> parse(
                final @NonNull CommandContext<@NonNull C> commandContext,
                final @NonNull CommandInput commandInput
        ) {
            if (commandInput.remainingTokens() < 2) {
                return ArgumentParseResult.failure(
                        new LocationParseException(
                                commandContext,
                                LocationParseException.FailureReason.WRONG_FORMAT,
                                commandInput.remainingInput()
                        )
                );
            }
            final LocationCoordinate[] coordinates = new LocationCoordinate[2];
            for (int i = 0; i < 2; i++) {
                final ArgumentParseResult<@NonNull LocationCoordinate> coordinate = this.locationCoordinateParser.parse(
                        commandContext,
                        commandInput
                );
                if (coordinate.getFailure().isPresent()) {
                    return ArgumentParseResult.failure(
                            coordinate.getFailure().get()
                    );
                }
                coordinates[i] = coordinate.getParsedValue().orElseThrow(NullPointerException::new);
            }
            final Location originalLocation;
            final CommandSender bukkitSender = commandContext.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER);

            if (bukkitSender instanceof BlockCommandSender) {
                originalLocation = ((BlockCommandSender) bukkitSender).getBlock().getLocation();
            } else if (bukkitSender instanceof Entity) {
                originalLocation = ((Entity) bukkitSender).getLocation();
            } else {
                originalLocation = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
            }

            if (coordinates[0].getType() == LocationCoordinateType.LOCAL && coordinates[1].getType() != LocationCoordinateType.LOCAL) {
                return ArgumentParseResult.failure(
                        new LocationParseException(
                                commandContext,
                                LocationParseException.FailureReason.MIXED_LOCAL_ABSOLUTE,
                                ""
                        )
                );
            }

            if (coordinates[0].getType() == LocationCoordinateType.ABSOLUTE) {
                originalLocation.setX(coordinates[0].getCoordinate());
            } else if (coordinates[0].getType() == LocationCoordinateType.RELATIVE) {
                originalLocation.add(coordinates[0].getCoordinate(), 0, 0);
            }

            if (coordinates[1].getType() == LocationCoordinateType.ABSOLUTE) {
                originalLocation.setZ(coordinates[1].getCoordinate());
            } else if (coordinates[1].getType() == LocationCoordinateType.RELATIVE) {
                originalLocation.add(0, 0, coordinates[1].getCoordinate());
            } else {
                final Vector declaredPos = new Vector(
                        coordinates[0].getCoordinate(),
                        0,
                        coordinates[1].getCoordinate()
                );
                final Location local = LocationArgument.LocationParser.toLocalSpace(originalLocation, declaredPos);
                return ArgumentParseResult.success(Location2D.from(
                        originalLocation.getWorld(),
                        local.getX(),
                        local.getZ()
                ));
            }

            return ArgumentParseResult.success(Location2D.from(
                    originalLocation.getWorld(),
                    originalLocation.getX(),
                    originalLocation.getZ()
            ));
        }

        @Override
        public @NonNull List<@NonNull String> stringSuggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return LocationArgument.LocationParser.getSuggestions(commandContext, input);
        }

        @Override
        public int getRequestedArgumentCount() {
            return EXPECTED_PARAMETER_COUNT;
        }
    }
}
