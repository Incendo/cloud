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
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.arguments.RangeArgument;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An argument parsing an unbounded {@link net.minecraft.advancements.critereon.MinMaxBounds.Ints integer range}, in the form
 * {@code [min]..[max]}, where both lower and upper bounds are optional.
 *
 * @param <C> the sender type
 * @since 2.0.0
 */
public final class IntRangeParser<C> extends WrappedBrigadierParser<C, MinMaxBounds.Ints> {

    /**
     * Creates a new int range parser.
     *
     * @param <C> command sender type
     * @return the created parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull ParserDescriptor<C, MinMaxBounds.Ints> intRangeParser() {
        return ParserDescriptor.of(new IntRangeParser<>(), MinMaxBounds.Ints.class);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #intRangeParser()} as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> CommandComponent.@NonNull Builder<C, MinMaxBounds.Ints> intRangeComponent() {
        return CommandComponent.<C, MinMaxBounds.Ints>builder().parser(intRangeParser());
    }

    IntRangeParser() {
        super(RangeArgument.intRange());
    }
}
