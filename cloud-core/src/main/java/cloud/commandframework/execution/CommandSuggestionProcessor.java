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

import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Processor that formats command suggestions
 *
 * @param <C> the command sender type
 * @since 2.0.0
 */
@FunctionalInterface
@API(status = API.Status.STABLE, since = "2.0.0")
public interface CommandSuggestionProcessor<C> {

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
        return (ctx, suggestion) -> suggestion;
    }

    /**
     * Processes the given {@code suggestion} and returns the result.
     * <p>
     * If {@code null} is returned, the suggestion will be dropped.
     *
     * @param context    the context
     * @param suggestion the suggestion
     * @return the result
     */
    @Nullable Suggestion process(@NonNull CommandPreprocessingContext<C> context, @NonNull Suggestion suggestion);
}
