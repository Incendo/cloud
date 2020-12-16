//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * A single literal or argument component of a command
 *
 * @param <C> Command sender type
 * @since 1.3.0
 */
public final class CommandComponent<C> {

    private final CommandArgument<C, ?> argument;
    private final Description description;

    /**
     * Initializes a new CommandComponent
     *
     * @param commandArgument Command Component Argument
     * @param commandDescription Command Component Description
     */
    private CommandComponent(
            final @NonNull CommandArgument<C, ?> commandArgument,
            final @NonNull Description commandDescription
    ) {
        this.argument = commandArgument;
        this.description = commandDescription;
    }

    /**
     * Gets the command component argument details
     *
     * @return command component argument details
     */
    public @NonNull CommandArgument<C, ?> getArgument() {
        return this.argument;
    }

    /**
     * Gets the command component description
     *
     * @return command component description
     */
    public @NonNull Description getDescription() {
        return this.description;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArgument(), getDescription());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof CommandComponent) {
            CommandComponent<?> that = (CommandComponent<?>) o;
            return getArgument().equals(that.getArgument())
                    && getDescription().equals(that.getDescription());
        } else {
            return false;
        }
    }

    @Override
    public @NonNull String toString() {
        return String.format("%s{argument=%s,description=%s}", this.getClass().getSimpleName(),
                this.argument, this.description);
    }

    /**
     * Creates a new CommandComponent with the provided argument and description
     *
     * @param <C> Command sender type
     * @param commandArgument Command Component Argument
     * @param commandDescription Command Component Description
     * @return new CommandComponent
     */
    public static <C> @NonNull CommandComponent<C> of(
            final @NonNull CommandArgument<C, ?> commandArgument,
            final @NonNull Description commandDescription
    ) {
        return new CommandComponent<C>(commandArgument, commandDescription);
    }
}
