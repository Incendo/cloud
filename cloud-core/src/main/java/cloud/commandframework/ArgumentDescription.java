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

import cloud.commandframework.arguments.CommandArgument;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.util.Objects.requireNonNull;

/**
 * A description for a {@link CommandArgument}
 *
 * @param <C> command sender
 * @since 1.4.0
 */
@API(status = API.Status.STABLE, since = "1.4.0")
public interface ArgumentDescription<C> {

    /**
     * Get an empty command description.
     *
     * @param <C> command sender type
     * @return Command description
     */
    static <C> @NonNull ArgumentDescription<C> empty() {
        return new Description<>("");
    }

    /**
     * Create a command description instance.
     *
     * @param <C> command sender type
     * @param string Command description
     * @return Created command description
     */
    static <C> @NonNull ArgumentDescription<C> of(final @NonNull String string) {
        if (requireNonNull(string, "string").isEmpty()) {
            return empty();
        } else {
            return new Description<>(string);
        }
    }

    /**
     * Get the plain-text description.
     *
     * @param commandSender the command sender that is viewing the description
     * @return Command description
     */
    @NonNull String description(@NonNull C commandSender);

    /**
     * Get whether this description contains contents.
     *
     * @param commandSender the command sender that is viewing the description
     * @return if this description is empty or not
     */
    default boolean isEmpty(@NonNull C commandSender) {
        return this.description(commandSender).isEmpty();
    }
}
