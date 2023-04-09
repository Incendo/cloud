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
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A suggestion which gives native minecraft chat component as description, used for brigadier support
 * @since 1.9.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.9.0")
public interface BrigadierCompletion extends Completion {

    /**
     * Gives a native chat component tooltip for suggestion
     * @return chat component
     */
    @NonNull Message tooltip();

    /**
     * Returns a new native suggestion with a new tooltip
     * @param tooltip the new tooltip message
     * @return a new suggestion instance with this tooltip
     */
    @NonNull BrigadierCompletion withTooltip(@NonNull Message tooltip);

    /**
     * Gives a suggestion instance with native brigadier message as description
     * @param suggestion the suggestion
     * @param description the plain text as description
     * @return a new suggestion instance
     */
    static @NonNull BrigadierCompletion of(@NonNull final String suggestion, @NonNull final String description) {
        return new BrigadierCompletionImpl(suggestion, new LiteralMessage(description));
    }
    /**
     * Gives a suggestion instance with native brigadier message as description
     * @param suggestion the suggestion
     * @param description the rich text as description
     * @return a new suggestion instance
     */
    static @NonNull BrigadierCompletion of(@NonNull final String suggestion, @NonNull final Message description) {
        return new BrigadierCompletionImpl(suggestion, description);
    }
}
