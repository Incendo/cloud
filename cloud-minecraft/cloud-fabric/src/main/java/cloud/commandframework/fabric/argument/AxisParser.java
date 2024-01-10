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
package cloud.commandframework.fabric.argument;

import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import io.leangen.geantyref.TypeToken;
import java.util.EnumSet;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.core.Direction;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An argument parser for a set of {@link net.minecraft.core.Direction.Axis axes}, described in Vanilla as a "swizzle".
 *
 * @param <C> the sender type
 * @since 2.0.0
 */
public final class AxisParser<C> extends WrappedBrigadierParser<C, EnumSet<Direction.Axis>> {

    private static final TypeToken<EnumSet<Direction.Axis>> TYPE = new TypeToken<EnumSet<Direction.Axis>>() {
    };

    /**
     * Creates a new axis parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, EnumSet<Direction.Axis>> axisParser() {
        return ParserDescriptor.of(new AxisParser<>(), TYPE);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #axisParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, EnumSet<Direction.Axis>> axisComponent() {
        return CommandComponent.<C, EnumSet<Direction.Axis>>builder().parser(axisParser());
    }

    AxisParser() {
        super(SwizzleArgument.swizzle());
    }
}
