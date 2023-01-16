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
package cloud.commandframework;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A completion with description
 * @since 1.9.0
 */
public interface DescriptiveCompletion extends Completion {

    /**
     * Returns a new instance with the new description
     * @param description new description
     * @return new instance
     */
    @NonNull DescriptiveCompletion withDescription(@NonNull String description);

    /**
     * Returns the description that this completion holds
     *
     * @return the description
     */
    @NonNull String description();

    /**
     * Returns new completion with description
     * @param suggestion the suggestion
     * @param description the description
     * @return a new completion with description
     */
    static @NonNull DescriptiveCompletion of(@NonNull String suggestion, @NonNull String description) {
        return new DescriptiveCompletionImpl(suggestion, description);
    }

    /**
     * Optionally returns a new completion with description if a description is provided
     * @param suggestion the suggestion
     * @param description the description
     * @return a completion if no description, a completion with description if description is present
     */
    static @NonNull Completion optional(@NonNull String suggestion, @Nullable String description) {
        return description == null ? Completion.of(suggestion) : new DescriptiveCompletionImpl(suggestion, description);
    }

}
