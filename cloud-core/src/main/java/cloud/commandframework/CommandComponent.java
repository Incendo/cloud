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

import cloud.commandframework.arguments.ComponentPreprocessor;
import cloud.commandframework.arguments.DefaultValue;
import cloud.commandframework.arguments.LiteralParser;
import cloud.commandframework.arguments.flags.CommandFlagParser;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserDescriptor;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.keys.CloudKey;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;

/**
 * A single literal or argument component of a command
 * <p>
 * This class does not preserve the type of the underlying parser.
 * If you need access to the underlying types, use {@link TypedCommandComponent}.
 *
 * @param <C> Command sender type
 * @since 1.3.0
 */
@API(status = API.Status.STABLE, since = "1.3.0")
public class CommandComponent<C> implements Comparable<CommandComponent<C>> {

    private final String name;
    private final ArgumentParser<C, ?> parser;
    private final ArgumentDescription description;
    private final ComponentType componentType;
    private final DefaultValue<C, ?> defaultValue;
    private final TypeToken<?> valueType;
    private final SuggestionProvider<C> suggestionProvider;
    private final Collection<@NonNull ComponentPreprocessor<C>> componentPreprocessors;

    private Command<C> owningCommand;

    /**
     * Creates a new mutable builder.
     *
     * @param <C> the command sender type
     * @param <T> the component value type
     * @return the builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C, T> @NonNull Builder<C, T> builder() {
        return new Builder<>();
    }

    /**
     * Creates a new mutable builder.
     *
     * @param <C> the command sender type
     * @param <T> the component value type
     * @param clazz the type of the component
     * @param name the name of the component
     * @return the builder
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static <C, T> @NonNull Builder<C, T> ofType(
            final @NonNull Class<T> clazz,
            final @NonNull String name
    ) {
        return CommandComponent.<C, T>builder().valueType(clazz).name(name);
    }

    CommandComponent(
        final @NonNull String name,
        final @NonNull ArgumentParser<C, ?> parser,
        final @NonNull TypeToken<?> valueType,
        final @NonNull ArgumentDescription description,
        final @NonNull ComponentType componentType,
        final @Nullable DefaultValue<C, ?> defaultValue,
        final @NonNull SuggestionProvider<C> suggestionProvider,
        final @NonNull Collection<@NonNull ComponentPreprocessor<C>> componentPreprocessors
    ) {
        this.name = name;
        this.parser = parser;
        this.valueType = valueType;
        this.componentType = componentType;
        this.description = description;
        this.defaultValue = defaultValue;
        this.suggestionProvider = suggestionProvider;
        this.componentPreprocessors = new ArrayList<>(componentPreprocessors);
    }

    /**
     * Returns the type of the values produced by the {@link #parser()}.
     *
     * @return the types of the values produced by the parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull TypeToken<?> valueType() {
        return this.valueType;
    }

    /**
     * Returns the parser.
     *
     * @return the parser
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull ArgumentParser<C, ?> parser() {
        return this.parser;
    }

    /**
     * Returns the name of the component.
     *
     * @return the component name
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public final @NonNull String name() {
        return this.name;
    }

    /**
     * Returns the aliases, if relevant.
     * <p>
     * Only literal components may have aliases. If this is a non-literal
     * component then an empty collection is returned.
     *
     * @return unmodifiable view of the aliases
     * @since 2.0.0
     */
    @SuppressWarnings("unchecked")
    @API(status = API.Status.STABLE, since = "2.0.0")
    public final @NonNull Collection<@NonNull String> aliases() {
        if (this.parser() instanceof LiteralParser) {
            return ((LiteralParser<C>) this.parser()).aliases();
        }
        return Collections.emptyList();
    }

    /**
     * Returns the aliases excluding the {@link #name() name}, if relevant.
     * <p>
     * Only literal components may have aliases. If this is a non-literal
     * component then an empty collection is returned.
     *
     * @return unmodifiable view of the aliases
     * @since 2.0.0
     */
    @SuppressWarnings("unchecked")
    @API(status = API.Status.STABLE, since = "2.0.0")
    public final @NonNull Collection<@NonNull String> alternativeAliases() {
        if (this.parser() instanceof LiteralParser) {
            return ((LiteralParser<C>) this.parser()).alternativeAliases();
        }
        return Collections.emptyList();
    }

