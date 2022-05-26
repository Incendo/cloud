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
package cloud.commandframework.annotations;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Processor that intercepts all cloud annotation strings.
 *
 * @since 1.7.0
 */
@FunctionalInterface
public interface StringProcessor {

    /**
     * Returns a string processor that simply returns the input string.
     *
     * @return no-op string processor
     */
    static @NonNull StringProcessor noOp() {
        return new NoOpStringProcessor();
    }

    /**
     * Processes the {@code input} string and returns the processed result.
     * <p>
     * This should always return a non-{@code null} result. If the input string
     * isn't applicable to the processor implementation, the original string should
     * be returned.
     *
     * @param input the input string
     * @return the processed string
     */
    @NonNull String processString(@NonNull String input);


    final class NoOpStringProcessor implements StringProcessor {

        @Override
        public @NonNull String processString(final @NonNull String input) {
            return input;
        }
    }
}
