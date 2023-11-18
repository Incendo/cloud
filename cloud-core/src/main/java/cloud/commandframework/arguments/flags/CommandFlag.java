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
package cloud.commandframework.arguments.flags;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.CommandComponent;
import cloud.commandframework.TypedCommandComponent;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.permission.CommandPermission;
import cloud.commandframework.permission.Permission;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A flag is an optional command component that may have an associated parser,
 * and is identified by its name. Essentially, it's a mixture of a command literal
 * and an optional variable command component.
 *
 * @param <T> Command component type. {@link Void} is used when no component is present.
 */
@SuppressWarnings({"unused", "UnusedTypeParameter"})
@API(status = API.Status.STABLE)
public final class CommandFlag<T> {

    private final @NonNull String name;
    private final @NonNull String @NonNull [] aliases;
    private final @NonNull ArgumentDescription description;
    private final @NonNull CommandPermission permission;
    private final @NonNull FlagMode mode;

    private final @Nullable TypedCommandComponent<?, T> commandComponent;

    private CommandFlag(
            final @NonNull String name,
            final @NonNull String @NonNull [] aliases,
            final @NonNull ArgumentDescription description,
            final @NonNull CommandPermission permission,
            final @Nullable TypedCommandComponent<?, T> commandComponent,
            final @NonNull FlagMode mode
    ) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.aliases = Objects.requireNonNull(aliases, "aliases cannot be null");
        this.description = Objects.requireNonNull(description, "description cannot be null");
        this.permission = Objects.requireNonNull(permission, "permission cannot be null");
        this.commandComponent = commandComponent;
        this.mode = Objects.requireNonNull(mode, "mode cannot be null");
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name flag name
     * @return new {@link Builder}
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    public static @NonNull Builder<Void> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Get the flag name
     *
     * @return Flag name
     */
    public @NonNull String getName() {
        return this.name;
    }

    /**
     * Get all flag aliases. This does not include the flag name
     *
     * @return Flag aliases
     */
    public @NonNull Collection<@NonNull String> getAliases() {
        return Arrays.asList(this.aliases);
    }

    /**
     * Returns the {@link FlagMode mode} of this flag.
     *
     * @return the flag mode
     */
    @API(status = API.Status.STABLE, since = "1.7.0")
    public @NonNull FlagMode mode() {
        return this.mode;
    }

    /**
     * Get the flag description.
     *
     * @return Flag description
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
    public @NonNull ArgumentDescription getArgumentDescription() {
        return this.description;
    }

    /**
     * Returns the command component, if it exists
     *
     * @return Command component, or {@code null}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @Nullable CommandComponent<?> commandComponent() {
        return this.commandComponent;
    }

    /**
     * Get the permission required to use this flag, if it exists
     *
     * @return Command permission, or {@code null}
     * @since 1.6.0
     */
    @API(status = API.Status.STABLE, since = "1.6.0")
    public CommandPermission permission() {
        return this.permission;
    }