    /**
     * Gets the command component description
     *
     * @return command component description
     * @since 1.4.0
     */
    @API(status = API.Status.STABLE, since = "1.4.0")
    public final @NonNull ArgumentDescription argumentDescription() {
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
    public final boolean required() {
        return this.componentType.required();
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
    public final boolean optional() {
        return this.componentType.optional();
    }

    /**
     * Returns the type of the component
     *
     * @return the component type
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public final @NonNull ComponentType type() {
        return this.componentType;
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
    public final boolean hasDefaultValue() {
        return this.optional() && this.defaultValue() != null;
    }

    /**
     * Returns the suggestion provider for this argument
     *
     * @return the suggestion provider
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public final @NonNull SuggestionProvider<C> suggestionProvider() {
        return this.suggestionProvider;
    }

    /**
     * Returns the command that owns this component.
     *
     * @return the owning command
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public final @MonotonicNonNull Command<C> owningCommand() {
        return this.owningCommand;
    }

    /**
     * Sets the command that owns this component.
     *
     * @param owningCommand the command
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public final void owningCommand(final @NonNull Command<C> owningCommand) {
        if (this.owningCommand != null) {
            throw new IllegalStateException("Cannot replace owning command");
        }
        this.owningCommand = owningCommand;
    }

    /**
     * Registers a new preprocessor. If all preprocessor has succeeding {@link ArgumentParseResult results}
     * that all return {@code true}, the component will be passed onto the parser.
     * <p>
     * It is important that the preprocessor doesn't pop any input. Instead, it should only peek.
     *
     * @param preprocessor Preprocessor
     * @return {@code this}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public final @NonNull @This CommandComponent<C> addPreprocessor(
            final @NonNull ComponentPreprocessor<C> preprocessor
    ) {
        this.componentPreprocessors.add(preprocessor);
        return this;
    }

    /**
     * Preprocess command input. This will immediately forward any failed component parse results.
     * If none fails, a {@code true} result will be returned
     *
     * @param context Command context
     * @param input   Remaining command input. None will be popped
     * @return parsing error, or argument containing {@code true}
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public final @NonNull ArgumentParseResult<Boolean> preprocess(
            final @NonNull CommandContext<C> context,
            final @NonNull CommandInput input
    ) {
        for (final ComponentPreprocessor<C> preprocessor : this.componentPreprocessors) {
            final ArgumentParseResult<Boolean> result = preprocessor.preprocess(
                    context,
                    input
            );
            if (result.getFailure().isPresent()) {
                return result;
            }
        }
        return ArgumentParseResult.success(true);
    }

    protected final @NonNull Collection<@NonNull ComponentPreprocessor<C>> preprocessors() {
        return Collections.unmodifiableCollection(this.componentPreprocessors);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.name(), this.valueType());
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof CommandComponent) {
            final CommandComponent<?> that = (CommandComponent<?>) o;
            return this.name().equals(that.name()) && this.valueType().equals(that.valueType());
        } else {
            return false;
        }
    }

    @Override
    public final @NonNull String toString() {
        return String.format(
                "%s{name=%s,type=%s,valueType=%s",
                this.getClass().getSimpleName(),
                this.name(),
                this.type(),
                this.valueType().getType().getTypeName()
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
                this.name(),
                this.parser(),
                this.valueType(),
                this.argumentDescription(),
                this.type(),
                this.defaultValue(),
                this.suggestionProvider(),
                this.preprocessors()
        );
    }

    @Override
    public final int compareTo(final @NonNull CommandComponent<C> other) {
        if (this.type() == ComponentType.LITERAL) {
            if (other.type() == ComponentType.LITERAL) {
                return this.name().compareTo(other.name());
            }
            return -1;
        }
        if (other.type() == ComponentType.LITERAL) {
            return 1;
        }
        return 0;
    }


    @API(status = API.Status.STABLE, since = "2.0.0")
    public static class Builder<C, T> {

        private CommandManager<C> commandManager;
        private String name;
        private ArgumentParser<C, T> parser;
        private ArgumentDescription description = ArgumentDescription.empty();
        private boolean required = true;
        private DefaultValue<C, ?> defaultValue;
        private TypeToken<T> valueType;
        private SuggestionProvider<C> suggestionProvider;
        private final Collection<@NonNull ComponentPreprocessor<C>> componentPreprocessors = new ArrayList<>();

        /**
         * Sets the command manager, which will be used to create a parser if none is provided.
         *
         * @param commandManager the command manager
         * @return {@code this}
         */
        public @NonNull @This Builder<C, T> commandManager(final @Nullable CommandManager<C> commandManager) {
            this.commandManager = commandManager;
            return this;
        }

        /**
         * Sets the name and the value type to the values contained in the given {@code cloudKey}.
         *
         * @param cloudKey the key
         * @return {@code this}
         */
        public @NonNull @This Builder<C, T> key(final @NonNull CloudKey<T> cloudKey) {
            return this.name(cloudKey.getName()).valueType(cloudKey.getType());
        }

        /**
         * Sets the {@code name}.
         *
         * @param name the name
         * @return {@code this}
         */
        public @NonNull @This Builder<C, T> name(final @NonNull String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the {@code valueType}.
         *
         * @param valueType the value type
         * @return {@code this}
         */
        public @NonNull @This Builder<C, T> valueType(final @NonNull TypeToken<T> valueType) {
            this.valueType = valueType;
            return this;
        }

        /**
         * Sets the {@code valueType}.
         *
         * @param valueType the value type
         * @return {@code this}
         */
        public @NonNull @This Builder<C, T> valueType(final @NonNull Class<T> valueType) {
            return this.valueType(TypeToken.get(valueType));
        }

        /**
         * Sets the {@code parser} and {@code valueType}.
         *
         * @param parserDescriptor descriptor of the parser
         * @return {@code this}
         */
        public @NonNull @This Builder<C, T> parser(final @NonNull ParserDescriptor<C, T> parserDescriptor) {
            return this.parser(parserDescriptor.parser()).valueType(parserDescriptor.valueType());
        }

        /**
         * Sets the {@code defaultValue}. This should not be set if {@code required} is {@code true}.
         *
         * @param defaultValue the default value
         * @return {@code this}
         */
        public @NonNull @This Builder<C, T> defaultValue(final @Nullable DefaultValue<C, T> defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Sets whether the component is required. Defaults to {@code true}.
         *
         * @param required whether component is required
         * @return {@code this}
         */
        public @NonNull @This Builder<C, T> required(final boolean required) {
            this.required = required;
            return this;
        }

        /**
         * Sets {@code required} to {@code true}.
         *
         * @return {@code this}
         */
        public @NonNull @This Builder<C, T> required() {
            return this.required(true);
        }

        /**
         * Sets {@code required} to {@code false}.
         *
         * @return {@code this}
         */
        public @NonNull @This Builder<C, T> optional() {
            return this.required(false);
        }

        /**
         * Sets {@code required} to {@code true} and updates the {@code defaultValue}.
         *
         * @param defaultValue the default value
         * @return {@code this}
         */
        public @NonNull @This Builder<C, T> optional(final @Nullable DefaultValue<C, T> defaultValue) {
            return this.optional().defaultValue(defaultValue);
        }

        /**
         * Sets the {@code description}. Defaults to {@link ArgumentDescription#empty()}.
         *
         * @param description the description
         * @return {@code this}
         */
        public @NonNull @This Builder<C, T> description(final @NonNull ArgumentDescription description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the {@code suggestionProvider}. Defaults to {@code parser}.
         *
         * @param suggestionProvider the suggestion provider
         * @return {@code this}
         */
        public @NonNull @This Builder<C, T> suggestionProvider(final @Nullable SuggestionProvider<C> suggestionProvider) {
            this.suggestionProvider = suggestionProvider;
            return this;
        }

        /**
         * Adds the {@code preprocessor}.
         *
         * @param preprocessor the preprocessor
         * @return {@code this}
         */
        public @NonNull @This Builder<C, T> preprocessor(final @NonNull ComponentPreprocessor<C> preprocessor) {
            this.componentPreprocessors.add(preprocessor);
            return this;
        }

        /**
         * Adds the {@code preprocessors}.
         *
         * @param preprocessors the preprocessors
         * @return {@code this}
         */
        public @NonNull @This Builder<C, T> preprocessors(final @NonNull Collection<ComponentPreprocessor<C>> preprocessors) {
            this.componentPreprocessors.addAll(preprocessors);
            return this;
        }

        /**
         * Sets the {@code parser}. If no parser is set and the {@code manager} has been set then the default
         * parser for the {@code valueType} will be used instead.
         * <p>
         * If both {@code parser} and {@code manager} are null it will not be possible to parse the component.
         *
         * @param parser the parser
         * @return {@code this}
         */
        public @NonNull @This Builder<C, T> parser(final @NonNull ArgumentParser<C, T> parser) {
            this.parser = parser;
            return this;
        }

        /**
         * Builds a command component using this builder.
         * <p>
         * Each invocation produces a unique component.
         *
         * @return the built component
         */
        public @NonNull TypedCommandComponent<C, T> build() {
            ArgumentParser<C, T> parser = null;
            if (this.parser != null) {
                parser = this.parser;
            } else if (this.commandManager != null) {
               parser = this.commandManager.parserRegistry()
                       .createParser(this.valueType, ParserParameters.empty())
                       .orElse(null);
            }
            if (parser == null) {
                parser = (c, i) -> ArgumentParseResult
                        .failure(new UnsupportedOperationException("No parser was specified"));
            }

            final ComponentType componentType;
            if (this.parser instanceof LiteralParser) {
                componentType = ComponentType.LITERAL;
            } else if (this.parser instanceof CommandFlagParser) {
                componentType = ComponentType.FLAG;
            } else if (this.required) {
                componentType = ComponentType.REQUIRED_VARIABLE;
            } else {
                componentType = ComponentType.OPTIONAL_VARIABLE;
            }

            final SuggestionProvider<C> suggestionProvider;
            if (this.suggestionProvider == null) {
                suggestionProvider = parser;
            } else {
                suggestionProvider = this.suggestionProvider;
            }

            return new TypedCommandComponent<>(
                    Objects.requireNonNull(this.name, "name"),
                    parser,
                    Objects.requireNonNull(this.valueType, "valueType"),
                    Objects.requireNonNull(this.description, "description"),
                    componentType,
                    this.defaultValue,
                    suggestionProvider,
                    Objects.requireNonNull(this.componentPreprocessors, "componentPreprocessors")
            );
        }
    }


    @API(status = API.Status.STABLE, since = "2.0.0")
    public enum ComponentType {
        /**
         * A literal component that can be parsed by its name, or any of its aliases.
         */
        LITERAL(true /* required */),
        /**
         * A required variable argument that is parsed into an object.
         */
        REQUIRED_VARIABLE(true /* required */),
        /**
         * An optional variable that is parsed into an object if present. May have a default fallback value.
         */
        OPTIONAL_VARIABLE(false /* required */),
        /**
         * An optional argument that represents multiple command flags.
         */
        FLAG(false /* required */);

        private final boolean required;

        ComponentType(final boolean required) {
            this.required = required;
        }

        /**
         * Returns whether the component is required
         * <p>
         * This always returns the opposite of {@link #optional()}.
         *
         * @return whether the component is required
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public boolean required() {
            return this.required;
        }

        /**
         * Returns whether the component is optional
         * <p>
         * This always returns the opposite of {@link #required()}.
         *
         * @return whether the component is optional
         * @since 2.0.0
         */
        @API(status = API.Status.STABLE, since = "2.0.0")
        public boolean optional() {
            return !this.required;
        }
    }
}
