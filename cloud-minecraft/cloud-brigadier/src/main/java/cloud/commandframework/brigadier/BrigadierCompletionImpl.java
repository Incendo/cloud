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
package cloud.commandframework.brigadier;

import cloud.commandframework.Completion;
import com.mojang.brigadier.Message;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.INTERNAL)
public final class BrigadierCompletionImpl implements BrigadierCompletion {

    private final @NonNull String suggestion;
    private final @NonNull Message tooltip;

    BrigadierCompletionImpl(final @NonNull String suggestion, final @NonNull Message tooltip) {
        this.suggestion = suggestion;
        this.tooltip = tooltip;
    }

    @Override
    public @NonNull String suggestion() {
        return this.suggestion;
    }

    @Override
    public @NonNull Completion withSuggestion(@NonNull final String suggestion) {
        return new BrigadierCompletionImpl(suggestion, this.tooltip);
    }

    @Override
    public @NonNull Message tooltip() {
        return this.tooltip;
    }

    @Override
    public @NonNull BrigadierCompletion withTooltip(@NonNull final Message tooltip) {
        return new BrigadierCompletionImpl(this.suggestion, tooltip);
    }
}
