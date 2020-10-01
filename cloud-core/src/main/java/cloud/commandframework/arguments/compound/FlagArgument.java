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
package cloud.commandframework.arguments.compound;

import cloud.commandframework.types.tuples.DynamicTuple;
import cloud.commandframework.types.tuples.Tuple;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

/**
 * Container for flag parsing logic. This should not be be used directly.
 * Internally, a flag argument is a special case of a {@link CompoundArgument}.
 *
 * @param <C> Command sender type
 */
public class FlagArgument<C> extends CompoundArgument<DynamicTuple, C, DynamicTuple> {

    FlagArgument(final @NonNull Tuple names,
                 final @NonNull Tuple parserTuple,
                 final @NonNull Tuple types,
                 final @NonNull Function<@NonNull DynamicTuple, @NonNull DynamicTuple> mapper,
                 final @NonNull TypeToken<DynamicTuple> valueType) {
        super(false,
              "flags",
              names,
              parserTuple,
              types,
              mapper,
              DynamicTuple::of,
              valueType);
    }

}
