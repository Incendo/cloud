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
package cloud.commandframework.component;

import cloud.commandframework.CommandManager;
import cloud.commandframework.component.preprocessor.ComponentPreprocessor;
import cloud.commandframework.component.preprocessor.PreprocessorHolder;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.description.Describable;
import cloud.commandframework.description.Description;
import cloud.commandframework.key.CloudKey;
import cloud.commandframework.parser.ArgumentParseResult;
import cloud.commandframework.parser.ArgumentParser;
import cloud.commandframework.parser.LiteralParser;
import cloud.commandframework.parser.ParserDescriptor;
import cloud.commandframework.parser.ParserParameters;
import cloud.commandframework.parser.flag.CommandFlagParser;
import cloud.commandframework.suggestion.SuggestionProvider;
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
 * @param <C> command sender type
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public class CommandComponent<C> implements Comparable<CommandComponent<C>>, PreprocessorHolder<C>, Describable {

    private final String name;
    private final ArgumentParser<C, ?> parser;
    private final Description description;
    private final ComponentType componentType;
    private final DefaultValue<C, ?> defaultValue;
    private final TypeToken<?> valueType;
    private final SuggestionProvider<C> suggestionProvider;
    private final Collection<@NonNull ComponentPreprocessor<C>> componentPreprocessors;

    /**
     * Creates a new mutable builder.
     *
     * <p>The builder returns a {@link TypedCommandComponent} which can be used to retrieve parsed values from the
     * {@link CommandContext}.</p>
     *
     * @param <C> command sender type
     * @param <T> component value type
     * @return the builder
     */
    public static <C, T> @NonNull Builder<C, T> builder() {
        return new Builder<>();
    }

    /**
     * Creates a new mutable builder.
     *
     * <p>The builder returns a {@link TypedCommandComponent} which can be used to retrieve parsed values from the
     * {@link CommandContext}.</p>
     *
     * @param <C>   command sender type
     * @param <T>   component value type
     * @param clazz type of the component
     * @param name  name of the component
     * @return the builder
     */
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
            final @NonNull Description description,
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
     */
    public @NonNull TypeToken<?> valueType() {
        return this.valueType;
    }

    /**
     * Returns the parser.
     *
     * @return the parser
     */
    public @NonNull ArgumentParser<C, ?> parser() {
        return this.parser;
    }

    /**
     * Returns the name of the component.
     *
     * @return the component name
     */
    public final @NonNull String name() {
        return this.name;
    }

    /**
     * Returns the aliases, if relevant.
     *
     * <p>Only literal components may have aliases. If this is a non-literal
     * component then an empty collection is returned.</p>
     *
     * @return unmodifiable view of the aliases
     */
    @SuppressWarnings("unchecked")
    public final @NonNull Collection<@NonNull String> aliases() {
        if (this.parser() instanceof LiteralParser) {
            return ((LiteralParser<C>) this.parser()).aliases();
        }
        return Collections.emptyList();
    }

    /**
     * Returns the aliases excluding the {@link #name() name}, if relevant.
     *
     * <p>Only literal components may have aliases. If this is a non-literal
     * component then an empty collection is returned.</p>
     *
     * @return unmodifiable view of the aliases
     */
    @SuppressWarnings("unchecked")
    public final @NonNull Collection<@NonNull String> alternativeAliases() {
        if (this.parser() instanceof LiteralParser) {
            return ((LiteralParser<C>) this.parser()).alternativeAliases();
        }
        return Collections.emptyList();
    }

    @Override
    public final @NonNull Description description() {
        return this.description;
    }

    /**
     * Returns whether the argument is required.
     *
     * <p>This always returns the opposite of {@link #optional()}.</p>
     *
     * @return whether the argument is required
     */
    public final boolean required() {
        return this.componentType.required();
    }

    /**
     * Returns whether the argument is optional.
     *
     * <p>This always returns the opposite of {@link #required()}.</p>
     *
     * @return whether the argument is optional
     */
    public final boolean optional() {
        return this.componentType.optional();
    }

    /**
     * Returns the type of the component.
     *
     * @return the component type
     */
    public final @NonNull ComponentType type() {
        return this.componentType;
    }

    /**
     * Returns the default value, if specified.
     * <p>
     * This should always return {@code null} if {@link #required()} is {@code true}.
     *
     * @return the default value if specified, else {@code null}
     */
    public @Nullable DefaultValue<C, ?> defaultValue() {
        return this.defaultValue;
    }

    /**
     * Returns whether this component has a default value.
     *
     * @return {@code true} if the component has a default value, else {@code false}
     */
    public final boolean hasDefaultValue() {
        return this.optional() && this.defaultValue() != null;
    }

    /**
     * Returns the suggestion provider for this argument.
     *
     * @return the suggestion provider
     */
    public final @NonNull SuggestionProvider<C> suggestionProvider() {
        return this.suggestionProvider;
    }

    /**
     * Registers a new preprocessor.
     *
     * <p>If all preprocessor has succeeding {@link ArgumentParseResult results}
     * that all return {@code true}, the component will be passed onto the parser.</p>
     *
     * <p>It is important that the preprocessor doesn't pop any input. Instead, it should only peek.</p>
     *
     * @param preprocessor Preprocessor
     * @return {@code this}
     */
    public final @This @NonNull CommandComponent<C> addPreprocessor(
            final @NonNull ComponentPreprocessor<C> preprocessor
    ) {
        this.componentPreprocessors.add(Objects.requireNonNull(preprocessor, "preprocessor"));
        return this;
    }

    /**
     * Preprocess command input.
     *
     * <p>This will immediately forward any failed component parse results.
     * If none fails, a {@code true} result will be returned.</p>
     *
     * @param context Command context
     * @param input   Remaining command input. None will be popped
     * @return parsing error, or argument containing {@code true}
     */
    public final @NonNull ArgumentParseResult<Boolean> preprocess(
            final @NonNull CommandContext<C> context,
            final @NonNull CommandInput input
    ) {
        for (final ComponentPreprocessor<C> preprocessor : this.componentPreprocessors) {
            final ArgumentParseResult<Boolean> result = preprocessor.preprocess(
                    context,
                    input
            );
            if (result.failure().isPresent()) {
                return result;
            }
        }
        return ArgumentParseResult.success(true);
    }

    @Override
    public final @NonNull Collection<@NonNull ComponentPreprocessor<C>> preprocessors() {
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


    @SuppressWarnings("unchecked")
    @API(status = API.Status.STABLE, since = "2.0.0")
    public static class Builder<C, T> {

        private CommandManager<C> commandManager;
        private String name;
        private ArgumentParser<C, T> parser;
        private Description description = Description.empty();
        private boolean required = true;
        private DefaultValue<C, ?> defaultValue;
        private TypeToken<T> valueType;
        private SuggestionProvider<C> suggestionProvider;
        private final Collection<@NonNull ComponentPreprocessor<C>> componentPreprocessors = new ArrayList<>();

        /**
         * Sets the command manager, which will be used to create a parser if none is provided.
         *
         * <p>If the command manager is {@code null} and no {@link #parser(ArgumentParser)} is specified then
         * the command component is invalid.</p>
         *
         * @param commandManager the command manager
         * @return {@code this}
         */
        public @This @NonNull Builder<C, T> commandManager(final @Nullable CommandManager<C> commandManager) {
            this.commandManager = commandManager;
            return this;
        }

        /**
         * Sets the {@link #name(String)} and the {@link #valueType(TypeToken) value type} to the values contained in the given
         * {@code cloudKey}.
         *
         * @param cloudKey the key
         * @return {@code this}
         */
        public @This @NonNull Builder<C, T> key(final @NonNull CloudKey<T> cloudKey) {
            return this.name(cloudKey.name()).valueType(cloudKey.type());
        }

        /**
         * Returns the current name, if it has been set.
         *
         * @return current name
         */
        public @MonotonicNonNull String name() {
            return this.name;
        }

        /**
         * Sets the {@code name} of the component.
         *
         * <p>The name is used to extract the parsed values from the {@link CommandContext}.</p>
         *
         * @param name the name
         * @return {@code this}
         */
        public @This @NonNull Builder<C, T> name(final @NonNull String name) {
            this.name = Objects.requireNonNull(name, "name");
            return this;
        }

        /**
         * Sets the {@code valueType}.
         *
         * <p>This is always required and the component will be invalid is the value type is {@code null} when the component is
         * built.</p>
         *
         * <p>If no {@link #parser(ArgumentParser) parser} is specified then the default parser will be retrieved from the
         * {@link #commandManager(CommandManager) command manager}.</p>
         *
         * @param valueType the value type
         * @return {@code this}
         */
        public @This @NonNull Builder<C, T> valueType(final @NonNull TypeToken<T> valueType) {
            this.valueType = Objects.requireNonNull(valueType, "valueType");
            return this;
        }

        /**
         * Sets the {@code valueType}.
         *
         * <p>This is always required and the component will be invalid is the value type is {@code null} when the component is
         * built.</p>
         *
         * <p>If no {@link #parser(ArgumentParser) parser} is specified then the default parser will be retrieved from the
         * {@link #commandManager(CommandManager) command manager}.</p>
         *
         * @param valueType the value type
         * @return {@code this}
         */
        public @This @NonNull Builder<C, T> valueType(final @NonNull Class<T> valueType) {
            return this.valueType(TypeToken.get(valueType));
        }

        /**
         * Returns the current parser, if it has been set.
         *
         * @return current parser
         */
        public @MonotonicNonNull ParserDescriptor<C, T> parser() {
            if (this.valueType == null || this.parser == null) {
                return null;
            }
            return ParserDescriptor.of(this.parser, this.valueType);
        }

        /**
         * Sets the {@code parser} and {@code valueType}.
         *
         * @param parserDescriptor descriptor of the parser
         * @return {@code this}
         */
        public @This @NonNull Builder<C, T> parser(final @NonNull ParserDescriptor<? super C, T> parserDescriptor) {
            return this.parser(parserDescriptor.parser()).valueType(parserDescriptor.valueType());
        }

        /**
         * Returns the current default value, if it has been set.
         *
         * @return current default value
         */
        public @Nullable DefaultValue<C, T> defaultValue() {
            if (this.defaultValue == null) {
                return null;
            }
            return (DefaultValue<C, T>) this.defaultValue;
        }

        /**
         * Sets the {@code defaultValue}.
         *
         * <p>This should not be set if {@code required} is {@code true}.</p>
         *
         * @param defaultValue the default value
         * @return {@code this}
         */
        public @This @NonNull Builder<C, T> defaultValue(final @Nullable DefaultValue<? super C, T> defaultValue) {
            this.defaultValue = (DefaultValue<C, ?>) defaultValue;
            return this;
        }

        /**
         * Sets whether the component is required.
         *
         * <p>Defaults to {@code true}.</p>
         *
         * @param required whether component is required
         * @return {@code this}
         */
        public @This @NonNull Builder<C, T> required(final boolean required) {
            this.required = required;
            return this;
        }

        /**
         * Sets {@code required} to {@code true}.
         *
         * @return {@code this}
         */
        public @This @NonNull Builder<C, T> required() {
            return this.required(true);
        }

        /**
         * Sets {@code required} to {@code false}.
         *
         * @return {@code this}
         */
        public @This @NonNull Builder<C, T> optional() {
            return this.required(false);
        }

        /**
         * Sets {@code required} to {@code true} and updates the {@code defaultValue}.
         *
         * @param defaultValue the default value
         * @return {@code this}
         */
        public @This @NonNull Builder<C, T> optional(final @Nullable DefaultValue<? super C, T> defaultValue) {
            return this.optional().defaultValue(defaultValue);
        }

        /**
         * Returns the current description.
         *
         * @return current description
         */
        public @MonotonicNonNull Description description() {
            return this.description;
        }

        /**
         * Sets the {@code description}.
         *
         * <p>Defaults to {@link Description#empty()}.</p>
         *
         * @param description the description
         * @return {@code this}
         */
        public @This @NonNull Builder<C, T> description(final @NonNull Description description) {
            this.description = Objects.requireNonNull(description, "description");
            return this;
        }

        /**
         * Returns the current suggestion provider, if it has been set.
         *
         * @return current suggestion provider
         */
        public @MonotonicNonNull SuggestionProvider<C> suggestionProvider() {
            return this.suggestionProvider;
        }

        /**
         * Sets the {@code suggestionProvider}.
         *
         * <p>Defaults to {@code parser}.</p>
         *
         * @param suggestionProvider the suggestion provider
         * @return {@code this}
         */
        public @This @NonNull Builder<C, T> suggestionProvider(final @Nullable SuggestionProvider<? super C> suggestionProvider) {
            this.suggestionProvider = (SuggestionProvider<C>) suggestionProvider;
            return this;
        }

        /**
         * Adds the {@code preprocessor}.
         *
         * @param preprocessor the preprocessor
         * @return {@code this}
         */
        public @This @NonNull Builder<C, T> preprocessor(final @NonNull ComponentPreprocessor<? super C> preprocessor) {
            this.componentPreprocessors.add((ComponentPreprocessor<C>) Objects.requireNonNull(preprocessor, "preprocessor"));
            return this;
        }

        /**
         * Adds the {@code preprocessors}.
         *
         * @param preprocessors the preprocessors
         * @return {@code this}
         */
        public @This @NonNull Builder<C, T> preprocessors(final @NonNull Collection<ComponentPreprocessor<C>> preprocessors) {
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
        public @This @NonNull Builder<C, T> parser(final @NonNull ArgumentParser<? super C, T> parser) {
            this.parser = (ArgumentParser<C, T>) Objects.requireNonNull(parser, "parser");
            return this;
        }

        /**
         * Builds a command component using this builder.
         *
         * <p>Each invocation produces a unique component.</p>
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
                parser = (ctx, input) -> ArgumentParseResult.failure(
                        new UnsupportedOperationException("No parser was specified"));
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
                suggestionProvider = parser.suggestionProvider();
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
         *
         * <p>This always returns the opposite of {@link #optional()}.</p>
         *
         * @return whether the component is required
         */
        public boolean required() {
            return this.required;
        }

        /**
         * Returns whether the component is optional
         *
         * <p>This always returns the opposite of {@link #required()}.</p>
         *
         * @return whether the component is optional
         */
        public boolean optional() {
            return !this.required;
        }
    }
}
