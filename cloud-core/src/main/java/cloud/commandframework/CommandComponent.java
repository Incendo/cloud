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
import cloud.commandframework.arguments.DefaultValue;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A single literal or argument component of a command
 *
 * @param <C> Command sender type
 * @since 1.3.0
 */
@API(status = API.Status.STABLE, since = "1.3.0")
public final class CommandComponent<C> {

    private final CommandArgument<C, ?> argument;
    private final ArgumentDescription description;
    private final boolean required;
    private final DefaultValue<C, ?> defaultValue;

    /**
     * Initializes a new CommandComponent
     *
     * @param argument     Command Component Argument
     * @param description  Command Component Description
     * @param required     Whether the command is required
     * @param defaultValue Default value used when an optional argument is omitted, should be {@code null} of
     * {@link #required()} is {@code true}
     */
    private CommandComponent(
            final @NonNull CommandArgument<C, ?> argument,
            final @NonNull ArgumentDescription description,
            final boolean required,
            final @Nullable DefaultValue<C, ?> defaultValue
    ) {
        this.argument = argument;
        this.description = description;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    /**
     * Gets the command component argument details
     *
     * @return command component argument details
     */
    public @NonNull CommandArgument<C, ?> argument() {
        return this.argument;
    }

    /**
     * Gets the command component description
     *
     * @return command component description
     * @deprecated for removal since 1.4.0. Use {@link #argumentDescription()} instead.
     */
    @Deprecated
    @API(status = API.Status.DEPRECATED, since = "1.4.0")
    public @NonNull Description description() {
        if (this.description instanceof Description) {
            return (Description) this.description;
        } else {
            return new Description(this.description.getDescription());
        }
    }

    /**
     * Gets the command component description
     *
     * @return command component description
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
    public @NonNull ArgumentDescription argumentDescription() {
        return this.description;
    }

    /**
     * Returns whether the argument is required
     * <p>
     * This always returns the opposite of {@link #optional()}.
     *
     * @return whether the argument is required
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public boolean required() {
        return this.required;
    }

    /**
     * Returns whether the argument is optional
     * <p>
     * This always returns the opposite of {@link #required()}.
     *
     * @return whether the argument is optional
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public boolean optional() {
        return !this.required;
    }

    /**
     * Returns the default value, if specified
     * <p>
     * This should always return {@code null} if {@link #required()} is {@code true}.
     *
     * @return the default value if specified, else {@code null}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @Nullable DefaultValue<C, ?> defaultValue() {
        return this.defaultValue;
    }

    /**
     * Returns whether this component has a default value
     *
     * @return {@code true} if the component has a default value, else {@code false}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public boolean hasDefaultValue() {
        return this.optional() && this.defaultValue() != null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.argument(), this.argumentDescription());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof CommandComponent) {
            final CommandComponent<?> that = (CommandComponent<?>) o;
            return this.argument().equals(that.argument())
                    && this.argumentDescription().equals(that.argumentDescription());
        } else {
            return false;
        }
    }

    @Override
    public @NonNull String toString() {
        return String.format("%s{argument=%s,description=%s,required=%s,defaultValue=%s}", this.getClass().getSimpleName(),
                this.argument, this.description, this.required, this.defaultValue
        );
    }

    /**
     * Returns a deep copy of this component.
     *
     * @return copy of the component
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull CommandComponent<C> copy() {
        return new CommandComponent<>(
                this.argument().copy(),
                this.description(),
                this.required(),
                this.defaultValue()
        );
    }

    /**
     * Creates a new required component with the provided argument and description
     *
     * @param <C>                Command sender type
     * @param commandArgument    Command Component Argument
     * @param commandDescription Command Component Description
     * @return new CommandComponent
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull CommandComponent<C> required(
            final @NonNull CommandArgument<C, ?> commandArgument,
            final @NonNull ArgumentDescription commandDescription
    ) {
        return new CommandComponent<C>(commandArgument, commandDescription, true, null);
    }

    /**
     * Creates a new optional component with the provided argument, description and default value
     *
     * @param <C>                Command sender type
     * @param commandArgument    Command Component Argument
     * @param commandDescription Command Component Description
     * @param defaultValue       The default value, or {@code null}
     * @return new CommandComponent
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull CommandComponent<C> optional(
            final @NonNull CommandArgument<C, ?> commandArgument,
            final @NonNull ArgumentDescription commandDescription,
            final @Nullable DefaultValue<C, ?> defaultValue
    ) {
        return new CommandComponent<C>(commandArgument, commandDescription, false, defaultValue);
    }

    /**
     * Creates a new optional component with the provided argument and description, and no default value
     *
     * @param <C>                Command sender type
     * @param commandArgument    Command Component Argument
     * @param commandDescription Command Component Description
     * @return new CommandComponent
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C> @NonNull CommandComponent<C> optional(
            final @NonNull CommandArgument<C, ?> commandArgument,
            final @NonNull ArgumentDescription commandDescription
    ) {
        return new CommandComponent<C>(commandArgument, commandDescription, false, null);
    }
}
