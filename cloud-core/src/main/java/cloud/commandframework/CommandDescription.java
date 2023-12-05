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

import cloud.commandframework.internal.ImmutableImpl;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value;

@ImmutableImpl
@Value.Immutable
@SuppressWarnings("unused")
@API(status = API.Status.STABLE, since = "2.0.0")
public interface CommandDescription extends Describable {

    /**
     * Returns an empty command description.
     * <p>
     * Both {@link #description()} and {@link #verboseDescription()} will return {@link Description#empty()}.
     *
     * @return the description instance
     */
    static @NonNull CommandDescription empty() {
        return CommandDescriptionImpl.of(Description.empty(), Description.empty());
    }

    /**
     * Returns a new command description using the given {@code description} and {@code verboseDescription}.
     *
     * @param description        the command description
     * @param verboseDescription the verbose command description
     * @return the description instance
     */
    static @NonNull CommandDescription commandDescription(
            final @NonNull Description description,
            final @NonNull Description verboseDescription
    ) {
        return CommandDescriptionImpl.of(description, verboseDescription);
    }

    /**
     * Returns a new command description using the given {@code description}.
     * <p>
     * The {@link #verboseDescription()} will return the same value as {@link #description()}.
     *
     * @param description the command description
     * @return the description instance
     */
    static @NonNull CommandDescription commandDescription(final @NonNull Description description) {
        return CommandDescriptionImpl.of(description, description);
    }

    /**
     * Returns a new command description using the given {@code description} and {@code verboseDescription}.
     *
     * @param description        the command description
     * @param verboseDescription the verbose command description
     * @return the description instance
     */
    static @NonNull CommandDescription commandDescription(
            final @NonNull String description,
            final @NonNull String verboseDescription
    ) {
        return CommandDescriptionImpl.of(Description.of(description), Description.of(verboseDescription));
    }

    /**
     * Returns a new command description using the given {@code description}.
     * <p>
     * The {@link #verboseDescription()} will return the same value as {@link #description()}.
     *
     * @param description the command description
     * @return the description instance
     */
    static @NonNull CommandDescription commandDescription(final @NonNull String description) {
        return CommandDescriptionImpl.of(Description.of(description), Description.of(description));
    }

    /**
     * Returns the command description.
     *
     * @return the command description
     */
    @Override
    @NonNull Description description();

    /**
     * Returns the verbose version of the command description.
     *
     * @return the verbose command description
     */
    @NonNull Description verboseDescription();

    /**
     * Returns whether the description is empty.
     *
     * @return whether the description equals {@link Description#empty()}
     */
    default boolean isEmpty() {
        return this.description().equals(Description.empty());
    }
}