    @Override
    public String toString() {
        return String.format("--%s", this.name);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final CommandFlag<?> that = (CommandFlag<?>) o;
        return this.getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getName());
    }


    @API(status = API.Status.STABLE)
    public static final class Builder<T> {

        private final String name;
        private final String[] aliases;
        private final ArgumentDescription description;
        private final CommandPermission permission;
        private final TypedCommandComponent<?, T> commandComponent;
        private final FlagMode mode;

        private Builder(
                final @NonNull String name,
                final @NonNull String[] aliases,
                final @NonNull ArgumentDescription description,
                final @NonNull CommandPermission permission,
                final @Nullable TypedCommandComponent<?, T> commandComponent,
                final @NonNull FlagMode mode
        ) {
            this.name = name;
            this.aliases = aliases;
            this.description = description;
            this.permission = permission;
            this.commandComponent = commandComponent;
            this.mode = mode;
        }

        private Builder(final @NonNull String name) {
            this(name, new String[0], ArgumentDescription.empty(), Permission.empty(), null, FlagMode.SINGLE);
        }

        /**
         * Create a new builder instance using the given flag aliases.
         * These may at most be one character in length
         *
         * @param aliases Flag aliases
         * @return New builder instance
         */
        public @NonNull Builder<T> withAliases(final @NonNull String... aliases) {
            return this.withAliases(Arrays.asList(aliases));
        }

        /**
         * Create a new builder instance using the given flag aliases.
         * These may at most be one character in length
         *
         * @param aliases Flag aliases
         * @return New builder instance
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public @NonNull Builder<T> withAliases(final @NonNull Collection<@NonNull String> aliases) {
            final Set<String> filteredAliases = new HashSet<>();
            for (final String alias : aliases) {
                if (alias.isEmpty()) {
                    continue;
                }
                if (alias.length() > 1) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Alias '%s' has name longer than one character. This is not allowed",
                                    alias
                            )
                    );
                }
                filteredAliases.add(alias);
            }
            return new Builder<>(
                    this.name,
                    filteredAliases.toArray(new String[0]),
                    this.description,
                    this.permission,
                    this.commandComponent,
                    this.mode
            );
        }

        /**
         * Create a new builder instance using the given flag description
         *
         * @param description Flag description
         * @return New builder instance
         * @since 1.4.0
         */
        @API(status = API.Status.STABLE, since = "1.4.0")
        public @NonNull Builder<T> withDescription(final @NonNull ArgumentDescription description) {
            return new Builder<>(this.name, this.aliases, description, this.permission, this.commandComponent, this.mode);
        }

        /**
         * Create a new builder instance using the given command component
         *
         * @param component Command component
         * @param <N>     New component type
         * @return New builder instance
         */
        public <N> @NonNull Builder<N> withComponent(final @NonNull TypedCommandComponent<?, N> component) {
            return new Builder<>(this.name, this.aliases, this.description, this.permission, component, this.mode);
        }

        // TODO(City): Remove this.
        /**
         * Create a new builder instance using the given command component
         *
         * @param component Command component
         * @param <N>     New component type
         * @return New builder instance
         */
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <N> @NonNull Builder<N> withComponent(final @NonNull CommandArgument<?, N> component) {
            final CommandComponent.Builder builder = CommandComponent.builder();
            return new Builder<>(
                    this.name,
                    this.aliases,
                    this.description,
                    this.permission,
                    builder.key(component.getKey()).parser(component.parserDescriptor()).build(),
                    this.mode
            );
        }

        /**
         * Create a new builder instance using the given command component
         *
         * @param builder Command component builder. {@link CommandComponent.Builder#build()} will be invoked.
         * @param <N>     New component type
         * @return New builder instance
         */
        public <N> @NonNull Builder<N> withComponent(final CommandComponent.@NonNull Builder<?, N> builder) {
            return this.withComponent(builder.build());
        }

        /**
         * Create a new builder instance using the given flag permission
         *
         * @param permission Flag permission
         * @return New builder instance
         * @since 1.6.0
         */
        @API(status = API.Status.STABLE, since = "1.6.0")
        public @NonNull Builder<T> withPermission(final @NonNull CommandPermission permission) {
            return new Builder<>(this.name, this.aliases, this.description, permission, this.commandComponent, this.mode);
        }

        /**
         * Marks the flag as {@link FlagMode#REPEATABLE}.
         *
         * @return new builder instance
         * @since 1.7.0
         */
        @API(status = API.Status.STABLE, since = "1.7.0")
        public @NonNull Builder<T> asRepeatable() {
            return new Builder<>(
                    this.name,
                    this.aliases,
                    this.description,
                    this.permission,
                    this.commandComponent,
                    FlagMode.REPEATABLE
            );
        }

        /**
         * Build a new command flag instance
         *
         * @return Constructed instance
         */
        public @NonNull CommandFlag<T> build() {
            return new CommandFlag<>(
                    this.name,
                    this.aliases,
                    this.description,
                    this.permission,
                    this.commandComponent,
                    this.mode
            );
        }
    }


    @API(status = API.Status.STABLE, since = "1.7.0")
    public enum FlagMode {
        /**
         * Only a single value can be provided for the flag, and should be extracted
         * using {@link FlagContext#get(CommandFlag)}.
         */
        SINGLE,
        /**
         * Multiple values can be provided for the flag, and sdhould be extracted
         * using {@link FlagContext#getAll(CommandFlag)}.
         */
        REPEATABLE
    }
}
