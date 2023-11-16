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
package cloud.commandframework.annotations;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.permission.CommandPermission;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0")
public final class FlagDescriptor implements Descriptor {

    private final Parameter parameter;
    private final String name;
    private final Set<@NonNull String> aliases;
    private final String parserName;
    private final String suggestions;
    private final CommandPermission permission;
    private final ArgumentDescription description;
    private final boolean repeatable;

    /**
     * Creates a new builder.
     *
     * @return the created builder
     */
    public static @NonNull Builder builder() {
        return new Builder();
    }

    private FlagDescriptor(
            final @NonNull Parameter parameter,
            final @NonNull String name,
            final @NonNull Collection<@NonNull String> aliases,
            final @Nullable String parserName,
            final @Nullable String suggestions,
            final @Nullable CommandPermission permission,
            final @Nullable ArgumentDescription description,
            final boolean repeatable
    ) {
        this.parameter = parameter;
        this.name = name;
        this.aliases = Collections.unmodifiableSet(new HashSet<>(aliases));
        this.parserName = parserName;
        this.suggestions = suggestions;
        this.permission = permission;
        this.description = description;
        this.repeatable = repeatable;
    }

    /**
     * Returns the parameter.
     *
     * @return the parameter
     */
    public @NonNull Parameter parameter() {
        return this.parameter;
    }

    /**
     * Returns the flag name.
     *
     * @return the flag name
     */
    @Override
    public @NonNull String name() {
        if (this.name.equals(AnnotationParser.INFERRED_ARGUMENT_NAME)) {
            return this.parameter.getName();
        }
        return this.name;
    }

    /**
     * Returns an unmodifiable view of the flag aliases.
     *
     * @return the flag aliases
     */
    public @NonNull Collection<@NonNull String> aliases() {
        return this.aliases;
    }

    /**
     * Returns the name of the parser to use. If {@code null} the default parser for the parameter type will be used.
     *
     * @return the parser name, or {@code null}
     */
    public @Nullable String parserName() {
        return this.parserName;
    }

    /**
     * Returns the name of the suggestion provider to use. If the string is {@code null}, the default
     * provider for the argument parser will be used. Otherwise,
     * the {@link cloud.commandframework.arguments.parser.ParserRegistry} instance in the
     * {@link cloud.commandframework.CommandManager} will be queried for a matching suggestion provider.
     * <p>
     * For this to work, the suggestion needs to be registered in the parser registry. To do this, use
     * {@link cloud.commandframework.arguments.parser.ParserRegistry#registerSuggestionProvider(String, SuggestionProvider)}.
     * The registry instance can be retrieved using {@link cloud.commandframework.CommandManager#parserRegistry()}.
     *
     * @return the name of the suggestion provider, or {@code null}
     */
    public @Nullable String suggestions() {
        return this.suggestions;
    }

    /**
     * Returns the permission of the flag.
     *
     * @return the flag permission, or {@code null}
     */
    public @Nullable CommandPermission permission() {
        return this.permission;
    }

    /**
     * Returns the description of the flag.
     *
     * @return the flag description, or {@code null}
     */
    public @Nullable ArgumentDescription description() {
        return this.description;
    }

