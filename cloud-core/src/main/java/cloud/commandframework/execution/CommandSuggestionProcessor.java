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
package cloud.commandframework.execution;

import cloud.commandframework.Completion;
import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import java.util.List;
import java.util.function.BiFunction;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Processor that formats command suggestions
 *
 * @param <C> Command sender type
 * @deprecated Can result in lose of suggestion's additional data
 */
@Deprecated
@API(status = API.Status.STABLE)
public interface CommandSuggestionProcessor<C> extends
        BiFunction<@NonNull CommandPreprocessingContext<C>, @NonNull List<String>, @NonNull List<String>> {

    /**
     * Create a pass through {@link CommandSuggestionProcessor} that simply returns
     * the input.
     *
     * @param <C> sender type
     * @return new processor
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    static <C> @NonNull CommandSuggestionProcessor<C> passThrough() {
        return (ctx, suggestions) -> suggestions;
    }
    /**
     * Transforms to a full suggestion processor
     * @return a {@link CommandCompletionProcessor} analog
     */
    default CommandCompletionProcessor<C> toFull() {
        return (c, s) -> Completion.of(this.apply(c, Completion.raw(s)));
    }
}
