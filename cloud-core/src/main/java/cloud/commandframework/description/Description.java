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
package cloud.commandframework.description;

import cloud.commandframework.internal.ImmutableImpl;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;

import static java.util.Objects.requireNonNull;

/**
 * A description for a command or a command component.
 *
 */
@ImmutableImpl
@Value.Immutable
@API(status = API.Status.STABLE)
public interface Description {

    Description EMPTY = DescriptionImpl.of("");

    /**
     * Returns an empty command description.
     *
     * @return Command description
     */
    static @NonNull Description empty() {
        return EMPTY;
    }

    /**
     * Creates a command description instance.
     *
     * @param string Command description
     * @return Created command description
     */
    static @NonNull Description of(final @NonNull String string) {
        if (requireNonNull(string, "string").isEmpty()) {
            return empty();
        } else {
            return DescriptionImpl.of(string);
        }
    }

    /**
     * Creates a command description instance.
     *
     * @param string Command description
     * @return Created command description
     */
    static @NonNull Description description(final @NonNull String string) {
        return of(string);
    }

    /**
     * Returns the plain-text version of this description.
     * <p>
     * If {@link #isEmpty()} is {@code true} this will return an empty string.
     *
     * @return plain-text description
     */
    @NonNull String textDescription();

    /**
     * Returns whether this description contains contents.
     *
     * @return {@code true} if the description is empty, or {@code false} if not
     */
    default boolean isEmpty() {
        return this.textDescription().isEmpty();
    }
}
