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
import cloud.commandframework.arguments.ComponentPreprocessor;
import cloud.commandframework.arguments.DefaultValue;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.arguments.compound.FlagArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
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
 *
 * @param <C> Command sender type
 * @since 1.3.0
 */
@API(status = API.Status.STABLE, since = "1.3.0")
public final class CommandComponent<C> implements Comparable<CommandComponent<C>> {

    private final String name;
    private final ArgumentParser<C, ?> parser;
    private final ArgumentDescription description;
    private final ComponentType componentType;
    private final DefaultValue<C, ?> defaultValue;
    private final TypeToken<?> valueType;
    private final SuggestionProvider<C> suggestionProvider;
    private final Collection<@NonNull ComponentPreprocessor<C>> componentPreprocessors;

    private Command<C> owningCommand;

    private CommandComponent(
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

    private CommandComponent(
            final @NonNull CommandArgument<C, ?> argument,
            final @NonNull ArgumentDescription description,
            final boolean required,
            final @Nullable DefaultValue<C, ?> defaultValue,
            final @NonNull Collection<@NonNull ComponentPreprocessor<C>> componentPreprocessors
    ) {
        this(
                argument.getName(),
                argument.getParser(),
                argument.getValueType(),
                description,
                type(argument, required),
                defaultValue,
                argument.suggestionProvider(),
                componentPreprocessors
        );
    }

    private CommandComponent(
            final @NonNull CommandArgument<C, ?> argument,
            final @NonNull ArgumentDescription description,
            final boolean required,
            final @Nullable DefaultValue<C, ?> defaultValue
    ) {
        this(argument, description, required, defaultValue, argument.argumentPreprocessors());
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
    public @NonNull String name() {
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
    public @NonNull Collection<@NonNull String> aliases() {
        if (this.parser() instanceof StaticArgument.StaticArgumentParser) {
            return ((StaticArgument.StaticArgumentParser<C>) this.parser()).aliases();
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
    public @NonNull Collection<@NonNull String> alternativeAliases() {
        if (this.parser() instanceof StaticArgument.StaticArgumentParser) {
            return ((StaticArgument.StaticArgumentParser<C>) this.parser()).alternativeAliases();
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
    public boolean optional() {
        return this.componentType.optional();
    }

    /**
     * Returns the type of the component
     *
     * @return the component type
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull ComponentType type() {
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
    public boolean hasDefaultValue() {
        return this.optional() && this.defaultValue() != null;
    }

    /**
     * Returns the suggestion provider for this argument
     *
     * @return the suggestion provider
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @NonNull SuggestionProvider<C> suggestionProvider() {
        return this.suggestionProvider;
    }

    /**
     * Returns the command that owns this component.
     *
     * @return the owning command
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public @MonotonicNonNull Command<C> owningCommand() {
        return this.owningCommand;
    }

    /**
     * Sets the command that owns this component.
     *
     * @param owningCommand the command
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    public void owningCommand(final @NonNull Command<C> owningCommand) {
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
    public @NonNull @This CommandComponent<C> addPreprocessor(
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
    public @NonNull ArgumentParseResult<Boolean> preprocess(
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

    @Override
    public int hashCode() {
        return Objects.hash(this.name(), this.valueType());
    }

    @Override
    public boolean equals(final Object o) {
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
    public @NonNull String toString() {
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
                this.componentPreprocessors
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
        return new CommandComponent<>(commandArgument, commandDescription, true, null);
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
        return new CommandComponent<>(commandArgument, commandDescription, false, defaultValue);
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
        return new CommandComponent<>(commandArgument, commandDescription, false, null);
    }

    private static @NonNull ComponentType type(final @NonNull CommandArgument<?, ?> argument, final boolean required) {
        if (argument.getParser() instanceof StaticArgument.StaticArgumentParser) {
            return ComponentType.LITERAL;
        } else if (argument.getParser() instanceof FlagArgument.FlagArgumentParser) {
            return ComponentType.FLAG;
        } else if (required) {
            return ComponentType.REQUIRED_VARIABLE;
        } else {
            return ComponentType.OPTIONAL_VARIABLE;
        }
    }

    @Override
    public int compareTo(final @NonNull CommandComponent<C> other) {
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
