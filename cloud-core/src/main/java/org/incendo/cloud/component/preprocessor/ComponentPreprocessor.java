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
package org.incendo.cloud.component.preprocessor;

import java.util.function.BiFunction;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;

@API(status = API.Status.STABLE)
@FunctionalInterface
public interface ComponentPreprocessor<C> {

    /**
     * Wraps the given {@code function} in a preprocessor.
     *
     * @param function the function
     * @return the wrapped function
     * @param <C> the command sender type
     */
    static <C> @NonNull ComponentPreprocessor<C> wrap(
            final @NonNull BiFunction<@NonNull CommandContext<C>, @NonNull CommandInput,
                    @NonNull ArgumentParseResult<Boolean>> function
    ) {
        return function::apply;
    }

    /**
     * Pre-processes the associated component.
     * <p>
     * If the preprocessor fails then the command parsing will fail immediately.
     *
     * @param context      the command context
     * @param commandInput the current command input
     * @return the result
     */
    @NonNull ArgumentParseResult<Boolean> preprocess(
            @NonNull CommandContext<C> context,
            @NonNull CommandInput commandInput
    );
}
