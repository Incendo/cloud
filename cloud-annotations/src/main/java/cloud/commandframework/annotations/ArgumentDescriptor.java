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
import java.lang.reflect.Parameter;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0")
public final class ArgumentDescriptor {

    private final Parameter parameter;
    private final String name;
    private final String parserName;
    private final String suggestions;
    private final String defaultValue;
    private final ArgumentDescription description;

    /**
     * Creates a new builder.
     *
     * @return the created builder
     */
    public static @NonNull Builder builder() {
        return new Builder();
    }

    private ArgumentDescriptor(
            final @NonNull Parameter parameter,
            final @NonNull String name,
            final @Nullable String parserName,
            final @Nullable String suggestions,
            final @Nullable String defaultValue,
            final @Nullable ArgumentDescription description
    ) {
        this.parameter = parameter;
        this.name = name;
        this.parserName = parserName;
        this.suggestions = suggestions;
        this.defaultValue = defaultValue;
        this.description = description;
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
     * Returns the argument name
     *
     * @return the argument name
     */
    public @NonNull String name() {
        if (this.name.equals(AnnotationParser.INFERRED_ARGUMENT_NAME)) {
            return this.parameter.getName();
        }
        return this.name;
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
     * Returns the default value.
     *
     * @return the default value, or {@code null}
     */
    public @Nullable String defaultValue() {
        return this.defaultValue;
    }

    /**
     * Returns the description of the argument.
     *
     * @return the argument description, or {@code null}
     */
    public @Nullable ArgumentDescription description() {
        return this.description;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ArgumentDescriptor that = (ArgumentDescriptor) object;
        return Objects.equals(this.parameter, that.parameter)
                && Objects.equals(this.name, that.name)
                && Objects.equals(this.parserName, that.parserName)
                && Objects.equals(this.suggestions, that.suggestions)
                && Objects.equals(this.defaultValue, that.defaultValue)
                && Objects.equals(this.description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.parameter, this.name, this.parserName, this.suggestions, this.defaultValue, this.description);
    }


    public static final class Builder {

        private final Parameter parameter;
        private final String name;
        private final String parserName;
        private final String suggestions;
        private final String defaultValue;
        private final ArgumentDescription description;

        private Builder(
                final @Nullable Parameter parameter,
                final @Nullable String name,
                final @Nullable String parserName,
                final @Nullable String suggestions,
                final @Nullable String defaultValue,
                final @Nullable ArgumentDescription description
        ) {
            this.parameter = parameter;
            this.name = name;
            this.parserName = parserName;
            this.suggestions = suggestions;
            this.defaultValue = defaultValue;
            this.description = description;
        }

        private Builder() {
            this(null, null, null, null, null, null);
        }

        /**
         * Returns an updated builder with the given {@code parameter}.
         *
         * @param parameter the new parameter
         * @return the builder containing the updated parameter
         */
        public @NonNull Builder parameter(final @NonNull Parameter parameter) {
            return new Builder(parameter, this.name, this.parserName, this.suggestions, this.defaultValue, this.description);
        }

        /**
         * Returns an updated builder with the given {@code name}.
         * <p>
         * This can be set to {@link AnnotationParser#INFERRED_ARGUMENT_NAME} to infer the argument name
         * from the parameter name.
         *
         * @param name the new name
         * @return the builder containing the updated name
         */
        public @NonNull Builder name(final @NonNull String name) {
            return new Builder(this.parameter, name, this.parserName, this.suggestions, this.defaultValue, this.description);
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
            return new Builder(this.parameter, this.name, nullableName, this.suggestions, this.defaultValue, this.description);
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
            return new Builder(this.parameter, this.name, this.parserName, nullableName, this.defaultValue, this.description);
        }

        /**
         * Returns an updated builder with the given {@code defaultValue}.
         *
         * @param defaultValue the new default value
         * @return the builder containing the updated default value
         */
        public @NonNull Builder defaultValue(final @Nullable String defaultValue) {
            final String nullableName;
            if (defaultValue != null && defaultValue.isEmpty()) {
                nullableName = null;
            } else {
                nullableName = defaultValue;
            }
            return new Builder(this.parameter, this.name, this.parserName, this.suggestions, nullableName, this.description);
        }

        /**
         * Returns an updated builder with the given {@code description}.
         *
         * @param description the new description
         * @return the builder containing the updated description
         */
        public @NonNull Builder description(final @Nullable ArgumentDescription description) {
            return new Builder(this.parameter, this.name, this.parserName, this.suggestions, this.defaultValue, description);
        }

        /**
         * Returns the build argument descriptor.
         *
         * @return the argument descriptor
         */
        public @NonNull ArgumentDescriptor build() {
            return new ArgumentDescriptor(
                    Objects.requireNonNull(this.parameter, "parameter"),
                    Objects.requireNonNull(this.name, "name"),
                    this.parserName,
                    this.suggestions,
                    this.defaultValue,
                    this.description
            );
        }
    }
}
