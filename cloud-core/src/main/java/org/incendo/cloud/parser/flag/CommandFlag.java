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
package org.incendo.cloud.parser.flag;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.component.TypedCommandComponent;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.permission.Permission;

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
    private final @NonNull String @NonNull[] aliases;
    private final @NonNull Description description;
    private final @NonNull Permission permission;
    private final @NonNull FlagMode mode;

    private final @Nullable TypedCommandComponent<?, T> commandComponent;

    private CommandFlag(
            final @NonNull String name,
            final @NonNull String @NonNull[] aliases,
            final @NonNull Description description,
            final @NonNull Permission permission,
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
     * @param <C> command sender type
     * @param name flag name
     * @return new {@link Builder}
     */
    @API(status = API.Status.STABLE)
    public static <C> @NonNull Builder<C, Void> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Returns the name of the flag.
     *
     * @return flag name
     */
    public @NonNull String name() {
        return this.name;
    }

    /**
     * Returns the flag aliases, not including the {@link #name()}.
     *
     * @return flag aliases
     */
    public @NonNull Collection<@NonNull String> aliases() {
        return Arrays.asList(this.aliases);
    }

    /**
     * Returns the {@link FlagMode mode} of this flag.
     *
     * @return the flag mode
     */
    @API(status = API.Status.STABLE)
    public @NonNull FlagMode mode() {
        return this.mode;
    }

    /**
     * Returns the flag description.
     *
     * @return flag description
     */
    @API(status = API.Status.STABLE)
    public @NonNull Description description() {
        return this.description;
    }

    /**
     * Returns the command component, if this is a value flag.
     *
     * @return command component, or {@code null} if this is a presence flag
     */
    @API(status = API.Status.STABLE)
    public @Nullable CommandComponent<?> commandComponent() {
        return this.commandComponent;
    }

    /**
     * Returns the permission required to use this flag, if it exists.
     *
     * @return Command permission, or {@code null}
     */
    @API(status = API.Status.STABLE)
    public Permission permission() {
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
        return this.name().equals(that.name());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name());
    }


    @API(status = API.Status.STABLE)
    public static final class Builder<C, T> {

        private final String name;
        private final String[] aliases;
        private final Description description;
        private final Permission permission;
        private final TypedCommandComponent<C, T> commandComponent;
        private final FlagMode mode;

        private Builder(
                final @NonNull String name,
                final @NonNull String[] aliases,
                final @NonNull Description description,
                final @NonNull Permission permission,
                final @Nullable TypedCommandComponent<C, T> commandComponent,
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
            this(name, new String[0], Description.empty(), Permission.empty(), null, FlagMode.SINGLE);
        }

        /**
         * Create a new builder instance using the given flag aliases.
         * These may at most be one character in length
         *
         * @param aliases Flag aliases
         * @return New builder instance
         */
        public @NonNull Builder<C, T> withAliases(final @NonNull String... aliases) {
            return this.withAliases(Arrays.asList(aliases));
        }

        /**
         * Create a new builder instance using the given flag aliases.
         * These may at most be one character in length
         *
         * @param aliases Flag aliases
         * @return New builder instance
         */
        @API(status = API.Status.STABLE)
        public @NonNull Builder<C, T> withAliases(final @NonNull Collection<@NonNull String> aliases) {
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
         */
        @API(status = API.Status.STABLE)
        public @NonNull Builder<C, T> withDescription(final @NonNull Description description) {
            return new Builder<>(this.name, this.aliases, description, this.permission, this.commandComponent, this.mode);
        }

        /**
         * Create a new builder instance using the given command component
         *
         * @param component Command component
         * @param <N>     New component type
         * @return New builder instance
         */
        public <N> @NonNull Builder<C, N> withComponent(final @NonNull TypedCommandComponent<C, N> component) {
            return new Builder<>(this.name, this.aliases, this.description, this.permission, component, this.mode);
        }

        /**
         * Create a new builder instance using the given command component
         *
         * @param parserDescriptor descriptor of the parser
         * @param <N>              new component type
         * @return New builder instance
         */
        public <N> @NonNull Builder<C, N> withComponent(final @NonNull ParserDescriptor<? super C, N> parserDescriptor) {
            return this.withComponent(CommandComponent.builder(this.name, parserDescriptor));
        }

        /**
         * Create a new builder instance using the given command component
         *
         * @param builder Command component builder. {@link CommandComponent.Builder#build()} will be invoked.
         * @param <N>     New component type
         * @return New builder instance
         */
        public <N> @NonNull Builder<C, N> withComponent(final CommandComponent.@NonNull Builder<C, N> builder) {
            return this.withComponent(builder.build());
        }

        /**
         * Create a new builder instance using the given flag permission
         *
         * @param permission Flag permission
         * @return New builder instance
         */
        @API(status = API.Status.STABLE)
        public @NonNull Builder<C, T> withPermission(final @NonNull Permission permission) {
            return new Builder<>(this.name, this.aliases, this.description, permission, this.commandComponent, this.mode);
        }

        /**
         * Create a new builder instance using the given flag permission
         *
         * @param permissionString Flag permission
         * @return New builder instance
         */
        @API(status = API.Status.STABLE)
        public @NonNull Builder<C, T> withPermission(final @NonNull String permissionString) {
            return this.withPermission(Permission.of(permissionString));
        }

        /**
         * Marks the flag as {@link FlagMode#REPEATABLE}.
         *
         * @return new builder instance
         */
        @API(status = API.Status.STABLE)
        public @NonNull Builder<C, T> asRepeatable() {
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


    @API(status = API.Status.STABLE)
    public enum FlagMode {
        /**
         * Only a single value can be provided for the flag, and should be extracted
         * using {@link FlagContext#get(CommandFlag)}.
         */
        SINGLE,
        /**
         * Multiple values can be provided for the flag, and should be extracted
         * using {@link FlagContext#getAll(CommandFlag)}.
         */
        REPEATABLE
    }
}
