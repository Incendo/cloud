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
package cloud.commandframework.bukkit.parsers.location;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Queue;

/**
 * A single coordinate, meant to be used as an element in a position vector
 *
 * @param <C> Command sender type
 * @since 1.1.0
 */
public final class LocationCoordinateParser<C> implements ArgumentParser<C, LocationCoordinate> {

    @Override
    public @NonNull ArgumentParseResult<@NonNull LocationCoordinate> parse(
            final @NonNull CommandContext<@NonNull C> commandContext,
            final @NonNull Queue<@NonNull String> inputQueue
    ) {
        String input = inputQueue.peek();

        if (input == null) {
            return ArgumentParseResult.failure(new NoInputProvidedException(
                    PlayerArgument.PlayerParser.class,
                    commandContext
            ));
        }

        /* Determine the type */
        final LocationCoordinateType locationCoordinateType;
        if (input.startsWith("^")) {
            locationCoordinateType = LocationCoordinateType.LOCAL;
            input = input.substring(1);
        } else if (input.startsWith("~")) {
            locationCoordinateType = LocationCoordinateType.RELATIVE;
            input = input.substring(1);
        } else {
            locationCoordinateType = LocationCoordinateType.ABSOLUTE;
        }

        final double coordinate;
        try {
            coordinate = input.isEmpty() ? 0 : Double.parseDouble(input);
        } catch (final Exception e) {
            return ArgumentParseResult.failure(
                    new DoubleArgument.DoubleParseException(
                            input,
                            Double.NEGATIVE_INFINITY,
                            Double.POSITIVE_INFINITY,
                            commandContext
                    )
            );
        }

        inputQueue.remove();
        return ArgumentParseResult.success(
                LocationCoordinate.of(
                        locationCoordinateType,
                        coordinate
                )
        );
    }

}
