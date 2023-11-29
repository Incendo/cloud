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

import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

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
        return new CommandDescriptionImpl(Description.empty());
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
        return new CommandDescriptionImpl(description, verboseDescription);
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
        return new CommandDescriptionImpl(description);
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
        return new CommandDescriptionImpl(Description.of(description), Description.of(verboseDescription));
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
        return new CommandDescriptionImpl(Description.of(description));
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


    final class CommandDescriptionImpl implements CommandDescription {

        private final Description description;
        private final Description verboseDescription;

        private CommandDescriptionImpl(
                final @NonNull Description description,
                final @NonNull Description verboseDescription
        ) {
            this.description = description;
            this.verboseDescription = verboseDescription;
        }

        private CommandDescriptionImpl(
                final @NonNull Description description
        ) {
            this(description, description);
        }

        @Override
        public @NonNull Description description() {
            return this.description;
        }

        @Override
        public @NonNull Description verboseDescription() {
            return this.verboseDescription;
        }

        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            final CommandDescriptionImpl that = (CommandDescriptionImpl) object;
            return Objects.equals(this.description, that.description)
                    && Objects.equals(this.verboseDescription, that.verboseDescription);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.description, this.verboseDescription);
        }

        @Override
        public String toString() {
            return "CommandDescriptionImpl{description=" + this.description
                    + ", verboseDescription=" + this.verboseDescription + '}';
        }
    }
}
