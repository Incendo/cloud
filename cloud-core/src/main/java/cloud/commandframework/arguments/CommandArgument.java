//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
package cloud.commandframework.arguments;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.keys.CloudKeyHolder;
import cloud.commandframework.keys.SimpleCloudKey;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * A argument that belongs to a command
 *
 * @param <C> Command sender type
 * @param <T> The type that the argument parses into
 */
@SuppressWarnings("unused")
public class CommandArgument<C, T> implements Comparable<CommandArgument<?, ?>>, CloudKeyHolder<T> {

    /**
     * Pattern for command argument names
     */
    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z0-9\\-_]+");

    /**
     * A typed key representing this argument
     */
    private final CloudKey<T> key;
    /**
     * Indicates whether or not the argument is required
     * or not. All arguments prior to any other required
     * argument must also be required, such that the predicate
     * (∀ c_i ∈ required)({c_0, ..., c_i-1} ⊂ required) holds true,
     * where {c_0, ..., c_n-1} is the set of command arguments.
     */
    private final boolean required;
    /**
     * The command argument name. This might be exposed
     * to command senders and so should be chosen carefully.
     */
    private final String name;
    /**
     * The parser that is used to parse the command input
     * into the corresponding command type
     */
    private final ArgumentParser<C, T> parser;
    /**
     * Default value, will be empty if none was supplied
     */
    private final String defaultValue;
    /**
     * The type that is produces by the argument's parser
     */
    private final TypeToken<T> valueType;
    /**
     * Suggestion provider
     */
    private final BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider;
    /**
     * Argument preprocessors that allows for extensions to existing argument types
     * without having to update all parsers
     */
    private final Collection<BiFunction<@NonNull CommandContext<C>,
            @NonNull Queue<@NonNull String>, @NonNull ArgumentParseResult<Boolean>>> argumentPreprocessors;

    /**
     * A description that will be used when registering this argument if no override is provided.
     */
    private final ArgumentDescription defaultDescription;

    /**
     * Whether or not the argument has been used before
     */
    private boolean argumentRegistered = false;

    private Command<C> owningCommand;

    /**
     * Construct a new command argument
     *
     * @param required              Whether or not the argument is required
     * @param name                  The argument name
     * @param parser                The argument parser
     * @param defaultValue          Default value used when no value is provided by the command sender
     * @param valueType             Type produced by the parser
     * @param suggestionsProvider   Suggestions provider
     * @param defaultDescription    Default description to use when registering
     * @param argumentPreprocessors Argument preprocessors
     * @since 1.4.0
     */
    public CommandArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull String defaultValue,
            final @NonNull TypeToken<T> valueType,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription,
            final @NonNull Collection<@NonNull BiFunction<@NonNull CommandContext<C>, @NonNull Queue<@NonNull String>,
                    @NonNull ArgumentParseResult<Boolean>>> argumentPreprocessors
    ) {
        this.required = required;
        this.name = Objects.requireNonNull(name, "Name may not be null");
        if (!NAME_PATTERN.asPredicate().test(name)) {
            throw new IllegalArgumentException("Name must be alphanumeric");
        }
        this.parser = Objects.requireNonNull(parser, "Parser may not be null");
        this.defaultValue = defaultValue;
        this.valueType = valueType;
        this.suggestionsProvider = suggestionsProvider == null
                ? buildDefaultSuggestionsProvider(this)
                : suggestionsProvider;
        this.defaultDescription = Objects.requireNonNull(defaultDescription, "Default description may not be null");
        this.argumentPreprocessors = new LinkedList<>(argumentPreprocessors);
        this.key = SimpleCloudKey.of(this.name, this.valueType);
    }

    /**
     * Construct a new command argument
     *
     * @param required              Whether or not the argument is required
     * @param name                  The argument name
     * @param parser                The argument parser
     * @param defaultValue          Default value used when no value is provided by the command sender
     * @param valueType             Type produced by the parser
     * @param suggestionsProvider   Suggestions provider
     * @param argumentPreprocessors Argument preprocessors
     */
    public CommandArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull String defaultValue,
            final @NonNull TypeToken<T> valueType,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull Collection<@NonNull BiFunction<@NonNull CommandContext<C>, @NonNull Queue<@NonNull String>,
                    @NonNull ArgumentParseResult<Boolean>>> argumentPreprocessors
    ) {
        this(
            required,
            name,
            parser,
            defaultValue,
            valueType,
            suggestionsProvider,
            ArgumentDescription.empty(),
            argumentPreprocessors
        );
    }

    /**
     * Construct a new command argument
     *
     * @param required            Whether or not the argument is required
     * @param name                The argument name
     * @param parser              The argument parser
     * @param defaultValue        Default value used when no value is provided by the command sender
     * @param valueType           Type produced by the parser
     * @param suggestionsProvider Suggestions provider
     */
    public CommandArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull String defaultValue,
            final @NonNull TypeToken<T> valueType,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider
    ) {
        this(required, name, parser, defaultValue, valueType, suggestionsProvider, Collections.emptyList());
    }

    /**
     * Construct a new command argument
     *
     * @param required            Whether or not the argument is required
     * @param name                The argument name
     * @param parser              The argument parser
     * @param defaultValue        Default value used when no value is provided by the command sender
     * @param valueType           Type produced by the parser
     * @param suggestionsProvider Suggestions provider
     * @param defaultDescription    Default description to use when registering
     * @since 1.4.0
     */
    public CommandArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull String defaultValue,
            final @NonNull TypeToken<T> valueType,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        this(required, name, parser, defaultValue, valueType, suggestionsProvider, defaultDescription, Collections.emptyList());
    }

    /**
     * Construct a new command argument
     *
     * @param required            Whether or not the argument is required
     * @param name                The argument name
     * @param parser              The argument parser
     * @param defaultValue        Default value used when no value is provided by the command sender
     * @param valueType           Type produced by the parser
     * @param suggestionsProvider Suggestions provider
     */
    public CommandArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull String defaultValue,
            final @NonNull Class<T> valueType,
            final @Nullable BiFunction<@NonNull CommandContext<C>,
                    @NonNull String, @NonNull List<@NonNull String>> suggestionsProvider
    ) {
        this(required, name, parser, defaultValue, TypeToken.get(valueType), suggestionsProvider);
    }

    /**
     * Construct a new command argument
     *
     * @param required            Whether or not the argument is required
     * @param name                The argument name
     * @param parser              The argument parser
     * @param defaultValue        Default value used when no value is provided by the command sender
     * @param valueType           Type produced by the parser
     * @param suggestionsProvider Suggestions provider
     * @param defaultDescription    Default description to use when registering
     * @since 1.4.0
     */
    public CommandArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull String defaultValue,
            final @NonNull Class<T> valueType,
            final @Nullable BiFunction<@NonNull CommandContext<C>,
                    @NonNull String, @NonNull List<@NonNull String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        this(required, name, parser, defaultValue, TypeToken.get(valueType), suggestionsProvider, defaultDescription);
    }

    /**
     * Construct a new command argument
     *
     * @param required  Whether or not the argument is required
     * @param name      The argument name
     * @param parser    The argument parser
     * @param valueType Type produced by the parser
     */
    public CommandArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull ArgumentParser<C, T> parser,
            final @NonNull Class<T> valueType
    ) {
        this(required, name, parser, "", valueType, null);
    }

    private static <C> @NonNull BiFunction<@NonNull CommandContext<C>, @NonNull String,
            @NonNull List<String>> buildDefaultSuggestionsProvider(final @NonNull CommandArgument<C, ?> argument) {
        return new DelegatingSuggestionsProvider<>(argument.getName(), argument.getParser());
    }

    /**
     * Create a new command argument
     *
     * @param clazz Argument class
     * @param name  Argument name
     * @param <C>   Command sender type
     * @param <T>   Argument Type. Used to make the compiler happy.
     * @return Argument builder
     */
    public static <C, T> CommandArgument.@NonNull Builder<C, T> ofType(
            final @NonNull TypeToken<T> clazz,
            final @NonNull String name
    ) {
        return new Builder<>(clazz, name);
    }

    /**
     * Create a new command argument
     *
     * @param clazz Argument class
     * @param name  Argument name
     * @param <C>   Command sender type
     * @param <T>   Argument Type. Used to make the compiler happy.
     * @return Argument builder
     */
    public static <C, T> CommandArgument.@NonNull Builder<@NonNull C, @NonNull T> ofType(
            final @NonNull Class<T> clazz,
            final @NonNull String name
    ) {
        return new Builder<>(TypeToken.get(clazz), name);
    }

    @Override
    public final @NonNull CloudKey<T> getKey() {
        return this.key;
    }

    /**
     * Check whether or not the command argument is required
     *
     * @return {@code true} if the argument is required, {@code false} if not
     */
    public boolean isRequired() {
        return this.required;
    }

    /**
     * Get the command argument name;
     *
     * @return Argument name
     */
    public @NonNull String getName() {
        return this.name;
    }

    /**
     * Get the parser that is used to parse the command input
     * into the corresponding command type
     *
     * @return Command parser
     */
    public @NonNull ArgumentParser<C, T> getParser() {
        return this.parser;
    }

    @Override
    public final @NonNull String toString() {
        return String.format("%s{name=%s}", this.getClass().getSimpleName(), this.name);
    }

    /**
     * Register a new preprocessor. If all preprocessor has succeeding {@link ArgumentParseResult results}
     * that all return {@code true}, the argument will be passed onto the parser.
     * <p>
     * It is important that the preprocessor doesn't pop any input. Instead, it should only peek.
     *
     * @param preprocessor Preprocessor
     * @return {@code this}
     */
    public @NonNull CommandArgument<C, T> addPreprocessor(
            final @NonNull BiFunction<@NonNull CommandContext<C>, @NonNull Queue<String>,
                    @NonNull ArgumentParseResult<Boolean>> preprocessor
    ) {
        this.argumentPreprocessors.add(preprocessor);
        return this;
    }

    /**
     * Preprocess command input. This will immediately forward any failed argument parse results.
     * If none fails, a {@code true} result will be returned
     *
     * @param context Command context
     * @param input   Remaining command input. None will be popped
     * @return Parsing error, or argument containing {@code true}
     */
    public @NonNull ArgumentParseResult<Boolean> preprocess(
            final @NonNull CommandContext<C> context,
            final @NonNull Queue<String> input
    ) {
        for (final BiFunction<@NonNull CommandContext<C>, @NonNull Queue<String>,
                @NonNull ArgumentParseResult<Boolean>> preprocessor : this.argumentPreprocessors) {
            final ArgumentParseResult<Boolean> result = preprocessor.apply(
                    context,
                    input
            );
            if (result.getFailure().isPresent()) {
                return result;
            }
        }
        return ArgumentParseResult.success(true);
    }

    /**
     * Get the owning command
     *
     * @return Owning command
     */
    @Nullable
    public Command<C> getOwningCommand() {
        return this.owningCommand;
    }

    /**
     * Set the owning command
     *
     * @param owningCommand Owning command
     */
    public void setOwningCommand(final @NonNull Command<C> owningCommand) {
        if (this.owningCommand != null) {
            throw new IllegalStateException("Cannot replace owning command");
        }
        this.owningCommand = owningCommand;
    }

    /**
     * Get the argument suggestions provider
     *
     * @return Suggestions provider
     */
    public final @NonNull BiFunction<@NonNull CommandContext<C>, @NonNull String,
            @NonNull List<String>> getSuggestionsProvider() {
        return this.suggestionsProvider;
    }

    /**
     * Get the default description to use when registering and no other is provided.
     *
     * @return the default description
     */
    public final @NonNull ArgumentDescription getDefaultDescription() {
        return this.defaultDescription;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CommandArgument<?, ?> that = (CommandArgument<?, ?>) o;
        return isRequired() == that.isRequired() && Objects.equals(getName(), that.getName());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(isRequired(), getName());
    }

    @Override
    public final int compareTo(final @NonNull CommandArgument<?, ?> o) {
        if (this instanceof StaticArgument) {
            if (o instanceof StaticArgument) {
                return (this.getName().compareTo(o.getName()));
            } else {
                return -1;
            }
        } else {
            if (o instanceof StaticArgument) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Get the default value
     *
     * @return Default value
     */
    public @NonNull String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Check if the argument has a default value
     *
     * @return {@code true} if the argument has a default value, {@code false} if not
     */
    public boolean hasDefaultValue() {
        return !this.isRequired()
                && !this.getDefaultValue().isEmpty();
    }

    /**
     * Get the type of this argument's value
     *
     * @return Value type
     */
    public @NonNull TypeToken<T> getValueType() {
        return this.valueType;
    }

    /**
     * Create a copy of the command argument
     *
     * @return Copied argument
     */
    public @NonNull CommandArgument<C, T> copy() {
        CommandArgument.Builder<C, T> builder = ofType(this.valueType, this.name);
        builder = builder.withSuggestionsProvider(this.suggestionsProvider);
        builder = builder.withParser(this.parser);
        if (this.isRequired()) {
            builder = builder.asRequired();
        } else if (this.defaultValue.isEmpty()) {
            builder = builder.asOptional();
        } else {
            builder = builder.asOptionalWithDefault(this.defaultValue);
        }
        builder = builder.withDefaultDescription(this.defaultDescription);

        return builder.build();
    }

    /**
     * Check whether or not the argument has been used in a command
     *
     * @return {@code true} if the argument has been used in a command, else {@code false}
     */
    public boolean isArgumentRegistered() {
        return this.argumentRegistered;
    }

    /**
     * Indicate that the argument has been associated with a command
     */
    public void setArgumentRegistered() {
        this.argumentRegistered = true;
    }


    /**
     * Mutable builder for {@link CommandArgument} instances
     *
     * @param <C> Command sender type
     * @param <T> Argument value type
     */
    public static class Builder<C, T> {

        private final TypeToken<T> valueType;
        private final String name;

        private CommandManager<C> manager;
        private boolean required = true;
        private ArgumentParser<C, T> parser;
        private String defaultValue = "";
        private BiFunction<@NonNull CommandContext<C>, @NonNull String, @NonNull List<String>> suggestionsProvider;
        private @NonNull ArgumentDescription defaultDescription = ArgumentDescription.empty();

        private final Collection<BiFunction<@NonNull CommandContext<C>,
                @NonNull String, @NonNull ArgumentParseResult<Boolean>>> argumentPreprocessors = new LinkedList<>();

        protected Builder(
                final @NonNull TypeToken<T> valueType,
                final @NonNull String name
        ) {
            this.valueType = valueType;
            this.name = name;
        }

        protected Builder(
                final @NonNull Class<T> valueType,
                final @NonNull String name
        ) {
            this(TypeToken.get(valueType), name);
        }

        /**
         * Set the command manager. Will be used to create a default parser
         * if none was provided
         *
         * @param manager Command manager
         * @return Builder instance
         */
        public @NonNull Builder<@NonNull C, @NonNull T> manager(final @NonNull CommandManager<C> manager) {
            this.manager = manager;
            return this;
        }

        /**
         * Indicates that the argument is required.
         * All arguments prior to any other required
         * argument must also be required, such that the predicate
         * (∀ c_i ∈ required)({c_0, ..., c_i-1} ⊂ required) holds true,
         * where {c_0, ..., c_n-1} is the set of command arguments.
         *
         * @return Builder instance
         */
        public @NonNull Builder<@NonNull C, @NonNull T> asRequired() {
            this.required = true;
            return this;
        }

        /**
         * Indicates that the argument is optional.
         * All arguments prior to any other required
         * argument must also be required, such that the predicate
         * (∀ c_i ∈ required)({c_0, ..., c_i-1} ⊂ required) holds true,
         * where {c_0, ..., c_n-1} is the set of command arguments.
         *
         * @return Builder instance
         */
        public @NonNull Builder<@NonNull C, @NonNull T> asOptional() {
            this.required = false;
            return this;
        }

        /**
         * Indicates that the argument is optional.
         * All arguments prior to any other required
         * argument must also be required, such that the predicate
         * (∀ c_i ∈ required)({c_0, ..., c_i-1} ⊂ required) holds true,
         * where {c_0, ..., c_n-1} is the set of command arguments.
         *
         * @param defaultValue Default value that will be used if none was supplied
         * @return Builder instance
         */
        public @NonNull Builder<@NonNull C, @NonNull T> asOptionalWithDefault(final @NonNull String defaultValue) {
            this.defaultValue = defaultValue;
            this.required = false;
            return this;
        }

        /**
         * Set the argument parser
         *
         * @param parser Argument parser
         * @return Builder instance
         */
        public @NonNull Builder<@NonNull C, @NonNull T> withParser(final @NonNull ArgumentParser<@NonNull C, @NonNull T> parser) {
            this.parser = Objects.requireNonNull(parser, "Parser may not be null");
            return this;
        }

        /**
         * Set the suggestions provider
         *
         * @param suggestionsProvider Suggestions provider
         * @return Builder instance
         */
        public @NonNull Builder<@NonNull C, @NonNull T> withSuggestionsProvider(
                final @NonNull BiFunction<@NonNull CommandContext<C>,
                        @NonNull String, @NonNull List<String>> suggestionsProvider
        ) {
            this.suggestionsProvider = suggestionsProvider;
            return this;
        }

        /**
         * Set the default description to be used for this argument.
         *
         * <p>The default description is used when no other description is provided for a certain argument.</p>
         *
         * @param defaultDescription The default description
         * @return Builder instance
         * @since 1.4.0
         */
        public @NonNull Builder<@NonNull C, @NonNull T> withDefaultDescription(
                final @NonNull ArgumentDescription defaultDescription
        ) {
            this.defaultDescription = Objects.requireNonNull(defaultDescription, "Default description may not be null");
            return this;
        }

        /**
         * Construct a command argument from the builder settings
         *
         * @return Constructed argument
         */
        public @NonNull CommandArgument<@NonNull C, @NonNull T> build() {
            if (this.parser == null && this.manager != null) {
                this.parser = this.manager.getParserRegistry().createParser(valueType, ParserParameters.empty())
                        .orElse(null);
            }
            if (this.parser == null) {
                this.parser = (c, i) -> ArgumentParseResult
                        .failure(new UnsupportedOperationException("No parser was specified"));
            }
            if (this.suggestionsProvider == null) {
                this.suggestionsProvider = new DelegatingSuggestionsProvider<>(this.name, this.parser);
            }
            return new CommandArgument<>(
                    this.required,
                    this.name,
                    this.parser,
                    this.defaultValue,
                    this.valueType,
                    this.suggestionsProvider,
                    this.defaultDescription
            );
        }

        protected final @NonNull String getName() {
            return this.name;
        }

        protected final boolean isRequired() {
            return this.required;
        }

        protected final @NonNull ArgumentParser<@NonNull C, @NonNull T> getParser() {
            return this.parser;
        }

        protected final @NonNull String getDefaultValue() {
            return this.defaultValue;
        }

        protected final @NonNull BiFunction<@NonNull CommandContext<C>, @NonNull String, @NonNull List<String>>
        getSuggestionsProvider() {
            return this.suggestionsProvider;
        }

        protected final @NonNull ArgumentDescription getDefaultDescription() {
            return this.defaultDescription;
        }

    }

}
