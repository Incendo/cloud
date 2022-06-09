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
package cloud.commandframework;

import cloud.commandframework.arguments.CommandArgument;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.util.Objects.requireNonNull;

/**
 * A description for a {@link CommandArgument}
 *
 * @param <C> Command sender type
 * @since 1.4.0
 */
@API(status = API.Status.STABLE, since = "1.4.0")
public interface ArgumentDescription<C> {

    /**
     * Get an empty command description.
     *
     * @param <C> Command sender type
     * @return Command description
     */
    @SuppressWarnings("deprecation")
    static <C> @NonNull ArgumentDescription<C> empty() {
        return Description.empty();
    }

    /**
     * Create a command description instance.
     *
     * @param string Command description
     * @param <C> Command sender type
     * @return Created command description
     */
    @SuppressWarnings("deprecation")
    static <C> @NonNull ArgumentDescription<C> of(final @NonNull String string) {
        if (requireNonNull(string, "string").isEmpty()) {
            return Description.empty();
        } else {
            return new Description<>(string);
        }
    }

    /**
     * Get the plain-text description.
     *
     * @return Command description
     */
    @NonNull String getDescription();

    /**
     * Get the plain-text description for a command sender.
     *
     * @param commandSender the command sender
     * @return Command description
     */
    default @NonNull String getDescription(final @NonNull C commandSender) {
        return this.getDescription();
    }

    /**
     * Get whether or not this description contains contents.
     *
     * @return if this description is empty or not
     */
    default boolean isEmpty() {
        return this.getDescription().isEmpty();
    }


    /**
     * Get whether or not this description contains contents for a command sender.
     *
     * @param commandSender the command sender
     * @return if this description is empty or not
     */
    default boolean isEmpty(final @NonNull C commandSender) {
        return this.getDescription(commandSender).isEmpty();
    }

}