    /**
     * Returns whether the flag is repeatable.
     *
     * @return whether the flag is repeatable
     */
    public boolean repeatable() {
        return this.repeatable;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        FlagDescriptor that = (FlagDescriptor) object;
        return Objects.equals(this.parameter, that.parameter)
                && Objects.equals(this.name, that.name)
                && Objects.equals(this.aliases, that.aliases)
                && Objects.equals(this.parserName, that.parserName)
                && Objects.equals(this.suggestions, that.suggestions)
                && Objects.equals(this.permission, that.permission)
                && Objects.equals(this.description, that.description)
                && this.repeatable == that.repeatable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.parameter,
                this.name,
                this.aliases,
                this.parserName,
                this.suggestions,
                this.permission,
                this.description,
                this.repeatable
        );
    }


    @API(status = API.Status.STABLE, since = "2.0.0")
    public static final class Builder {

        private final Parameter parameter;
        private final String name;
        private final Collection<String> aliases;
        private final String parserName;
        private final String suggestions;
        private final CommandPermission permission;
        private final ArgumentDescription description;
        private final boolean repeatable;

        private Builder(
                final @Nullable Parameter parameter,
                final @Nullable String name,
                final @NonNull Collection<String> aliases,
                final @Nullable String parserName,
                final @Nullable String suggestions,
                final @Nullable CommandPermission permission,
                final @Nullable ArgumentDescription description,
                final boolean repeatable
        ) {
            this.parameter = parameter;
            this.name = name;
            this.aliases = aliases;
            this.parserName = parserName;
            this.suggestions = suggestions;
            this.permission = permission;
            this.description = description;
            this.repeatable = repeatable;
        }

        private Builder() {
            this(null, null, Collections.emptySet(), null, null, null, null, false);
        }

        /**
         * Returns an updated builder with the given {@code parameter}.
         *
         * @param parameter the new parameter
         * @return the builder containing the updated parameter
         */
        public @NonNull Builder parameter(final @NonNull Parameter parameter) {
            return new Builder(
                    parameter,
                    this.name,
                    this.aliases,
                    this.parserName,
                    this.suggestions,
                    this.permission,
                    this.description,
                    this.repeatable
            );
        }

        /**
         * Returns an updated builder with the given {@code name}.
         * <p>
         * This can be set to {@link AnnotationParser#INFERRED_ARGUMENT_NAME} to infer the flag name
         * from the parameter name.
         *
         * @param name the new name
         * @return the builder containing the updated name
         */
        public @NonNull Builder name(final @NonNull String name) {
            return new Builder(
                    this.parameter,
                    name,
                    this.aliases,
                    this.parserName,
                    this.suggestions,
                    this.permission,
                    this.description,
                    this.repeatable
            );
        }

        /**
         * Returns an updated builder with the given {@code aliases}.
         *
         * @param aliases the new aliases
         * @return the builder containing the updated aliases
         */
        public @NonNull Builder aliases(final @NonNull Collection<@NonNull String> aliases) {
            return new Builder(
                    this.parameter,
                    this.name,
                    aliases,
                    this.parserName,
                    this.suggestions,
                    this.permission,
                    this.description,
                    this.repeatable
            );
        }

        /**
         * Returns an updated builder with the given {@code parserName}.
         *
         * @param parserName the new parserName
         * @return the builder containing the updated parserName
         */
        public @NonNull Builder parserName(final @Nullable String parserName) {
            final String nullableName;
            if (parserName != null && parserName.isEmpty()) {
                nullableName = null;
            } else {
                nullableName = parserName;
            }
            return new Builder(
                    this.parameter,
                    this.name,
                    this.aliases,
                    nullableName,
                    this.suggestions,
                    this.permission,
                    this.description,
                    this.repeatable
            );
        }

        /**
         * Returns an updated builder with the given {@code suggestions}.
         *
         * @param suggestions the new suggestions
         * @return the builder containing the updated suggestions
         */
        public @NonNull Builder suggestions(final @Nullable String suggestions) {
            final String nullableName;
            if (suggestions != null && suggestions.isEmpty()) {
                nullableName = null;
            } else {
                nullableName = suggestions;
            }
            return new Builder(
                    this.parameter,
                    this.name,
                    this.aliases,
                    this.parserName,
                    nullableName,
                    this.permission,
                    this.description,
                    this.repeatable
            );
        }

        /**
         * Returns an updated builder with the given {@code permission}.
         *
         * @param permission the new permission
         * @return the builder containing the updated permission
         */
        public @NonNull Builder permission(final @Nullable CommandPermission permission) {
            return new Builder(
                    this.parameter,
                    this.name,
                    this.aliases,
                    this.parserName,
                    this.suggestions,
                    permission,
                    this.description,
                    this.repeatable
            );
        }

        /**
         * Returns an updated builder with the given {@code description}.
         *
         * @param description the new description
         * @return the builder containing the updated description
         */
        public @NonNull Builder description(final @Nullable ArgumentDescription description) {
            return new Builder(
                    this.parameter,
                    this.name,
                    this.aliases,
                    this.parserName,
                    this.suggestions,
                    this.permission,
                    description,
                    this.repeatable
            );
        }

        /**
         * Returns an updated builder with the given {@code repeatable} value.
         *
         * @param repeatable whether the flag is repeatable
         * @return the builder containing the updated repeatable value
         */
        public @NonNull Builder repeatable(final boolean repeatable) {
            return new Builder(
                    this.parameter,
                    this.name,
                    this.aliases,
                    this.parserName,
                    this.suggestions,
                    this.permission,
                    this.description,
                    repeatable
            );
        }

        /**
         * Returns the built flag descriptor.
         *
         * @return the flag descriptor
         */
        public @NonNull FlagDescriptor build() {
            return new FlagDescriptor(
                    Objects.requireNonNull(this.parameter, "parameter"),
                    Objects.requireNonNull(this.name, "name"),
                    Objects.requireNonNull(this.aliases, "aliases"),
                    this.parserName,
                    this.suggestions,
                    this.permission,
                    this.description,
                    this.repeatable
            );
        }
    }
}
