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

class DescriptiveCompletionImpl implements DescriptiveCompletion {

    private final String suggestion;
    private final String description;

    DescriptiveCompletionImpl(final String suggestion, final String description) {
        this.suggestion = suggestion;
        this.description = description;
    }

    @Override
    public @NonNull String suggestion() {
        return this.suggestion;
    }

    @Override
    public @NonNull Completion withSuggestion(@NonNull final String suggestion) {
        return new DescriptiveCompletionImpl(suggestion, this.description);
    }

    @Override
    public @NonNull DescriptiveCompletion withDescription(final @NonNull String description) {
        return new DescriptiveCompletionImpl(this.suggestion, description);
    }

    @Override
    public @NonNull String description() {
        return this.description;
    }
}
