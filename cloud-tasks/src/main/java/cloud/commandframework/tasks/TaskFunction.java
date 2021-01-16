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
package cloud.commandframework.tasks;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

/**
 * Task step that produces output from given input
 *
 * @param <I> Input type
 * @param <O> Output type
 */
public interface TaskFunction<I, O> extends Function<@NonNull I, @NonNull O>, TaskRecipeStep {

    /**
     * Equivalent to {@link Function#identity()}
     *
     * @param <I> Input type
     * @return Function that maps the input to itself
     */
    static <I> TaskFunction<I, I> identity() {
        return i -> i;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull O apply(@NonNull I input);

}
