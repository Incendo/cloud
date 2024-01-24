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
package cloud.commandframework.parser.compound;

import cloud.commandframework.parser.ArgumentParser;
import cloud.commandframework.parser.ParserDescriptor;
import cloud.commandframework.type.tuple.Tuple;
import io.leangen.geantyref.TypeToken;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Compound argument
 *
 * @param <T> Tuple type
 * @param <C> command sender type
 * @param <O> Output type
 */
@API(status = API.Status.INTERNAL)
public class CompoundArgument<T extends Tuple, C, O> implements ParserDescriptor<C, O> {

    private final ParserDescriptor<C, O> parser;

    /**
     * Construct a Compound Argument
     *
     * @param names        Names of the sub-arguments (in order)
     * @param parserTuple  The sub arguments
     * @param types        Types of the sub-arguments (in order)
     * @param mapper       Mapper that maps the sub-arguments to the output type
     * @param tupleFactory Function to use when creating tuple
     * @param valueType    The output type
     */
    public CompoundArgument(
            final @NonNull Tuple names,
            final @NonNull Tuple parserTuple,
            final @NonNull Tuple types,
            final @NonNull BiFunction<@NonNull C, @NonNull T, @NonNull O> mapper,
            final @NonNull Function<@NonNull Object[], @NonNull T> tupleFactory,
            final @NonNull TypeToken<O> valueType
    ) {
        this.parser = ParserDescriptor.of(
                new CompoundParser<>(
                        names.toArray(),
                        types.toArray(),
                        parserTuple.toArray(),
                        mapper,
                        tupleFactory,
                        valueType
                ),
                valueType
        );
    }

    @Override
    public final @NonNull ArgumentParser<C, O> parser() {
        return this.parser.parser();
    }

    @Override
    public final @NonNull TypeToken<O> valueType() {
        return this.parser.valueType();
    }
}
